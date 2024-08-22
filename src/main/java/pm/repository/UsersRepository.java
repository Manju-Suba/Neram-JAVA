package pm.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import pm.model.users.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, Integer> {


        @Query(value = "SELECT * FROM users WHERE status = true and is_deleted= false", nativeQuery = true)
        List<Users> getAll();

        Optional<Users> findByUsername(String username);

        Optional<Users> findByEmail(String email);

        @Query(value = "SELECT * from users u WHERE u.id = :id AND u.is_deleted = false", nativeQuery = true)
        Users findByUserId(int id);

        @Query(value = "SELECT * from users u WHERE u.id = :id AND status = true AND u.is_deleted = false", nativeQuery = true)
        Users findByUserIdandStatus(int id);

        @Query(value = "SELECT username from users u WHERE u.id = :id", nativeQuery = true)
        String findByIdGetUsername(int id);

        @Query(value = "SELECT username from users u WHERE u.id = :id", nativeQuery = true)
        String findByIdGetUsernameforcommonUpdate(String id);

        @Query(value = "SELECT name from users u WHERE u.username = :username", nativeQuery = true)
        String findByUserNameGetName(String username);

        @Query(value = "SELECT id from users u WHERE u.username = :username", nativeQuery = true)
        int findByEmpidGetUserId(String username);

        @Query(value = "SELECT id from users u WHERE u.username = :username", nativeQuery = true)
        Integer findByusernameGetUserId(String username);

        @Query(value = "SELECT * from users u WHERE u.username = :username", nativeQuery = true)
        Optional<Users> findByUserNameGetAll(String username);

        @Query(value = "SELECT * FROM users u WHERE u.username IN :usernames", nativeQuery = true)
        List<Users> findByUsernames(@Param("usernames") List<String> usernames);

        Boolean existsByUsername(String username);

        @Query(value = "SELECT * from users u WHERE u.username = :username AND u.is_deleted = false", nativeQuery = true)
        Optional<Users> findByUsernameAndIs_deletedFalse(String username);

        @Query(value = "SELECT * from users u WHERE u.email = :email AND u.is_deleted = false", nativeQuery = true)
        Optional<Users> findByEmailAndIs_deletedFalse(String email);

        @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.username = :username AND u.is_deleted = false")
        Boolean existsByUsernameAndIs_deletedFalse(@Param("username") String username);

        @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u WHERE u.email = :email AND u.is_deleted = false")
        Boolean existsByEmailAndIs_deletedFalse(@Param("email") String email);

        @Query(value = "select * from users where is_deleted= false and status = true ", nativeQuery = true)
        List<Users> getActiveEmployees();

        @Query(value = "SELECT * FROM users WHERE is_deleted = false", nativeQuery = true)
        Page<Users> getPageActiveEmployees(Pageable pageable);

        @Query(value = "select * from users where is_deleted= false and status = true ", nativeQuery = true)
        List<Users> getActiveEmployeeswithoutDelete();

        // mail concept user details
        @Query(value = "select * from users where is_deleted= false and status = true and designation not in ('Approver','Head','Owner','Internal Admin')", nativeQuery = true)
        List<Users> getActiveEmployeesForEmail();

        @Query(value = "select * from users where is_deleted= false and status = true and  designation not in ('Approver')", nativeQuery = true)
        List<Users> getnotEnteredMail();

        @Query(value = "SELECT * FROM users WHERE is_deleted = false AND status = true", nativeQuery = true)
        Page<Users> getActiveEmployeeswithoutDeletepage(Pageable pageable);

        // @Query(value = "SELECT users.* FROM users JOIN users_roles ON users.id =
        // users_roles.users_id JOIN roles ON users_roles.roles_id=roles.id WHERE
        // roles.name='Product Head';", nativeQuery = true)
        @Query(value = "SELECT * FROM users where designation ='Head' And branch='Product' and status = true and is_deleted= false ", nativeQuery = true)
        List<Users> getProd_headdetails();

        // @Query(value = "SELECT users.* FROM users JOIN users_roles ON users.id =
        // users_roles.users_id JOIN roles ON users_roles.roles_id=roles.id WHERE
        // roles.name='Technical Head';", nativeQuery = true)
        @Query(value = "SELECT * FROM users where designation ='Head' And branch='Technical' and status = true and is_deleted= false", nativeQuery = true)
        List<Users> gettech_headdetails();

        @Query(value = "SELECT * FROM users where designation ='Head' And branch='Data' and status = true and is_deleted= false", nativeQuery = true)
        List<Users> getdata_headdetails();

        @Query(value = "SELECT * FROM users where designation ='Head' And branch='HOW' and status = true and is_deleted= false", nativeQuery = true)
        List<Users> gethowheaddetails();

        @Query(value = "SELECT * FROM users where designation ='Owner' And branch='Product' and status = true and is_deleted= false ", nativeQuery = true)
        // @Query(value = "SELECT users.* FROM users JOIN users_roles ON users.id =
        // users_roles.users_id JOIN roles ON users_roles.roles_id=roles.id WHERE
        // roles.name='Product Owner';", nativeQuery = true)
        List<Users> getProd_ownerdetails();

        @Query(value = "SELECT * FROM users where designation ='Owner' And branch='Technical' and status = true and is_deleted= false ", nativeQuery = true)
        // @Query(value = "SELECT users.* FROM users JOIN users_roles ON users.id =
        // users_roles.users_id JOIN roles ON users_roles.roles_id=roles.id WHERE
        // roles.name='Technical Owner';", nativeQuery = true)
        List<Users> gettech_ownerdetails();

        @Query(value = "SELECT * FROM users where designation ='Owner' And branch='Data' and status = true and is_deleted= false ", nativeQuery = true)
        List<Users> getdata_ownerdetails();

        @Query(value = "SELECT * FROM users where designation ='Owner' And branch='HOW' and status = true and is_deleted= false ", nativeQuery = true)
        List<Users> gethow_ownerdetails();

        @Query(value = "SELECT * FROM users where designation =:desg and status = true and is_deleted= false ", nativeQuery = true)
        List<Users> findByDesignation(String desg);

        @Query(value = "SELECT * FROM users where designation IN :desgs and status = true and is_deleted= false ", nativeQuery = true)
        List<Users> findByDesignationall(List<String> desgs);

        List<Users> findByDesignationAndIdNotIn(String designation, List<Integer> ids);

        @Query("SELECT u FROM Users u JOIN FETCH u.role_id WHERE u.id = :id")
        Optional<Users> findByIdWithRoles(@Param("id") int id);

        List<Users> findByBranch(String branch);

        @Query(value = "SELECT * FROM users WHERE branch = :branch AND is_deleted = false", nativeQuery = true)
        List<Users> findByActiveEmployeeBranch(@Param("branch") String branch);

        @Query(value = "SELECT * FROM users WHERE id = :id AND is_deleted = false", nativeQuery = true)
        Optional<Users> findByActiveEmployeeById(@Param("id") int id);

        @Query(value = "SELECT * FROM users WHERE id IN (:ids) AND is_deleted = false AND designation = :desg AND status = true", nativeQuery = true)
        List<Users> findByActiveEmployeeByIdAndDesignation(@Param("ids") List<Integer> ids, @Param("desg") String desg);

        @Query(value = "SELECT * FROM users WHERE id =:id AND is_deleted = false AND designation = :desg AND branch = :branch AND status = true", nativeQuery = true)
        Optional<Users> findByActiveEmployeeByIdAndDesignationandBranch(@Param("id") Integer ids,
                        @Param("desg") String desg, @Param("branch") String branch);

        @Query(value = "SELECT COUNT(*) > 0 FROM users WHERE id = :id AND is_deleted = false", nativeQuery = true)
        Long countNonDeletedUsers(@Param("id") Integer id);

        @Query(value = "SELECT " +
                        "    CASE WHEN status = true THEN 1 ELSE 0 END AS statusPresent, " +
                        "    (SELECT COUNT(*) > 0 FROM users WHERE id = :id AND is_deleted = false) AS nonDeletedCount "
                        +
                        "FROM users WHERE id = :id", nativeQuery = true)
        Map<Integer, Object> getStatusAndNonDeletedCount(@Param("id") Integer id);

        @Query(value = "SELECT * FROM users WHERE supervisor = :supervisorId AND role_type = :roleType and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findBySupervisorAndRoleType(@Param("supervisorId") String userId,
                        @Param("roleType") String roleType);

        @Query(value = "SELECT * FROM users WHERE supervisor = :supervisorId  and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findBySupervisorwithoutRoleType(@Param("supervisorId") String userId);

        @Query("SELECT u FROM Users u WHERE NOT EXISTS "
                        + "(SELECT 1 FROM TaskActivity t WHERE t.user = u AND t.activity_date = :currentDate)")
        List<Users> findUsersWithMissingTasks(@Param("currentDate") LocalDate currentDate);

        List<Users> findByDesignationAndBranch(String desg, String branch);

        @Query(value = "SELECT * FROM users  WHERE designation = :desg and branch = :branch and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findByDesignationAndBranchandDeleteted(String desg, String branch);

        List<Users> findBySupervisor(String id);

        Page<Users> findBySupervisor(String empid, Pageable pageable);

        @Query(value = "SELECT * FROM users  WHERE supervisor = :supervisor and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findByStatusandActiveEmployees(String supervisor);

        @Query(value = "SELECT * FROM users  WHERE username in (:username) and status = true and is_deleted= false", nativeQuery = true)
        List<Users> getUserList(List<String> username);

        @Query(value = "SELECT * FROM users  WHERE supervisor = :id and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findBySupervisorAndIsActive(String id);

        @Query(value = "SELECT * FROM users  WHERE final_approve = :id and status = true and is_deleted= false", nativeQuery = true)
        List<Users> findByFinalApproveAndIsActive(String id);

        List<Users> findByFinalApprove(String id);
        ////// ======================================Get
        ////// Email=========================================================================

        @Query(value = "SELECT email FROM users  WHERE id = :id", nativeQuery = true)
        String findByIdGetEmail(@Param("id") int id);
        // ============================================================================================================================

        @Query("SELECT u FROM Users u LEFT JOIN FETCH u.role_id WHERE u.email = :email")
        Optional<Users> findByEmailWithRoles(@Param("email") String email);

        @Query(value = "SELECT CASE WHEN status = true THEN 1 ELSE 0 END FROM users WHERE id = :id", nativeQuery = true)
        Integer findByIdandStatus(int id);

        @Query(value = "SELECT DISTINCT supervisor FROM users where is_deleted= false and status = true", nativeQuery = true)
        List<String> findAllSupervisor();

        @Query(value = "SELECT username FROM users WHERE is_deleted = false AND status = true AND  (email LIKE '%@hepl.com%' OR username = 'DY006') AND username = :userids", nativeQuery = true)
        String findAllSupervisorbasedCompany(@Param("userids") String userids);

        @Query(value = "SELECT username FROM users WHERE is_deleted = false AND status = true AND  (email LIKE '%@cavininfotech.com%' AND username != 'DY006') and username = :userids", nativeQuery = true)
        String findAllSupervisorbasedCompanywithothepl(@Param("userids") String userids);

        @Query(value = "SELECT name FROM users u WHERE u.id =:id", nativeQuery = true)
        String findByUserName(@Param("id") int id);

        @Query(value = "SELECT name FROM users u WHERE u.username =:username", nativeQuery = true)
        String findByUserNamegetName(@Param("username") String username);

        @Query(value = "SELECT * FROM users u WHERE u.id = :userId AND u.role_type = :roleType And is_deleted= false AND status = true", nativeQuery = true)
        Users findByIdAndRoleType(@Param("userId") int userId, @Param("roleType") String roleType);

        @Query(value = " select case when count(id) > 0 then 'true' else 'false' end  count from  users where supervisor =:id", nativeQuery = true)
        String getsupervisorcount(String id);

        @Query(value = " select case when count(id) > 0 then 'true' else 'false' end  count from  users where final_approve =:id", nativeQuery = true)
        String getfinalApproverCount(String id);

        @Query(value = "SELECT " +
                        "    (SELECT CASE WHEN COUNT(id) > 0 THEN 'true' ELSE 'false' END " +
                        "     FROM users WHERE supervisor = :id) AS supervisorCount, " +
                        "    (SELECT CASE WHEN COUNT(id) > 0 THEN 'true' ELSE 'false' END " +
                        "     FROM users WHERE final_approve = :id) AS finalApproverCount", nativeQuery = true)
        Map<String, String> getSupervisorAndFinalApproverCounts(@Param("id") String id);

        @Query(value = "SELECT DISTINCT cta.id, u.name, u.email, u.created_at,  p.name , cta.activity_date, cta.task, cta.hours, cta.status, cta.description FROM users u  JOIN common_task_activities cta ON u.id = cta.user_id JOIN products p ON cta.prod_id = p.id WHERE u.id =:id  AND cta.activity_date = :date ", nativeQuery = true)
        List<Object[]> getUsersDataWithIdAndDate(@Param("id") int id, @Param("date") LocalDate date);

        @Query(value = "SELECT DISTINCT cta.id, u.name, u.email, u.created_at, p.name, cta.activity_date, cta.task, cta.hours, cta.status, cta.description, cta.supervisor_status, ctl.remarks, cta.final_approve, cta.owner_status, u.role_type, cta.supervisor, u.final_approve, u.branch, p.prod_owner, p.tech_owner, p.id, u.id, p.data_owner, p.how_owner FROM users u JOIN common_task_activities cta ON u.id = cta.user_id JOIN products p ON cta.prod_id = p.id LEFT JOIN common_task_activity_log ctl ON cta.id = ctl.task_activity_id  WHERE u.id = :id AND cta.activity_date BETWEEN :fromdate AND :todate AND draft=false ORDER BY cta.activity_date", nativeQuery = true)
        List<Object[]> getUsersDataWithIdAndDatebetween(@Param("id") int id, @Param("fromdate") LocalDate fromdate,
                        @Param("todate") LocalDate todate);

        // ========================================================================
        // product id based List
        // @Query(value = "SELECT cta.id, cta.prod_id FROM common_task_activities cta
        // WHERE cta.user_id =:id AND cta.activity_date = :date ", nativeQuery = true)
        // List<Object[]> getProductDataWithIdAndDate(@Param("id") int id,
        // @Param("date") LocalDate date);
        @Query(value = "SELECT DISTINCT cta.id, u.name, u.email, u.created_at,  p.name , cta.activity_date, cta.task, cta.hours, cta.status, cta.description FROM users u  JOIN common_task_activities cta ON u.id = cta.user_id JOIN products p ON cta.prod_id = p.id WHERE u.id =:id  AND cta.activity_date = :date ", nativeQuery = true)
        List<Object[]> getUsersDataWithIdAndDatewithProd(@Param("id") int id, @Param("date") LocalDate date);

        @Query(value = "SELECT cta.id,p.name,cta.hours FROM users u JOIN common_task_activities cta ON u.id = cta.user_id AND cta.activity_date = :date JOIN products p ON cta.prod_id = p.id WHERE u.id = :id", nativeQuery = true)
        List<Object[]> getDataForPdfList(@Param("id") int id, @Param("date") LocalDate date);

        @Modifying
        @Transactional
        @Query(value = "UPDATE users u SET u.is_deleted = 1 WHERE u.id IN :id", nativeQuery = true)
        void updateIsDeleted(@Param("id") List<Integer> id);

        boolean existsByEmail(String email);

        @Query(value = "SELECT u.id, u.username, u.password, u.email, u.name, u.profile_pic, u.designation, u.role_type, u.supervisor, u.branch, u.final_approve, u.status, u.created_at, u.updated_at, u.is_deleted,u.jod "
                        + "FROM users u "
                        + "LEFT JOIN users_roles ur ON u.id = ur.users_id "
                        + "LEFT JOIN roles r ON ur.roles_id = r.id "
                        + "WHERE ((u.designation REGEXP :value "
                        + "OR u.username REGEXP :value "
                        + "OR u.branch REGEXP :value "
                        + "OR u.role_type REGEXP :value "
                        + "OR u.name REGEXP :value "
                        + "OR u.email REGEXP :value) "
                        + "OR (supervisor IN :userId OR final_approve IN :userId)"
                        + "OR r.name  REGEXP :value)"
                        + "and u.is_deleted  = false", nativeQuery = true)
        Page<Users> searchAllFields(@Param("value") String value, @Param("userId") List<Integer> userId,
                        Pageable pageable);

        @Query(value = "SELECT id FROM users WHERE name REGEXP :value and is_deleted  = false", nativeQuery = true)
        List<Integer> searchNameFields(@Param("value") String value);

        @Query("SELECT u FROM Users u JOIN u.role_id r WHERE r.name = :roleName")
        List<Users> findByRoleName(String roleName);

        @Query(value = "SELECT u.id, u.username, u.password, u.email, u.name, u.profile_pic, u.designation, u.role_type, u.supervisor, u.branch, u.final_approve, u.status, u.created_at, u.updated_at, u.is_deleted, u.jod "
                        + "FROM users u "
                        + "LEFT JOIN users_roles ur ON u.id = ur.users_id "
                        + "LEFT JOIN roles r ON ur.roles_id = r.id "
                        + "WHERE ((u.designation REGEXP :value "
                        + "OR u.username REGEXP :value "
                        + "OR u.branch REGEXP :value "
                        + "OR u.role_type REGEXP :value "
                        + "OR u.name REGEXP :value "
                        + "OR u.email REGEXP :value) "
                        + "OR r.name  REGEXP :roleName)"
                        + "and u.is_deleted  = false", nativeQuery = true)
        Page<Users> searchAllFieldsWithRoleName(@Param("value") String value, @Param("roleName") String roleName,
                        Pageable pageable);

        @Query(value = "SELECT * FROM users WHERE supervisor = :supervisorId AND role_type = :roleType And is_deleted= false AND status = true", nativeQuery = true)
        List<Users> findBySupervisorIdandRoleType(@Param(value = "supervisorId") String supervisorId,
                        @Param(value = "roleType") String roleType);

        @Query(value = "SELECT * FROM users WHERE supervisor = :supervisorId AND is_deleted= false AND status = true", nativeQuery = true)
        List<Users> findBySupervisorIdwithoutRoleType(@Param(value = "supervisorId") String supervisorId);

        @Query(value = "SELECT COUNT(*) FROM users WHERE is_deleted = false AND status = true AND designation IN ('Employee','Approver','Owner','Head')", nativeQuery = true)
        Long countByMember();

        @Query(value = "SELECT COUNT(*) FROM users where is_deleted = false", nativeQuery = true)
        Long countByMemberisdeletefalse();

        @Query(value = "SELECT COUNT(*) FROM users where is_deleted = false and status = true and role_type = 'Contract'", nativeQuery = true)
        Long countByContractMember();

        @Query(value = "select  count( distinct supervisor) as supervisor from users where supervisor !='0' and status = 1 and is_deleted = false", nativeQuery = true)
        Long countBySupervisor();

        @Query(value = "select u.name,u1.name from users u join users u1 on u.supervisor = u1.username where u.role_type= 'Contract' and u.status = 1 and u.is_deleted = false", nativeQuery = true)
        List<Object[]> getListOfMemeberAndSupervisorName();

        @Query(value = "SELECT COUNT(*) FROM users WHERE designation = :designation and is_deleted= false AND status = true", nativeQuery = true)
        Long countByDesignation(@Param(value = "designation") String designation);

        @Query(value = "SELECT COUNT(*) FROM users WHERE status = :status and is_deleted= false", nativeQuery = true)
        Long countByStatus(@Param(value = "status") Boolean status);

        @Query(value = "SELECT COUNT(*) FROM users WHERE supervisor = :supervisorId AND is_deleted= false AND status = true", nativeQuery = true)
        Long countBySupervisorId(@Param(value = "supervisorId") String supervisorId);

        @Query(value = "SELECT COUNT(*) FROM users WHERE supervisor = :supervisorId AND role_type = :roleType AND is_deleted= false AND status = true", nativeQuery = true)
        Long countBySupervisorIdAndRole(@Param(value = "supervisorId") String supervisorId,
                        @Param(value = "roleType") String roleType);

        Boolean existsBySupervisor(String supervisor);

        @Query(value = "SELECT u.name FROM Users u WHERE u.supervisor = :supervisor", nativeQuery = true)
        List<String> findAllUserBySupervisor(@Param("supervisor") String supervisor);

}