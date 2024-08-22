package pm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pm.model.product.BussinessCategory;
import pm.model.task.TaskCategory;

@Repository
public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Integer> {

    List<TaskCategory> findAll();

    boolean existsByCategory(String category);

    @Modifying
    @Transactional
    @Query(value = "UPDATE task_categories t SET t.is_deleted = 1 WHERE t.id IN :id", nativeQuery = true)
    void updateIsDeleted(@Param("id") List<Integer> id);

    // =============================================get all List
    // ================================
    @Query(value = "select * from task_categories where is_deleted = 0", nativeQuery = true)
    List<TaskCategory> getallTaskCategoryActive();

    @Query(value = "select * from task_categories where is_deleted = 0", nativeQuery = true)
    Page<TaskCategory> getallTaskCategoryActiveWithPage(Pageable pageable);

    @Query(value = "SELECT * FROM task_categories WHERE (LOWER(category) REGEXP LOWER(:nameRegex) OR LOWER(group_name) REGEXP LOWER(:nameRegex)) AND is_deleted = false", nativeQuery = true)
    Page<TaskCategory> getallTaskCategoryActiveWithSearch(@Param("nameRegex") String nameRegex, Pageable Pageable);

    // ========================================get List By Id
    // ==============================
    @Query(value = "select * from task_categories where id =:id and is_deleted = 0", nativeQuery = true)
    Optional<TaskCategory> getByIdisActive(@Param("id") Integer id);

    boolean existsByGroupName(String groupName);

    Optional<TaskCategory> findByGroupName(String name);

    @Query(value = "SELECT * from task_categories t WHERE t.group_name = :name AND t.is_deleted = false", nativeQuery = true)
    Optional<TaskCategory> findByTaskNameAndIs_deletedFalse(@Param("name") String name);

    @Query(value = "SELECT COUNT(*) FROM task_categories WHERE is_deleted = false", nativeQuery = true)
    Long countByTaskGroup();
}
