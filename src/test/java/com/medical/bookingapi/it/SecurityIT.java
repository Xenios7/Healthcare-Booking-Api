package com.medical.bookingapi.it;

import io.restassured.RestAssured;
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

import com.medical.bookingapi.model.*;
import com.medical.bookingapi.repository.*;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityIT {

  @DynamicPropertySource
  static void jwtProps(DynamicPropertyRegistry r) {
    // 32-byte key, Base64 (keep quotes exactly as is)
    r.add("security.jwt.secret", () -> "kztivzfn2xsCsQ0D+yfCqNIHTNvtHEcexKJSWoxcp2g=");
    r.add("security.jwt.issuer", () -> "bookingapi-test");
    r.add("security.jwt.expiration", () -> "PT1H"); // 1 hour, ISO-8601 duration
  }

  @BeforeAll
  static void logOnlyOnFail() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
  }

  @SuppressWarnings("resource")
  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
      .withDatabaseName("bookingapi_it_sec")
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
  Long patientAId;
  Long patientBId;
  Long slotIdForDoctor;

  @BeforeEach
  void setUp() {
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    // hard reset (respect FK order)
    appointmentRepository.deleteAllInBatch();
    slotRepository.deleteAllInBatch();
    doctorRepository.flush();
    patientRepository.flush();

    // Admin
    userRepository.findByEmail("admin@test.local").orElseGet(() -> {
      Admin a = new Admin();
      a.setFirstName("Admin");
      a.setLastName("One");
      a.setEmail("admin@test.local");
      a.setPassword_hash(passwordEncoder.encode("AdminPass123!"));
      a.setRole("ADMIN");
      return userRepository.save(a);
    });

    // Doctor
    Doctor doc = doctorRepository.findByEmail("doc@test.local").orElseGet(() -> {
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

    // Patient A
    Patient pa = patientRepository.findByEmail("patA@test.local").orElseGet(() -> {
      Patient p = new Patient();
      p.setFirstName("Pat");
      p.setLastName("Alpha");
      p.setEmail("patA@test.local");
      p.setPassword_hash(passwordEncoder.encode("PatPass123!"));
      p.setRole("PATIENT");
      return patientRepository.save(p);
    });
    patientAId = pa.getId();

    // Patient B
    Patient pb = patientRepository.findByEmail("patB@test.local").orElseGet(() -> {
      Patient p = new Patient();
      p.setFirstName("Pat");
      p.setLastName("Beta");
      p.setEmail("patB@test.local");
      p.setPassword_hash(passwordEncoder.encode("PatPass123!"));
      p.setRole("PATIENT");
      return patientRepository.save(p);
    });
    patientBId = pb.getId();

    // one free slot for doctor
    LocalDateTime start = LocalDateTime.now().plusHours(2).withMinute(0).withSecond(0).withNano(0);
    LocalDateTime end = start.plusMinutes(30);
    AppointmentSlot slot = new AppointmentSlot();
    slot.setDoctor(doc);
    slot.setStartTime(start);
    slot.setEndTime(end);
    slot.setBooked(false);
    slot.setNotes("SEC-IT slot");
    slotIdForDoctor = slotRepository.saveAndFlush(slot).getId();
  }

  // --- helpers ---
  private String login(String email, String password) {
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

  private long bookAsPatient(String token, long doctorId, long patientId, long slotId) {
    return ((Number) RestAssured.given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("""
              {
                "doctorId": %d,
                "patientId": %d,
                "slotId": %d,
                "notes": "sec-it"
              }
              """.formatted(doctorId, patientId, slotId))
        .when()
        .post("/api/appointments")
        .then()
        .statusCode(anyOf(is(200), is(201)))
        .extract()
        .path("id")).longValue();
  }

  // ------------------ TESTS ------------------

  @Test
  void protected_endpoint_without_token_returns_401_or_403() {
    RestAssured.given()
        .when()
        .get("/api/appointments")
        .then()
        .statusCode(anyOf(is(401), is(403))); // depending on your entrypoint
  }

  @Test
  void protected_endpoint_with_malformed_token_returns_401() {
    RestAssured.given()
        .header("Authorization", "Bearer not-a-jwt")
        .when()
        .get("/api/appointments")
        .then()
        .statusCode(401);
  }

  @Test
  void patient_cannot_access_admin_endpoint_403_and_admin_can_200() {
    String patientToken = login("patA@test.local", "PatPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + patientToken)
        .when()
        .get("/api/admins/ping")
        .then()
        .statusCode(403);

    String adminToken = login("admin@test.local", "AdminPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .get("/api/admins/ping")
        .then()
        .statusCode(200)
        .body(equalTo("admin ok"));
  }

  @Test
  void patient_cannot_confirm_appointment_403_but_doctor_can_200() {
    // patient A books
    String patientToken = login("patA@test.local", "PatPass123!");
    long appointmentId = bookAsPatient(patientToken, doctorId, patientAId, slotIdForDoctor);

    // patient tries to confirm -> 403
    RestAssured.given()
        .header("Authorization", "Bearer " + patientToken)
        .accept(ContentType.JSON)
        .queryParam("status", "CONFIRMED")
        .when()
        .put("/api/appointments/{id}", appointmentId)
        .then()
        .statusCode(403);

    // doctor confirms -> 200
    String doctorToken = login("doc@test.local", "DocPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + doctorToken)
        .accept(ContentType.JSON)
        .queryParam("status", "CONFIRMED")
        .when()
        .put("/api/appointments/{id}", appointmentId)
        .then()
        .statusCode(200)
        .contentType(ContentType.JSON);
  }

  @Test
  void patient_cannot_read_other_patients_appointments() {
    // create some data for patient A
    String tokenA = login("patA@test.local", "PatPass123!");
    bookAsPatient(tokenA, doctorId, patientAId, slotIdForDoctor);

    // patient B tries to read A's list
    String tokenB = login("patB@test.local", "PatPass123!");
    RestAssured.given()
        .header("Authorization", "Bearer " + tokenB)
        .when()
        .get("/api/appointments?patientId={pid}", patientAId)
        .then()
        // If your policy is filtering instead of forbidding, change to 200 + empty list:
        // .statusCode(200).body("$", empty());
        .statusCode(403);
  }
}
