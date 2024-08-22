package pm.controller.mobileApp;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import pm.mobileApp.dto.InputDto;
import pm.mobileApp.dto.MyteamFilterDto;
import pm.mobileApp.service.TimesheetService;
import pm.mobileAppDto.MembersActivityResponse;
import pm.response.ApiResponse;

@RequestMapping("/mobile/timesheet")
@CrossOrigin("*")
@RestController
public class TimesheetController {

    private final TimesheetService timesheetService;

    public TimesheetController(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @GetMapping("/product-list")
    @Operation(summary = "Draft product list for timesheet", description = "Draft list for timesheet with pagination")
    public ResponseEntity<ApiResponse> getDraftListWithFilters(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) String filter, @RequestParam String status) {
        try {
            return timesheetService.getDraftProductListWithFilters(page, size, date, filter, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Data not found", e.getMessage()));
        }
    }

    @GetMapping("/task-list/{prodId}")
    @Operation(summary = "Draft task list for timesheet", description = "Draft task list for timesheet with pagination")
    public ResponseEntity<ApiResponse> getDraftTaskListWithFilters(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate date,
            @PathVariable int prodId, @RequestParam String status) {
        try {
            return timesheetService.getDraftTaskListWithFilters(page, size, date, prodId, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Data not found", e.getMessage()));
        }
    }

    @GetMapping("/members-activity")
    @Operation(summary = "Members activity list for timesheet", description = "Members activity list for timesheet with pagination")
    public ResponseEntity<?> getMembersActivityListWithFilters(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate fromdate,
                                                              @RequestParam(required = false) LocalDate todate,
            @RequestParam(required = false, defaultValue = "0") int userId,
            @RequestParam(required = false) String status,@RequestParam(required = false) String filter,@RequestParam(required = false,defaultValue = "0" )int prodid) {
        try {
            return timesheetService.getMembersActivityListWithFilters(page, size, fromdate,todate, userId, status,filter,prodid);
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/contract-members-activity")
    @Operation(summary = "Contract Members activity list for timesheet", description = "Contract Members activity list for timesheet with pagination")
    public ResponseEntity<?> getContractMembersActivityListWithFilters(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false,defaultValue = "0" ) int userId,
            @Parameter(description = "Status", schema = @Schema(type = "string", allowableValues = { "both", "Approved",
                    "Rejected", "Pending" })) @RequestParam String status,@RequestParam(required = false)boolean filter,@RequestParam(required=false,defaultValue = "0")int prodid) {
        try {
            return timesheetService.getContractMembersActivityListWithFilters(page, size, date, userId, status,filter,prodid);
        } catch (Exception e) {
            return null;
        }
    }


    @GetMapping("/products-members-activity")
    @Operation(summary = "Get products Based Member Activity List", description = "Get products Based Member Activity List")
    public ResponseEntity<?> getProductsMembersActivityListWithFilters(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false)@DateTimeFormat(pattern = "yyyy/MM/dd") LocalDate date,
            @RequestParam(required = false,defaultValue = "0" ) int userId,
            @RequestParam(required = false) String status,@RequestParam(required = false) String filter,@RequestParam(required = false,defaultValue = "0" )int prodid) {
        try {
        	System.out.println(date);
            return timesheetService.getProductsMembersActivityListWithFilters(page, size, date, userId, status,filter,prodid);
        } catch (Exception e) {
            return null;
        }
    }


    @Operation(summary = "get the My Report for the Mobile app")
    @GetMapping("myreport/{id}/{date}")
    public ResponseEntity<?> getMyReport(@PathVariable("id") int id, @PathVariable("date") LocalDate date) {
        return timesheetService.getMyReportDetails(id, date);
        
    }

    @Operation(summary = "get the report for the person on the mobile app")
    @GetMapping("myteamReport")
    public ResponseEntity<?> getMyTeamReport(InputDto inputDto) {
        return timesheetService.getMyTeamReportList(inputDto);
    }

    @Operation(summary = "Myteam report get the single person report on the mobile app")
    @GetMapping("myteamRepoet-singlemember")
    public ResponseEntity<?> getMyTeamReportList(MyteamFilterDto inputDto) {
        return timesheetService.getMyteamSingleMemberReport(inputDto);
    }


}
