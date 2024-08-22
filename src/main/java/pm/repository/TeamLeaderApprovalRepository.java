package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pm.model.taskapproval.TeamLeaderApproval;

import java.time.LocalDate;

public interface TeamLeaderApprovalRepository extends JpaRepository<TeamLeaderApproval, Long> {

     @Query(value = "SELECT * FROM team_leader_approval WHERE user_id = :userId AND approved_at = :date", nativeQuery = true)
     TeamLeaderApproval findByUserIdAndApprovedAt(@Param("userId") Integer userId, @Param("date") LocalDate date);

     @Query(value = "SELECT COUNT(*) FROM team_leader_approval WHERE user_id = :userId AND approved_at = :date", nativeQuery = true)
     Long countByUserIdAndApprovedAt(Integer userId, LocalDate date);
}
