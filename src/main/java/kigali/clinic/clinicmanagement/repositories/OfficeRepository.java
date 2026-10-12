package kigali.clinic.clinicmanagement.repositories;

import kigali.clinic.clinicmanagement.models.Office;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfficeRepository extends JpaRepository<Office, UUID> {

    boolean existsByOfficeNumberIgnoreCase(String officeNumber);

    boolean existsByOfficeNumberIgnoreCaseAndIdNot(String officeNumber, UUID id);

    // C3: Appointment -> Doctor -> Office, busiest first
    @Query("SELECT o.name, o.officeNumber, COUNT(a) FROM Appointment a "
            + "JOIN a.doctor d JOIN d.office o "
            + "GROUP BY o.id, o.name, o.officeNumber "
            + "ORDER BY COUNT(a) DESC, o.officeNumber ASC")
    List<Object[]> findOfficesByAppointmentCount(Pageable pageable);
}
