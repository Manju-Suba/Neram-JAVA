package pm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pm.model.task.CommonTaskActivityLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommonTaskActivityLogRepository extends JpaRepository<CommonTaskActivityLog, Integer> {
        @Query(value = "SELECT * FROM common_task_activity_log WHERE created_by = :id AND DATE(created_at) = :date AND status = :status", nativeQuery = true)
        List<CommonTaskActivityLog> findByUserAndActivityDateAndDraftNative(@Param("date") LocalDate date,
                        @Param("status") String status, @Param("id") Integer id);

        @Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
                +
                "ta.draft, ta.created_at , ta.updated_at, " +
                "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, " +
                "p.name AS product_name, ta.final_approve " +
                "FROM common_task_activity_log al " +
                "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
                "JOIN users u1 ON al.created_by = u1.id " +
                "JOIN users u2 ON ta.user_id = u2.id " +

                "JOIN products p ON ta.prod_id = p.id " +
                "WHERE al.created_by = :createdBy and al.status = :status " +
                "AND al.created_at = (SELECT MAX(created_at) FROM common_task_activity_log WHERE task_activity_id = al.task_activity_id)",
                nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedBy(@Param("createdBy") Integer createdBy,
                                                        @Param("status") String status);

        @Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
        +
        "ta.draft, ta.created_at , ta.updated_at, " +
        "u1.name AS approval_user_name, u2.name AS task_user_name,u2.id, ta.task, " +
         "p.name AS product_name,p.id,ta.final_approve " +
        "FROM common_task_activity_log al " +
        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
         "JOIN users u1 ON al.created_by = u1.id " +
         "JOIN users u2 ON ta.user_id = u2.id " +
                                        
        "JOIN products p ON ta.prod_id = p.id " +
        "WHERE al.created_by = :createdBy and al.status = :status " +
        "AND al.created_at = (SELECT MAX(created_at) FROM common_task_activity_log WHERE task_activity_id = al.task_activity_id)",
         nativeQuery = true)
          List<Object[]> getActivityLogDetailsByCreatedByDetail(@Param("createdBy") Integer createdBy,
          @Param("status") String status);                                                


