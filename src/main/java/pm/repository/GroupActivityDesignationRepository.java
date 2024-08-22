package pm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import pm.model.task.GroupActivityDesignation;

public interface GroupActivityDesignationRepository extends JpaRepository<GroupActivityDesignation, Integer> {

    Optional<GroupActivityDesignation> findByGroupId(int groupId);

    @Query(value = "SELECT gad.group_id \n" +
            "FROM group_activity_designation gad \n" +
            "JOIN task_categories tc ON gad.group_id = tc.id \n" +
            "WHERE FIND_IN_SET(:roleId, gad.role_id) > 0 \n" +
            "AND tc.is_deleted = false;", nativeQuery = true)
    Integer findGroupId(@Param("roleId") Integer roleId);

    @Query(value = "SELECT * FROM group_activity_designation WHERE group_id = :groupId and is_deleted = false", nativeQuery = true)
    Optional<GroupActivityDesignation> findByGroupIdandDeleted(int groupId);
}
