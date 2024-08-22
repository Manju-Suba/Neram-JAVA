package pm.controller.product.task;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import pm.model.task.Task;
//import pm.model.task.TaskActivity;
//import pm.model.users.Users;
//import pm.request.ActivityRequest;
//import pm.request.TaskActivityRequest;
//import pm.service.TaskService;
//
//@RequestMapping("/task")
//@CrossOrigin("*")
//@RestController
//public class TaskController {
//
//    @Autowired
//    private TaskService taskService;
//
//    @PostMapping("/create")
//    public ResponseEntity<?> create(Task task, @RequestParam(value = "files", required = false) MultipartFile file) {
//        return taskService.create(task, file);
//    }
//
//    @GetMapping(value = "/get")
//    public ResponseEntity<?> get() {
//        return taskService.get();
//    }
//
//    @GetMapping("/view/{id}")
//    public ResponseEntity<?> view(@PathVariable int id) {
//        return taskService.view(id);
//    }
//
//    @GetMapping("/category")
//    public ResponseEntity<?> getCategory() {
//        return taskService.getCategory();
//    }
//
//    @GetMapping("/memberlist/{id}")
//    public ResponseEntity<?> memberList(@PathVariable int id) {
//        return taskService.memberList(id);
//    }
//
//    @PostMapping("/activity/log")
//    public ResponseEntity<?> updateActivity(ActivityRequest activityRequest) {
//        return taskService.updateActivity(activityRequest);
//
//    }
//
//    @PostMapping("/member/activity")
//    public ResponseEntity<?> createMemberActivity(TaskActivityRequest taskActivity) {
//        return taskService.createMemberActivity(taskActivity);
//    }
//
//    @PostMapping("/member/save/draft")
//    public ResponseEntity<?> saveasdraftActivity(TaskActivityRequest taskActivity) {
//        return taskService.saveasdraftActivity(taskActivity);
//    }
//
//    @GetMapping("/productby/user")
//    public ResponseEntity<?> getProductByUser() {
//        return taskService.getProductByUser();
//    }
//
//    @GetMapping("/taskby/product/{id}")
//    public ResponseEntity<?> getTaskbyproduct(@PathVariable int id) {
//        return taskService.getTaskbyproduct(id);
//    }
//
//    @GetMapping("/member-activity")
//    public ResponseEntity<?> getTaskbyweekly(@RequestParam(required = false) LocalDate fromdata,
//            @RequestParam(required = false) int type) {
//        return taskService.getWeeklytaskActivity(fromdata, type);
//    }
//    
//    
//    @GetMapping("/member-activity-all/{fromdate}")
//    public ResponseEntity<?> getTaskbyweeklyall(@PathVariable(required = false) String fromdate
//            ) {
//        return taskService.getWeeklytaskActivityall(fromdate);
//    }
//
//    @GetMapping("/product/byuser/{id}")
//    public ResponseEntity<?> getProductByUserAndProductId(@PathVariable int id) {
//        return taskService.getProductByUserAndProductId(id);
//    }
//
//    @GetMapping("/yesterday/activity")
//    public ResponseEntity<?> getTaskActivity() {
//        return taskService.notifyTeamLeaderIfTasksMissing();
//
//    }
//
//    @GetMapping("/task_activity_request_dates")
//    public ResponseEntity<?> TaskActivityRequestDates() {
//        return taskService.getTaskActivityRequestDates();
//    }

//}
