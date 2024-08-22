package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pm.model.task.TaskHistory;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {

}