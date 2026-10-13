package com.college.college_backend;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentController {

    private final StudentRepository studentRepository;
    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Student registration
    @PostMapping("/register")
    public ResponseEntity<?> registerStudent(@RequestBody Student student) {

        if (student.getName() == null || student.getName().isBlank()
                || student.getEmail() == null || student.getEmail().isBlank()
                || student.getPassword() == null
                || student.getPassword().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Name, email and password are required.");
        }

        if (studentRepository.existsByEmail(student.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Email is already registered.");
        }

        student.setPassword(
                passwordEncoder.encode(student.getPassword()));

        Student savedStudent = studentRepository.save(student);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedStudent);
    }

    // Student login
    @PostMapping("/login")
    public ResponseEntity<?> loginStudent(@RequestBody Student student) {

        if (student.getEmail() == null || student.getPassword() == null) {
            return ResponseEntity.badRequest()
                    .body("Email and password are required.");
        }

        Optional<Student> result =
                studentRepository.findByEmail(student.getEmail());

        if (result.isEmpty()
                || !passwordEncoder.matches(
                        student.getPassword(),
                        result.get().getPassword())) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        Student loggedInStudent = result.get();

        return ResponseEntity.ok(loggedInStudent);
    }

    // Get all students
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Get student by ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {

        return studentRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}