package pm.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pm.model.activityrequest.ActivityRequest;
import pm.model.product.EProductApproStatus;

@Repository
public interface ActivityRequestRepository extends JpaRepository<ActivityRequest, Integer> {
        @Query(value = "SELECT * FROM activity_request WHERE user_id =:userId AND request_date=:date ORDER BY created_at DESC LIMIT 1 ", nativeQuery = true)
        ActivityRequest findbyUserIdAndRequestDate(int userId, LocalDate date);

        @Query(value = "SELECT * FROM activity_request WHERE user_id =:userId AND request_date=:date ", nativeQuery = true)
        Page<ActivityRequest> findbyUserIdAndRequestDate(int userId, LocalDate date, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request WHERE user_id =:userId AND status =:status ", nativeQuery = true)
        Page<ActivityRequest> findbyUserIdAndStatus(int userId, String status, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND request_date=:date AND status='Pending'", nativeQuery = true)
        List<ActivityRequest> findbySendedToAndRequestDate(int userId, LocalDate date);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND status='Pending' ", nativeQuery = true)
        List<ActivityRequest> findbySendedToAndRequestDate(int userId);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND status IN ('Approved','Rejected') ", nativeQuery = true)
        List<ActivityRequest> findbySendedToAndStatus(int userId);

        @Query(value = "SELECT * FROM activity_request  WHERE user_id =:userId AND request_date =:date AND status IN (:statuses)", nativeQuery = true)
        ActivityRequest findbyUserIdAndRequestDateAndStatus(int userId, LocalDate date, List<String> statuses);

        // @Query(value = "SELECT * FROM activity_request WHERE user_id = :userId AND
        // request_date BETWEEN :startOfMonth AND :endOfMonth", nativeQuery = true)
        // List<ActivityRequest> findByUserIdAndRequestDateBetween(@Param("userId") int
        // userId, @Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth")
        // LocalDate endOfMonth);

        @Query(value = "SELECT * FROM activity_request WHERE user_id =:userId", nativeQuery = true)
        List<ActivityRequest> findbyUserId(int userId);

        @Query(value = "SELECT * FROM activity_request WHERE user_id = :userId", nativeQuery = true)
        List<ActivityRequest> findByUserIdAndRequestDateBetween(@Param("userId") int userId);

        @Query(value = "SELECT * FROM activity_request WHERE request_date =:dateRange AND user_id = :userId AND status = 'Pending'  LIMIT 1", nativeQuery = true)
        ActivityRequest findByRaisedRequestIdAndDate(@Param("userId") int userId,
                        @Param("dateRange") LocalDate dateRange);

        @Query(value = "SELECT DISTINCT DATE(c.activity_date) AS activity_date "
                        + "FROM common_task_activities c "
                        + "WHERE DATE(c.activity_date) BETWEEN :fromdate AND :todate "
                        + "AND c.draft = false AND c.user_id = :user_id", nativeQuery = true)
        List<Date> findActivityDatesForUserSubmitted(@Param("user_id") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT DISTINCT DATE(a.applied_date) AS activity_date " +
                        "FROM attendance_table a " +
                        "WHERE a.status = 'leave' " +
                        "AND DATE(a.applied_date) BETWEEN :startDate AND :endDate " +
                        "AND a.user_id = :userId", nativeQuery = true)
        List<Date> findAttendanceDatesByUserId(@Param("userId") int userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query(value = "SELECT DISTINCT DATE(a.request_date) AS activity_date " +
                        "FROM activity_request a " +
                        "WHERE DATE(a.request_date) BETWEEN :start_date AND :end_date " +
                        "AND a.user_id = :userId " +
                        "AND a.status NOT IN ('Approved')", nativeQuery = true)

        List<Date> findRaisedRequestDatesByUserId(@Param("userId") int userId, @Param("start_date") LocalDate startDate,
                        @Param("end_date") LocalDate endDate);

        @Query(value = "SELECT DISTINCT DATE(a.request_date) AS activity_date " +
                        "FROM activity_request a " +
                        "WHERE DATE(a.request_date) BETWEEN :start_date AND :end_date " +
                        "AND a.user_id = :userId " +
                        "AND a.status IN ('Approved')", nativeQuery = true)

        List<Date> findRaisedRequestApprovedDatesByUserId(@Param("userId") int userId,
                        @Param("start_date") LocalDate startDate,
                        @Param("end_date") LocalDate endDate);


        @Query(value = "SELECT DISTINCT DATE(a.request_date) AS activity_date " +
                        "FROM activity_request a " +
                        "WHERE DATE(a.request_date) BETWEEN :start_date AND :end_date " +
                        "AND a.user_id = :userId " +
                        "AND a.status IN ('Pending')", nativeQuery = true)
        List<Date> findRaisedRequestPendingDatesByUserId(@Param("userId") int userId,
                        @Param("start_date") LocalDate startDate,
                        @Param("end_date") LocalDate endDate);


        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND request_date=:date AND user_id=:userid AND status=:status ", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndUserIdAndStatusAndPageable(int userId, LocalDate date,
                        int userid, String status, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND request_date=:date AND user_id=:userid AND status IN (:statuses)", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndUserIdAndPageableAndStatus(int userId, LocalDate date,
                        int userid, Pageable pageable, List<String> statuses);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND user_id=:userid AND status IN (:statuses)", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndUserIdAndPageableAndStatus(int userId, int userid, Pageable pageable,
                        List<String> statuses);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND request_date=:date AND status IN (:statuses)", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndPageableAndStatus(int userId, LocalDate date,
                        Pageable pageable, List<String> statuses);

