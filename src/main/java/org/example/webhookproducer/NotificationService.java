package org.example.webhookproducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // ─── Passenger Notifications ─────────────────────────────────────────────

    public void notifyBookingCreated(UUID passengerId, Object bookingId, Object fare) {
        log.info("[PUSH → PASSENGER {}] 🔍 Looking for your driver! | bookingId={}, estimatedFare=₹{}",
                passengerId, bookingId, fare);
    }

    public void notifyBookingCancelledPassenger(UUID passengerId, Object bookingId, Double charge) {
        String msg = (charge != null && charge > 0)
                ? "Cancellation fee of ₹" + charge + " applies."
                : "No cancellation charges.";
        log.info("[PUSH → PASSENGER {}] ❌ Booking cancelled. {} | bookingId={}",
                passengerId, msg, bookingId);
    }

    public void notifyDriverAssigned(UUID passengerId, Object bookingId, Object driverName) {
        log.info("[PUSH → PASSENGER {}] 🚗 {} is on the way! | bookingId={}",
                passengerId, driverName, bookingId);
    }

    public void notifyTripStartedPassenger(UUID passengerId, Object bookingId, Object startTime) {
        log.info("[PUSH → PASSENGER {}] 🟢 Trip started! Sit back and enjoy! | bookingId={}, startTime={}",
                passengerId, bookingId, startTime);
    }

    public void notifyTripCompletedPassenger(UUID passengerId, Object bookingId, Object fare) {
        log.info("[PUSH → PASSENGER {}] ✅ You have arrived! Total fare: ₹{} | bookingId={}",
                passengerId, fare, bookingId);
    }

    public void notifyPaymentCaptured(UUID passengerId, Object bookingId, Object amount, Object method, Object ref) {
        log.info("[PUSH → PASSENGER {}] 💳 Payment successful! ₹{} paid via {} | ref={}, bookingId={}",
                passengerId, amount, method, ref, bookingId);
    }

    public void notifyPaymentRefunded(UUID passengerId, Object bookingId, Object amount, Object reason) {
        log.info("[PUSH → PASSENGER {}] 💸 Refund of ₹{} initiated! Reflects in 3-5 business days. | reason={}, bookingId={}",
                passengerId, amount, reason, bookingId);
    }

    // ─── Driver Notifications ─────────────────────────────────────────────────

    public void notifyBookingCancelledDriver(UUID driverId, Object bookingId) {
        log.info("[PUSH → DRIVER {}] ❌ Ride cancelled by passenger. You are now available. | bookingId={}",
                driverId, bookingId);
    }

    public void notifyTripCompletedDriver(UUID driverId, Object bookingId, Object fare) {
        log.info("[PUSH → DRIVER {}] 💰 Ride complete! ₹{} earned. | bookingId={}",
                driverId, fare, bookingId);
    }
}