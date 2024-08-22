package pm.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pm.model.users.UserWidgets;
import pm.model.users.Users;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserWidgetsRepository extends JpaRepository<UserWidgets, Integer> {

    @Query(value = "SELECT widget_count,widget_table,total_widget,remaining_widget,remaining_widget_count,remaining_widget_table FROM user_widgets WHERE emp_id = :empId", nativeQuery = true)
    List<Object[]> findBYEmp_idAndStatusActive(@Param(value = "empId") String empId);

    @Query(value = "SELECT * FROM user_widgets WHERE emp_id = :empId ", nativeQuery = true)
    Optional<UserWidgets> findByEmp_id(@Param(value = "empId") String empId);

    @Query(value = "SELECT widget_count FROM role_wise_widgets WHERE role = :designation   ", nativeQuery = true)
    String checkCountPresentorNot( String designation);
    @Query(value = "SELECT widget_table FROM role_wise_widgets WHERE role = :designation  ", nativeQuery = true)
    String checkTablePresentorNot(String designation);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_widgets WHERE emp_id = :empId", nativeQuery = true)
    void deleteByEmpId(String empId);

    @Query(value = "SELECT total_widget,remaining_widget_count,remaining_widget_table FROM user_widgets where emp_id = :empId", nativeQuery = true)
    List<Object[]> findByEmpId(String empId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user_widgets SET total_widget = :str, remaining_widget_count = :count, remaining_widget_table = :table WHERE emp_id = :empId", nativeQuery = true)
    void updateWidgetInfo(@Param("empId") String empId, @Param("str") String str, @Param("count") String count, @Param("table") String table);
}
