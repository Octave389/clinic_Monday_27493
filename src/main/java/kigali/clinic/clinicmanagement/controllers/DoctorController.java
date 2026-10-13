package kigali.clinic.clinicmanagement.controllers;

import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.services.DoctorService;
import kigali.clinic.clinicmanagement.utils.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = {"/api/doctor", "/api/doctors"})
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @PostMapping(value = "/save", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveDoctor(@RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.saveDoctor(doctor), HttpStatus.CREATED);
    }

    @PutMapping(value = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctor) {
        return ApiResponses.fromMessage(doctorService.updateDoctor(id, doctor), HttpStatus.OK);
    }

    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors());
    }

    @GetMapping(value = "/by-specialization", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorsBySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.getDoctorsBySpecialization(name));
    }

    @GetMapping(value = "/without-office", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorsWithoutOffice() {
        return ResponseEntity.ok(doctorService.getDoctorsWithoutOffice());
    }

    @PutMapping(value = "/{doctorId}/specializations/{specializationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        return ApiResponses.fromMessage(doctorService.addSpecialization(doctorId, specializationId), HttpStatus.OK);
    }

    @DeleteMapping(value = "/{doctorId}/specializations/{specializationId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> removeSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        return ApiResponses.fromMessage(doctorService.removeSpecialization(doctorId, specializationId), HttpStatus.OK);
    }

    @PatchMapping(value = "/{doctorId}/office", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeOffice(@PathVariable UUID doctorId, @RequestParam String officeNumber) {
        return ApiResponses.fromMessage(doctorService.changeOffice(doctorId, officeNumber), HttpStatus.OK);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getDoctorById(@PathVariable UUID id) {
        return ApiResponses.fromData(doctorService.getDoctorById(id), DoctorService.DOCTOR_NOT_FOUND);
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteDoctor(@PathVariable UUID id) {
        return ApiResponses.fromMessage(doctorService.deleteDoctor(id), HttpStatus.OK);
    }
}
