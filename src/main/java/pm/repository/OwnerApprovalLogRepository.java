package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pm.model.taskapproval.OwnerApprovalLog;

public interface OwnerApprovalLogRepository extends JpaRepository<OwnerApprovalLog, Integer> {
}
