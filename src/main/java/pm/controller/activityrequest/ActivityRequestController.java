package pm.controller.activityrequest;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pm.request.CommonTaskDraft;
import pm.request.RaisedRequest;
import pm.service.ActivityRequestService;

@RequestMapping("/activity")
@CrossOrigin("*")
@RestController
public class ActivityRequestController {

    @Autowired
    private ActivityRequestService activityRequestService;

    @PostMapping("/request")
    public ResponseEntity<?> sendRequest(@RequestBody RaisedRequest raisedRequest) {
        return activityRequestService.sendRequest(raisedRequest);
    }

    // @GetMapping("/raisedRequest/byuser")
    // public ResponseEntity<?> getRaisedRequestByUser() {
    // return activityRequestService.getRaisedRequestByUser();
    // }

    @GetMapping("/raisedRequest/byuser")
    public ResponseEntity<?> getRaisedRequestByUser(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean filter,
            @RequestParam(required = false) LocalDate date, @RequestParam(required = false) String status) {
        return activityRequestService.getRaisedRequestByUser(page, size, filter, date, status);
    }

    @GetMapping("/raisedRequest/tosupervisor/{date}")
    public ResponseEntity<?> getRaisedRequestToSupervisor(@PathVariable LocalDate date) {
        return activityRequestService.getRaisedRequestToSupervisor(date);
    }

    // for sprint3
//    @GetMapping("/raisedRequest/tosupervisor")
//    public ResponseEntity<?> getRaisedRequestToSupervisor() {
//        return activityRequestService.getRaisedRequestToSupervisor();
//    }

    // for sprint 2.1.1
     @Operation(summary = "Get the list of Request Data")
     @GetMapping("/raisedRequest/tosupervisor")
     public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
     @RequestParam(defaultValue = "10") int size, @RequestParam boolean filter, @RequestParam(required = false) LocalDate date, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int userid) {
     return activityRequestService.getRaisedRequestToSupervisor(page, size, filter, date, status, userid);
    }

    // for sprint3
//    @Operation(summary = "Approved Reject List ")
//    @GetMapping("/raisedRequest/tosupervisor/status")
//    public ResponseEntity<?> getRaisedRequestToSupervisorByStatus() {
//        return activityRequestService.getRaisedRequestToSupervisorByStatus();
//    }
    // for sprint 2.1.1

     @Operation(summary = "Get the list of Raised Request Data")
     @GetMapping("/raisedRequest/tosupervisor/status")
     public ResponseEntity<?> getRaisedRequestToSupervisorByStatus(@RequestParam(defaultValue = "0") int page,@RequestParam(defaultValue = "10") int size, @RequestParam boolean filter,@RequestParam(required = false) LocalDate date, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int memberId) {
     return activityRequestService.getRaisedRequestToSupervisorByStatus(page,size,filter,date,status,memberId);
     }

    @GetMapping("/raisedRequest/status/{date}")
    public ResponseEntity<?> getRaisedRequestStatus(@PathVariable LocalDate date) {
        return activityRequestService.getRaisedRequestStatus(date);
    }

    @Operation(summary = "Returns raised request by status", hidden = true)
    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(@RequestBody RaisedRequest raisedRequest, @PathVariable int id) {
        return activityRequestService.approveRequest(raisedRequest, id);
    }

    @PutMapping("/approve")
    public ResponseEntity<?> approveRequests(
            @RequestBody RaisedRequest raisedRequest,
            @RequestParam("ids") List<Integer> ids) {
        return activityRequestService.approveRequests(raisedRequest, ids);
    }

    // Attendance management

    @PostMapping("/attendanceSheet/{date}/status/{status}")
    public ResponseEntity<?> createAttendanceSheetByDate(@PathVariable LocalDate date, @PathVariable String status) {
        return activityRequestService.createAttendanceSheetByDate(date, status);
    }

    @GetMapping("timeSheet/status")
    public ResponseEntity<?> getTimeSheetByStatus(@RequestParam("month") int month, @RequestParam("year") int year) {
        return activityRequestService.getTimeSheetByStatus(month, year);
    }

    @Operation(summary = "Returns raised request by status", hidden = false)
    @GetMapping("/attendanceSheet/byuser")
    public ResponseEntity<?> getAttendanceSheetByUser(@RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return activityRequestService.getAttendanceSheetByUser(date, page, size);
    }

    @Operation(summary = "getAttendanceSheetBySupervisor", hidden = false)
    @GetMapping("/attendanceSheet/bysupervisor")
    public ResponseEntity<?> getAttendanceSheetBySupervisor(
            @RequestParam(value = "date", required = false) LocalDate date,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(required = false) Integer userid) {
        return activityRequestService.getAttendanceSheetBySupervisor(date, page, size, userid);
    }

    @PutMapping("/attendance/delete")
    public ResponseEntity<?> deleteAttendance(@RequestParam("id") Integer id) {
        return activityRequestService.deleteAttendance(id);
    }

    // Self Activity Tab API

    @GetMapping("/selfactivity")
    public ResponseEntity<?> getSelfActivityMethod(@RequestParam(value = "month", required = false) String month,@RequestParam(value = "year", required = false) String year,@RequestParam(defaultValue = "0")int id) {
        return activityRequestService.getSelfActivity(month,year,id);
    }

    @GetMapping("/raisedrequests/userdate")
    public ResponseEntity<?> getRaisedRequestByUserDate(@RequestParam(value = "date", required = false) LocalDate date,@RequestParam(defaultValue = "0")int id) {
        return activityRequestService.getRaisedRequestByUserDate(date,id);
    }


    @GetMapping("/attendance/userdate")
    public ResponseEntity<?> getAttendanceRequestByUserDate(@RequestParam(value = "date", required = false) LocalDate date,@RequestParam(defaultValue = "0")int id) {
        return activityRequestService.getAttendanceRequestByUserDate(date,id);
    }

    

// FOR TEMPORARY API CALL FOR RAISED REQUEST ********************************************************
    
    @PostMapping("/raisedRequest")
    public ResponseEntity<?> raisedRequestForAll() {
        return activityRequestService.raisedRequestForAll();
    }

    @GetMapping("raisedRequest/getData/{id}")
    public ResponseEntity<?> getDataBasedOnID(@PathVariable("id") int id) {
        return activityRequestService.getDataBasedOnID(id);
    }

	@PutMapping("/raisedRequest/update/{id}")
	public ResponseEntity<?> updateRaisedRequest(@PathVariable("id") int id, @RequestParam LocalDate date, @RequestParam(value = "reason") String reason) {
		return activityRequestService.updateRaisedRequest(id, date, reason);
	}


}
