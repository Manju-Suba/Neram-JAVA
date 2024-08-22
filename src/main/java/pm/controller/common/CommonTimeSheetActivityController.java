package pm.controller.common;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import pm.model.task.CommonTimeSheetActivity;
import pm.request.ActivityRequest;
import pm.request.CommonTaskActivityRequest;
import pm.request.CommonTaskDraft;
import pm.request.TaskActivityRequest;
import pm.response.ApiResponse;
import pm.service.EmailService;
import pm.service.TaskTimeSheetService;
import pm.utils.AuthUserData;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("/common/timesheet")
@CrossOrigin("*")
@RestController
public class CommonTimeSheetActivityController {
	@Autowired
	private TaskTimeSheetService taskTimeSheeService;



	// ==========================================================================Timesheet
	// Create
	// =======================================================================
	@Operation(summary = "Create a timesheet activity   !================================For Mobile========================")
	@PostMapping("/activity")
	public ResponseEntity<?> createMemberActivity(CommonTaskActivityRequest taskActivity) {
		return taskTimeSheeService.createMemberActivity(taskActivity);
	}

	// ================================================================================Draft
	// Delete
	// Method====================================================================
	@Operation(summary = "Delete draft lists   !================================For Mobile========================")
	@DeleteMapping("/activity/draft/{ids}")
	public ResponseEntity<?> deleteActivity(@PathVariable List<String> ids) {
		List<Integer> idList = ids.stream().map(Integer::parseInt).collect(Collectors.toList());
		return taskTimeSheeService.deleteActivity(idList);
	}

	// ================================================================User Id Based
	// Task Activity Submitted
	// result============================================================
	// @Operation(summary = "User Id & Date Based Task Activity Submitted")
	// @GetMapping("/activity/userlist/{date}")
	// public ResponseEntity<?> getCommonTimeSheetUser(@PathVariable LocalDate date)
	// {
	// return taskTimeSheeService.getCommonTimeSheetUser(date);
	// }

	// ==========================================================================Supervisor
	// Id Based Time sheet
	// Details================================================================
	@Operation(summary = "Supervisor Id & Date Based Members Activity Details")
	@GetMapping("/activity/superviserlist/{date}")
	public ResponseEntity<?> getCommonTimeSheetsupervisor(@PathVariable LocalDate date) {
		return taskTimeSheeService.getCommonTimeSheet(date);
	}

	/// ===================================================================== get
	/// date based on Product List Filter and datas with timesheet
	/// data============================================================
	@Operation(summary = "Supervisor Id & from Date and To date Based Members Activity Details")
	@GetMapping("/activity/superviserlist/prouctlist/{fromdate}/{todate}")
	public ResponseEntity<?> getCommonTimeSheetDateRangebyProduct(@PathVariable LocalDate fromdate,
																  @PathVariable LocalDate todate) {
		return taskTimeSheeService.getCommonTimeSheetDateRangebyProduct(fromdate, todate);
	}

	/// ======================================================================== get
	/// date based & Product Id Product List Filter and
	/// datas=============================================================
	@Operation(summary = "Supervisor Id & from Date and To date,Product Id  Based Members Activity Details")
	@GetMapping("/activity/superviserlist/prouct/{fromdate}/{todate}/{prodId}")
	public ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(@PathVariable LocalDate fromdate,
																		@PathVariable LocalDate todate, @PathVariable Integer prodId) {
		return taskTimeSheeService.getCommonTimeSheetDateRangebyProductidlist(fromdate, todate, prodId);
	}

	/// ===============================================================================
	/// get date based & Product Id & User Id Based Product List Filter and
	/// datas=======================================
	@Operation(summary = " from Date and To date,Product Id,User Id   Based Members Activity Details")
	@GetMapping("/activity/superviserlist/prouct/{fromdate}/{todate}/{prodId}/{userid}")
	public ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(@PathVariable LocalDate fromdate,
																		@PathVariable LocalDate todate, @PathVariable Integer prodId, @PathVariable Integer userid) {
		return taskTimeSheeService.getCommonTimeSheetDateRangebyProductidlist(userid, fromdate, todate, prodId);
	}

