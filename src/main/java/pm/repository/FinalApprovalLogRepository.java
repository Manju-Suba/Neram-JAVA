package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pm.model.taskapproval.FinalApprovalLog;

@Repository
public interface FinalApprovalLogRepository extends JpaRepository<FinalApprovalLog, Integer> {

}
