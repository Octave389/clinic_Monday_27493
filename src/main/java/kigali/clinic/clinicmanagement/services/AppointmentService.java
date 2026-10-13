package kigali.clinic.clinicmanagement.services;

import kigali.clinic.clinicmanagement.models.Appointment;
import kigali.clinic.clinicmanagement.models.AppointmentStatus;
import kigali.clinic.clinicmanagement.models.Doctor;
import kigali.clinic.clinicmanagement.models.Patient;
import kigali.clinic.clinicmanagement.repositories.AppointmentRepository;
import kigali.clinic.clinicmanagement.repositories.DoctorRepository;
import kigali.clinic.clinicmanagement.repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static kigali.clinic.clinicmanagement.utils.Validations.normalize;

@Service
public class AppointmentService {

    private static final String DOCTOR_BOOKED = "Doctor is already booked on that date";

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Transactional
    public String saveAppointment(Appointment appointment) {

        String error = resolvePatient(appointment);

        if (error != null) {
            return error;
        }

        error = resolveDoctor(appointment);

        if (error != null) {
            return error;
        }

        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }

        if (appointment.getAppointmentDate().isBefore(LocalDateTime.now())) {
            return "Appointment date must be in the future";
        }

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

        if (isDoctorBooked(appointment, null)) {
            return DOCTOR_BOOKED;
        }

        appointment.setId(null);
        appointment.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(appointment);

        return "Appointment is saved Successfully";
    }

    @Transactional
    public String updateAppointment(UUID id, Appointment appointment) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Appointment not found";
        }

        String error = resolvePatient(appointment);

        if (error != null) {
            return error;
        }

        error = resolveDoctor(appointment);

        if (error != null) {
            return error;
        }

        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }

        // a past date stays allowed here, so old appointments can still be closed
        if (appointment.getStatus() == null) {
            appointment.setStatus(existing.getStatus());
        }

        // only an appointment that takes a new slot is checked, so a day that already
        // holds two appointments can still have them confirmed or completed
        boolean takesNewSlot = existing.getStatus() == AppointmentStatus.CANCELLED
                || !existing.getDoctor().getId().equals(appointment.getDoctor().getId())
                || !existing.getAppointmentDate().toLocalDate().equals(appointment.getAppointmentDate().toLocalDate());

        if (takesNewSlot && isDoctorBooked(appointment, id)) {
            return DOCTOR_BOOKED;
        }

        existing.setPatient(appointment.getPatient());
        existing.setDoctor(appointment.getDoctor());
        existing.setAppointmentDate(appointment.getAppointmentDate());
        existing.setStatus(appointment.getStatus());
        existing.setReason(normalize(appointment.getReason()));
        appointmentRepo.save(existing);

        return "Appointment is updated Successfully";
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepo.findAll();
    }

    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> getAppointmentsBetween(LocalDate start, LocalDate end) {
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start.atStartOfDay(), endOfDay(end));
    }

    public List<Object[]> getAppointmentCountsByStatus() {
        return appointmentRepo.countAppointmentsPerStatus();
    }

    // COMPLETED visits stay untouched; already CANCELLED ones are skipped so the count is only what changed
    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {

        int cancelled = appointmentRepo.cancelDoctorDay(
                doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay(), LocalDateTime.now());

        return cancelled + " appointments cancelled";
    }

    public Appointment getAppointmentById(UUID id) {
        return appointmentRepo.findById(id).orElse(null);
    }

    @Transactional
    public String deleteAppointment(UUID id) {

        Appointment existing = appointmentRepo.findById(id).orElse(null);

        if (existing == null) {
            return "Appointment not found";
        }

        appointmentRepo.delete(existing);

        return "Appointment is deleted Successfully";
    }

    // a CANCELLED appointment does not hold the day, so it never blocks and is never blocked
    private boolean isDoctorBooked(Appointment appointment, UUID ignoredId) {

        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return false;
        }

        UUID doctorId = appointment.getDoctor().getId();
        LocalDate day = appointment.getAppointmentDate().toLocalDate();

        return ignoredId == null
                ? appointmentRepo.existsByDoctorIdAndAppointmentDateBetweenAndStatusNot(
                        doctorId, day.atStartOfDay(), endOfDay(day), AppointmentStatus.CANCELLED)
                : appointmentRepo.existsByDoctorIdAndAppointmentDateBetweenAndStatusNotAndIdNot(
                        doctorId, day.atStartOfDay(), endOfDay(day), AppointmentStatus.CANCELLED, ignoredId);
    }

    // Postgres keeps microseconds, so LocalTime.MAX would round up to midnight of the next day
    private static LocalDateTime endOfDay(LocalDate day) {
        return day.atTime(LocalTime.MAX).truncatedTo(ChronoUnit.MICROS);
    }

    private String resolvePatient(Appointment appointment) {

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Patient is required";
        }

        Patient patient = patientRepo.findById(appointment.getPatient().getId()).orElse(null);

        if (patient == null) {
            return "Patient not found";
        }

        appointment.setPatient(patient);

        return null;
    }

    private String resolveDoctor(Appointment appointment) {

        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Doctor is required";
        }

        Doctor doctor = doctorRepo.findById(appointment.getDoctor().getId()).orElse(null);

        if (doctor == null) {
            return "Doctor not found";
        }

        appointment.setDoctor(doctor);

        return null;
    }
}
