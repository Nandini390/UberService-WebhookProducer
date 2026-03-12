package org.example.webhookproducer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class WebhookController {

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object>payload){
        Object passengerId = payload.get("passengerId");
        Object startLocation = payload.get("Start Location");
        Object endLocation = payload.get("End Location");

        System.out.println("PassengerId: " + passengerId);
        System.out.println("Start Location: " + startLocation);
        System.out.println("End Location: " + endLocation);

        return ResponseEntity.ok("webhook received and processed");
    }
}
