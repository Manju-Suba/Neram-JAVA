package pm.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pm.model.task.Task;

public interface TaskRepository extends JpaRepository<Task, Integer> {

    @Query("SELECT t FROM Task t WHERE t.prodId = :prodId")
    List<Task> findByProdId(@Param("prodId") Integer prodId);

    @Query("SELECT t FROM Task t WHERE t.prodId = :prodId and t.branch = :branch")
    List<Task> findByProdIdandbranch(@Param("prodId") Integer prodId, @Param("branch") String branch);

    boolean existsByProdId(Integer productId);

    List<Task> findByProdIdAndCreatedBy(Integer id, Integer userid);

    List<Task> findByProdIdAndCreatedByAndBranch(Integer prodId, Integer createdBy, String branch);

    @Query("SELECT t FROM Task t WHERE t.prodId = :prodId and t.branch = :branch and t.createdBy = :createdBy")
    List<Task> findByProdIdandbranchAndCreatedBy(@Param("prodId") Integer prodId, @Param("branch") String branch,
            @Param("createdBy") Integer createdBy);

    @Query(value = "SELECT DISTINCT cta.id, u.name, u.email, u.created_at , p.name , cta.activity_date, cta.task, cta.hours, cta.status, cta.description,cta.final_approve FROM users u  JOIN common_task_activities cta ON u.id = cta.user_id AND cta.activity_date = :date  JOIN products p ON cta.prod_id = p.id WHERE u.id = :id", nativeQuery = true)
    List<Object[]> getUsersDataWithIdAndDate(@Param("id") int id, @Param("date") LocalDate date);
}