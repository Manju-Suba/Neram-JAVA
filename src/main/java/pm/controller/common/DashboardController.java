package pm.controller.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import pm.request.DashboardSetDefault;
import pm.request.DashboardUpdateRequest;
import pm.response.ApiResponse;
import pm.response.ApiResponsePageable;
import pm.service.DashboardService;

@RequestMapping("/dashboard")
@CrossOrigin("*")
@RestController
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Get the list of users count based on designation")
    @GetMapping("/members-with-designation-count")
    public ResponseEntity<?> designationMemberCount() {
        return dashboardService.designationMemberCount();
    }

    @Operation(summary = "Get the list of active inactive users count")
    @GetMapping("/members-status-count")
    public ResponseEntity<?> memberStatusCount() {
        return dashboardService.memberStatusCount();
    }

    @Operation(summary = "Get the list of active inactive Flows count")
    @GetMapping("/flows-status-count")
    public ResponseEntity<?> flowsStatusWiseCount() {
        return dashboardService.flowsStatusWiseCount();
    }

    @Operation(summary = "Get the list of designation count")
    @GetMapping("/designation-count")
    public ResponseEntity<?> designationCount() {
        return dashboardService.designationCount();
    }

    @Operation(summary = "Get the list of business Category count")
    @GetMapping("/business-category-count")
    public ResponseEntity<?> businessCategoryCount() {
        return dashboardService.businessCategoryCount();
    }

    @Operation(summary = "Get the list of task group count")
    @GetMapping("/task-group-count")
    public ResponseEntity<?> taskGroupCount() {
        return dashboardService.taskGroupCount();
    }

    @Operation(summary="master Category count")
    @GetMapping("/master-category-count")
    public ResponseEntity<?> masterCategoryCount() {
        return dashboardService.masterCategoryCount();
    }




    @Operation(summary = "Get the list of team member count")
    @GetMapping("/team-member-count")
    public ResponseEntity<?> teamMemberCount() {
        return dashboardService.teamMemberCount();
    }

    @Operation(summary = "Get the list of team member activities count based on sup. status")
    @GetMapping("/team-member-activity")
    public ResponseEntity<?> teamMemberActivity() {
        return dashboardService.teamMemberActivityStatus();
    }

    @Operation(summary = "Get the list of team member second level activities count based on final approval status")
    @GetMapping("/team-member-second-level-activity-count")
    public ResponseEntity<?> teamMemberSecondLevelActivity() {
        return dashboardService.teamMemberSecondLevelActivity();
    }

    @Operation(summary = "Get the list of submitted activity status count")
    @GetMapping("/submitted-activity-status-count")
    public ResponseEntity<?> submittedActivityStatusCount() {
        return dashboardService.submittedActivityStatusCount();
    }

    @Operation(summary = "Get the list of second Level submitted activity status count")
    @GetMapping("/sec-level-submitted-activity-status-count")
    public ResponseEntity<?> secLevelSubmittedActivityStatusCount() {
        return dashboardService.secLevelSubmittedActivityStatusCount();
    }

    @Operation(summary = "Get the list of product (total , approved, rejected, pending) count")
    @GetMapping("/product-status-wise-count")
    public ResponseEntity<?> productStatusCount() {
        return dashboardService.productStatusWiseCount();
    }

    @Operation(summary = "Assigned owner product count")
    @GetMapping("/product-assigned-to-owner-count")
    public ResponseEntity<?> assignedToMemberCount() {
        return dashboardService.assignedToMemberCount();
    }

    @Operation(summary = "Get the list of assigned and unassigned product count")
    @GetMapping("/assigned-unassigned-product-count")
    public ResponseEntity<?> assignedUnassignedProductCount() {
        return dashboardService.assignedUnassignedProductCount();
    }

    @Operation(summary = "Get the list of approved/rejected product count in approver")
    @GetMapping("/approver-approved-rejected-product-count")
    public ResponseEntity<?> approverStatusWiseProductCount() {
        return dashboardService.approverStatusWiseProductCount();
    }

    @Operation(summary = "Get the list of my timesheet details")
    @GetMapping("/get-my-timesheet-details")
    public ResponseEntity<?> myTimesheetDetails(@RequestParam(defaultValue = "0") int filterValue,
            @RequestParam LocalDate date) {
        return dashboardService.myTimesheetDetails(filterValue, date);
    }

    @Operation(summary = "Get the list of role based timesheet details")
    @GetMapping("/role-based-timesheet-details")
    public ResponseEntity<?> roleBasedTimesheetDetails(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) boolean search,
            @RequestParam(required = false) String value,
            @Parameter(schema = @Schema(type = "string", allowableValues = { "On Role",
                    "Contract" })) @RequestParam String memberType) {
        return dashboardService.roleBasedTimesheetDetails(page, size, search, value, memberType);
    }

    // for widget set
    @Operation(summary = "widgets data set to all")
    @GetMapping("/widget-set-to-employee")
    public ResponseEntity<?> widgetSetToEmployee( @Parameter(schema = @Schema(type = "Boolean", allowableValues = { "True",
            "False" }))@RequestParam(required = false) boolean status,@RequestParam(required = false) String employeeId) {
        return dashboardService.widgetSetToEmployee(status,employeeId);
    }

    @Operation(summary = "Get dashboard sequence list for particular user")
    @GetMapping("/dashboard-sequence-list")
    public ResponseEntity<?> dashboardSequenceList() {
        return dashboardService.dashboardSequenceList();
    }

    @Operation(summary = "Get the Total timesheet entry days based on month")
    @GetMapping("/timesheet-entry-days-count-per-month")
    public ResponseEntity<?> timeSheetDaysCountPerMonth(@RequestParam(required = true) int month,
            @RequestParam(required = true) int year) {
        return dashboardService.timeSheetDaysCountPerMonth(month, year);
    }

    @Operation(summary = "Flow Access count for Head")
    @GetMapping("/flow-access-count-head")
    public ResponseEntity<?> flowAccessCountHead() {
        return dashboardService.flowAccessCountHead();
    }

    @Operation(summary = "Dashboard sequence updation for particular user")
    @GetMapping("/dashboard-sequence-update")
    public ResponseEntity<?> dashboardSequenceUpdate(@RequestParam String empId,
            @Parameter(schema = @Schema(type = "string", allowableValues = { "widgetCount",
                    "widgetTable" })) @RequestParam String replacementType,
            @RequestParam String replaceFrom, @RequestParam String replaceTo) {
        return dashboardService.dashboardSequenceUpdate(empId, replacementType, replaceFrom, replaceTo);
    }


    @Operation(summary = "Dashboard sequence updation List  for particular user")
    @PutMapping("/dashboard-sequence-update")
    public ResponseEntity<?> dashboardSequenceUpdateList(@RequestBody DashboardUpdateRequest dashboardUpdateRequestList,@RequestParam(value = "key",required = false) String key) {
        return dashboardService.dashboardSequenceUpdateList(dashboardUpdateRequestList,key);
    }


    @Operation(summary = "Dashboard contract user and Supervisor List")
    @GetMapping("/dashboard-contract-user-supervisor-list")
    public ResponseEntity<?> dashboardContractUserAndSupervisorList() {
        return dashboardService.dashboardContractUserAndSupervisorList();
    }


    @Operation(summary = "Dashboard weekly based report")
    @GetMapping("/dashboard-weekly-based-timeline")
    public ResponseEntity<?> dashboardWeeklyBasedTimeline(@RequestParam(required = false) LocalDate fromdate,@RequestParam(required = false) LocalDate todate) {
        return dashboardService.dashboardWeeklyBasedTimeline(fromdate,todate);
    }


    @Operation(summary = "Dashboard Default page update")
    @PutMapping("/dashboard-default-page-update")
    public ResponseEntity<?> dashboardDefaultPageUpdate(@RequestBody List<DashboardSetDefault> dashboardSetDefaults) {
        try {
            // Process each DashboardSetDefault object
            dashboardSetDefaults.forEach(DashboardSetDefault::addDoubleQuotes);

            // Call service method to handle further processing
            return dashboardService.dashboardDefaultPageUpdate(dashboardSetDefaults);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error occurred: " + e.getMessage());
        }
    }

    @Operation(summary = "Dashboard for Supervisor Month Wise datas")
    @GetMapping("/membersCount")
ResponseEntity<ApiResponsePageable>userBasedMonthdata(@RequestParam(value = "month")String month, @RequestParam(value = "year")String year, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size){
        return dashboardService.userBasedMonthdata(month,year,page,size);
    }

    @Operation(summary = "Update A Dashboard Data a Card or Table ")
    @PostMapping("/update-dashboard-data")
    public ResponseEntity<?> updateDashboardData(@RequestParam String desigination ,@RequestParam String replacementType,@RequestParam String value ){
        return dashboardService.dashboardAddDashboard(desigination,replacementType,value);
    }

}
