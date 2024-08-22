package pm.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pm.request.DashboardSetDefault;
import pm.request.DashboardUpdateRequest;
import pm.response.ApiResponse;
import pm.response.ApiResponsePageable;

import java.time.LocalDate;
import java.util.List;

@Service
public interface DashboardService {

    ResponseEntity<?> designationMemberCount();

    ResponseEntity<?> memberStatusCount();

    ResponseEntity<?> flowsStatusWiseCount();

    ResponseEntity<?> designationCount();

    ResponseEntity<?> businessCategoryCount();

    ResponseEntity<?> taskGroupCount();

    ResponseEntity<?> teamMemberCount();

    ResponseEntity<?> teamMemberActivityStatus();

    ResponseEntity<?> teamMemberSecondLevelActivity();

    ResponseEntity<?> submittedActivityStatusCount();

    ResponseEntity<?> secLevelSubmittedActivityStatusCount();

    ResponseEntity<?> productStatusWiseCount();

    ResponseEntity<?> assignedToMemberCount();

    ResponseEntity<?> assignedUnassignedProductCount();

    ResponseEntity<?> approverStatusWiseProductCount();

    ResponseEntity<?> myTimesheetDetails(int filterVaule, LocalDate date);

    ResponseEntity<?> roleBasedTimesheetDetails(int page, int size, boolean search, String vaule, String memberType);

    ResponseEntity<?> widgetSetToEmployee(boolean status,String employeeId);

    ResponseEntity<?> dashboardSequenceList();

    ResponseEntity<?> timeSheetDaysCountPerMonth(int month, int year);

    ResponseEntity<?> flowAccessCountHead();

    ResponseEntity<?> dashboardSequenceUpdate(String empId, String replacementType, String replaceFrom,
            String replaceTo);


    ResponseEntity<?> dashboardSequenceUpdateList(DashboardUpdateRequest dashboardUpdateRequestList,String key);


    ResponseEntity<?> dashboardContractUserAndSupervisorList();

    ResponseEntity<?> dashboardWeeklyBasedTimeline(LocalDate fromdate,LocalDate todate);

    ResponseEntity<?> masterCategoryCount();

    ResponseEntity<?> dashboardDefaultPageUpdate(List<DashboardSetDefault> dashboardDefaultPageRequest);

    ResponseEntity<ApiResponsePageable> userBasedMonthdata(String month, String year, int page, int size);

    ResponseEntity<?> dashboardAddDashboard(String desigination, String replacementType,String value);
}