        @Query(value = "SELECT * FROM activity_request "
                        + "WHERE sended_to = :userId AND request_date = :date AND user_id = :userid AND status = 'Pending' "
                        + "ORDER BY request_date DESC", countQuery = "SELECT COUNT(*) FROM activity_request "
                                        + "WHERE sended_to = :userId AND request_date = :date AND user_id = :userid AND status = 'Pending'", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndUserIdAndPageable(@Param("userId") int userId,
                        @Param("date") LocalDate date,
                        @Param("userid") int userid,
                        Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND user_id=:userid AND status=:status ", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndUserIdAndStatusAndPageable(int userId, int userid, String status,
                        Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND request_date=:date AND status=:status ", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndStatusAndPageable(int userId, LocalDate date,
                        String status, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request "
                        + "WHERE sended_to = :userId AND request_date = :date AND status = 'Pending'", countQuery = "SELECT COUNT(*) FROM activity_request "
                                        + "WHERE sended_to = :userId AND request_date = :date AND status = 'Pending'", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndRequestDateAndPageable(@Param("userId") int userId,
                        @Param("date") LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND status=:status order by request_date desc ", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndStatusAndPageable(int userId, String status, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request "
                        + "WHERE sended_to = :userId AND user_id = :userid AND status = 'Pending' "
                        + "ORDER BY request_date DESC", countQuery = "SELECT COUNT(*) FROM activity_request "
                                        + "WHERE sended_to = :userId AND user_id = :userid AND status = 'Pending'", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndUserIdAndPageable(@Param("userId") int userId,
                        @Param("userid") int userid,
                        Pageable pageable);

        @Query(value = "SELECT * FROM activity_request "
                        + "WHERE sended_to = :userId AND status = 'Pending' "
                        + "ORDER BY request_date DESC", countQuery = "SELECT COUNT(*) FROM activity_request "
                                        + "WHERE sended_to = :userId AND status = 'Pending'", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndPageable(@Param("userId") int userId, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE sended_to =:userId AND status IN (:statuses) order by request_date desc", nativeQuery = true)
        Page<ActivityRequest> findbySendedToAndPageableAndStatus(int userId, List<String> statuses, Pageable pageable);

        @Query(value = "SELECT * FROM activity_request  WHERE user_id =:userId AND request_date =:date AND status =:status", nativeQuery = true)
        Page<ActivityRequest> findbyUserIdAndRequestDateAndStatusAndPageable(int userId, LocalDate date, String status,
                        Pageable pageable);

        @Query(value = "SELECT * FROM activity_request WHERE user_id =:userId", nativeQuery = true)
        Page<ActivityRequest> findbyUserId(int userId, Pageable pageable);

        // mobile
        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.status=:status order by req.request_date DESC", nativeQuery = true)
        Page<Object[]> findbySendedToAndStatusAndPageableToSupervisor(int userId, String status,
                        Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.status=:status AND req.user_id = :memberId order by req.request_date DESC", nativeQuery = true)
        Page<Object[]> findbySendedToAndStatusAndUserIdAndPageableToSupervisor(int userId, String status, int memberId,
                        Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.request_date=:date AND req.status=:status ", nativeQuery = true)
        Page<Object[]> findbySendedToAndRequestDateAndStatusAndPageableToSupervisor(int userId, LocalDate date,
                        String status, Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.request_date=:date AND req.status=:status AND req.user_id = :memberId ", nativeQuery = true)
        Page<Object[]> findbySendedToAndRequestDateAndStatusAndUserIdAndPageableToSupervisor(int userId, LocalDate date,
                        String status, int memberId, Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason,req.status FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.status IN (:status) order by req.request_date DESC", nativeQuery = true)
        Page<Object[]> findbySendedToAndStatusAndPageableToSupervisor(int userId, List<String> status,
                        Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason,req.status FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.status IN (:status) AND req.user_id = :memberId order by req.request_date DESC", nativeQuery = true)
        Page<Object[]> findbySendedToAndStatusAndUserIdAndPageableToSupervisor(int userId, List<String> status,
                        int memberId,
                        Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason,req.status FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.request_date=:date AND req.status IN (:status) ", nativeQuery = true)
        Page<Object[]> findbySendedToAndRequestDateAndStatusAndPageableToSupervisor(int userId, LocalDate date,
                        List<String> status, Pageable pageable);

        @Query(value = "SELECT req.id,u.name,req.request_date,req.reason,req.status FROM activity_request as req JOIN users as u ON u.id = req.user_id  WHERE req.sended_to =:userId AND req.request_date=:date AND req.status IN (:status) AND req.user_id = :memberId ", nativeQuery = true)
        Page<Object[]> findbySendedToAndRequestDateAndStatusAndUserIdAndPageableToSupervisor(int userId, LocalDate date,
                        List<String> status, int memberId, Pageable pageable);

        @Query(value = "SELECT count(*) FROM activity_request where user_id=:id and status = 'Pending' and request_date =:date", nativeQuery = true)
        Long getRaisedRequestCount(LocalDate date, int id);

        @Query(value = "SELECT ar.id, ar.request_date, ar.status, ar.reason, u.name " +
                        "FROM activity_request ar " +
                        "JOIN users u ON ar.user_id = u.id " +
                        "WHERE ar.user_id = :id AND ar.request_date = :date", nativeQuery = true)
        List<Object[]> findRaisedRequestByUserIdAndDate(@Param("id") int id, @Param("date") LocalDate date);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO activity_request (created_at, is_deleted, request_date, sended_to, status, updated_at, user_id, reason) "
                        +
                        "VALUES (now(), false, :requestDate, :sendedTo, 'Approved', now(), :userId, '')", nativeQuery = true)
        void insertActivityRequest(@Param("requestDate") LocalDate requestDate,
                        @Param("sendedTo") int sendedTo,
                        @Param("userId") int userId);

}
