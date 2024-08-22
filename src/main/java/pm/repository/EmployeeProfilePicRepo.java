package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import pm.model.users.EmployeeProfilePic;

public interface EmployeeProfilePicRepo extends JpaRepository<EmployeeProfilePic,Long> {
    EmployeeProfilePic findByEmpid(String empid);

    @Query(value = "SELECT COUNT(*) FROM profilepic WHERE empid = ?",nativeQuery = true)
    Long countByEmployeeId(String employeeId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE profilepic SET profile_pic = :profilePicUrl WHERE empid = :employeeId", nativeQuery = true)
    void updateProfilePicUrl(@Param("profilePicUrl") String profilePicUrl, @Param("employeeId") String employeeId);




}
