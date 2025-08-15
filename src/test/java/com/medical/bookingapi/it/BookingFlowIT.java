package com.medical.bookingapi.it;

import io.restassured.RestAssured;

import com.medical.bookingapi.model.*;
import com.medical.bookingapi.repository.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookingFlowIT {

  @BeforeAll
  static void raLogsOnlyWhenFail() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @SuppressWarnings("resource")
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("bookingapi_it")
      .withUsername("test")
      .withPassword("test");

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry r) {
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    r.add("spring.jpa.show-sql", () -> "false");
    r.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
    r.add("security.jwt.secret", () -> "test-secret-please-change");
    r.add("security.jwt.expirationMinutes", () -> "120");
  }

  @LocalServerPort int port;

  @Autowired UserRepository userRepository;
  @Autowired DoctorRepository doctorRepository;
  @Autowired PatientRepository patientRepository;
  @Autowired AppointmentSlotRepository slotRepository;
  @Autowired AppointmentRepository appointmentRepository;
  @Autowired PasswordEncoder passwordEncoder;

  Long doctorId;
  Long patientId;
  Long slotId;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    // hard reset (child -> parent) to satisfy FKs
    appointmentRepository.deleteAllInBatch();
    slotRepository.deleteAllInBatch();
    doctorRepository.flush();
    patientRepository.flush();

    // seed users
    userRepository.findByEmail("admin@test.local")
        .orElseGet(() -> {
          Admin a = new Admin();
          a.setFirstName("Admin");
          a.setLastName("One");
          a.setEmail("admin@test.local");
          a.setPassword_hash(passwordEncoder.encode("AdminPass123!"));
          a.setRole("ADMIN");
          return userRepository.save(a);
        });

    Doctor doc = doctorRepository.findByEmail("doc@test.local")
        .orElseGet(() -> {
          Doctor d = new Doctor();
          d.setFirstName("Doc");
          d.setLastName("Who");
          d.setEmail("doc@test.local");
          d.setPassword_hash(passwordEncoder.encode("DocPass123!"));
          d.setRole("DOCTOR");
          d.setLocation("Nicosia");
          d.setSpeciality("Cardiology");
          d.setLicenseNumber("LIC-123");
          return doctorRepository.save(d);
        });
    doctorId = doc.getId();

    Patient pat = patientRepository.findByEmail("pat@test.local")
        .orElseGet(() -> {
          Patient p = new Patient();
          p.setFirstName("Pat");
          p.setLastName("Smith");
          p.setEmail("pat@test.local");
          p.setPassword_hash(passwordEncoder.encode("PatPass123!"));
          p.setRole("PATIENT");
          return patientRepository.save(p);
        });
    patientId = pat.getId();

    // seed one free slot
    LocalDateTime start = LocalDateTime.now()
        .plusHours(2).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime end = start.plusMinutes(30);

    AppointmentSlot slot = new AppointmentSlot();
    slot.setDoctor(doc);
    slot.setStartTime(start);
    slot.setEndTime(end);
    slot.setBooked(false);
    slot.setNotes("IT slot");
    slotId = slotRepository.saveAndFlush(slot).getId();
  }

  private String loginAndGetToken(String email, String password) {
    return RestAssured.given()
        .contentType(ContentType.JSON)
        .body("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}")
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .body("token", notNullValue())
        .extract()
        .path("token");
  }

  @Test
  void full_booking_flow_patient_books_doctor_confirms_lists_work() {
    // 1) Login as PATIENT
    String patientToken = loginAndGetToken("pat@test.local", "PatPass123!");

    // 2) Book appointment
    Long appointmentId =
        ((Number) RestAssured.given()
            .header("Authorization", "Bearer " + patientToken)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "doctorId": %d,
                    "patientId": %d,
                    "slotId": %d,
                    "notes": "book via IT"
                  }
                  """.formatted(doctorId, patientId, slotId))
            .when()
            .post("/api/appointments")
            .then()
            .statusCode(anyOf(is(200), is(201)))
            .body("doctorId", is(doctorId.intValue()))
            .body("patientId", is(patientId.intValue()))
            .body("slotId", is(slotId.intValue()))
            .extract()
            .path("id"))
        .longValue();

    // 3) Login as DOCTOR and confirm (requires ?status=)
    String doctorToken = loginAndGetToken("doc@test.local", "DocPass123!");

    RestAssured.given()
        .header("Authorization", "Bearer " + doctorToken)
        .accept(ContentType.JSON)
        .queryParam("status", "CONFIRMED")
        .when()
        .put("/api/appointments/{id}", appointmentId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);

    // 4a) List by doctor (use existing mapping)
    RestAssured.given()
        .header("Authorization", "Bearer " + doctorToken)
        .when()
        .get("/api/appointments/by-doctor/{docId}", doctorId)
        .then()
        .statusCode(200)
        .body("$", not(empty()))
        .body("find { it.id == %s }.status".formatted(appointmentId), equalTo("CONFIRMED"));

    // 4b) List by patient (use existing mapping)
    RestAssured.given()
        .header("Authorization", "Bearer " + patientToken)
        .when()
        .get("/api/appointments/by-patient/{patId}", patientId)
        .then()
        .statusCode(200)
        .body("$", not(empty()))
        .body("find { it.id == %s }.status".formatted(appointmentId), equalTo("CONFIRMED"));
  }

  @Test
  void security_checks_401_without_token_and_403_with_wrong_role() {
    // 401/403: no token on protected endpoint
    RestAssured.given()
        .when()
        .get("/api/admins/ping")
        .then()
        .statusCode(anyOf(is(401), is(403)));

    // PATIENT tries admin endpoint -> 403
    String patientToken = loginAndGetToken("pat@test.local", "PatPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + patientToken)
        .when()
        .get("/api/admins/ping")
        .then()
        .statusCode(403);

    // ADMIN happy path -> 200
    String adminToken = loginAndGetToken("admin@test.local", "AdminPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/api/admins/ping")
        .then()
        .statusCode(200)
        .body(equalTo("admin ok"));
  }
}
