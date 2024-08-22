package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pm.model.task.TimeLimit;

public interface TimeSheetTimeControl extends JpaRepository<TimeLimit, Integer> {

}
