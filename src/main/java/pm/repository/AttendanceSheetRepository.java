package pm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pm.dto.AttendanceSheetDto;
import pm.model.attendanceSheet.AttendanceSheet;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceSheetRepository extends JpaRepository<AttendanceSheet, Integer> {
        boolean existsByUserid(int userid);

        AttendanceSheet findByUseridAndAppliedDate(int userid, LocalDate date);

        boolean existsByUseridAndAppliedDate(int userid, LocalDate date);

        boolean existsByAppliedDate(LocalDate date);

        @Query(value = "SELECT a.status FROM attendance_table a WHERE a.user_id = :userId and a.applied_date=:date", nativeQuery = true)
        String findStatusByUserId(int userId, LocalDate date);

        @Query(value = "SELECT * FROM attendance_table WHERE applied_date = :date AND user_id = :id AND status = :status LIMIT 1", nativeQuery = true)
        AttendanceSheet findByUserIdAndApplieddateDetail(@Param("id") int id, @Param("date") LocalDate date,
                        @Param("status") String status);

        @Query(value = "SELECT * FROM attendance_table WHERE user_id = :userId AND status='leave'", nativeQuery = true)
        Page<AttendanceSheet> findAllByUserid(int userId, Pageable pageable);

        @Query(value = "SELECT * FROM attendance_table WHERE applied_date = :date AND user_id = :userId AND status='leave'", nativeQuery = true)
        Page<AttendanceSheet> findByUseridAndAppliedDatewithpagination(int userId, LocalDate date, Pageable pageable);

        //
        @Query(value = "SELECT at1.applied_date,u1.name,at1.status FROM attendance_table at1 JOIN users u1 ON at1.user_id = u1.id WHERE u1.supervisor = :supervisorId AND at1.status='leave' and at1.applied_date = :date", nativeQuery = true)
        Page<Object[]> findByUseridAndAppliedDateforSupervisorWithDate(String supervisorId, LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT at1.applied_date,u1.name,at1.status FROM attendance_table at1 JOIN users u1 ON at1.user_id = u1.id WHERE u1.supervisor = :supervisorId AND at1.status='leave'", nativeQuery = true)
        Page<Object[]> findByUseridAndAppliedDateforSupervisor(String supervisorId, Pageable pageable);

        @Query(value = "SELECT at1.applied_date,u1.name,at1.status FROM attendance_table at1 JOIN users u1 ON at1.user_id = u1.id WHERE u1.supervisor = :supervisorId AND at1.status='leave' and at1.applied_date = :date and at1.user_id=:userId", nativeQuery = true)
        Page<Object[]> findByUseridAndAppliedDateforSupervisor(String supervisorId, int userId, LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT at1.applied_date,u1.name,at1.status FROM attendance_table at1 JOIN users u1 ON at1.user_id = u1.id WHERE u1.supervisor = :supervisorId AND at1.status='leave'and at1.user_id=:userId", nativeQuery = true)
        Page<Object[]> findByUseridAndAppliedDateforUserandDate(String supervisorId, int userId, Pageable pageable);

        @Query(value = "SELECT COUNT(*) FROM attendance_table WHERE user_id = :userId AND MONTH(applied_date) = :month AND YEAR(applied_date) = :year AND status = 'leave'", nativeQuery = true)
        int countByUseridAndMonth(@Param("userId") Integer userId, @Param("month") int month, @Param("year") int year);

        @Query(value = "SELECT COUNT(*) FROM attendance_table WHERE user_id = :id AND applied_date = :date and status ='leave'", nativeQuery = true)
        Long countByUseridandDate(LocalDate date, int id);

        @Query(value = "SELECT status FROM attendance_table WHERE user_id = :id AND applied_date = :date AND (status ='leave' OR status='Leave')", nativeQuery = true)
        String statusForAppliedDateandUser(LocalDate date, int id);

        @Query(value = "SELECT ar.id, ar.applied_date, ar.status, u.username, u.name " +
                        "FROM attendance_table ar " +
                        "JOIN users u ON ar.user_id = u.id " +
                        "WHERE ar.user_id = :id AND ar.applied_date = :date", nativeQuery = true)
        List<Object[]> findAttendanceRequestByUserIdAndDate(int id, LocalDate date);
}
