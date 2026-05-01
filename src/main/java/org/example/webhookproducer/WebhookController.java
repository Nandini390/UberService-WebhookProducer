package org.example.webhookproducer;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);
    private final NotificationService notificationService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String eventType  = (String) payload.get("eventType");
            String occurredAt = (String) payload.get("occurredAt");
            Object data       = payload.get("data");

            if (eventType == null || data == null) {
                log.warn("Malformed webhook payload — missing eventType or data");
                return ResponseEntity.badRequest().body("Invalid webhook payload");
            }

            log.info("Webhook received — eventType: {}, occurredAt: {}", eventType, occurredAt);

            switch (eventType) {
                case "BOOKING_CREATED"   -> handleBookingCreated(data);
                case "BOOKING_CANCELLED" -> handleBookingCancelled(data);
                case "DRIVER_ASSIGNED"   -> handleDriverAssigned(data);
                case "TRIP_STARTED"      -> handleTripStarted(data);
                case "TRIP_COMPLETED"    -> handleTripCompleted(data);
                case "PAYMENT_CAPTURED"  -> handlePaymentCaptured(data);
                case "PAYMENT_REFUNDED"  -> handlePaymentRefunded(data);
                default -> log.warn("Unhandled event type: {}", eventType);
            }

            return ResponseEntity.ok("Webhook received and processed");

        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok("Webhook received with processing error");
        }
    }


    // ─── Handlers — only extract data and delegate to NotificationService ────

    private void handleBookingCreated(Object data) {
        Map<String, Object> booking = toMap(data);
        UUID passengerId = toUUID(booking.get("passengerId"));

        notificationService.notifyBookingCreated(
                passengerId,
                booking.get("bookingId"),
                booking.get("estimatedFare")
        );
    }

    private void handleBookingCancelled(Object data) {
        Map<String, Object> booking = toMap(data);
        UUID passengerId = toUUID(booking.get("passengerId"));
        Double charge    = toDouble(booking.get("cancellationCharge"));

        // notify passenger
        notificationService.notifyBookingCancelledPassenger(
                passengerId,
                booking.get("bookingId"),
                charge
        );

        // notify driver if one was assigned
        if (booking.get("driver") != null) {
            Map<String, Object> driver = toMap(booking.get("driver"));
            UUID driverId = toUUID(driver.get("id"));
            notificationService.notifyBookingCancelledDriver(
                    driverId,
                    booking.get("bookingId")
            );
        }
    }

    private void handleDriverAssigned(Object data) {
        Map<String, Object> booking = toMap(data);
        UUID passengerId = toUUID(booking.get("passengerId"));

        // try to get driver name from nested driver object
        Object driverName = "Your driver";
        if (booking.get("driver") != null) {
            Map<String, Object> driver = toMap(booking.get("driver"));
            if (driver.get("name") != null) {
                driverName = driver.get("name");
            }
        }

        notificationService.notifyDriverAssigned(
                passengerId,
                booking.get("bookingId"),
                driverName
        );
    }

    private void handleTripStarted(Object data) {
        Map<String, Object> booking = toMap(data);
        UUID passengerId = toUUID(booking.get("passengerId"));

        notificationService.notifyTripStartedPassenger(
                passengerId,
                booking.get("bookingId"),
                booking.get("startTime")
        );
    }

    private void handleTripCompleted(Object data) {
        Map<String, Object> booking = toMap(data);
        UUID passengerId = toUUID(booking.get("passengerId"));

        // notify passenger
        notificationService.notifyTripCompletedPassenger(
                passengerId,
                booking.get("bookingId"),
                booking.get("estimatedFare")
        );

        // notify driver
        if (booking.get("driver") != null) {
            Map<String, Object> driver = toMap(booking.get("driver"));
            UUID driverId = toUUID(driver.get("id"));
            notificationService.notifyTripCompletedDriver(
                    driverId,
                    booking.get("bookingId"),
                    booking.get("estimatedFare")
            );
        }
    }

    private void handlePaymentCaptured(Object data) {
        Map<String, Object> payment = toMap(data);
        UUID passengerId = toUUID(payment.get("passengerId"));

        notificationService.notifyPaymentCaptured(
                passengerId,
                payment.get("bookingId"),
                payment.get("amount"),
                payment.get("paymentMethod"),
                payment.get("providerReference")
        );
    }

    private void handlePaymentRefunded(Object data) {
        Map<String, Object> payment = toMap(data);
        UUID passengerId = toUUID(payment.get("passengerId"));

        notificationService.notifyPaymentRefunded(
                passengerId,
                payment.get("bookingId"),
                payment.get("amount"),
                payment.get("failureReason")
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Object data) {
        if (data == null) return Map.of();
        return (Map<String, Object>) data;
    }

    private UUID toUUID(Object value) {
        if (value == null) return null;
        try { return UUID.fromString(value.toString()); }
        catch (Exception e) {
            log.warn("Failed to parse UUID: {}", value);
            return null;
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        try { return Double.parseDouble(value.toString()); }
        catch (Exception e) { return null; }
    }
}