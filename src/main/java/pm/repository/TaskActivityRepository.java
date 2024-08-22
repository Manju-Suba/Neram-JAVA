package pm.repository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pm.dto.TaskActivityResponse;
import pm.model.product.Product;
import pm.model.task.TaskActivity;
import pm.model.users.Users;
import pm.response.ActivityDTO;

@Repository
public interface TaskActivityRepository extends JpaRepository<TaskActivity, Integer> {

	@Query(value = "SELECT * FROM task_activities WHERE activity_date BETWEEN :startDate AND :endDate", nativeQuery = true)
	List<TaskActivity> findByActivityDateBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	List<TaskActivity> findByProduct(Product prodId);

	// @Query(value = "SELECT * FROM task_activities WHERE prod_id = :productId AND
	// activity_date BETWEEN :fromDate AND :toDate", nativeQuery = true)
	// List<TaskActivity> findByProductAndActivityDateBetweenNative(
	// @Param("productId") Integer productId,
	// @Param("fromDate") LocalDate localStartDate,
	// @Param("toDate") LocalDate localEndDate
	// );
	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name as userName, t.task_name as taskName, p.name as productName FROM task_activities ta INNER JOIN users u ON ta.user_id = u.id INNER JOIN tasks t ON ta.task_id = t.id INNER JOIN products p ON ta.prod_id = p.id WHERE ta.prod_id = :productId AND ta.activity_date = :startDate", nativeQuery = true)
	List<Object[]> findByProductAndActivityDateBetweenNative(@Param("productId") Integer productId,
			@Param("startDate") LocalDate startDate);

	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name as userName, t.task_name as taskName, p.name as productName,ta.final_approve,ta.branch FROM task_activities ta INNER JOIN users u ON ta.user_id = u.id INNER JOIN tasks t ON ta.task_id = t.id INNER JOIN products p ON ta.prod_id = p.id WHERE ta.prod_id = :productId AND ta.activity_date = :startDate and ta.branch = :branch", nativeQuery = true)
	List<Object[]> findByProductAndActivityDateBetweenNative(@Param("productId") Integer productId,
															 @Param("startDate") LocalDate startDate, @Param("branch")String branch);

	
	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name as userName, t.task_name as taskName, p.name as productName,ta.final_approve,ta.branch FROM task_activities ta INNER JOIN users u ON ta.user_id = u.id INNER JOIN tasks t ON ta.task_id = t.id INNER JOIN products p ON ta.prod_id = p.id WHERE ta.prod_id = :productId AND  ta.branch = :branch", nativeQuery = true)
	List<Object[]> findByProduc(@Param("productId") Integer productId,
															 @Param("branch")String branch);
	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name as userName, t.task_name as taskName, p.name as productName FROM task_activities ta INNER JOIN users u ON ta.user_id = u.id INNER JOIN tasks t ON ta.task_id = t.id INNER JOIN products p ON ta.prod_id = p.id WHERE ta.prod_id = :productId AND ta.activity_date = :startDate AND ta.draft =true AND ta.is_approved = false ", nativeQuery = true)
	List<Object[]> findByProductAndActivityDateBetweenNativedraft(@Param("productId") Integer productId,
			@Param("startDate") LocalDate startDate);

	@Query(value = "SELECT COUNT(*) FROM task_activities WHERE user_id = :userId AND activity_date = :date", nativeQuery = true)
	BigInteger countByUserAndActivityDateNative(@Param("userId") Integer userId, @Param("date") LocalDate date);

	@Query(value = "SELECT * FROM task_activities WHERE user_id = :id AND activity_date = :date AND draft = :b", nativeQuery = true)
	List<TaskActivity> findByUserAndActivityDateAndDraftNative(@Param("date") LocalDate date, @Param("b") boolean b,
			@Param("id") Integer id);

	@Query(value = "SELECT * FROM task_activities WHERE user_id = :id AND activity_date = :date", nativeQuery = true)
	List<TaskActivity> findByUserIdAndActivityDate(int id, LocalDate date);

	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, t.task_name, p.name, ta.final_approve,ta.branch FROM task_activities ta JOIN users u ON ta.user_id = u.id JOIN tasks t ON ta.task_id = t.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND ta.draft =false AND ta.is_approved = false ", nativeQuery = true)
	List<Object[]> findTaskDTOsByUserIdAndActivityDate(@Param("userId") Integer userId, @Param("date") LocalDate date);


	//without date
	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, t.task_name, p.name, ta.final_approve,ta.branch FROM task_activities ta JOIN users u ON ta.user_id = u.id JOIN tasks t ON ta.task_id = t.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.draft =false AND ta.is_approved = false ", nativeQuery = true)
	List<Object[]> findTaskDTOsByUserId(@Param("userId") Integer userId);

