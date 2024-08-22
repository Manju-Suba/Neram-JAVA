package pm.controller.mail;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pm.response.ApiResponse;
import pm.service.EmployeeService;
import pm.service.TaskTimeSheetService;
import pm.serviceImplements.SupervisorMailImpl;
import pm.serviceImplements.TaskTimeSheetImpl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/mail/service")
@CrossOrigin("*")
@RestController
public class SupervisorContentController {
    @Autowired
    private SupervisorMailImpl supervisorMailImpl;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TaskTimeSheetService taskTimeSheetImpl;

    @Scheduled(cron = "0 45 5 * * *") // Executes every day at 10:15 AM 04:30AM UTC
    @GetMapping("/supervisor/mail")
    public ResponseEntity<?> SupervisorContent() throws MessagingException, IOException {
        return supervisorMailImpl.dailySupervisorReport();
    }

//    @Scheduled(cron = "0 40 4 ? * MON") // Executes every Monday at 10 AM
//    @GetMapping("/remainder/mail")
//    public String remainderall() {
//        return supervisorMailImpl.remainderall();
//    }

//    @Scheduled(cron = "0 30 6 ? * MON") // Executes every Monday after 12 PM
//    @GetMapping("/weeky-expire/mail")
//    public String weeklyTimeSheetExpireMail() {
//        return supervisorMailImpl.weeklyTimeSheetExpireMail();
//    }

    @Scheduled(cron = "0 0 5 * * MON-FRI") // Executes every day at 10:00 AM from Monday to Friday 04:30AM UTC
    @GetMapping("notenterd/mail")
    public String notEnterdMail() {
        return supervisorMailImpl.notEnterdMail();
    }

    @Operation(summary = "!!!!!!!!! Rstricted API !!!!!!!  Update user Joining date", hidden = false)
    @GetMapping("/update-jod")
    public ResponseEntity<?> updateJOD() {
        return employeeService.updateJOD();
    }

    @Operation(summary = "!!!!!!!!! Rstricted API !!!!!!!  Delete Timesheet", hidden = false)
    @GetMapping("/timesheet-delete/{id}")
    public ResponseEntity<?> deleteTimesheet(@RequestParam List<Integer> id) {
        return taskTimeSheetImpl.deleteTimesheet(id);
    }

    @Operation(summary = "!!!!!!!!! Rstricted API !!!!!!!  Delete Timesheet", hidden = false)
    @GetMapping("/timesheet-deletedata")
    public ResponseEntity<?> deleteTimesheetdaterangebased(@RequestParam int id, @RequestParam LocalDate fromdate,@RequestParam LocalDate todate) {
        return taskTimeSheetImpl.deleteTimesheetdaterange(id,fromdate,todate);
    }


    @Operation(summary = "!!!!!!!!! Rstricted API !!!!!!!  Delete Timesheet", hidden = false)
    @GetMapping("/timesheet-insertlog")
    public ResponseEntity<?> insertActivityRequests(@RequestParam int userId,
                                                    @RequestParam String fromDate,
                                                    @RequestParam String toDate,
                                                    @RequestParam int sender) {
        LocalDate start = LocalDate.parse(fromDate);
        LocalDate end = LocalDate.parse(toDate);

        return taskTimeSheetImpl.insertActivityRequest(userId, start, end, sender);
    }





}
