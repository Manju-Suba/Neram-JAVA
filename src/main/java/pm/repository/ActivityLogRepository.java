package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pm.model.task.ActivityLog;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {

}
