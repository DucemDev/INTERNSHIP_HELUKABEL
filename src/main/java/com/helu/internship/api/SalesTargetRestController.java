package com.helu.internship.api;

import com.helu.internship.dto.request.SalesTargetRequest;
import com.helu.internship.dto.response.SalesTargetResponse;
import com.helu.internship.service.SalesTargetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/targets")
@RequiredArgsConstructor
public class SalesTargetRestController {

    private final SalesTargetService salesTargetService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SalesTargetResponse>> getTargetsByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(salesTargetService.getTargetsByUser(userId));
    }

    @GetMapping("/user/{userId}/year/{year}")
    public ResponseEntity<List<SalesTargetResponse>> getTargetsByUserAndYear(
            @PathVariable UUID userId,
            @PathVariable Integer year) {
        return ResponseEntity.ok(salesTargetService.getTargetsByUserAndYear(userId, year));
    }

    @PostMapping
    public ResponseEntity<SalesTargetResponse> saveOrUpdateTarget(
            @RequestBody SalesTargetRequest request,
            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(salesTargetService.saveOrUpdateTarget(request, principal.getName()));
    }

    @DeleteMapping("/{targetId}")
    public ResponseEntity<Void> deleteTarget(@PathVariable Long targetId) {
        salesTargetService.deleteTarget(targetId);
        return ResponseEntity.noContent().build();
    }
}
