package com.jobrec.notification.controller;

import com.jobrec.notification.dto.NotificationResponse;
import com.jobrec.notification.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<NotificationResponse> list(Authentication authentication) { return service.list(userId(authentication)); }
    @PostMapping("/{id}/read") public NotificationResponse read(@PathVariable UUID id, Authentication authentication) { return service.markRead(userId(authentication), id); }
    @PostMapping("/read-all") public ReadAllResponse readAll(Authentication authentication) { return new ReadAllResponse(service.markAllRead(userId(authentication))); }
    private UUID userId(Authentication authentication) { return UUID.fromString(authentication.getName()); }
    public record ReadAllResponse(int updated) {}
}
