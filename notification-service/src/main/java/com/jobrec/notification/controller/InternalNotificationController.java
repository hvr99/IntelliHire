package com.jobrec.notification.controller;

import com.jobrec.notification.dto.CreateNotificationRequest;
import com.jobrec.notification.dto.NotificationResponse;
import com.jobrec.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {
    private final NotificationService service;
    public InternalNotificationController(NotificationService service) { this.service = service; }
    @PostMapping public NotificationResponse create(@Valid @RequestBody CreateNotificationRequest request) { return service.create(request); }
}
