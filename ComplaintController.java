
package com.college.college_backend;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/complaints")
@CrossOrigin(origins = "http://localhost:5173")
public class ComplaintController {

    private final ComplaintRepository repository;

    public ComplaintController(ComplaintRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public ResponseEntity<?> createComplaint(
            @RequestBody Complaint complaint) {

        if (blank(complaint.getStudentName())
                || blank(complaint.getEmail())
                || blank(complaint.getTitle())
                || blank(complaint.getCategory())
                || blank(complaint.getDepartment())
                || blank(complaint.getDescription())) {

            return ResponseEntity.badRequest()
                    .body("All complaint fields are required.");
        }

        complaint.setId(null);
        complaint.setStatus("Pending");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(complaint));
    }

    @GetMapping
    public List<Complaint> getAllComplaints() {
        return repository.findAll();
    }

    @GetMapping("/email")
    public ResponseEntity<?> getComplaintsByEmail(
            @RequestParam String email) {

        if (blank(email)) {
            return ResponseEntity.badRequest()
                    .body("Email is required.");
        }

        return ResponseEntity.ok(
            repository.findByEmailIgnoreCaseOrderByIdDesc(email)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Complaint> getComplaintById(
            @PathVariable Long id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusRequest request) {

        if (request == null || blank(request.getStatus())) {
            return ResponseEntity.badRequest()
                    .body("Status is required.");
        }

        String status = request.getStatus()
                .trim()
                .replace(' ', '_')
                .toUpperCase();

        if (!List.of("PENDING", "IN_PROGRESS",
                "RESOLVED", "REJECTED").contains(status)) {
            return ResponseEntity.badRequest()
                    .body("Invalid status.");
        }

        Optional<Complaint> result = repository.findById(id);

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Complaint complaint = result.get();
        complaint.setStatus(status.equals("IN_PROGRESS")
                ? "In Progress"
                : status.substring(0, 1)
                    + status.substring(1).toLowerCase());

        return ResponseEntity.ok(repository.save(complaint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComplaint(
            @PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    public static class StatusRequest {
        private String status;

        public StatusRequest() {}

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}