	@Query(value = "SELECT COUNT(*) FROM task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
	long countByActivityDateAndActivityIdAndStatus(@Param("date") LocalDate date, @Param("id") Integer id);

	@Query(value = "SELECT * FROM task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
	TaskActivity countByActivityDateAndActivityIdAndStatusvalue(@Param("date") LocalDate date, @Param("id") Integer id);

	// @Query(value = "SELECT * FROM task_activities WHERE user_id = :userId AND
	// activity_date BETWEEN :startOfMonth AND :endOfMonth", nativeQuery = true)
	// List<TaskActivity> findByUserIdAndActivityDateBetween(@Param("userId") int
	// userId, @Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth")
	// LocalDate endOfMonth);
	@Query(value = "SELECT * FROM task_activities WHERE user_id = :userId", nativeQuery = true)
	List<TaskActivity> findByUserIdAndActivityDateBetween(@Param("userId") int userId);

	TaskActivity findByUserId(Integer user_id);

	@Query(value = "SELECT COUNT(*) FROM task_activities WHERE activity_date = :date AND user_id = :id AND draft =false", nativeQuery = true)
	Long countByUserIdAndActivityDate(@Param("id") Integer id, @Param("date") LocalDate date);

	@Query(value = "SELECT ta.id, ta.activity_date, ta.hours, ta.description, ta.status, ta.draft, ta.created_at, ta.updated_at, ta.is_deleted, u.name, t.task_name, p.name, ta.final_approve,ta.branch FROM task_activities ta JOIN users u ON ta.user_id = u.id JOIN tasks t ON ta.task_id = t.id JOIN products p ON ta.prod_id = p.id WHERE u.id = :userId AND ta.activity_date = :date AND ta.final_approve IN ('TL Approved', 'Approved')", nativeQuery = true)
	List<Object[]> findTaskDTOsByUserIdAndActivityDateAndFinalApprove(
			@Param("userId") Integer userId,
			@Param("date") LocalDate date);


	@Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, " +
			"ta.draft, ta.created_at , ta.updated_at, " +
			"u1.name AS approval_user_name, u2.name AS task_user_name, t.task_name, " +
			"p.name AS product_name, ta.final_approve " +
			"FROM activity_log al " +
			"JOIN task_activities ta ON al.task_activity_id = ta.id " +
			"JOIN users u1 ON al.created_by = u1.id " +
			"JOIN users u2 ON ta.user_id = u2.id " +
			"JOIN tasks t ON ta.task_id = t.id " +
			"JOIN products p ON ta.prod_id = p.id " +
			"WHERE al.created_by = :createdBy and al.status = :status", nativeQuery = true)
	List<Object[]> getActivityLogDetailsByCreatedBydata(@Param("createdBy") Integer createdBy,@Param("status") String status);


	@Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, " +
			"ta.draft, ta.created_at , ta.updated_at, " +
			"u1.name AS approval_user_name, u2.name AS task_user_name, t.task_name, " +
			"p.name AS product_name, ta.final_approve " +
			"FROM activity_log al " +
			"JOIN task_activities ta ON al.task_activity_id = ta.id " +
			"JOIN users u1 ON al.created_by = u1.id " +
			"JOIN users u2 ON ta.user_id = u2.id " +
			"JOIN tasks t ON ta.task_id = t.id " +
			"JOIN products p ON ta.prod_id = p.id " +
			"WHERE al.created_by = :createdBy and al.status = :status", nativeQuery = true)
	List<Object[]> getActivityLogDetailsByCreatedBy(@Param("createdBy") Integer createdBy,@Param("status") String status);

	@Query(value = "SELECT al.id, al.status as supervisor_approved, ta.activity_date, ta.hours, ta.description, ta.status, " +
			"ta.draft, ta.created_at , ta.updated_at, " +
			"u1.name AS approval_user_name, u2.name AS task_user_name,u2.id, t.task_name, " +
			"p.name AS product_name,p.id, ta.final_approve " +
			"FROM activity_log al " +
			"JOIN task_activities ta ON al.task_activity_id = ta.id " +
			"JOIN users u1 ON al.created_by = u1.id " +
			"JOIN users u2 ON ta.user_id = u2.id " +
			"JOIN tasks t ON ta.task_id = t.id " +
			"JOIN products p ON ta.prod_id = p.id " +
			"WHERE al.created_by = :createdBy and al.status = :status", nativeQuery = true)
	List<Object[]> getActivityLogDetailsByCreatedByDetail(@Param("createdBy") Integer createdBy,@Param("status") String status);
}



