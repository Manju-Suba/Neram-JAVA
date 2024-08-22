package pm.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pm.model.task.CommonTimeSheetActivity;

import java.time.LocalDate;
import java.util.List;

public interface CommonTimeSheetActivityRepository extends JpaRepository<CommonTimeSheetActivity, Integer> {
        List<CommonTimeSheetActivity> findByUserId(int id);

        // ================================================================Supervisor Id
        // Based Time Sheet Activity Showing
        // =======================================================================================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDate(@Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        @Modifying
        @Transactional
        @Query(value = "DELETE FROM common_task_activities WHERE draft = true AND id = :id", nativeQuery = true)
        int deleteTimesheetRecord(@Param("id") int id);

        @Query(value = "select ta.prod_id,p.name from common_task_activities ta JOIN products p ON ta.prod_id = p.id  WHERE ta.user_id = :userId  AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findByUserIdAndActivityDatebetweenProductname(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        // distinict product name for supervisor based list
        @Query(value = "SELECT DISTINCT p.id, p.name AS product_name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ta.activity_date BETWEEN :fromdate AND :todate " +
                        "AND ta.supervisor = :supervisor " +
                        "AND ta.draft = false " +
                        "AND ta.supervisor_approved = false", nativeQuery = true)
        // distinict product name for supervisor based list
        List<Object[]> findDistinctProductNames(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate,
                        @Param("supervisor") String supervisor);

        @Query(value = "SELECT DISTINCT ta.user_id,u.name,u.profile_pic from common_task_activities ta JOIN users u ON ta.user_id = u.id  WHERE  ta.supervisor = :supervisor  AND ta.activity_date BETWEEN :fromdate AND :todate AND ta.draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findDisnictProductAndMemberNames(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate,
                        @Param("supervisor") String supervisor);

        @Query(value = "SELECT DISTINCT ta.user_id,u.name,u.profile_pic from common_task_activities ta JOIN users u ON ta.user_id = u.id  WHERE  ta.supervisor =:supervisor AND ta.prod_id = :prodid  AND ta.activity_date BETWEEN :fromdate AND :todate AND ta.draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findByUserIdAndActivityDatebetweenmembername(
                        @Param("prodid") Integer prodid,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("supervisor") String supervisor);

        @Query(value = "select prod_id from common_task_activities where WHERE user_id = :userId  AND activity_date BETWEEN :fromdate AND :todate AND draft = false AND supervisor_approved = false", nativeQuery = true)
        List<Integer> findByUserIdAndActivityDatebetweenProductBasedList(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        // ==========================product date range based list

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatebetweenBasedList(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatebetweenBasedListDetail(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND ta.prod_id =:prodId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatebetweenBasedProdname(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prodId") Integer prodId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND ta.prod_id =:prodId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatebetweenBasedProdnameDetail(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prodId") Integer prodId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.supervisor_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatebetween(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND draft = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermision(@Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND draft = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermisionList(@Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        // =====================================================================================supervisor
        // based List
        // Show======================================================================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND draft = false AND ta.supervisor_status in( 'Pending','Resubmit') and ta.supervisor_approved = false ", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserId(@Param("userId") Integer userId);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id = :id AND activity_date = :date AND draft = :b", nativeQuery = true)
        List<CommonTimeSheetActivity> findByUserAndActivityDateAndDraftNative(@Param("date") LocalDate date,
                        @Param("b") boolean b,
                        @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id = :id  AND draft = :draft", nativeQuery = true)
        List<CommonTimeSheetActivity> findByUserAndActivityDateAndDraft(@Param("draft") boolean draft,
                        @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id = :id  AND draft = :draft AND activity_date = :date", nativeQuery = true)
        List<CommonTimeSheetActivity> findByUserAndActivityDateAndDraftwithDate(@Param("draft") boolean draft,
                        @Param("id") Integer id, @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
        long countByActivityDateAndActivityIdAndStatus(@Param("date") LocalDate date, @Param("id") Integer id);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
        String hoursByActivityDateAndActivityIdAndStatus(@Param("date") LocalDate date, @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
        CommonTimeSheetActivity countByActivityDateAndActivityIdAndStatusvalue(@Param("date") LocalDate date,
                        @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id =:userId AND request_date=:date ", nativeQuery = true)
        CommonTimeSheetActivity findbyUserIdAndRequestDate(int userId, LocalDate date);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id  WHERE draft = false AND ta.supervisor_approved = true and ta.owner_approved = false AND ta.final_approve   NOT IN ('TL Approved','Approved')", nativeQuery = true)
        List<Object[]> findTaskDTOsByAndActivityDate();

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApprove(
                        @Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveDateRange(
                        @Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveDateRangeDetail(
                        @Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.prod_id =:prodId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findUserIdAndActivityDateAndFinalApproveDateRangeByproductId(
                        @Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prodId") Integer prodId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.prod_id =:prodId AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findUserIdAndActivityDateAndFinalApproveDateRangeByproductIdDetail(
                        @Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prodId") Integer prodId);

        @Query(value = "select ta.user_id,u.name,u.profile_pic from common_task_activities ta JOIN users u ON ta.user_id = u.id  WHERE ta.user_id = :userid AND ta.prod_id = :prodid  AND ta.activity_date BETWEEN :fromdate AND :todate AND ta.draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findByUserIdAndActivityDatebetweenmemberusername(@Param("userid") Integer userid,
                        @Param("prodid") Integer prodid,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "select ta.prod_id,p.name from common_task_activities ta JOIN products p ON ta.prod_id = p.id  WHERE ta.user_id = :userId  AND ta.activity_date BETWEEN :fromdate AND :todate AND draft = false AND ta.final_approve IN ('TL Approved')", nativeQuery = true)
        List<Object[]> findByUserIdActivityDateProductnameFinalApprove(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        // ====================================================================================
        // Final Approval List
        // =============================================================================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND draft = false AND ta.final_approve IN ('TL Approved')and ta.supervisor_approved = true and ta.is_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveall(
                        @Param("userId") Integer userId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND draft = false AND ta.final_approve IN ('TL Approved')and ta.supervisor_approved = true and ta.is_approved = false", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(
                        @Param("userId") Integer userId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve,ta.branch, fal.remarks FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id JOIN final_approve_log fal ON ta.id = fal.task_activity_id WHERE u.id = :userId  AND draft = false AND ta.final_approve IN ('Approved','Reject')", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityAndFinalApproveallApproved(
                        @Param("userId") Integer userId);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch, fal.remarks FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id JOIN final_approve_log fal ON ta.id = fal.task_activity_id WHERE u.id = :userId  AND draft = false AND ta.final_approve IN ('Approved','Reject')", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityAndFinalApproveallApprovedList(
                        @Param("userId") Integer userId);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id =:userId AND activity_date=:date AND draft = true ", nativeQuery = true)
        List<CommonTimeSheetActivity> findbyUserIdAndRequestDateanddraftTrue(int userId, LocalDate date);

        @Query(value = "SELECT * FROM common_task_activities WHERE activity_date = :date AND user_id = :id AND draft =false limit 1", nativeQuery = true)
        CommonTimeSheetActivity findUserIdAndActivityDate(@Param("date") LocalDate date, @Param("id") Integer id);

        // daterange mani code
        @Query(value = "SELECT * FROM common_task_activities WHERE activity_date BETWEEN :fromDate AND :toDate AND user_id = :id AND draft = false ", nativeQuery = true)
        List<CommonTimeSheetActivity> findUserIdAndActivityDateDetail(@Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate, @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities WHERE activity_date = :date OR user_id = :id AND draft =false", nativeQuery = true)
        List<CommonTimeSheetActivity> findUserIdORActivityDate(@Param("date") LocalDate date, @Param("id") Integer id);

        @Query(value = "SELECT * FROM common_task_activities c WHERE c.activity_date = :date AND c.user_id IN :ids ", nativeQuery = true)
        List<CommonTimeSheetActivity> findUserIdAndActivityDates(@Param("ids") List<Integer> memberIds,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT * FROM common_task_activities c WHERE c.activity_date = :date AND c.user_id =:ids limit 1 ", nativeQuery = true)
        // emaya
        CommonTimeSheetActivity findUserIdAndActivityDatesbyloop(@Param("ids") Integer memberIds,
                        @Param("date") LocalDate date);// emaya
        // daterange mani code

        @Query(value = "SELECT * FROM common_task_activities c WHERE c.activity_date BETWEEN :fromDate AND :toDate AND c.user_id = :ids ", nativeQuery = true)
        List<CommonTimeSheetActivity> findUserIdAndActivityDatesbyloops(@Param("ids") Integer memberIds,
                        @Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate); // mani

        @Query(value = "SELECT * FROM common_task_activities c WHERE c.activity_date = :date OR c.user_id IN :ids", nativeQuery = true)
        List<CommonTimeSheetActivity> findUserIdORActivityDates(@Param("ids") List<Integer> memberIds,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities c WHERE c.user_id = :id AND c.activity_date = :date AND c.draft =false", nativeQuery = true)
        String findbyhours(@Param("date") LocalDate date, @Param("id") Integer id);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities c WHERE c.user_id = :id AND c.id != :passid AND c.activity_date = :date AND c.draft =false", nativeQuery = true)
        String findbyHoursBasedonreSubmit(@Param("date") LocalDate date, @Param("id") Integer id,
                        @Param("passid") int passid);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities c WHERE c.user_id = :id AND c.activity_date = :date AND c.draft =true", nativeQuery = true)
        String findbyhoursForDraft(@Param("date") LocalDate date, @Param("id") Integer id);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities c WHERE NOT c.id=:activityid AND c.user_id = :id AND c.activity_date = :date AND c.draft =false", nativeQuery = true)
        String findbyhoursAndId(@Param("date") LocalDate date, @Param("id") Integer id,
                        @Param("activityid") Integer activityid);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities WHERE user_id = :userId AND activity_date = :date", nativeQuery = true)
        String findHoursByUserIdAndActivityDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities WHERE user_id = :userId AND activity_date = :date", nativeQuery = true)
        String findHoursByPersonIdAndActivityDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities WHERE user_id = :userId AND activity_date BETWEEN :fromdate AND :todate", nativeQuery = true)
        String findHoursByPersonIdAndActivityDateRange(@Param("userId") Integer userId,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate date);

        @Query(value = "SELECT CASE " +
                        "WHEN total_hours IS NULL THEN hours " +
                        "ELSE total_hours " +
                        "END AS total_hours " +
                        "FROM (" +
                        "SELECT " +
                        "SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours, " +
                        "MAX(hours) AS hours " + // We use MAX to ensure that hours is included but not aggregated
                        "FROM common_task_activities " +
                        "WHERE user_id = :userId " +
                        "AND activity_date BETWEEN :fromdate AND :todate " +
                        "GROUP BY user_id, activity_date " +
                        "ORDER BY total_hours DESC " + // Order by total_hours in descending order
                        "LIMIT 1" + // Select only the first result
                        ") AS subquery", nativeQuery = true)
        String getTotalHours(@Param("userId") Integer userId, @Param("fromdate") LocalDate fromDate,
                        @Param("todate") LocalDate toDate);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND final_approve = 'Approved'", nativeQuery = true)
        Long countApprovedRecords(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND final_approve = 'Reject'", nativeQuery = true)
        Long countRejectRecords(@Param("userId") Integer userId, @Param("date") LocalDate date);

        // count of owner approval
        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND owner_status = 'Approved'", nativeQuery = true)
        Long countApprovedRecordsOwner(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND owner_status = 'Reject'", nativeQuery = true)
        Long countRejectRecordsOwner(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND supervisor_approved = true", nativeQuery = true)
        Long countApprovedbySupervisor(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT final_approve FROM common_task_activities WHERE user_id = :userId AND activity_date = :date and draft = false", nativeQuery = true)
        List<String> findByUserIdAndActivityDate(@Param("userId") Integer userId, @Param("date") LocalDate date);

        // supervisor status
        @Query(value = "SELECT supervisor_status FROM common_task_activities WHERE user_id = :userId AND activity_date = :date and draft = false", nativeQuery = true)
        List<String> findByUserIdAndActivityDateSupervisorStaus(@Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND supervisor_status = 'Approved'", nativeQuery = true)
        Long countApprovedbySupervisorApproved(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND supervisor_status = 'Reject'", nativeQuery = true)
        Long countApprovedbySupervisorReject(@Param("userId") Integer userId, @Param("date") LocalDate date);

        // on role persons
        @Query(value = "SELECT owner_status FROM common_task_activities WHERE user_id = :userId AND activity_date = :date and draft = false", nativeQuery = true)
        List<String> findByUserIdAndActivityDateOnRole(@Param("userId") Integer userId, @Param("date") LocalDate date);

        @Query(value = "SELECT supervisor_approved FROM common_task_activities WHERE user_id = :userId AND activity_date = :date", nativeQuery = true)
        List<String> findByUserIdAndActivityDateSupervisorApproved(@Param("userId") Integer userId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT  cta.id, cta.prod_id FROM  common_task_activities cta  WHERE cta.user_id =:id  AND cta.activity_date = :date ", nativeQuery = true)
        List<Object[]> getProductDataWithIdAndDate(@Param("id") int id, @Param("date") LocalDate date);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities WHERE user_id = :userId AND activity_date = :date and prod_id =:prodId", nativeQuery = true)
        String findHoursByPersonIdAndActivityDateandproductid(@Param("userId") Integer userId,
                        @Param("date") LocalDate date, @Param("prodId") Integer prodId);

        /// particural daye

        @Query(value = "SELECT  cta.id , p.name ,p.id, cta.hours FROM  common_task_activities cta  JOIN products p ON cta.prod_id = p.id WHERE cta.user_id =:id  AND cta.activity_date = :date ", nativeQuery = true)
        List<Object[]> getUsersDataWithIdAndDatewithProd(@Param("id") int id, @Param("date") LocalDate date);

        @Query(value = "SELECT  cta.id , p.name ,p.id, cta.hours,u.name,cta.activity_date FROM  common_task_activities cta  JOIN products p ON cta.prod_id = p.id  join users u on cta.user_id=u.id where cta.user_id =:id AND cta.activity_date  between :fromdate and :todate  order by activity_date ", nativeQuery = true)
        List<Object[]> getUserDateRangePdf(@Param("id") int id, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate);

        @Query(value = "SELECT p.id, p.name, SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities cta JOIN products p ON cta.prod_id = p.id WHERE cta.user_id = :id AND cta.activity_date = :date AND cta.prod_id = :prod_id GROUP BY p.id, p.name ", nativeQuery = true)
        List<Object[]> getHoursbyUsersandProductidandDate(@Param("id") int id, @Param("date") LocalDate date,
                        @Param("prod_id") int prod_id);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities cta  WHERE cta.user_id = :id AND cta.activity_date = :date ", nativeQuery = true)
        List<Object[]> getHoursbyUsersandProductidandDatetotalHours(@Param("id") int id, @Param("date") LocalDate date);

        @Query(value = "SELECT p.id, p.name,u.name,cta.activity_date, SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours FROM common_task_activities cta JOIN products p ON cta.prod_id = p.id join users u on cta.user_id=u.id WHERE cta.user_id = :id AND cta.activity_date = :date AND cta.prod_id = :prod_id GROUP BY p.id, p.name ", nativeQuery = true)
        List<Object[]> getHoursbyUsersandProductidandDaterangepdftotal(@Param("id") int id,
                        @Param("date") LocalDate date,
                        @Param("prod_id") int prod_id);

        @Query(value = "SELECT p.id, p.name,u.name,cta.activity_date,cta.task,cta.hours,sup.name AS supervisor_name,cta.id FROM common_task_activities cta JOIN products p ON cta.prod_id = p.id join users u on cta.user_id=u.id LEFT JOIN "
                        +
                        "    users sup ON u.supervisor = sup.username WHERE cta.user_id = :id AND cta.activity_date = :date AND cta.prod_id = :prod_id ", nativeQuery = true)
        List<Object[]> getHoursbyUsersandProductidandDaterangepdf(@Param("id") int id, @Param("date") LocalDate date,
                        @Param("prod_id") int prod_id);

        // Owner Time List Show
        @Query(value = "SELECT \r\n" + //
                        "    ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, \r\n" + //
                        "    ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, \r\n" + //
                        "    p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id \r\n" + //
                        "FROM \r\n" + //
                        "    common_task_activities ta \r\n" + //
                        "JOIN \r\n" + //
                        "    users u ON ta.user_id = u.id \r\n" + //
                        "JOIN \r\n" + //
                        "    products p ON ta.prod_id = p.id \r\n" + //
                        "LEFT JOIN \r\n" + //
                        "    members m ON m.prod_id = ta.prod_id AND (m.assigned_by =:ownerId AND m.member = ta.user_id)\r\n"
                        + //
                        "WHERE \r\n" + //
                        "    ta.draft = false \r\n" + //
                        "    AND ta.prod_id in(:prod_id) \r\n" + //
                        "    AND ta.owner_status IN ('Supervisor Approved') \r\n" + //
                        "    AND ta.supervisor_approved = true \r\n" + //
                        "    AND ta.is_approved = false \r\n" + //
                        "    AND (m.member IS NULL OR (m.assigned_by =:ownerId AND m.member = ta.user_id));", nativeQuery = true)
        List<Object[]> getProductidmemberList(@Param("prod_id") List<Integer> prod_id, Integer ownerId);

        // Owner List Show By Date Range
        @Query(value = "SELECT DISTINCT p.id, p.name FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE ta.draft = false AND ta.prod_id IN (:prod_id) AND ta.owner_status IN ('Supervisor Approved') AND ta.supervisor_approved = true AND ta.is_approved = false AND ta.activity_date BETWEEN :fromdate AND :todate", nativeQuery = true)
        List<Object[]> getdProductidmemberListByDateRange(@Param("prod_id") List<Integer> prod_id,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate);

        @Query(value = "SELECT DISTINCT ta.user_id,u.name,u.profile_pic FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE ta.draft = false AND ta.prod_id =:prod_id AND ta.owner_status IN ('Supervisor Approved') AND ta.supervisor_approved = true AND ta.is_approved = false AND ta.activity_date BETWEEN :fromdate AND :todate", nativeQuery = true)
        List<Object[]> getdProductidmemberListByDateRangeandproductid(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, @Param("prod_id") Integer prod_id);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name, ta.final_approve, ta.branch, ta.owner_status,p.id,u.id FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE ta.draft = false AND ta.prod_id =:prod_id AND ta.owner_status IN ('Supervisor Approved') AND ta.supervisor_approved = true AND ta.is_approved = false AND ta.activity_date BETWEEN :fromdate AND :todate AND ta.user_id =:user_id", nativeQuery = true)
        List<Object[]> getdProductidmemberListByDateRangeandproductidanduserid(@Param("prod_id") Integer prod_id,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("user_id") Integer user_id);

        @Query(value = "SELECT * FROM common_task_activities WHERE user_id = :id AND draft = :b AND activity_date = :date", nativeQuery = true)
        List<CommonTimeSheetActivity> findByUserAndActivityDateAndDraftDetail(@Param("b") boolean b,
                        @Param("id") Integer id, @Param("date") LocalDate date);

        // ====================================Supervisor BAsed All Data
        // Query============================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
                        +
                        "u.name AS user_name, ta.task, p.name AS product_name, p.id AS product_id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON ta.user_id = u.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ta.supervisor = :supervisor " +
                        "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') " +
                        "AND ta.draft = false " +
                        "AND ta.supervisor_approved = false " +
                        "AND ta.supervisor_status IN ('Pending', 'Resubmit') " +
                        "ORDER BY ta.activity_date DESC", countQuery = "SELECT count(*) " +
                                        "FROM common_task_activities ta " +
                                        "JOIN users u ON ta.user_id = u.id " +
                                        "JOIN products p ON ta.prod_id = p.id " +
                                        "WHERE ta.supervisor = :supervisor " +
                                        "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') " +
                                        "AND ta.draft = false " +
                                        "AND ta.supervisor_approved = false " +
                                        "AND ta.supervisor_status IN ('Pending', 'Resubmit')", nativeQuery = true)
        Page<Object[]> findTaskDTOs(@Param("supervisor") String supervisor, Pageable pageable);

        // ====================================Supervisor date BAsed Data
        // Query============================

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "AND ta.activity_date >= :startDate "
                        + "AND ta.activity_date <= :endDate "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                                        + "AND ta.activity_date >= :startDate "
                                        + "AND ta.activity_date <= :endDate", nativeQuery = true)
        Page<Object[]> findTaskDTOsDate(@Param("supervisor") String supervisor,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND ta.prod_id = :prod_id "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND ta.prod_id = :prod_id "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval')", nativeQuery = true)
        Page<Object[]> findTaskDTOsProduct(@Param("supervisor") String supervisor,
                        @Param("prod_id") Integer prodId,
                        Pageable pageable);

        // ================================= based on user id get members activity
        // data===============================================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND ta.user_id = :member_id "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND ta.user_id = :member_id "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval')", nativeQuery = true)
        Page<Object[]> findTaskDTOsMember(@Param("supervisor") String supervisor,
                        @Param("member_id") int memberId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "AND ta.activity_date >= :startDate "
                        + "AND ta.activity_date <= :endDate "
                        + "AND ta.prod_id = :prod_id "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                                        + "AND ta.activity_date >= :startDate "
                                        + "AND ta.activity_date <= :endDate "
                                        + "AND ta.prod_id = :prod_id", nativeQuery = true)
        Page<Object[]> findTaskDTOsDateAndProduct(@Param("supervisor") String supervisor,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("prod_id") int prodId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "AND ta.activity_date >= :startDate "
                        + "AND ta.activity_date <= :endDate "
                        + "AND ta.user_id = :member_id "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                                        + "AND ta.activity_date >= :startDate "
                                        + "AND ta.activity_date <= :endDate "
                                        + "AND ta.user_id = :member_id", nativeQuery = true)
        Page<Object[]> findTaskDTOsDateAndMember(@Param("supervisor") String supervisor,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("member_id") int memberId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND ta.prod_id = :prod_id "
                        + "AND ta.user_id = :member_id "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND ta.prod_id = :prod_id "
                                        + "AND ta.user_id = :member_id "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval')", nativeQuery = true)
        Page<Object[]> findTaskDTOsProductAndMember(@Param("supervisor") String supervisor,
                        @Param("prod_id") int prodId,
                        @Param("member_id") int memberId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch, ta.is_approved, ta.supervisor_status, ta.user_id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.supervisor = :supervisor "
                        + "AND ta.draft = false "
                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                        + "AND ta.supervisor_approved = false "
                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                        + "AND ta.activity_date >= :startDate "
                        + "AND ta.activity_date <= :endDate "
                        + "AND ta.prod_id = :prod_id "
                        + "AND ta.user_id = :member_id "
                        + "ORDER BY ta.activity_date DESC", countQuery = "SELECT COUNT(*) "
                                        + "FROM common_task_activities ta "
                                        + "JOIN users u2 ON ta.user_id = u2.id "
                                        + "JOIN products p ON ta.prod_id = p.id "
                                        + "WHERE ta.supervisor = :supervisor "
                                        + "AND ta.draft = false "
                                        + "AND ta.supervisor_status IN ('Pending', 'Resubmit') "
                                        + "AND ta.supervisor_approved = false "
                                        + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
                                        + "AND ta.activity_date >= :startDate "
                                        + "AND ta.activity_date <= :endDate "
                                        + "AND ta.prod_id = :prod_id "
                                        + "AND ta.user_id = :member_id", nativeQuery = true)
        Page<Object[]> findTaskDTOsAll(@Param("supervisor") String supervisor,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate,
                        @Param("prod_id") Integer prodId,
                        @Param("member_id") Integer memberId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT user_id FROM common_task_activities", nativeQuery = true)
        List<Integer> findDistinctUserIds();

        @Modifying
        @Transactional
        @Query(value = "UPDATE common_task_activities c SET c.supervisor = :newSupervisorId WHERE c.user_id = :userId", nativeQuery = true)
        int updateSupervisorForActivities(@Param("userId") int userId,
                        @Param("newSupervisorId") String newSupervisorId);

        // =================================================Final Approval
        // apis=================================================================================================
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false "
                        + "ORDER BY ta.activity_date DESC", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(@Param("supervisorId") String userId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDate(
                        @Param("supervisorId") String userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false  AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProduct(
                        @Param("supervisorId") String userId, @Param("prod_id") Integer prodId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndMember(
                        @Param("supervisorId") String userId, @Param("member_id") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndStatus(
                        @Param("supervisorId") String userId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndProduct(
                        @Param("supervisorId") String userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate, @Param("prod_id") Integer prodId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // = + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHEREta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndStatus(
                        @Param("supervisorId") String userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.status=:status AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProductAndStatus(
                        @Param("supervisorId") String userId, @Param("prod_id") Integer prodId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.status=:status AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndMemberAndStatus(
                        @Param("supervisorId") String userId, @Param("member_id") int memberId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndMember(
                        @Param("supervisorId") String userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate, @Param("member_id") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.user_id=:member_id AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProductAndMember(
                        @Param("supervisorId") String userId, @Param("prod_id") Integer prodId,
                        @Param("member_id") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.prod_id=:prod_id AND ta.user_id=:member_id AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndFinalApproveallDetailAndAll(@Param("supervisorId") String userId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        @Param("prod_id") Integer prodId, @Param("member_id") int memberId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.prod_id=:prod_id AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndFinalApproveallDetailAnddpm(@Param("supervisorId") String userId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        @Param("prod_id") Integer prodId, @Param("member_id") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.prod_id=:prod_id AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndFinalApproveallDetailAnddps(@Param("supervisorId") String userId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        @Param("prod_id") Integer prodId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.prod_id=:prod_id AND ta.user_id=:member_id AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndFinalApproveallDetailAndpms(@Param("supervisorId") String userId,
                        @Param("prod_id") Integer prodId, @Param("member_id") int memberId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve, ta.branch "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ta.user_id in (select id from users  where final_approve =:supervisorId ) AND ta.draft = false AND (ta.final_approve IN ('TL Approved') AND ta.supervisor_approved = true OR ta.contract_status = 'finalApproval' AND ta.final_approve != 'Reject') AND ta.is_approved = false AND ta.activity_date >= :startDate AND ta.activity_date <= :endDate AND ta.user_id=:member_id AND ta.status=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndFinalApproveallDetailAnddms(@Param("supervisorId") String userId,
                        @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate,
                        @Param("member_id") Integer memberId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermision(@Param("userId") Integer userId,
                        @Param("date") LocalDate date, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND ta.supervisor_status = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermisionSuperviser(@Param("userId") Integer userId,
                        @Param("date") LocalDate date, @Param("status") String status, Pageable pageable);

        // without date
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.supervisor_status = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndpermisionSuperviser(@Param("userId") Integer userId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND ta.owner_status = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermisionOwnerStatus(@Param("userId") Integer userId,
                        @Param("date") LocalDate date, @Param("status") String status, Pageable pageable);

        // without date
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.owner_status = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndpermisionOwnerStatus(@Param("userId") Integer userId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND ta.final_approve = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityDatepermisionFinalApprover(@Param("userId") Integer userId,
                        @Param("date") LocalDate date, @Param("status") String status, Pageable pageable);

        // without date and status
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.final_approve = :status AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdpermisionFinalApprover(@Param("userId") Integer userId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId  AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivity(@Param("userId") Integer userId, Pageable pageable);
        // =================================================================END===================================================================================

        // final Approveer
        // @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description,
        // ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name,
        // ta.task, p.name, p.id, ta.final_approve, ta.branch "
        // + "FROM common_task_activities ta "
        // + "JOIN users u2 ON ta.user_id = u2.id "
        //// + "JOIN users u1 ON u2.final_approve = u1.id "
        // + "JOIN products p ON ta.prod_id = p.id "
        // + "WHERE ta.user_id in (select id from users where final_approve=:userId) AND
        // ta.draft = false AND ta.final_approve IN ('TL Approved') AND
        // ta.supervisor_approved = true AND ta.is_approved = false "
        // + "ORDER BY ta.activity_date DESC", nativeQuery = true)
        // Page<Object[]>
        // findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(@Param("userId")
        // String userId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve=:status AND ta.activity_date= :date AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndMemberAndStatus(
                        @Param("userId") String userId, @Param("date") LocalDate date,
                        @Param("member_id") Integer memberId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve=:status AND ta.prod_id=:prod_id AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndMemberAndStatus(
                        @Param("userId") String userId, @Param("prod_id") Integer prodId,
                        @Param("member_id") Integer memberId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve=:status AND ta.activity_date= :date AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProductAndStatus(
                        @Param("userId") String userId, @Param("date") LocalDate date, @Param("prod_id") Integer prodId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.prod_id=:prod_id AND ta.activity_date= :date AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProductAndMember(
                        @Param("userId") String userId, @Param("date") LocalDate date, @Param("prod_id") Integer prodId,
                        @Param("member_id") Integer memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve=:status AND ta.activity_date= :date AND ta.prod_id=:prod_id AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndAll(@Param("userId") String userId,
                        @Param("date") LocalDate date, @Param("prod_id") Integer prodId,
                        @Param("member_id") Integer memberId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.prod_id=:prod_id AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndMember(@Param("userId") String userId,
                        @Param("prod_id") Integer prodId, @Param("member_id") Integer memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.user_id=:member_id AND ta.activity_date= :date "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndMember(@Param("userId") String userId,
                        @Param("date") LocalDate date, @Param("member_id") Integer memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.user_id=:member_id AND ta.final_approve=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndMemberAndStatus(@Param("userId") String userId,
                        @Param("member_id") Integer memberId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.prod_id=:prod_id AND ta.final_approve=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndStatus(@Param("userId") String userId,
                        @Param("prod_id") Integer prodId, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.activity_date= :date AND ta.final_approve=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndStatus(@Param("userId") String userId,
                        @Param("date") LocalDate date, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.prod_id=:prod_id AND ta.activity_date= :date "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProduct(@Param("userId") String userId,
                        @Param("date") LocalDate date, @Param("prod_id") Integer prodId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve=:status "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndStatus(@Param("userId") String userId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.user_id=:member_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndMember(@Param("userId") String userId,
                        @Param("member_id") Integer memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.prod_id=:prod_id "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndProduct(@Param("userId") String userId,
                        @Param("prod_id") Integer prodId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        // + "JOIN users u1 ON u2.final_approve = u1.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId)  AND draft = false AND ta.final_approve IN ('Approved','Reject') AND ta.activity_date=:date "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndApprovedAndDate(@Param("userId") String userId,
                        @Param("date") LocalDate date, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u2.name, ta.task, p.name, p.id, ta.final_approve,ta.branch, fal.remarks "
                        + "FROM common_task_activities ta "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "JOIN final_approve_log fal ON ta.id = fal.task_activity_id "
                        + "WHERE ta.user_id in (select id from users where final_approve=:userId) AND draft = false AND ta.final_approve IN ('Approved','Reject') "
                        + "ORDER BY ta.activity_date DESC ", nativeQuery = true)
        Page<Object[]> findTaskDTOsByUserIdAndActivityAndFinalApproveallApproved(@Param("userId") String userId,
                        Pageable pageable);

        // distinict product name for Final based list
        @Query(value = "SELECT DISTINCT p.id, p.name AS product_name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ta.activity_date BETWEEN :fromdate AND :todate " +
                        "AND ta.user_id in (select id from users where final_approve=:supervisor) " +
                        "AND ta.draft = false " +
                        "AND ta.final_approve in('TL Approved')", nativeQuery = true)
        List<Object[]> findDistinctProductNamesforFinalapprove(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate,
                        @Param("supervisor") String supervisor);

        // distinict member name for Owner based list
        @Query(value = "SELECT DISTINCT u.id,u.name,u.profile_pic FROM common_task_activities ta JOIN users u ON ta.user_id = u.id JOIN products p ON ta.prod_id = p.id WHERE ta.draft = false AND ta.prod_id IN (:prod_id) AND ta.owner_status IN ('Supervisor Approved') AND ta.supervisor_approved = true AND ta.is_approved = false AND ta.activity_date BETWEEN :fromdate AND :todate", nativeQuery = true)
        List<Object[]> findDisnictProductAndMemberNamesforOwner(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, @Param("prod_id") List<Integer> prod_id);

        @Query(value = "SELECT DISTINCT ta.user_id,u.name,u.profile_pic from common_task_activities ta JOIN users u ON ta.user_id = u.id  WHERE  ta.user_id in (select id from users where final_approve=:supervisor)  AND ta.activity_date BETWEEN :fromdate AND :todate AND ta.draft = false AND ta.final_approve in('TL Approved')", nativeQuery = true)
        List<Object[]> findDisnictProductAndMemberNamesforFinalapprove(@Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate,
                        @Param("supervisor") String supervisor);

        // product members activity
        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND u.branch = :branch "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberList(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.activity_date BETWEEN :fromdate AND :todate "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndDate(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.prod_id= :prod "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndProduct(@Param("prod_id") List<Integer> prod_id,
                        @Param("prod") int prod, @Param("branch") String branch, @Param("ownerId") Integer ownerId,
                        Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.prod_id= :prod "
                        + "AND ta.activity_date BETWEEN :fromdate AND :todate "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndProductAndDate(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prod") int prodId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.user_id= :memberId "
                        + "AND ta.activity_date BETWEEN :fromdate AND :todate "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndMemberAndDate(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("memberId") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.user_id= :memberId "
                        + "AND ta.prod_id= :prod "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndMemberAndProduct(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch, @Param("prod") int prod,
                        @Param("memberId") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.user_id= :member_id "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAndMember(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch,
                        @Param("member_id") int memberId, Pageable pageable);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, "
                        + "ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, "
                        + "p.name, ta.final_approve, ta.branch, ta.owner_status, p.id, u.id "
                        + "FROM common_task_activities ta "
                        + "JOIN users u ON ta.user_id = u.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :ownerId AND m.member = ta.user_id) "
                        + "WHERE ta.draft = false "
                        + "AND ta.prod_id IN (:prod_id) "
                        + "AND ta.owner_status IN ('Supervisor Approved') "
                        + "AND ta.supervisor_approved = true "
                        + "AND ta.is_approved = false "
                        + "AND (m.member IS NULL OR (m.assigned_by = :ownerId AND m.member = ta.user_id))"
                        + "AND ta.user_id= :memberId "
                        + "AND ta.prod_id= :prod "
                        + "AND ta.activity_date BETWEEN :fromdate AND :todate "
                        + "AND u.branch = :branch "
                        + "ORDER BY ta.id DESC  ", nativeQuery = true)
        Page<Object[]> getProductIdMemberListAll(@Param("prod_id") List<Integer> prod_id,
                        @Param("ownerId") Integer ownerId, @Param("branch") String branch,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prod") int prod, @Param("memberId") int memberId, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.prod_id = :prod_id "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndProduct(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses,
                        @Param("prod_id") int prodId, Pageable pageable);
        // @Query(value = "SELECT ol.id, ol.status AS supervisor_approved,
        // ta.activity_date, ta.hours, ta.description, ta.status, "
        // + "ta.draft, ta.created_at , ta.updated_at, "
        // + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
        // + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
        // + "FROM owner_approve_log ol "
        // + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
        // + "JOIN users u1 ON ol.created_by = u1.id "
        // + "JOIN users u2 ON ta.user_id = u2.id "
        // + "JOIN products p ON ta.prod_id = p.id "
        // + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND
        // ta.owner_approved = true "
        // + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
        // + "FROM owner_approve_log ol_inner "
        // + "WHERE ol_inner.task_activity_id = ol.task_activity_id)"
        // + "AND u2.branch = :branch "
        // + "ORDER BY ol.id DESC ",
        // nativeQuery = true)
        //
        // List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy")
        // Integer createdBy, @Param("branch") String branch, @Param("statuses")
        // List<String> statuses, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.user_id = :user_id "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndMember(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses,
                        @Param("user_id") int userId, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.activity_date = :date "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDate(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses,
                        @Param("date") LocalDate date, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.activity_date = :date "
                        + "AND ta.prod_id = :prod_id "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDateAndProduct(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses,
                        @Param("date") LocalDate date, @Param("prod_id") int prodId, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.activity_date = :date "
                        + "AND ta.user_id = :user_id "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDateAndMember(@Param("createdBy") Integer createdBy,
                        @Param("statuses") List<String> statuses, @Param("date") LocalDate date,
                        @Param("user_id") int userId, Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id) "
                        + "AND ta.user_id = :user_id "
                        + "AND ta.prod_id = :prod_id "
                        + "AND ta.activity_date = :date "
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerAll(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses,
                        @Param("date") LocalDate date, @Param("prod_id") int prodId, @Param("user_id") int userId,
                        Pageable pageable);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                        + "ta.draft, ta.created_at , ta.updated_at, "
                        + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
                        + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
                        + "FROM owner_approve_log ol "
                        + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
                        + "JOIN users u1 ON ol.created_by = u1.id "
                        + "JOIN users u2 ON ta.user_id = u2.id  "
                        + "JOIN products p ON ta.prod_id = p.id "
                        + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
                        + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
                        + "FROM owner_approve_log ol_inner "
                        + "WHERE ol_inner.task_activity_id = ol.task_activity_id)"
                        + "AND u2.branch = :branch "
                        + "ORDER BY ol.id DESC  ", nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy") Integer createdBy,
                        @Param("branch") String branch, @Param("statuses") List<String> statuses, Pageable pageable);

        // mail service query
        @Query(value = "SELECT " +
                        "    u.name, " +
                        "    ta.activity_date, " +
                        "    SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(ta.hours, '%H:%i:%s')))) AS total_hours " +
                        "FROM " +
                        "    common_task_activities ta " +
                        "JOIN " +
                        "    users u ON ta.user_id = u.id " +
                        "WHERE " +
                        "    ta.supervisor = :supervisorid " +
                        "    AND activity_date = :activitydate " +
                        "    AND ta.draft = false " +
                        "GROUP BY " +
                        "    u.name, ta.activity_date ORDER BY " +
                        " activity_date desc", nativeQuery = true)
        List<Object[]> getSupervisorBasedUsersName(@Param("supervisorid") String supervisorid,
                        @Param("activitydate") LocalDate activitydate);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE supervisor = :supervisorId AND DATE(created_at) = :activitydate AND draft = false", nativeQuery = true)
        int usertimesheetcheck(String supervisorId, LocalDate activitydate);

        // mobile
        @Query(value = "SELECT DISTINCT ta.prod_id, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :user_id AND ta.draft = :draft ORDER BY ta.id DESC ", nativeQuery = true)
        Page<Object[]> findProdIdByUserIdAndDraftIsTrue(@Param("user_id") Integer user_id, Pageable pageable,
                        boolean draft);

        @Query(value = "SELECT DISTINCT ta.prod_id, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :user_id AND ta.draft = :draft AND ta.activity_date = :date ORDER BY ta.id DESC ", nativeQuery = true)
        Page<Object[]> findProdIdByUserIdAndDraftIsTrue(@Param("user_id") Integer user_id, Pageable pageable,
                        @Param("date") LocalDate date, boolean draft);

        @Query(value = "SELECT DISTINCT ta.prod_id, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :userid AND ta.draft = :draft AND ta.activity_date = :date AND p.name LIKE CONCAT ('%', :filter,'%') ORDER BY ta.id DESC ", nativeQuery = true)
        Page<Object[]> findProdIdByUserIdAndDraftIsTrue(@Param("userid") Integer user_id,
                        @Param("date") LocalDate date, @Param("filter") String filter, Pageable pageable,
                        boolean draft);

        @Query(value = "SELECT DISTINCT ta.prod_id, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :user_id AND ta.draft = :draft AND p.name LIKE %:filter% ORDER BY ta.id DESC ", nativeQuery = true)
        Page<Object[]> findProdIdByUserIdAndDraftIsTrue(@Param("user_id") Integer user_id, Pageable pageable,
                        String filter, boolean draft);

        @Query(value = "SELECT ta.id, ta.task, ta.hours, ta.description, ta.supervisor_status,ta.activity_date " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :userId AND ta.draft = :draft AND ta.activity_date = :date AND ta.prod_id = :prodId ", nativeQuery = true)
        Page<Object[]> getTaskByProdIdAndUserIdAndDate(@Param(value = "userId") int userId, Pageable pageable,
                        @Param(value = "date") LocalDate date, @Param(value = "prodId") int prodId, boolean draft);

        @Query(value = "SELECT ta.id, ta.task, ta.hours, ta.description, ta.supervisor_status,ta.activity_date " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :userId AND ta.draft = :draft AND ta.prod_id = :prodId ", nativeQuery = true)
        Page<Object[]> getTaskByProdIdAndUserIdAndDate(@Param(value = "userId") int userId, Pageable pageable,
                        @Param(value = "prodId") int prodId, boolean draft);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.activity_date BETWEEN :fromdate AND :todate " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataByDate(@Param("supervisor") String supervisor, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.activity_date BETWEEN :fromdate AND :todate " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "and c.prod_id = :prodId " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataByDatewithProductId(@Param("supervisor") String supervisor,
                        @Param("fromdate") LocalDate fromdate, @Param("todate") LocalDate todate,
                        @Param("prodId") int prodId, Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataAll(@Param("supervisor") String supervisor, Pageable pageable);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.user_id = :userId " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE c.supervisor = :supervisor " +
                                        "AND c.user_id = :userId " +
                                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                                        "AND c.supervisor_approved = false " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorAndUser(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, Pageable pageable);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.user_id = :userId " +
                        "And c.activity_date Between :fromdate And :todate " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE c.supervisor = :supervisor " +
                                        "AND c.user_id = :userId " +
                                        "And c.activity_date Between :fromdate And :todate " +
                                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                                        "AND c.supervisor_approved = false " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorAndUserWithDate(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, Pageable pageable);

        // product id based

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.user_id = :userId " +
                        "AND c.prod_id = :prodId " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE c.supervisor = :supervisor " +
                                        "AND c.user_id = :userId " +
                                        "AND c.prod_id = :prodId " +
                                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                                        "AND c.supervisor_approved = false " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorAndUserWithProdId(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("prodId") int prodId, Pageable pageable);

        // date and prodid

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.user_id = :userId " +
                        "AND c.prod_id = :prodId " +
                        "And c.activity_date Between :fromdate And :todate " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE c.supervisor = :supervisor " +
                                        "AND c.user_id = :userId " +
                                        "AND c.prod_id = :prodId " +
                                        "And c.activity_date Between :fromdate And :todate " +
                                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                                        "AND c.supervisor_approved = false " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorAndUserWithDateandProdId(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, @Param("prodId") int prodId, Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name, u2.id " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND al.status = :status", nativeQuery = true)
        Page<Object[]> findTasksByApprovedSupervisorByStatus(@Param("supervisor") int supervisorId,
                        @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "al.status AS supervisor_approved " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities c ON al.task_activity_id = c.id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND al.status = :status " +
                        "AND c.user_id = :userId", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorByUserAndStatus(@Param("supervisor") int supervisorId,
                        @Param("userId") int userId,
                        @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "al.status AS supervisor_approved " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities c ON al.task_activity_id = c.id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND al.status = :status " +
                        "AND c.user_id = :userId And c.prod_id =:prodId ", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorByUserAndStatusandProdId(@Param("supervisor") int supervisorId,
                        @Param("userId") int userId,
                        @Param("status") String status,
                        @Param("prodId") int prodId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "al.status AS supervisor_approved " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities c ON al.task_activity_id = c.id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND al.status = :status " +
                        "And c.activity_date Between :fromdate And :todate " +
                        "AND c.user_id = :userId", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorByUserAndStatusandDate(@Param("supervisor") int supervisorId,
                        @Param("userId") int userId,
                        @Param("status") String status,
                        @Param("fromdate") LocalDate fromDate,
                        @Param("todate") LocalDate todate,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "al.status AS supervisor_approved " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities c ON al.task_activity_id = c.id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND al.status = :status " +
                        "And c.activity_date Between :fromdate And :todate " +
                        "AND c.user_id = :userId And c.prod_id =:prodId ", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorByUserAndStatusandDateAndProdId(@Param("supervisor") int supervisorId,
                        @Param("userId") int userId,
                        @Param("status") String status,
                        @Param("fromdate") LocalDate fromDate,
                        @Param("todate") LocalDate todate,
                        @Param("prodId") int prodId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name, u2.id " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN ( " +
                        "    SELECT task_activity_id, MAX(created_at) AS max_created_at " +
                        "    FROM common_task_activity_log " +
                        "    GROUP BY task_activity_id " +
                        ") al_max ON al.task_activity_id = al_max.task_activity_id AND al.created_at = al_max.max_created_at "
                        +
                        "WHERE al.created_by = :supervisor " +
                        "AND ta.activity_date BETWEEN :fromdate AND :todate " +
                        "AND al.status = :status", nativeQuery = true)
        Page<Object[]> findTasksByApprovedSupervisorBasedonDate(@Param("supervisor") int supervisorId,
                        @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate,
                        @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "al.status AS supervisor_approved " +
                        "FROM common_task_activity_log al  " +
                        "JOIN common_task_activities c ON al.task_activity_id = c.id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE al.created_by=:supervisor  and c.activity_date = :activitdate " +
                        "AND al.status = :status and c.user_id=:userId ", nativeQuery = true)
        Page<Object[]> findTasksBySupervisorByUserandStatusWithDate(@Param("supervisor") int supervisorId,
                        @Param("userId") int userId, @Param("status") String status,
                        @Param("activitdate") LocalDate activityDate, Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name,u2.id " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN (SELECT al2.task_activity_id, MAX(al2.created_at) as max_created_at " +
                        "      FROM common_task_activity_log al2 " +
                        "      GROUP BY al2.task_activity_id) sub " +
                        "ON al.task_activity_id = sub.task_activity_id AND al.created_at = sub.max_created_at " +
                        "WHERE al.created_by=:supervisor " +
                        "AND ta.user_id = :userId " +
                        "AND al.status = :status ", nativeQuery = true)
        Page<Object[]> findByUserNameByUserandStatus(@Param("supervisor") int supapproveId, @Param("userId") int userId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name,u2.id " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "WHERE al.created_by=:supervisor " +
                        "AND ta.prod_id = :prodId " +
                        "AND ta.supervisor_status = :status ", nativeQuery = true)
        Page<Object[]> findByProductNameByStatus(@Param("supervisor") int supapproveId, @Param("prodId") int prodId,
                        @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name,u2.id " +
                        "FROM common_task_activity_log al " +
                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "WHERE al.created_by=:supervisor " +
                        "AND ta.user_id = :userId " +
                        "AND ta.prod_id = :prodId " +
                        "AND al.status = :status ", nativeQuery = true)
        Page<Object[]> findByUserIdStatusProductId(@Param("supervisor") int supapproveId, @Param("userId") int userId,
                        @Param("prodId") int prodId,
                        @Param("status") String status, Pageable pageable);

        // Contract
        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.activity_date = :activityDate " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true And c.user_id = :userId " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersByDateanduseridteForPending(@Param("finalApprover") String finalApprover,
                        @Param("activityDate") LocalDate date,
                        Pageable pageable, List<String> status, int userId);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true And c.user_id = :userId " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersByanduseridteForPending(@Param("finalApprover") String finalApprover,
                        Pageable pageable, List<String> status, int userId);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.activity_date = :activityDate " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true And c.user_id = :userId And c.prod_id =:prodid " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersByDateanduseridandProdIDForPending(@Param("finalApprover") String finalApprover,
                        @Param("activityDate") LocalDate date,
                        Pageable pageable, List<String> status, int userId, int prodid);

        ///
        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.activity_date = :activityDate " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataByDateForApproval(@Param("finalApprover") String finalApprover,
                        @Param("activityDate") LocalDate date,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.activity_date = :activityDate " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) And prod_id =:prodId " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataByDateForApprovalbyproductid(@Param("finalApprover") String finalApprover,
                        @Param("activityDate") LocalDate date,
                        Pageable pageable, List<String> status, int prodId);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) And prod_id =:prodId " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataByForApprovalbyproductid(@Param("finalApprover") String finalApprover,
                        Pageable pageable, List<String> status, int prodId);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) And user_id =:userId And prod_id =:prodId " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersByuseridandProdIDForPending(@Param("finalApprover") String finalApprover,
                        Pageable pageable, List<String> status, int userId, int prodId);

        @Query(value = "SELECT DISTINCT u.name,u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND u.role_type = 'Contract' " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false ", nativeQuery = true)
        Page<Object[]> getusersDataAllForApproval(@Param("finalApprover") String finalApprover,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.user_id = :userId " +
                        "And c.activity_date = :activityDate " +
                        "AND c.supervisor_status = 'Approved'" +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE u.final_approve = :finalApprover " +
                                        "AND c.user_id = :userId " +
                                        "And c.activity_date = :activityDate " +
                                        "AND c.supervisor_status = 'Approved'" +
                                        "AND c.final_approve IN (:status) " +
                                        "AND c.supervisor_approved = true " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksByFinalApproverAndUserWithDate(@Param("finalApprover") String finalApprover,
                        @Param("userId") int userId, @Param("activityDate") LocalDate activityDate,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.user_id = :userId " +
                        "AND c.supervisor_status = 'Approved'" +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE u.final_approve = :finalApprover " +
                                        "AND c.user_id = :userId " +
                                        "AND c.supervisor_status = 'Approved' " +
                                        "AND c.final_approve IN (:status) " +
                                        "AND c.supervisor_approved = true " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTasksByFinalApproverAndUser(@Param("finalApprover") String finalApprover,
                        @Param("userId") int userId, Pageable pageable, List<String> status);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE supervisor = :supervisorId AND draft = false", nativeQuery = true)
        Long countBySupervisorIdAndStatusAll(@Param(value = "supervisorId") String supervisorId);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE supervisor = :supervisorId AND supervisor_status = :supervisorStatus AND draft = false", nativeQuery = true)
        Long countBySupervisorIdAndStatus(@Param(value = "supervisorId") String supervisorId,
                        @Param(value = "supervisorStatus") String supervisorStatus);

        @Query(value = "SELECT COUNT(*) " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON u.id = ta.user_id " +
                        "WHERE ta.supervisor = :supervisorId AND ta.supervisor_status = 'Approved' AND ta.final_approve IN ('TL Approved', 'Approved', 'Reject') AND u.role_type = 'Contract' AND ta.draft = false "
                        +
                        "GROUP BY ta.prod_id", nativeQuery = true)
        Long countBySupervisorIdAndContractMemberAndStatusAll(@Param(value = "supervisorId") String supervisorId);

        @Query(value = "SELECT COUNT(*) " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON u.id = ta.user_id " +
                        "WHERE ta.supervisor = :supervisorId AND ta.supervisor_status = 'Approved' AND ta.final_approve = :final_approve AND u.role_type = 'Contract' AND ta.draft = false "
                        +
                        "GROUP BY ta.prod_id", nativeQuery = true)
        Long countBySupervisorIdAndContractMemberAndStatus(@Param(value = "supervisorId") String supervisorId,
                        @Param(value = "final_approve") String final_approve);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        // "AND LOWER(p.name) LIKE CONCAT('%', LOWER(:productName), '%') ",
                        "And c.prod_id = :productId ", nativeQuery = true)
        Page<Object[]> findByProductName(@Param("supervisor") String supervisorId, @Param("productId") int productName,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "AND c.user_id = :userId ", nativeQuery = true)
        Page<Object[]> findByUserNameByUser(@Param("supervisor") String supervisorId, @Param("userId") int userId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "WHERE c.supervisor = :supervisor " +
                        " AND c.prod_id =:prodId " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "AND c.user_id = :userId ", nativeQuery = true)
        Page<Object[]> findByUserNameByUserAndProductFilter(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("prodId") int productId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "AND c.user_id = :userId  and c.activity_date Between :fromdate And :todate ", nativeQuery = true)
        Page<Object[]> findByUserNameByUserandProduct(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM common_task_activities c " +
                        "JOIN users u ON u.id = c.user_id " +
                        "WHERE c.supervisor = :supervisor " +
                        "AND c.supervisor_status IN ('Pending', 'Resubmit') " +
                        "AND c.supervisor_approved = false " +
                        "AND c.draft = false " +
                        "AND c.user_id = :userId  and c.activity_date Between :fromdate And :todate  and c.prod_id = :productId ", nativeQuery = true)
        Page<Object[]> findByUserNameByUseranddatewithProduct(@Param("supervisor") String supervisorId,
                        @Param("userId") int userId, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate, @Param("productId") int productId, Pageable pageable);

        @Query(value = "select count(*) from common_task_activities  where  user_id = :id and activity_date = :activityDate and draft = false", nativeQuery = true)
        Long findYesterdayTimesheetRecord(int id, LocalDate activityDate);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND draft = false", nativeQuery = true)
        Long countByUserIdAndStatusAll(@Param(value = "userId") Integer userId);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = :supervisorStatus AND draft = false", nativeQuery = true)
        Long countByUserIdAndStatus(@Param(value = "userId") Integer userId,
                        @Param(value = "supervisorStatus") String supervisorStatus);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = 'Approved' AND owner_status IN ('Supervisor Approved', 'Approved', 'Reject') AND draft = false", nativeQuery = true)
        Long countByUserIdAndOnroleAndStatusAll(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = 'Approved' AND owner_status = :owner_status AND draft = false", nativeQuery = true)
        Long countByUserIdAndOnroleAndStatus(@Param(value = "userId") Integer userId,
                        @Param(value = "owner_status") String owner_status);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = 'Approved' AND final_approve IN ('TL Approved', 'Approved', 'Reject') AND draft = false", nativeQuery = true)
        Long countByUserIdAndContractMemberAndStatusAll(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = 'Approved' AND final_approve = :final_approve AND draft = false", nativeQuery = true)
        Long countByUserIdAndContractMemberAndStatus(@Param(value = "userId") Integer userId,
                        @Param(value = "final_approve") String final_approve);

        @Query(value = "SELECT ta.hours, ta.description, ta.task, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :authUserId AND ta.draft = false AND activity_date = :date", nativeQuery = true)
        List<Object[]> findMyTimesheetData(@Param("authUserId") int authUserId, @Param("date") LocalDate date);

        @Query(value = "SELECT ta.hours, ta.description, ta.task, p.name " +
                        "FROM common_task_activities ta " +
                        "JOIN products p ON p.id = ta.prod_id " +
                        "WHERE ta.user_id = :authUserId AND ta.prod_id = :prod_id AND ta.draft = false AND activity_date = :date", nativeQuery = true)
        List<Object[]> findMyTimesheetDataAndProduct(@Param("authUserId") int authUserId, @Param("date") LocalDate date,
                        @Param(value = "prod_id") int prod_id);

        // ON Role Person TimeSheet without search
        @Query(value = "SELECT u.name AS userName, ta.activity_date, u1.name AS supervisorName, ta.supervisor_status, cal.remarks AS calRemarks, ta.owner_status, oal.remarks AS oalRemarks"
                        +
                        " FROM common_task_activities ta " +
                        " JOIN users u ON u.id = ta.user_id " +
                        " JOIN users u1 ON u1.username = ta.supervisor " +
                        " LEFT JOIN common_task_activity_log cal ON cal.task_activity_id = ta.id " +
                        " LEFT JOIN owner_approve_log oal ON oal.task_activity_id = ta.id " +
                        " WHERE ta.draft = false AND u.role_type = 'On ROle'", nativeQuery = true)
        Page<Object[]> getOverallTimeSheetData(Pageable pageable);

        // Contract Person TimeSheet without search
        @Query(value = "SELECT u.name AS userName, ta.activity_date, u1.name AS supervisorName, ta.supervisor_status, cal.remarks AS calRemarks, ta.final_approve, fal.remarks AS falRemarks"
                        +
                        " FROM common_task_activities ta " +
                        " JOIN users u ON u.id = ta.user_id " +
                        " JOIN users u1 ON u1.username = ta.supervisor " +
                        " LEFT JOIN common_task_activity_log cal ON cal.task_activity_id = ta.id " +
                        " LEFT JOIN final_approve_log fal ON fal.task_activity_id = ta.id " +
                        " WHERE ta.draft = false AND u.role_type = 'Contract'", nativeQuery = true)
        Page<Object[]> getOverallContractPersonTimeSheetData(Pageable pageable);

        // ON Role Person TimeSheet with search
        @Query(value = "SELECT u.name AS userName, ta.activity_date, u1.name AS supervisorName, ta.supervisor_status, cal.remarks AS calRemarks, ta.owner_status, oal.remarks AS oalRemarks"
                        +
                        " FROM common_task_activities ta " +
                        " JOIN users u ON u.id = ta.user_id " +
                        " JOIN users u1 ON u1.username = ta.supervisor " +
                        " LEFT JOIN common_task_activity_log cal ON cal.task_activity_id = ta.id " +
                        " LEFT JOIN owner_approve_log oal ON oal.task_activity_id = ta.id " +
                        " WHERE ta.draft = false AND u.role_type = 'On Role' AND (u.name REGEXP :value " +
                        " OR u1.name REGEXP :value " +
                        " OR ta.supervisor_status REGEXP :value " +
                        " OR ta.owner_status  REGEXP :value) ", nativeQuery = true)
        Page<Object[]> getOverallTimeSheetDataAndSearch(String value, Pageable pageable);

        // Contract Person TimeSheet with search
        @Query(value = "SELECT u.name AS userName, ta.activity_date, u1.name AS supervisorName, ta.supervisor_status, cal.remarks AS calRemarks, ta.final_approve, fal.remarks AS falRemarks"
                        +
                        " FROM common_task_activities ta " +
                        " JOIN users u ON u.id = ta.user_id " +
                        " JOIN users u1 ON u1.username = ta.supervisor " +
                        " LEFT JOIN common_task_activity_log cal ON cal.task_activity_id = ta.id " +
                        " LEFT JOIN final_approve_log fal ON fal.task_activity_id = ta.id " +
                        " WHERE ta.draft = false AND u.role_type = 'Contract' AND (u.name REGEXP :value " +
                        " OR u1.name REGEXP :value " +
                        " OR ta.supervisor_status REGEXP :value " +
                        " OR ta.final_approve  REGEXP :value) ", nativeQuery = true)
        Page<Object[]> getOverallContractPersonTimeSheetDataAndSearch(String value, Pageable pageable);

        @Query(value = "SELECT COUNT(DISTINCT DATE(activity_date)) FROM common_task_activities WHERE user_id = :userId AND MONTH(activity_date) = :month AND YEAR(activity_date) = :year", nativeQuery = true)
        Long countDistinctDaysInMonth(@Param("userId") Integer userId, @Param("month") int month,
                        @Param("year") int year);

        @Query(value = "SELECT DISTINCT  u.name,u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedData(@Param("authid") int authId, @Param("branch") String branch,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT  u.name,u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false and ta.activity_date =:date\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDatawithDate(@Param("authid") int authId, @Param("branch") String branch,
                        @Param("date") LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT u.name AS task_user_name, " +
                        "p2.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.final_approve, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.owner_status, " +
                        "ta.supervisor_status " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON ta.user_id = u.id " +
                        "JOIN products p2 ON ta.prod_id = p2.id " +
                        "JOIN ( " +
                        "    SELECT id " +
                        "    FROM products " +
                        "    WHERE FIND_IN_SET(:authId, tech_owner) > 0 OR FIND_IN_SET(:authId, prod_owner) > 0 " +
                        ") p ON ta.prod_id = p.id " +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authId AND m.member = ta.user_id) "
                        +
                        "WHERE ta.draft = false " +
                        "AND ta.owner_status IN ('Supervisor Approved') " +
                        "AND ta.supervisor_approved = true " +
                        "AND ta.is_approved = false " +
                        "AND u.branch = :branch " +
                        "AND (m.member IS NULL OR (m.assigned_by = :authId AND m.member = ta.user_id)) " +
                        "AND ta.user_id = :userId " + // Add condition for user_id
                        "ORDER BY ta.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByUserId(@Param("authId") int authId, @Param("branch") String branch,
                        @Param("userId") int userId, Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false\n" +
                        "AND ta.user_id = :userId\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithFilterUserId(@Param("authid") int authId, String branch,
                        @Param("userId") int userId, Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false\n" +
                        "AND ta.user_id = :userId\n" +
                        "AND ta.prod_id = :prodId\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByUserIdAndProductId(@Param("authid") int authId, String branch, int prodId,
                        int userId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        // "AND ta.is_approved = false\n" +
                        "AND ta.is_approved = false and ta.activity_date =:date\n" +
                        "AND ta.user_id = :userId\n" +
                        "AND ta.prod_id = :prodId\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByUserIdAndProductIdAndDate(@Param("authid") int authId, String branch,
                        int prodId, int userId, @Param("date") LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false\n" +
                        "AND ta.prod_id = :prodId\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByProductId(@Param("authid") int authId, String branch, int prodId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT  u2.name AS user_name,u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:status) " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithStatus(@Param("authId") int authId,
                        @Param("branch") String branch,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT DISTINCT u2.name AS user_name, u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:status) " +
                        "  AND ta.user_id = :userId " +
                        "  AND ta.activity_date = :date " +
                        "  AND ta.owner_approved = true " +
                        "  AND ta.prod_id = :prodId " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "ORDER BY u2.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithStatusWithDAteAndFilter(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("userId") int userId,
                        @Param("date") LocalDate date,
                        @Param("prodId") int prodId,
                        @Param("status") List<String> status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT  u2.name AS user_name ,u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:status) " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "AND ta.activity_date = :activityDate " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithStatusWithDate(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("activityDate") LocalDate activityDate,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT DISTINCT  u2.name AS user_name ,u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:status) " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "AND ta.user_id = :userid " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithStatusWithuserId(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("userid") int userid,
                        Pageable pageable, List<String> status);

        @Query(value = "SELECT DISTINCT  u2.name AS user_name ,u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:status) " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "AND ta.prod_id = :prodid " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithStatusWithProductId(@Param("authId") int authId,
                        @Param("branch") String branch,
                        Pageable pageable, List<String> status, int prodid);

        @Query(value = "SELECT u2.name AS task_user_name, " +
                        "p.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.owner_status, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.final_approve, " +
                        "ta.supervisor_status " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN ('Approved','Reject') " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "  AND ta.user_id = :userId " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getownerBasedDataByUserIdWithStatus(@Param("authId") int authId, String branch,
                        @Param("userId") int userId, Pageable pageable);

        @Query(value = "SELECT DISTINCT  u2.name AS user_name ,u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status in (:status) " +
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getownerBasedDataByUserIdWithStatuswithFilter(int authId, String branch, List<String> status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = ta.user_id)\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.activity_date = :date\n" +
                        "AND ta.is_approved = false\n" +
                        // "AND ta.user_id = :userId\n" +
                        "AND ta.prod_id = :prodId\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = ta.user_id))\n" +
                        "ORDER BY u.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByUserIdAndProductIdDate(@Param("authid") int authId, String branch,
                        @Param("date") LocalDate date, int prodId,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT  u.name,u.id\n" +
                        "FROM common_task_activities ta\n" +
                        "JOIN users u ON ta.user_id = u.id\n" +
                        "JOIN (\n" +
                        "    SELECT id\n" +
                        "    FROM products\n" +
                        "    WHERE FIND_IN_SET(:authid, tech_owner) > 0 OR FIND_IN_SET(:authid, prod_owner) > 0\n" +
                        ") p ON ta.prod_id = p.id\n" +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authid AND m.member = :userId )\n"
                        +
                        "WHERE ta.draft = false\n" +
                        "AND ta.owner_status IN ('Supervisor Approved')\n" +
                        "AND ta.user_id = :userId\n" +
                        "AND ta.activity_date = :date\n" +
                        "AND ta.supervisor_approved = true\n" +
                        "AND ta.is_approved = false\n" +
                        "AND u.branch = :branch " + // Added space before AND
                        "AND (m.member IS NULL OR (m.assigned_by = :authid AND m.member = :userId ))\n" +
                        "ORDER BY u.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataUserIdAndDate(@Param("authid") int authId, @Param("branch") String branch,
                        @Param("userId") int userId, @Param("date") LocalDate date,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u2.name AS user_name, u2.id AS user_id " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status in (:status) " +
                        "  AND ta.user_id = :userId " +
                        "  AND ta.activity_date = :date " +
                        "  AND ta.owner_approved = true " +
                        // " AND ta.prod_id = :prodId " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "ORDER BY u2.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataWithUserIdStatusWithDAteAndFilter(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("userId") int userId,
                        @Param("date") LocalDate date,
                        // @Param("prodId") int prodId,
                        @Param("status") List<String> status,
                        Pageable pageable);

        @Query(value = "SELECT DISTINCT u.name, u.id " +
                        "FROM users u " +
                        "WHERE u.supervisor = :supervisorId  And u.status = true and u.is_deleted = false " +
                        "AND u.id NOT IN ( " +
                        "    SELECT ta.user_id " +
                        "    FROM common_task_activities ta " +
                        "    WHERE activity_date = :activitydate " +
                        "    AND ta.draft = false " +
                        ")", nativeQuery = true)
        List<Object[]> notEnteredTimeSheet(@Param("supervisorId") String supervisorId,
                        @Param("activitydate") LocalDate yesterday);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours,activity_date "
                        +
                        "FROM common_task_activities " +
                        "WHERE user_id =:userId " +
                        "AND activity_date =:activityDate " +
                        "AND draft = false " +
                        "GROUP BY activity_date", nativeQuery = true)
        List<Object[]> getWeeklyBasedTimeline(@Param("userId") int userId,
                        @Param("activityDate") LocalDate currentDate);

        @Query(value = "SELECT SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours " +
                        "FROM common_task_activities " +
                        "WHERE user_id =:userId " +
                        "AND activity_date =:activityDate " +
                        "AND draft = false ", nativeQuery = true)
        List<Object[]> getTimeTotalHours(@Param("userId") int userId, @Param("activityDate") LocalDate currentDate);

        @Query(value = "SELECT activity_date, SEC_TO_TIME(SUM(TIME_TO_SEC(TIME_FORMAT(hours, '%H:%i')))) AS total_hours "
                        +
                        "FROM common_task_activities " +
                        "WHERE user_id = :userId " +
                        "AND activity_date BETWEEN :startDate AND :endDate " +
                        "AND draft = false " +
                        "GROUP BY activity_date", nativeQuery = true)
        List<Object[]> getWeeklyBasedTimeline(@Param("userId") int userId,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND supervisor_status = :supervisorStatus AND draft = false and activity_date =:date", nativeQuery = true)
        Long countByUserIdAndStatusandDate(@Param(value = "userId") Integer userId,
                        @Param(value = "supervisorStatus") String supervisorStatus,
                        @Param(value = "date") LocalDate date);

        @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, ta.task, p.name,p.id, ta.final_approve,ta.branch,ta.is_approved,ta.supervisor_status,ta.owner_status FROM common_task_activities ta JOIN users u ON ta.user_id = u.id  JOIN products p ON ta.prod_id = p.id WHERE ta.id = :id AND draft = false ORDER BY ta.activity_date DESC", nativeQuery = true)
        List<Object[]> findTaskDTOsByUserIdAndActivityRedirect(@Param("id") Integer id);

        @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN 1 ELSE 0 END FROM common_task_activities WHERE user_id = :id AND activity_date = :date AND draft = false", nativeQuery = true)
        Long enterCount(int id, LocalDate date);

        @Query(value = "SELECT COUNT(*) FROM common_task_activities WHERE user_id = :userId AND activity_date = :date AND draft = false", nativeQuery = true)
        Long countEntriesOnDate(@Param("userId") int userId, @Param("date") LocalDate date);

        @Query(value = "SELECT cta.id, cta.task, cta.hours, cta.description, ctal.status, ctal.remarks, p.name " +
                        "FROM common_task_activities cta " +
                        "JOIN common_task_activity_log ctal ON cta.id = ctal.task_activity_id " +
                        "JOIN products p ON cta.prod_id = p.id " +
                        "WHERE cta.user_id = :userId AND cta.activity_date = :date AND cta.draft = false", nativeQuery = true)
        List<Object[]> findActivityDetailsByUserIdAndActivityDate(@Param("userId") int userId,
                        @Param("date") LocalDate date);

        @Query(value = "SELECT DISTINCT u.id AS userId, u.name AS userName " +
                        "FROM common_task_activities cta " +
                        "JOIN users u ON cta.user_id = u.id " +
                        "WHERE cta.supervisor = :supervisor", nativeQuery = true)
        List<Object[]> getMyTeamReportDetailBySupervisorId(@Param("supervisor") String supervisor);

        @Query(value = "SELECT DISTINCT u.id AS userId, u.name AS userName " +
                        "FROM common_task_activities cta " +
                        "JOIN users u ON cta.user_id = u.id " +
                        "WHERE cta.supervisor = :supervisor " +
                        "AND  cta.activity_date >= :fromdate " +
                        "AND  cta.activity_date <= :todate", nativeQuery = true)
        List<Object[]> findDistinctUserNamesBySupervisorAndDateRange(@Param("supervisor") String supervisor,
                        @Param("fromdate") LocalDate fromDate, @Param("todate") LocalDate toDate);

        @Query(value = "SELECT DISTINCT u.id AS userId, u.name AS userName " +
                        "FROM common_task_activities cta " +
                        "JOIN users u ON cta.user_id = u.id " +
                        "WHERE cta.supervisor = :supervisor " +
                        "AND cta.activity_date >= :fromdate " +
                        "AND cta.activity_date <= :todate " +
                        "AND cta.user_id = :userId", nativeQuery = true)
        List<Object[]> findDistinctUserNamesBySupervisorDateRangeAndUserId(@Param("supervisor") String supervisor,
                        @Param("fromdate") LocalDate fromDate,
                        @Param("todate") LocalDate toDate, @Param("userId") int userId);

        @Query(value = "SELECT DISTINCT u.id AS userId, u.name AS userName " +
                        "FROM common_task_activities cta " +
                        "JOIN users u ON cta.user_id = u.id " +
                        "WHERE cta.user_id = :userId", nativeQuery = true)
        List<Object[]> findDistinctUserNamesByUserId(@Param("userId") int userId);

        @Query(value = "SELECT cta.id, cta.task, cta.hours, cta.description, ctal.status, ctal.remarks, p.name, cta.activity_date "
                        +
                        "FROM common_task_activities cta " +
                        "JOIN common_task_activity_log ctal ON cta.id = ctal.task_activity_id " +
                        " JOIN products p ON cta.prod_id = p.id " +
                        "WHERE cta.user_id = :userId " +
                        " AND cta.supervisor = :supervisor " +
                        "AND cta.draft = false;", nativeQuery = true)
        List<Object[]> getMyteamSingleMemberReportBySupervisorId(@Param("supervisor") String supervisorName,
                        @Param("userId") int userId);

        @Query(value = "SELECT cta.id, cta.task, cta.hours, cta.description, ctal.status, ctal.remarks, p.name , cta.activity_date "
                        +
                        "FROM common_task_activities cta " +
                        "JOIN common_task_activity_log ctal ON cta.id = ctal.task_activity_id " +
                        "JOIN products p ON cta.prod_id = p.id " +
                        "WHERE cta.user_id = :userId " +
                        "AND cta.activity_date BETWEEN :fromDate AND :toDate " +
                        "AND cta.supervisor = :supervisorName " +
                        " AND cta.draft = false;", nativeQuery = true)
        List<Object[]> getMyteamSingleMemberReportBySupervisorIdByDateRange(@Param("userId") int userId,
                        @Param("fromDate") LocalDate fromDate,
                        @Param("toDate") LocalDate toDate,
                        @Param("supervisorName") String supervisorName);

        @Query(value = "SELECT u.name " +
                        "FROM owner_approve_log oal " +
                        "JOIN users u ON oal.created_by = u.id " +
                        "WHERE oal.task_activity_id = :taskActivityId " +
                        "ORDER BY oal.created_at DESC " +
                        "LIMIT 1", nativeQuery = true)
        String findUsernameByTaskActivityId(Integer taskActivityId);

        @Query(value = "SELECT u.name FROM final_approve_log fal JOIN users u ON fal.created_by = u.id WHERE fal.task_activity_id = :taskactivityid ORDER BY fal.created_at DESC LIMIT 1", nativeQuery = true)
        String findFinalApproverByTaskActivityId(@Param("taskactivityid") int taskactivityid);

        @Modifying
        @Transactional
        @Query(value = "DELETE FROM common_task_activities WHERE user_id = :userId AND activity_date BETWEEN :startDate AND :endDate", nativeQuery = true)
        void deleteByUserIdAndDateRange(@Param("userId") int userId, @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query(value = "SELECT DISTINCT  u.name  FROM common_task_activities cta JOIN Users u ON cta.user_id = u.id WHERE cta.activity_date BETWEEN :fromdate AND :todate AND cta.supervisor = :supervisor AND cta.draft = false", nativeQuery = true)
        List<String> findEnteredTimesheetsByUserToSuperviserIdAndDateRange(@Param("supervisor") String supervisor,
                        @Param("fromdate") LocalDate fromDate, @Param("todate") LocalDate toDate);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.prod_id = :prodId " +
                        "AND c.activity_date = :date " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true And c.user_id =:userId " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE u.final_approve = :finalApprover " +
                                        "AND c.prod_id = :prodId " +
                                        "AND c.activity_date = :date " +
                                        "AND c.supervisor_status = 'Approved' " +
                                        "AND c.final_approve IN (:status) " +
                                        "AND c.supervisor_approved = true And c.user_id =:userId " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTaskByDateAndProductId(@Param("finalApprover") String finalApprover,
                        @Param("date") LocalDate date,
                        @Param("prodId") int prodId,
                        Pageable pageable,
                        List<String> status, @Param("userId") int userId);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.prod_id = :prodId " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true and c.user_id =:userId " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE u.final_approve = :finalApprover " +
                                        "AND c.prod_id = :prodId " +
                                        "AND c.supervisor_status = 'Approved' " +
                                        "AND c.final_approve IN (:status) " +
                                        "AND c.supervisor_approved = true and c.user_id =:userId " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTaskByProdIdAndWithoutFilter(@Param("finalApprover") String finalApprover,
                        @Param("prodId") int prodId,
                        Pageable pageable,
                        List<String> status, int userId);

        @Query(value = "SELECT u.name AS user_name, " +
                        "p.name AS product_name, " +
                        "c.id, " +
                        "c.activity_date, " +
                        "c.created_at, " +
                        "c.description, " +
                        "c.draft, " +
                        "c.hours, " +
                        "c.is_approved, " +
                        "c.is_deleted, " +
                        "c.status, " +
                        "c.task, " +
                        "c.updated_at, " +
                        "c.prod_id, " +
                        "c.user_id, " +
                        "c.supervisor, " +
                        "c.final_approve, " +
                        "c.branch, " +
                        "c.owner_approved, " +
                        "c.supervisor_approved, " +
                        "c.owner_status, " +
                        "c.supervisor_status " +
                        "FROM common_task_activities c " +
                        "JOIN products p ON c.prod_id = p.id " +
                        "JOIN users u ON c.user_id = u.id " +
                        "WHERE u.final_approve = :finalApprover " +
                        "AND c.activity_date = :date " +
                        "AND c.supervisor_status = 'Approved' " +
                        "AND c.final_approve IN (:status) " +
                        "AND c.supervisor_approved = true and c.user_id=:userId " +
                        "AND c.draft = false " +
                        "ORDER BY p.name, c.id", countQuery = "SELECT COUNT(*) FROM common_task_activities c " +
                                        "JOIN products p ON c.prod_id = p.id " +
                                        "JOIN users u ON c.user_id = u.id " +
                                        "WHERE u.final_approve = :finalApprover " +
                                        "AND c.activity_date = :date " +
                                        "AND c.supervisor_status = 'Approved' " +
                                        "AND c.final_approve IN (:status) " +
                                        "AND c.supervisor_approved = true and c.user_id=:userId " +
                                        "AND c.draft = false", nativeQuery = true)
        Page<Object[]> findTheTaskByDate(@Param("finalApprover") String finalApprover,
                        @Param("date") LocalDate date,
                        Pageable pageable,
                        List<String> status, @Param("userId") int userId);

        @Query(value = "SELECT u.name AS task_user_name, " +
                        "p2.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.final_approve, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.owner_status, " +
                        "ta.supervisor_status " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON ta.user_id = u.id " +
                        "JOIN products p2 ON ta.prod_id = p2.id " +
                        "JOIN ( " +
                        "    SELECT id " +
                        "    FROM products " +
                        "    WHERE FIND_IN_SET(:authId, tech_owner) > 0 OR FIND_IN_SET(:authId, prod_owner) > 0 " +
                        ") p ON ta.prod_id = p.id " +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authId AND m.member = ta.user_id) "
                        +
                        "WHERE ta.draft = false " +
                        "AND ta.owner_status IN ('Supervisor Approved') " +
                        "AND ta.supervisor_approved = true " +
                        "AND ta.is_approved = false " +
                        "AND u.branch = :branch " +
                        "AND ta.prod_id = :prodId " + // Add condition for prod_id
                        " And ta.user_id = :userId " +
                        "AND ta.activity_date = :date " + // Add condition for activity_date
                        "ORDER BY ta.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerBasedDataByDateAndProductId(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("date") LocalDate date,
                        @Param("prodId") int prodId,
                        Pageable pageable, @Param("userId") int userId);

        @Query(value = "SELECT u2.name AS task_user_name, " +
                        "p.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.owner_status, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.final_approve, " +
                        "ta.supervisor_status " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:statusList) " + // Replace fixed status with statusList parameter
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "  AND ta.prod_id = :prodId " + // Add condition for prod_id
                        " And ta.user_id = :userId " +
                        "  AND ta.activity_date = :date " + // Add condition for activity_date
                        "ORDER BY u2.id DESC ", nativeQuery = true)
        Page<Object[]> getOwnerDataWithDateAndProductIdWithTheStatus(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("date") LocalDate date,
                        @Param("prodId") int prodId,
                        @Param("statusList") List<String> statusList,
                        Pageable pageable, @Param("userId") int userId);

        @Query(value = "SELECT u.name AS task_user_name, " +
                        "p2.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.final_approve, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.owner_status, " +
                        "ta.supervisor_status " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON ta.user_id = u.id " +
                        "JOIN products p2 ON ta.prod_id = p2.id " +
                        "JOIN ( " +
                        "    SELECT id " +
                        "    FROM products " +
                        "    WHERE FIND_IN_SET(:authId, tech_owner) > 0 OR FIND_IN_SET(:authId, prod_owner) > 0 " +
                        ") p ON ta.prod_id = p.id " +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authId AND m.member = ta.user_id) "
                        +
                        "WHERE ta.draft = false " +
                        "AND ta.owner_status IN ('Supervisor Approved') " +
                        "AND ta.supervisor_approved = true " +
                        "AND ta.is_approved = false " +
                        "AND u.branch = :branch " +
                        "And ta.user_id = :userId " +
                        "AND ta.prod_id = :prodId " +
                        "ORDER BY ta.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerDataByProductIdOnly(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("prodId") int prodId,
                        Pageable pageable, @Param("userId") int userId);

        @Query(value = "SELECT u2.name AS task_user_name, " +
                        "p.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.owner_status, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.final_approve, " +
                        "ta.supervisor_status " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:statusList) " + // Filter by statusList
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "And ta.user_id = :userId " +
                        "  AND ta.prod_id = :prodId " + // Filter by prodId
                        "ORDER BY u2.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerDataByTheProductIdWithStatus(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("prodId") int prodId,
                        @Param("statusList") List<String> statusList,
                        Pageable pageable, @Param("userId") int userId);

        @Query(value = "SELECT u.name AS task_user_name, " +
                        "p2.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.final_approve, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.owner_status, " +
                        "ta.supervisor_status " +
                        "FROM common_task_activities ta " +
                        "JOIN users u ON ta.user_id = u.id " +
                        "JOIN products p2 ON ta.prod_id = p2.id " +
                        "JOIN ( " +
                        "    SELECT id " +
                        "    FROM products " +
                        "    WHERE FIND_IN_SET(:authId, tech_owner) > 0 OR FIND_IN_SET(:authId, prod_owner) > 0 " +
                        ") p ON ta.prod_id = p.id " +
                        "LEFT JOIN members m ON m.prod_id = ta.prod_id AND (m.assigned_by = :authId AND m.member = ta.user_id) "
                        +
                        "WHERE ta.draft = false " +
                        "AND ta.owner_status IN ('Supervisor Approved') " +
                        "AND ta.supervisor_approved = true " +
                        "And ta.user_id = :userId " +
                        "AND ta.is_approved = false " +
                        "AND u.branch = :branch " +
                        "AND ta.activity_date = :date " + // Filter by date
                        "ORDER BY ta.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerDataByDateWithoutFilter(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("date") LocalDate date,
                        Pageable pageable, @Param("userId") int userId);

        @Query(value = "SELECT u2.name AS task_user_name, " +
                        "p.name AS product_name, " +
                        "ta.id, " +
                        "ta.activity_date, " +
                        "ta.created_at, " +
                        "ta.description, " +
                        "ta.draft, " +
                        "ta.hours, " +
                        "ta.is_approved, " +
                        "ta.is_deleted, " +
                        "ta.status, " +
                        "ta.task, " +
                        "ta.updated_at, " +
                        "ta.prod_id, " +
                        "ta.user_id, " +
                        "ta.supervisor, " +
                        "ta.owner_status, " +
                        "ta.branch, " +
                        "ta.owner_approved, " +
                        "ta.supervisor_approved AS supervisor_approved, " +
                        "ta.final_approve, " +
                        "ta.supervisor_status " +
                        "FROM owner_approve_log ol " +
                        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                        "JOIN users u1 ON ol.created_by = u1.id " +
                        "JOIN users u2 ON ta.user_id = u2.id " +
                        "JOIN products p ON ta.prod_id = p.id " +
                        "WHERE ol.created_by = :authId " +
                        "  AND ol.status IN (:statusList) " + // Filter by statusList
                        "  AND ta.owner_approved = true " +
                        "  AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                        "                       FROM owner_approve_log ol_inner " +
                        "                       WHERE ol_inner.task_activity_id = ol.task_activity_id) " +
                        "  AND u2.branch = :branch " +
                        "  AND ta.activity_date = :date And ta.user_id = :userId " + // Filter by date
                        "ORDER BY u2.id DESC", nativeQuery = true)
        Page<Object[]> getOwnerDataByDateWithStatus(@Param("authId") int authId,
                        @Param("branch") String branch,
                        @Param("date") LocalDate date,
                        @Param("statusList") List<String> statusList,
                        Pageable pageable, @Param("userId") int userId);

}