	// ///===========================================================================
	// get date based on Product List Filter and datas without timesheet
	// datas=========================================================
	// @Operation(summary = "Get the list of users", hidden = true)
	// @GetMapping("/activity/superviserlist/daterange/{fromdate}/{todate}")
	// public ResponseEntity<?> getCommonTimeSheetsuperDateRange(@PathVariable
	// LocalDate fromdate,
	// @PathVariable LocalDate todate) {
	// return taskTimeSheeService.getCommonTimeSheetDateRange(fromdate, todate);
	// }
	// ///====================================================================================
	// get date based on Product List and UserId based Filter and datas without
	// timesheet datas
	// @Operation(summary = "Get the list of users", hidden = true)
	// @GetMapping("/activity/superviserlist/daterangeuser/{userid}/{fromdate}/{todate}")
	// public ResponseEntity<?> getCommonTimeSheetsuperDateRangeuser(@PathVariable
	// int userid,
	// @PathVariable LocalDate fromdate, @PathVariable LocalDate todate) {
	// return taskTimeSheeService.getCommonTimeSheetDateRangebyuser(fromdate,
	// todate, userid);
	// }

	// ===============================================================================================Members
	// Activity Supervisor Based All Data
	// List================================================F
//	@Operation(summary = "Get All Members Activity List Based On Supervisor Token ")
//	@GetMapping("/activity/superviserlistall")
//	public ResponseEntity<?> getCommonTimeSheetsuperviser() {
//		return taskTimeSheeService.getCommonTimeSheet();
//	}