//        @Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
//                        +
//                        "ta.draft, ta.created_at , ta.updated_at, " +
//                        "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, " +
//                        "p.name AS product_name, ta.final_approve " +
//                        "FROM common_task_activity_log al " +
//                        "JOIN common_task_activities ta ON al.task_activity_id = ta.id " +
//                        "JOIN users u1 ON al.created_by = u1.id " +
//                        "JOIN users u2 ON ta.user_id = u2.id " +
//
//                        "JOIN products p ON ta.prod_id = p.id " +
//                        "WHERE al.created_by = :createdBy and al.status in ('Approved', 'Reject')and ta.owner_approved = true ", nativeQuery = true)
//        List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy") Integer createdBy);


        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, " +
                "ta.draft, ta.created_at , ta.updated_at, " +
                "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, " +
                "p.name AS product_name, ta.final_approve " +
                "FROM owner_approve_log ol " +
                "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
                "JOIN users u1 ON ol.created_by = u1.id " +
                "JOIN users u2 ON ta.user_id = u2.id " +
                "JOIN products p ON ta.prod_id = p.id " +
                "WHERE ol.created_by = :createdBy AND ol.status IN ('Approved', 'Reject') AND ta.owner_approved = true " +
                "AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
                "                      FROM owner_approve_log ol_inner " +
                "                      WHERE ol_inner.task_activity_id = ol.task_activity_id)",
                nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy") Integer createdBy);

        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, " +
        "ta.draft, ta.created_at , ta.updated_at, " +
        "u1.name AS approval_user_name, u2.name AS task_user_name,u2.id, ta.task, " +
        "p.name AS product_name,p.id, ta.final_approve " +
        "FROM owner_approve_log ol " +
        "JOIN common_task_activities ta ON ol.task_activity_id = ta.id " +
        "JOIN users u1 ON ol.created_by = u1.id " +
        "JOIN users u2 ON ta.user_id = u2.id " +
        "JOIN products p ON ta.prod_id = p.id " +
        "WHERE ol.created_by = :createdBy AND ol.status IN ('Approved', 'Reject') AND ta.owner_approved = true " +
        "AND ol.created_at = (SELECT MAX(ol_inner.created_at) " +
        "                      FROM owner_approve_log ol_inner " +
        "                      WHERE ol_inner.task_activity_id = ol.task_activity_id)",
        nativeQuery = true)
        List<Object[]> getActivityLogDetailsByCreatedByOwnerDetail(@Param("createdBy") Integer createdBy);

        @Query(value = "SELECT * FROM common_task_activity_log where task_activity_id =:taskActivityId", nativeQuery = true)
        Optional<CommonTaskActivityLog> getByTaskActivityId(Integer taskActivityId);


    @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, "
            + "ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
            + "u2.name AS task_user_name, ta.task, p.name AS product_name, ta.prod_id, "
            + "ta.final_approve, u2.branch, ta.is_approved, al.status AS supervisor_approved, "
            + "ta.user_id "
            + "FROM common_task_activity_log al "
            + "JOIN common_task_activities ta ON al.task_activity_id = ta.id "
            + "JOIN users u1 ON al.created_by = u1.id "
            + "JOIN users u2 ON ta.user_id = u2.id "
            + "JOIN products p ON ta.prod_id = p.id "
            + "JOIN (SELECT task_activity_id, MAX(created_at) AS max_created_at "
            +       "FROM common_task_activity_log "
            +       "GROUP BY task_activity_id) AS recent_logs "
            + "ON al.task_activity_id = recent_logs.task_activity_id AND al.created_at = recent_logs.max_created_at "
            + "WHERE al.created_by = :createdBy AND al.status = :status AND ta.prod_id = :product "
            + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
            + "ORDER BY ta.activity_date DESC", nativeQuery = true)
    Page<Object[]> getActivityLogDetailsByCreatedByAndProduct(@Param("createdBy") Integer createdBy,
                                                              @Param("status") String status,
                                                              @Param("product") Integer productId,
                                                              Pageable pageable);




    @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, "
            + "ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
            + "u2.name AS task_user_name, ta.task, p.name AS product_name, ta.prod_id, "
            + "ta.final_approve, u2.branch, ta.is_approved, al.status AS supervisor_approved, "
            + "ta.user_id "
            + "FROM common_task_activity_log al "
            + "JOIN common_task_activities ta ON al.task_activity_id = ta.id "
            + "JOIN users u1 ON al.created_by = u1.id "
            + "JOIN users u2 ON ta.user_id = u2.id "
            + "JOIN products p ON ta.prod_id = p.id "
            + "JOIN (SELECT task_activity_id, MAX(created_at) AS max_created_at "
            +       "FROM common_task_activity_log "
            +       "GROUP BY task_activity_id) AS recent_logs "
            + "ON al.task_activity_id = recent_logs.task_activity_id AND al.created_at = recent_logs.max_created_at "
            + "WHERE al.created_by = :createdBy AND al.status = :status AND ta.prod_id = :product AND u2.id = :memberId "
            + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
            + "ORDER BY ta.activity_date DESC", nativeQuery = true)
    Page<Object[]> getActivityLogDetailsByCreatedByAndProductByAndMemberId(@Param("createdBy") Integer createdBy,
                                                                           @Param("status") String status,
                                                                           @Param("product") Integer productId,
                                                                           @Param("memberId") Integer memberId,
                                                                           Pageable pageable);



    @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, "
            + "ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
            + "u2.name AS task_user_name, ta.task, p.name AS product_name, ta.prod_id, "
            + "ta.final_approve, u2.branch, ta.is_approved, al.status AS supervisor_approved, "
            + "ta.user_id "
            + "FROM common_task_activity_log al "
            + "JOIN common_task_activities ta ON al.task_activity_id = ta.id "
            + "JOIN users u2 ON ta.user_id = u2.id "
            + "JOIN products p ON ta.prod_id = p.id "
            + "JOIN (SELECT task_activity_id, MAX(created_at) AS max_created_at "
            +       "FROM common_task_activity_log "
            +       "GROUP BY task_activity_id) AS recent_logs "
            + "ON al.task_activity_id = recent_logs.task_activity_id AND al.created_at = recent_logs.max_created_at "
            + "WHERE al.created_by = :createdBy AND al.status = :status "
            + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
            + "ORDER BY ta.activity_date DESC", nativeQuery = true)
    Page<Object[]> getActivityLogDetailsByCreatedBy(@Param("createdBy") Integer createdBy,
                                                    @Param("status") String status,
                                                    Pageable pageable);



    @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, "
            + "ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
            + "u2.name AS task_user_name, ta.task, p.name AS product_name, ta.prod_id, "
            + "ta.final_approve, u2.branch, ta.is_approved, al.status AS supervisor_approved, "
            + "ta.user_id "
            + "FROM common_task_activity_log al "
            + "JOIN common_task_activities ta ON al.task_activity_id = ta.id "
            + "JOIN users u2 ON ta.user_id = u2.id "
            + "JOIN products p ON ta.prod_id = p.id "
            + "JOIN (SELECT task_activity_id, MAX(created_at) AS max_created_at "
            +       "FROM common_task_activity_log "
            +       "GROUP BY task_activity_id) AS recent_logs "
            + "ON al.task_activity_id = recent_logs.task_activity_id AND al.created_at = recent_logs.max_created_at "
            + "WHERE al.created_by = :createdBy AND al.status = :status "
            + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
            + "AND ta.activity_date BETWEEN :startDate AND :endDate "
            + "ORDER BY ta.activity_date DESC", nativeQuery = true)
    Page<Object[]> getActivityLogDetailsByCreatedByAndDateRange(@Param("createdBy") Integer createdBy,
                                                                @Param("status") String status,
                                                                @Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                Pageable pageable);



    @Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, "
            + "ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, "
            + "u2.name AS task_user_name, ta.task, p.name AS product_name, ta.prod_id, "
            + "ta.final_approve, u2.branch, ta.is_approved, al.status AS supervisor_approved, "
            + "ta.user_id "
            + "FROM common_task_activity_log al "
            + "JOIN common_task_activities ta ON al.task_activity_id = ta.id "
            + "JOIN users u1 ON al.created_by = u1.id "
            + "JOIN users u2 ON ta.user_id = u2.id "
            + "JOIN products p ON ta.prod_id = p.id "
            + "JOIN (SELECT task_activity_id, MAX(created_at) AS max_created_at "
            +       "FROM common_task_activity_log "
            +       "GROUP BY task_activity_id) AS recent_logs "
            + "ON al.task_activity_id = recent_logs.task_activity_id AND al.created_at = recent_logs.max_created_at "
            + "WHERE al.created_by = :createdBy AND al.status = :status AND u2.id = :memberId "
            + "AND (ta.contract_status IS NULL OR ta.contract_status != 'finalApproval') "
            + "ORDER BY ta.activity_date DESC", nativeQuery = true)
    Page<Object[]> getActivityLogDetailsByCreatedByAndMemberId(@Param("createdBy") Integer createdBy,
                                                               @Param("status") String status,
                                                               @Param("memberId") Integer memberId,
                                                               Pageable pageable);





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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndProduct(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("prod_id") int prodId, Pageable pageable);


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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, Pageable pageable);

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
            + "ORDER BY ol.id DESC  ",
            nativeQuery = true)

    List<Object[]> getActivityLogDetailsByCreatedByOwnerAndMember(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("user_id") int userId, Pageable pageable);


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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDate(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("date") LocalDate date, Pageable pageable);

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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDateAndProduct(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("date") LocalDate date, @Param("prod_id") int prodId, Pageable pageable);


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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndDateAndMember(@Param("createdBy") Integer createdBy, @Param("statuses") List<String> statuses, @Param("date") LocalDate date, @Param("user_id") int userId, Pageable pageable);


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
                + "AND u2.branch = :branch "
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAndMemberAndProduct(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("prod_id") int prodId, @Param("user_id") int userId, Pageable pageable);

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
                + "ORDER BY ol.id DESC  ",
                nativeQuery = true)

        List<Object[]> getActivityLogDetailsByCreatedByOwnerAll(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, @Param("date") LocalDate date, @Param("prod_id") int prodId, @Param("user_id") int userId, Pageable pageable);

