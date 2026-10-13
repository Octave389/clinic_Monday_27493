package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.services.AppointmentService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = {"/api/appointment", "/api/appointments"})
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveAppointment(@RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.saveAppointment(appointment), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateAppointment(@PathVariable UUID id, @RequestBody Appointment appointment) {
        return ApiResponses.fromMessage(appointmentService.updateAppointment(id, appointment), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
    }

    // Spring turns the text into the enum; ApiExceptionHandler answers an unknown status with 400
    @GetMapping(value = "/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentsByStatus(@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByStatus(status));
    }

    // a badly written date throws DateTimeParseException, which ApiExceptionHandler turns into 400
    @GetMapping(value = "/between", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentsBetween(@RequestParam String start, @RequestParam String end) {

        LocalDate startDate = LocalDate.parse(start.trim());
        LocalDate endDate = LocalDate.parse(end.trim());

        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body(Map.of("message", "End date must not be before start date"));
        }

        return ResponseEntity.ok(appointmentService.getAppointmentsBetween(startDate, endDate));
    }

    @GetMapping(value = "/stats/by-status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentCountsByStatus() {
        return ResponseEntity.ok(appointmentService.getAppointmentCountsByStatus());
    }

    @PatchMapping(value = "/cancel-day", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> cancelDoctorDay(@RequestParam UUID doctorId,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(Map.of("message", appointmentService.cancelDoctorDay(doctorId, date)));
    }

    // bonus: sort arrives as "property,direction", e.g. appointmentDate,desc
    @GetMapping(value = "/page", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentsPage(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "5") int size,
                                                 @RequestParam(defaultValue = "appointmentDate,asc") String sort) {

        if (page < 0 || size < 1) {
            return ResponseEntity.badRequest().body(Map.of("message", "Page must be 0 or more and size at least 1"));
        }

        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromOptionalString(parts[1].trim()).orElse(null)
                : Sort.Direction.ASC;

        if (property.isEmpty() || direction == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Sort must look like appointmentDate,desc"));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        return ResponseEntity.ok(appointmentService.getAppointmentsPage(pageable));
    }

    @DeleteMapping(value = "/cancelled-before", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteCancelledBefore(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(Map.of("message", appointmentService.deleteCancelledBefore(date)));
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAppointmentById(@PathVariable UUID id) {
        return ApiResponses.fromData(appointmentService.getAppointmentById(id), "Appointment not found");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteAppointment(@PathVariable UUID id) {
        return ApiResponses.fromMessage(appointmentService.deleteAppointment(id), HttpStatus.OK);
    }
}