	@Operation(summary = "Get All Members Activity List Based On Supervisor Token ")
	@GetMapping("/activity/superviserlistall")
	public ResponseEntity<?> getCommonTimeSheetsuperviser(@RequestParam(defaultValue = "0") int page,
														  @RequestParam(defaultValue = "10") int size, @Parameter(description = "Category", schema = @Schema(type = "string", allowableValues = {"all", "default", "product", "member", "date", "dateandproduct", "dateandmember", "memberandproduct", "approveddefault", "approveddefaultdate", "approvedmember", "approvedall", "approvedproduct"})) @RequestParam String category, @RequestParam(required = false, defaultValue = "0") int productId, @RequestParam(required = false, defaultValue = "0") int memberId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate, @RequestParam(required = false) String status) {
		if ("product".equals(category) && productId == 0) {
			return ResponseEntity.badRequest().body("Product ID is required for category 'product'");
		} else if ("date".equals(category) && (startDate == null || endDate == null)) {
			return ResponseEntity.badRequest().body("Start date and end date are required for category 'date'");
		} else if ("member".equals(category) && memberId == 0) {
			return ResponseEntity.badRequest().body("Member ID is required for category 'member'");
		} else if ("all".equals(category) && (startDate == null || endDate == null || memberId == 0 || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID ,and member ID are required for category 'all'");
		} else if ("dateandmember".equals(category) && (startDate == null || endDate == null || memberId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and member ID are required for category 'dateandmember'");
		} else if ("dateandproduct".equals(category) && (startDate == null || endDate == null || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and product ID are required for category 'dateandproduct'");
		} else if ("memberandproduct".equals(category) && (productId == 0 || memberId == 0)) {
			return ResponseEntity.badRequest().body("Product ID and member ID are required for category 'memberandproduct'");
		}
		return taskTimeSheeService.getCommonTimeSheet(page, size, category, productId, memberId, startDate, endDate, status);
	}

	// ===========================================================================================
	// Date Based Final Approval List Members
	// Activity==================================================
	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/activity/{date}/final-approve-list")
	public ResponseEntity<?> getCommonTimeSheetFinal(@PathVariable LocalDate date) {
		return taskTimeSheeService.getCommonTimeSheetFinal(date);
	}

	// ==============================================================================================
	// Showing All Data Final Approval Members
	// // Activity===========================================
//	@Operation(summary = "Get All Members Activity List Based On Final Approval Token ")
//	@GetMapping("/activity/final-approve-list")
//	public ResponseEntity<?> getCommonTimeSheetFinalall() {
//		return taskTimeSheeService.getCommonTimeSheetFinalall();
//	}


	@Operation(summary = "Get All Members Activity List Based On Final Approval Token ")
	@GetMapping("/activity/final-approve-list")
	public ResponseEntity<?> getCommonTimeSheetFinalall(@RequestParam(defaultValue = "0") int page,
														@RequestParam(defaultValue = "10") int size, @Parameter(description = "Category", schema = @Schema(type = "string", allowableValues = {"all", "default", "product", "member", "status", "date", "dateandproduct", "dateandmember", "dateandstatus", "productandstatus", "memberandstatus", "memberandproduct", "dateandproductandmember", "dateandproductandstatus", "productandmemberandstatus", "dateandmemberandstatus"})) @RequestParam String category,
														@RequestParam(required = false, defaultValue = "0") int productId, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int memberId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
		if ("product".equals(category) && productId == 0) {
			return ResponseEntity.badRequest().body("Product ID is required for category 'product'");
		} else if ("date".equals(category) && (startDate == null || endDate == null)) {
			return ResponseEntity.badRequest().body("Start date and end date are required for category 'date'");
		} else if ("member".equals(category) && memberId == 0) {
			return ResponseEntity.badRequest().body("Member ID is required for category 'member'");
		} else if ("all".equals(category) && (startDate == null || endDate == null || memberId == 0 || productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID ,member ID and status are required for category 'all'");
		} else if ("dateandmember".equals(category) && (startDate == null || endDate == null || memberId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and member ID are required for category 'dateandmember'");
		} else if ("dateandproduct".equals(category) && (startDate == null || endDate == null || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and product ID are required for category 'dateandproduct'");
		} else if ("dateandstatus".equals(category) && (startDate == null || endDate == null || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, and Status are required for category 'dateandstatus'");
		} else if ("memberandproduct".equals(category) && (productId == 0 || memberId == 0)) {
			return ResponseEntity.badRequest().body("Product ID and member ID are required for category 'memberandproduct'");
		} else if ("productandstatus".equals(category) && (productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Product ID and Status are required for category 'productandstatus'");
		} else if ("memberandstatus".equals(category) && (memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("member ID and Status are required for category 'memberandstatus'");
		} else if ("dateandproductandmember".equals(category) && (startDate == null || endDate == null || memberId == 0 || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID and member ID are required for category 'dateandproductandmember'");
		} else if ("dateandproductandstatus".equals(category) && (startDate == null || endDate == null || productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID and status are required for category 'dateandproductandstatus'");
		} else if ("productandmemberandstatus".equals(category) && (productId == 0 || memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Product ID , member ID and status are required for category 'productandmemberandstatus'");
		} else if ("dateandmemberandstatus".equals(category) && (startDate == null || endDate == null || memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, member ID and status are required for category 'dateandmemberandstatus'");
		}

		return taskTimeSheeService.getCommonTimeSheetFinalall(page, size, category, productId, status, memberId, startDate, endDate);
	}


	// ================================================================================================
	// Showing Date Range Based on Final Approval Datas
	// ===========================================
	@Operation(summary = "Get Date Range Based on Final Approval Members Activity List  ")
	@GetMapping("/activity/final-approve-list/daterange/{fromdate}/{todate}")
	public ResponseEntity<?> getCommonTimeSheetFinalalldataapproveddaterange(@PathVariable LocalDate fromdate,
																			 @PathVariable LocalDate todate) {
		return taskTimeSheeService.getFinalApproveddaterangeProductandMember(fromdate, todate);
	}

	// ====================================================================================
	// Showing Date Range Based and Product Id Based on Final Approval Datas
	// ================================
	@Operation(summary = "Get Date Range & product Id Based on Final Approval Members Activity List  ")
	@GetMapping("/activity/final-approve-list/daterangeuser/{fromdate}/{todate}/{productid}")
	public ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproduct(@PathVariable LocalDate fromdate,
																			   @PathVariable LocalDate todate, @PathVariable Integer productid) {
		return taskTimeSheeService.getCommonTimeSheetFinalApproveddaterangebyproduct(fromdate, todate, productid);
	}
	// ==========================================================================
	// Showing Date Range Based and Product Id Based and User Id based on Final
	// Approval Datas ==========================

	@Operation(summary = "Get Date Range & product Id & User Id Based on Final Approval Members Activity List  ")
	@GetMapping("/activity/final-approve-list/daterangeuser/{fromdate}/{todate}/{productid}/{userid}")
	public ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproduct(@PathVariable LocalDate fromdate,
																			   @PathVariable LocalDate todate, @PathVariable Integer productid, @PathVariable Integer userid) {
		return taskTimeSheeService.getCommonTimeSheetFinalApproveddaterangebyproductByUser(fromdate, todate, productid,
				userid);
	}

	// ===============================================================================================Final
	// Approval List Showing
	// Apis============================================================
//	@GetMapping("/activity/final-approve-list/approved")
//	public ResponseEntity<?> getCommonTimeSheetFinalalldataapproved() {
//		return taskTimeSheeService.getCommonTimeSheetFinalallbyApproved();
//	}

	//infine scroll
	@GetMapping("/activity/final-approve-list/approved")
	public ResponseEntity<?> getCommonTimeSheetFinalalldataapproved(@RequestParam(defaultValue = "0") int page,
																	@RequestParam(defaultValue = "10") int size, @Parameter(description = "Category", schema = @Schema(type = "string", allowableValues = {"all", "default", "product", "member", "status", "date", "dateandproduct", "dateandmember", "dateandstatus", "productandstatus", "memberandstatus", "memberandproduct", "dateandproductandmember", "dateandproductandstatus", "productandmemberandstatus", "dateandmemberandstatus"})) @RequestParam String category,
																	@RequestParam(required = false, defaultValue = "0") int productId, @RequestParam(required = false) String status, @RequestParam(defaultValue = "0") int memberId, @RequestParam(required = false) LocalDate date) {

		if ("product".equals(category) && productId == 0) {
			return ResponseEntity.badRequest().body("Product ID is required for category 'product'");
		} else if ("date".equals(category) && (date == null)) {
			return ResponseEntity.badRequest().body("Start date and end date are required for category 'date'");
		} else if ("member".equals(category) && memberId == 0) {
			return ResponseEntity.badRequest().body("Member ID is required for category 'member'");
		} else if ("all".equals(category) && (date == null || memberId == 0 || productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID ,member ID and status are required for category 'all'");
		} else if ("dateandmember".equals(category) && (date == null || memberId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and member ID are required for category 'dateandmember'");
		} else if ("dateandproduct".equals(category) && (date == null || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and product ID are required for category 'dateandproduct'");
		} else if ("dateandstatus".equals(category) && (date == null || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, and Status are required for category 'dateandstatus'");
		} else if ("memberandproduct".equals(category) && (productId == 0 || memberId == 0)) {
			return ResponseEntity.badRequest().body("Product ID and member ID are required for category 'memberandproduct'");
		} else if ("productandstatus".equals(category) && (productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Product ID and Status are required for category 'productandstatus'");
		} else if ("memberandstatus".equals(category) && (memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("member ID and Status are required for category 'memberandstatus'");
		} else if ("dateandproductandmember".equals(category) && (date == null || memberId == 0 || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID and member ID are required for category 'dateandproductandmember'");
		} else if ("dateandproductandstatus".equals(category) && (date == null || productId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID and status are required for category 'dateandproductandstatus'");
		} else if ("productandmemberandstatus".equals(category) && (productId == 0 || memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Product ID , member ID and status are required for category 'productandmemberandstatus'");
		} else if ("dateandmemberandstatus".equals(category) && (date == null || memberId == 0 || status == "")) {
			return ResponseEntity.badRequest().body("Start date, end date, member ID and status are required for category 'dateandmemberandstatus'");
		}

		return taskTimeSheeService.getCommonTimeSheetFinalallbyApproved(page, size, category, productId, status, memberId, date);
	}


	// ========================================================================================Timesheet
	// Activity Upadte List in Draft
	// ===========================================================
	@Operation(summary = "Timesheet Activity Upadte List in Draft !================================For Mobile========================")
	@PutMapping("/activity/update")
	public ResponseEntity<?> updateCommonTaskActivity(@RequestBody List<CommonTaskDraft> taskActivity,@RequestParam(value = "draft",required = false)String draft) {
		return taskTimeSheeService.updateCommonTaskActivities(taskActivity,draft);
	}

	// =======================================================================================Supervisor
	// Activity Approval or Reject for
	// Supervisor================================================================
	@Operation(summary = "Supervisor Activity Approval or Reject")
	@PutMapping("/approval/update")
	public ResponseEntity<?> updateApproval(ActivityRequest taskActivity) {
		return taskTimeSheeService.updateActivity(taskActivity);
	}

	@Operation(summary = "Final Approver Memebers Activity Approval or Reject")
	@PutMapping("/approval/{type}/final-approved")
	public ResponseEntity<?> updateFinalApproved(ActivityRequest taskActivity, @PathVariable String type) {
		return taskTimeSheeService.updateFinalApproved(taskActivity, type);
	}

	// =========================================================================================
	// Showing Draft List
	// APis=================================================================
	@Operation(summary = "Get Draft List Datas Api  !================================For Mobile========================")
	@GetMapping("/list/status/{status}")
	public ResponseEntity<?> getApprovalList(@PathVariable boolean status,
											 @RequestParam(required = false) LocalDate date) {
		return taskTimeSheeService.getDraftsubmit(status, date);
	}

	@GetMapping("/list/status/date/{status}/{date}")
	public ResponseEntity<?> getDraftList(@PathVariable boolean status, @PathVariable LocalDate date) {
		return taskTimeSheeService.getDraftsubmitDetail(status, date);
	}

	// =======================================================================================Common
	// Task Activity Log Show Apis
	// =========================================================
	@Operation(summary = "Logs For Common Task Activityes ", hidden = true)
	@GetMapping("/approvallist/{date}/status/{status}")
	public ResponseEntity<?> getApprovalListorReject(@PathVariable LocalDate date, @PathVariable String status) {
		return taskTimeSheeService.getApprovedorRejectList(date, status);
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/activity/product/list")
	public ResponseEntity<?> getCommonTimeSheetProductowner() {
		return taskTimeSheeService.getCommonTimeSheetbyProductOwner();
	}

	@Operation(summary = "Get the list of members activity approval reject list ")
	@GetMapping("/approval/status/{status}")
	public ResponseEntity<?> getApprovedorRejectList(@PathVariable String status) {
		return taskTimeSheeService.getCommonApprovedorRejectListall(status);
	}

	@Operation(summary = "Get the list of users")
	@GetMapping("/approval/status")
	public ResponseEntity<?> getApprovedorRejectList(@RequestParam(defaultValue = "0") int page,
													 @RequestParam(defaultValue = "10") int size, @RequestParam String category,
													 @RequestParam(required = false) int productId, @RequestParam(required = false) int memberId,
													 @RequestParam String status) {
		return taskTimeSheeService.getCommonApprovedorRejectListall(page, size, category, productId, memberId, status);
	}

//	@Operation(summary = "Get the list of users for OWner Approval")
//	@GetMapping("/approval/owner/status")
//	public ResponseEntity<?> getApprovedorRejectListOwner() {
//		return taskTimeSheeService.getOwnerCommonApprovedorRejectListall();
//	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/activity/edit/{id}")
	public ResponseEntity<?> viewActivity(@PathVariable int id) {
		return taskTimeSheeService.viewActivity(id);
	}

	// @Operation(summary = "User Activity Rejected Updated")
	// @PutMapping("/activity/update/{id}")
	// public ResponseEntity<?> updateActivity(@RequestBody CommonTaskDraft taskActivity, @PathVariable int id) {
	// 	return taskTimeSheeService.updateActivity(taskActivity, id);
	// }

	@Operation(summary = "User Activity Rejected Updated")
	@PutMapping("/activity/update/{id}")
	public ResponseEntity<?> updateActivity(@RequestBody List<CommonTaskDraft> taskActivity, @PathVariable int id) {
		return taskTimeSheeService.updateActivity(taskActivity, id);
	}

	// ========================================================================================Product
	// Owner Based
	// List=========================================================================================
//	@Operation(summary = "Based on Products Owner View the Members Activity !!! New Implementaion")
//	@GetMapping("/activity/owner/list")
//	public ResponseEntity<?> getCommonTimeSheetUser() {
//		return taskTimeSheeService.getCommonTimeSheetbyProductOwnerList();
//	}

	// =====================================================================================================owner
	// Member Activity Approval or
	// Reject==================================================
	@Operation(summary = "Owner Memebers Activity Approval or Rejec")
	@PutMapping("/approval/owner/update")
	public ResponseEntity<?> ownerApproval(ActivityRequest taskActivity) {
		return taskTimeSheeService.productownerapproval(taskActivity);
	}
	// ========================================================================================================Owner
	// Members Activity Date Range Approval or
	// Reject==================================================
	// @Operation(summary = "Owner Members Activity Date Range Approval or Reject")
	// @GetMapping("/activity/ownerlist/daterange/{fromdate}/{todate}")
	// public ResponseEntity<?> ownerApprovaldaterange(@PathVariable LocalDate
	// fromdate, @PathVariable LocalDate todate) {
	// return taskTimeSheeService.productownerapprovaldaterange(fromdate, todate);
	// }

	@Operation(summary = "Owner Members Activity Date Range  Approval or Reject")
	@GetMapping("/activity/ownerlist/daterange/all")
	public ResponseEntity<?> ownerApprovaldaterange(@RequestParam LocalDate fromdate, @RequestParam LocalDate todate,
													@RequestParam(required = false) Integer productid, @RequestParam(required = false) Integer userid) {

		return taskTimeSheeService.productownerapprovaldaterangeandprodId(fromdate, todate, productid, userid);
	}

	// mail

	// @Scheduled(cron = "0 0 17 * * ?")
	// @Scheduled(cron = "0 * * * * ?")
	@Operation(summary = "Get the list of users", hidden = true)
	@PostMapping("/email")
	public String sendEmail() {
		// emailService.sendEmail("emayavarman.e@hepl.com", "test", "test");
		return taskTimeSheeService.getEmailByDraft();

	}
	// @GetMapping(value = "/pdf",produces = MediaType.APPLICATION_PDF_VALUE)
	// public void employeeDetailsReport(HttpServletResponse response) throws
	// IOException {
	//
	// DateFormat dateFormat = new SimpleDateFormat("YYYY-MM-DD:HH:MM:SS");
	// String fileType = "attachment; filename=employee_details_" +
	// dateFormat.format(new Date()) + ".pdf";
	// response.setHeader("Content-Disposition", fileType);
	//
	// pdfGenerator.employeeDetailReport(response);
	// }

	@Operation(summary = "User Id & Date  Based  Task Activity  Submitted")
	@GetMapping("/activity/submit-list")
	public ResponseEntity<?> getSumbittedActivity(@RequestParam(defaultValue = "0") int page,
												  @RequestParam(defaultValue = "10") int size,
												  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
												  @RequestParam(required = false) String approverType,
												  @RequestParam(required = false) String status,
												  @RequestParam(defaultValue = "false", required = true) boolean filter) {
		return taskTimeSheeService.getSumbittedActivity(page, size, date, approverType, status, filter);
	}


	@Operation(summary = "Based on Products Owner View the Members Activity !!! New Implementaion")
	@GetMapping("/activity/owner/list")
	public ResponseEntity<?> getCommonTimeSheetUser(@RequestParam(defaultValue = "0") int page,
													@RequestParam(defaultValue = "10") int size, @Parameter(description = "Category",
			schema = @Schema(type = "string", allowableValues = {"all", "default", "product", "member", "date", "dateandproduct", "dateandmember", "memberandproduct"})) @RequestParam String category, @RequestParam(required = false, defaultValue = "0") int productId, @RequestParam(required = false, defaultValue = "0") int memberId, @RequestParam(required = false) LocalDate startDate, @RequestParam(required = false) LocalDate endDate) {
		if ("product".equals(category) && productId == 0) {
			return ResponseEntity.badRequest().body("Product ID is required for category 'product'");
		} else if ("date".equals(category) && (startDate == null || endDate == null)) {
			return ResponseEntity.badRequest().body("Start date and end date are required for category 'date'");
		} else if ("member".equals(category) && memberId == 0) {
			return ResponseEntity.badRequest().body("Member ID is required for category 'member'");
		} else if ("all".equals(category) && (startDate == null || endDate == null || memberId == 0 || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, product ID ,and member ID are required for category 'all'");
		} else if ("dateandmember".equals(category) && (startDate == null || endDate == null || memberId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and member ID are required for category 'dateandmember'");
		} else if ("dateandproduct".equals(category) && (startDate == null || endDate == null || productId == 0)) {
			return ResponseEntity.badRequest().body("Start date, end date, and product ID are required for category 'dateandproduct'");
		} else if ("memberandproduct".equals(category) && (productId == 0 || memberId == 0)) {
			return ResponseEntity.badRequest().body("Product ID and member ID are required for category 'memberandproduct'");
		}
		return taskTimeSheeService.getCommonTimeSheetbyProductOwnerList(page, size, category, productId, memberId, startDate, endDate);
	}

	@Operation(summary = "Get the list of users for OWner Approval")
	@GetMapping("/approval/owner/status")
	public ResponseEntity<?> getApprovedorRejectListOwner(@RequestParam(defaultValue = "0") int page,
														  @RequestParam(defaultValue = "10") int size, @Parameter(description = "Category",
			schema = @Schema(type = "string", allowableValues = {"default", "all", "product", "member", "date", "dateandproduct", "dateandmember", "memberandproduct"})) @RequestParam(required = true) String category, @RequestParam(required = false, defaultValue = "0") int productId, @RequestParam(required = false, defaultValue = "0") int memberId, @RequestParam(required = false) LocalDate date, @Parameter(description = "Status",
			schema = @Schema(type = "string", allowableValues = {"all", "approved", "rejected"})) @RequestParam String status) {
		if ("product".equals(category) && productId == 0) {
			return ResponseEntity.badRequest().body("Product ID is required for category 'product'");
		} else if ("date".equals(category) && (date == null)) {
			return ResponseEntity.badRequest().body("Date  are required for category 'date'");
		} else if ("member".equals(category) && memberId == 0) {
			return ResponseEntity.badRequest().body("Member ID is required for category 'member'");
		} else if ("all".equals(category) && (date == null || memberId == 0 || productId == 0)) {
			return ResponseEntity.badRequest().body("Date,  product ID ,and member ID are required for category 'all'");
		} else if ("dateandmember".equals(category) && (date == null || memberId == 0)) {
			return ResponseEntity.badRequest().body("Date, end date, and member ID are required for category 'dateandmember'");
		} else if ("dateandproduct".equals(category) && (date == null || productId == 0)) {
			return ResponseEntity.badRequest().body("Date,  and product ID are required for category 'dateandproduct'");
		} else if ("memberandproduct".equals(category) && (productId == 0 || memberId == 0)) {
			return ResponseEntity.badRequest().body("Product ID and member ID are required for category 'memberandproduct'");
		}
		return taskTimeSheeService.getOwnerCommonApprovedorRejectListall(page, size, category, productId, memberId, status, date);
	}


	//Id Based Common Task Activity List

	@Operation(summary = "Get the id based Common Task Activity List")
	@GetMapping("taskActivitydata")
	public ResponseEntity<ApiResponse>idBasedCommonTaskActivityList(@RequestParam int id){
		return taskTimeSheeService.idBasedCommonTaskActivityList(id);
	}
	
	@Operation(summary = "Get the persons details who are not entered timesheet for more than two days")
	@GetMapping("notEnteredTimesheet-MoreThanTwoDays")
	public ResponseEntity<ApiResponse>notEnteredTimesheetMoreThanTwoDays( ){
		return taskTimeSheeService.notEnteredTimesheetMoreThanTwoDays();
	}  

}