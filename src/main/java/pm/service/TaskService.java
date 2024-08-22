package pm.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import pm.model.task.Task;
import pm.model.users.Users;
import pm.request.ActivityRequest;
import pm.request.TaskActivityRequest;

@Service
public interface TaskService {

    ResponseEntity<?> create(Task task, MultipartFile file);

    ResponseEntity<?> get();

    ResponseEntity<?> view(int id);

    ResponseEntity<?> getCategory();

    ResponseEntity<?> memberList(int id);

    ResponseEntity<?> updateActivity(ActivityRequest activityRequest);

    ResponseEntity<?> createMemberActivity(TaskActivityRequest taskActivity);

    ResponseEntity<?> saveasdraftActivity(TaskActivityRequest taskActivity);

    ResponseEntity<?> getProductByUser();

    ResponseEntity<?> getTaskbyproduct(int id);

    ResponseEntity<?> getWeeklytaskActivity(LocalDate fromdate, Integer type);

    ResponseEntity<?> getProductByUserAndProductId(int id);
    ResponseEntity<?> getTaskActivityRequestDates();

    ResponseEntity<?> notifyTeamLeaderIfTasksMissing();

	ResponseEntity<?> getWeeklytaskActivityall(String fromdate);
}