//        @Query(value = "SELECT ol.id, ol.status AS supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, "
//                + "ta.draft, ta.created_at , ta.updated_at, "
//                + "u1.name AS approval_user_name, u2.name AS task_user_name, ta.task, "
//                + "p.name AS product_name, ta.final_approve,ta.user_id,ta.prod_id "
//                + "FROM owner_approve_log ol "
//                + "JOIN common_task_activities ta ON ol.task_activity_id = ta.id "
//                + "JOIN users u1 ON ol.created_by = u1.id "
//                + "JOIN users u2 ON ta.user_id = u2.id  "
//                + "JOIN products p ON ta.prod_id = p.id "
//                + "WHERE ol.created_by = :createdBy AND ol.status IN :statuses AND ta.owner_approved = true "
//                + "AND ol.created_at = (SELECT MAX(ol_inner.created_at) "
//                + "FROM owner_approve_log ol_inner "
//                + "WHERE ol_inner.task_activity_id = ol.task_activity_id)"
//                + "AND u2.branch = :branch "
//                + "ORDER BY ol.id DESC  ",
//                nativeQuery = true)
//
//        List<Object[]> getActivityLogDetailsByCreatedByOwner(@Param("createdBy") Integer createdBy, @Param("branch") String branch, @Param("statuses") List<String> statuses, Pageable pageable);


}
