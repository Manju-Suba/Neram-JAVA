package pm.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pm.model.task.TaskActivity;
import pm.request.ActivityRequest;
import pm.request.CommonTaskActivityRequest;
import pm.request.CommonTaskDraft;
import pm.request.TaskActivityRequest;
import pm.response.ApiResponse;

@Service
public interface TaskTimeSheetService {

        ResponseEntity<?> getDraft(LocalDate date, String status);

        ResponseEntity<?> updateTaskActivity(int id, TaskActivity taskActivity);

        ResponseEntity<?> getTaskList(String date);

        ResponseEntity<?> taskApprovedList(LocalDate date);

        ResponseEntity<?> createMemberActivity(CommonTaskActivityRequest commontaskActivity);

        ResponseEntity<?> getCommonTimeSheet(LocalDate date);

        ResponseEntity<?> getCommonTimeSheetDateRangebyProduct(LocalDate fromdate, LocalDate todate);

        ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(LocalDate fromdate, LocalDate todate,
                        Integer prodId);

        ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(Integer userid, LocalDate fromdate,
                        LocalDate todate,
                        Integer prodId);
        // ResponseEntity<?> getCommonTimeSheetDateRange(LocalDate fromdate,LocalDate
        // todate);
        // ResponseEntity<?> getCommonTimeSheetDateRangebyuser(LocalDate
        // fromdate,LocalDate todate,int userid);

        ResponseEntity<?> getCommonTimeSheetUser(LocalDate date);

        ResponseEntity<?> updateCommonTaskActivities(List<CommonTaskDraft> taskActivities, String draft);

        ResponseEntity<?> updateActivity(ActivityRequest activityRequest);

        ResponseEntity<?> getDraftsubmit(Boolean status, LocalDate date);

        ResponseEntity<?> getApprovedorRejectList(LocalDate date, String status);

        ResponseEntity<?> updateTaskActivityDraft(TaskActivityRequest taskActivity);

        ResponseEntity<?> getCommonTimeSheetbyProductOwner();

        ResponseEntity<?> getTaskListByFinalApprove(LocalDate date);

        ResponseEntity<?> getCommonTimeSheetFinal(LocalDate date);

        ResponseEntity<?> getCommonTimeSheetFinalall();

        ResponseEntity<?> getCommonTimeSheetFinalall(int page, int size, String category, int productId, String status,
                        int memberId, LocalDate startDate, LocalDate endDate);

        ResponseEntity<?> updateFinalApproved(ActivityRequest taskActivity, String type);

        ResponseEntity<?> getTaskListdata();

        ResponseEntity<?> getApprovedorRejectListdata(LocalDate date, String status);

        ResponseEntity<?> getApprovedorRejectListall(String status);

        ResponseEntity<?> getCommonApprovedorRejectListall(String status);

        ResponseEntity<?> getCommonApprovedorRejectListall(int page, int size, String category, int productId,
                        int memberId,
                        String status);

        ResponseEntity<?> getOwnerCommonApprovedorRejectListall();

        ResponseEntity<?> getCommonTimeSheet();

        ResponseEntity<?> getCommonTimeSheet(int page,
                        int size, String category, int productId, int memberId, LocalDate startDate, LocalDate endDate,
                        String status);

        ResponseEntity<?> productownerapproval(ActivityRequest activityRequest);

        ResponseEntity<?> getCommonTimeSheetFinalallbyApproved();

        ResponseEntity<?> getCommonTimeSheetFinalallbyApproved(int page, int size, String category, int productId,
                        String status, int memberId, LocalDate date);

        ResponseEntity<?> viewActivity(int id);

        ResponseEntity<?> updateActivity(CommonTaskDraft taskActivity, int id);

        ResponseEntity<?> updateActivity(List<CommonTaskDraft> taskActivity, int id);

        String getEmailByDraft();

        ResponseEntity<?> deleteActivity(List<Integer> idList);

        ResponseEntity<?> getFinalApproveddaterangeProductandMember(LocalDate fromdate, LocalDate todate);

        ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproduct(LocalDate fromdate, LocalDate todate,
                        Integer productid);

        ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproductByUser(LocalDate fromdate, LocalDate todate,
                        Integer productid, Integer userid);

        ResponseEntity<?> getCommonTimeSheetbyProductOwnerList();

        // ResponseEntity<?> productownerapprovaldaterange(LocalDate fromdate,LocalDate
        // todate);

        ResponseEntity<?> productownerapprovaldaterangeandprodId(LocalDate fromdate, LocalDate todate,
                        Integer productid,
                        Integer userid);

        ResponseEntity<?> getDraftsubmitDetail(Boolean status, LocalDate date);

        ResponseEntity<?> getSumbittedActivity(int page, int size, LocalDate date,String approverType, String status, boolean filter);

        ResponseEntity<?> getCommonTimeSheetbyProductOwnerList(int page, int size, String category, int productId,
                        int memberId, LocalDate startDate, LocalDate endDate);

        ResponseEntity<?> getOwnerCommonApprovedorRejectListall(int page, int size, String category, int productId,
                        int memberId, String status, LocalDate date);

        ResponseEntity<ApiResponse> idBasedCommonTaskActivityList(int id);

        ResponseEntity<?> deleteTimesheet(List<Integer> ids);

        ResponseEntity<?> deleteTimesheetdaterange(int id, LocalDate fromdate, LocalDate todate);

        ResponseEntity<?> insertActivityRequest(int id,  LocalDate fromDate, LocalDate toDate, int sender);

        ResponseEntity<ApiResponse> notEnteredTimesheetMoreThanTwoDays();
}
