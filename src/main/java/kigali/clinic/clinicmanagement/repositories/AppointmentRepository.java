package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    boolean existsByDoctorIdAndDateAndStatusNot(UUID doctorId, LocalDateTime date, String status);

    boolean existsByDoctorIdAndDateAndStatusNotAndIdNot(UUID doctorId, LocalDateTime date, String status, UUID id);

    List<Appointment> findByStatusOrderByDateAsc(String status);

    List<Appointment> findByDateBetweenOrderByDateAsc(LocalDateTime start, LocalDateTime end);

    boolean existsByDoctorId(UUID doctorId);

    boolean existsByPatientId(UUID patientId);

    // C1: a status nobody uses forms no group, so it does not show up
    @Query("SELECT a.status, COUNT(a) FROM Appointment a "
            + "GROUP BY a.status ORDER BY COUNT(a) DESC, a.status ASC")
    List<Object[]> countAppointmentsPerStatus();

    // C4: date carries a time, so the day is matched as [dayStart, nextDayStart).
    // Bulk updates skip @PreUpdate, which is why updatedAt is set here.
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Appointment a SET a.status = 'CANCELLED', a.updatedAt = :now "
            + "WHERE a.doctor.id = :doctorId "
            + "AND a.date >= :dayStart AND a.date < :nextDayStart "
            + "AND a.status NOT IN ('COMPLETED', 'CANCELLED')")
    int cancelDoctorDay(@Param("doctorId") UUID doctorId,
                        @Param("dayStart") LocalDateTime dayStart,
                        @Param("nextDayStart") LocalDateTime nextDayStart,
                        @Param("now") LocalDateTime now);
}
