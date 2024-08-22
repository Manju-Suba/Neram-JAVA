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
import pm.model.users.Roles;
import pm.model.users.Users;

@Repository
public interface RolesRepository extends JpaRepository<Roles, Integer> {

    Optional<Roles> findByName(String name);

    boolean existsByName(String name);

    @Query(value = "SELECT * FROM roles WHERE FIND_IN_SET(id, :idList) = 0", nativeQuery = true)
    List<Roles> findAllRoles(@Param("idList") String idList);

    @Query(value = "SELECT * FROM roles WHERE FIND_IN_SET(id, :idList) > 0", nativeQuery = true)
    List<Roles> findAllRolesparticular(@Param("idList") String idList);

    @Modifying
    @Transactional
    @Query(value = "UPDATE roles r SET r.is_deleted = 1 WHERE r.id IN :ids", nativeQuery = true)
    void updateIsDelete(@Param("ids") List<Integer> ids);
     
    @Query(value = "select * from roles where is_deleted= false", nativeQuery = true)
    List<Roles> getActiveRoles();

    @Query(value = "select * from roles where is_deleted= false", nativeQuery = true)
    Page<Roles> getActiveRolesWithPage(Pageable Pageable);

    @Query(value = "SELECT * FROM roles WHERE id =:id AND is_deleted = false", nativeQuery = true)
    Optional<Roles> FindByActiveRoleById(@Param("id") int id);

    @Query(value = "SELECT * from roles r WHERE r.name = :name AND r.is_deleted = false", nativeQuery = true)
    Optional<Roles> findByRoleNameAndIs_deletedFalse(@Param("name") String name);

    @Query(value = "SELECT COUNT(*) > 0 FROM roles r WHERE r.name = :name AND r.is_deleted = false", nativeQuery = true)
    Integer existsByRoleNameAndIs_deletedFalse(@Param("name") String name);

    @Query(value = "SELECT * FROM roles WHERE LOWER(name) REGEXP LOWER(:nameRegex) AND is_deleted = false", nativeQuery = true)
    Page<Roles> findRolesByNameRegex(@Param("nameRegex") String nameRegex, Pageable Pageable);

    @Query(value = "SELECT COUNT(*) FROM roles WHERE is_deleted = false", nativeQuery = true)
    Long countByRoles();

}
