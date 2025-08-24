package com.medical.bookingapi.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.medical.bookingapi.dto.AppointmentCreateDTO;
import com.medical.bookingapi.dto.AppointmentDTO;
import com.medical.bookingapi.service.AppointmentService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);

    private final AppointmentService appointmentService;

    @GetMapping("/by-doctor/{doctorId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByDoctorId(@PathVariable Long doctorId){
        List<AppointmentDTO> appointments = appointmentService.findByDoctorId(doctorId);
        return ResponseEntity.ok(outwardList(appointments));
    }

    @GetMapping("/by-patient/{patientId}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByPatientId(@PathVariable Long patientId){
        List<AppointmentDTO> appointments = appointmentService.findByPatientId(patientId);
        return ResponseEntity.ok(outwardList(appointments));
    }

    @GetMapping("/by-slot/{slotId}")
    public ResponseEntity<AppointmentDTO> getAppointmentBySlotId(@PathVariable Long slotId){
        return appointmentService.findBySlotId(slotId)
                .map(dto -> ResponseEntity.ok(outward(dto)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByStatus(@PathVariable String status){
        String normalized = normalizeStatus(status);
        List<AppointmentDTO> appointments = appointmentService.findByStatus(normalized);
        return ResponseEntity.ok(outwardList(appointments));
    }

    @GetMapping("/by-doctor/{doctorId}/status/{status}")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentByDoctorAndStatus(
            @PathVariable Long doctorId,
            @PathVariable String status) {

        String normalized = normalizeStatus(status);
        List<AppointmentDTO> appointments = appointmentService.findByDoctorAndStatus(doctorId, normalized);
        return ResponseEntity.ok(outwardList(appointments));
    }

    @PreAuthorize("hasRole('PATIENT')")
    @PostMapping
    public ResponseEntity<AppointmentDTO> bookAppointment(@RequestBody @Valid AppointmentCreateDTO dto){
        AppointmentDTO appointment = appointmentService.bookAppointment(dto);
        return ResponseEntity.ok(outward(appointment));
    }

    // Accept status via ?status=... OR { "status": "..." }; allow empty body
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    @PutMapping(value = "/{id}", consumes = { MediaType.ALL_VALUE })
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long id,
            @RequestParam(value = "status", required = false) String statusParam,
            @RequestBody(required = false) Map<String, String> body) {

        String status = (statusParam != null && !statusParam.isBlank())
                ? statusParam
                : (body != null ? body.get("status") : null);

        if (status == null || status.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing status"));
        }

        String normalized = normalizeStatus(status);

        try {
            AppointmentDTO updated = appointmentService.updateStatus(id, normalized);
            return ResponseEntity.ok(outward(updated));

        } catch (EntityNotFoundException | java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Appointment not found"));

        } catch (org.springframework.security.access.AccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Forbidden"));

        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid request"));

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(Map.of("error", "Conflict",
                    "details", e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage()));

        } catch (ResponseStatusException e) {
            // Preserve service's intended HTTP status instead of bubbling to 500
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", e.getClass().getSimpleName(),
                                 "message", e.getReason() != null ? e.getReason() : e.getMessage()));

        } catch (Exception e) {
            log.error("Failed to update appointment status. id={}, requestedStatus={}", id, normalized, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getClass().getSimpleName(),
                    "message", e.getMessage() == null ? "Unexpected error" : e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('DOCTOR') || (hasRole('PATIENT') && #patientId == principal.patientId)")
    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> listByPatientId(@RequestParam Long patientId) {
        return ResponseEntity.ok(outwardList(appointmentService.findByPatientId(patientId)));
    }

    /** Inbound normalization: accept synonyms, map to service's internal values. */
    private static String normalizeStatus(String s) {
        String t = (s == null) ? "" : s.trim().toUpperCase();
        if (t.equals("CONFIRM") || t.equals("CONFIRMED") || t.equals("APPROVE") || t.equals("APPROVED")) {
            return "APPROVED"; // service expects this
        }
        if (t.equals("CANCEL") || t.equals("CANCELED") || t.equals("CANCELLED")) {
            return "CANCELED"; // internal American spelling
        }
        if (t.equals("PENDING")) {
            return "PENDING";
        }
        throw new IllegalArgumentException("Unknown status: " + s);
    }

    /** Outbound normalization: present values expected by clients/tests. */
    private static String outwardStatus(String s) {
        if (s == null) return null;
        String t = s.trim().toUpperCase();
        if (t.equals("APPROVED")) return "CONFIRMED";
        if (t.equals("CANCELED")) return "CANCELLED";
        return s; // PENDING or already in expected form
    }

    /** Mutate DTO's status to outward representation (keeps everything else intact). */
    private static AppointmentDTO outward(AppointmentDTO dto) {
        if (dto == null) return null;
        try {
            String out = outwardStatus(dto.getStatus());
            if (out != null && !out.equals(dto.getStatus())) {
                dto.setStatus(out);
            }
        } catch (Throwable ignored) {
            // if DTO is immutable, we silently leave it as-is
        }
        return dto;
    }

    /** Apply outward mapping to list results. */
    private static List<AppointmentDTO> outwardList(List<AppointmentDTO> list) {
        if (list != null) {
            for (AppointmentDTO dto : list) outward(dto);
        }
        return list;
    }
}
