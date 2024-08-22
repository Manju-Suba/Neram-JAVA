package pm.serviceImplements;

import java.security.Key;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.config.TimeConfig;
import pm.dto.ApprovalResponse;
import pm.dto.AttendanceSheetDto;
import pm.dto.RaisedRequestApproval;
import pm.model.activityrequest.ActivityRequest;
import pm.model.attendanceSheet.AttendanceSheet;
import pm.model.product.EProductApproStatus;
import pm.model.product.Product;
import pm.model.task.CommonTimeSheetActivity;
import pm.model.users.Users;
import pm.repository.ActivityRequestRepository;
import pm.repository.AttendanceSheetRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.Login_historyRepository;
import pm.repository.ProdApprovalHistoryRepository;
import pm.repository.TaskActivityRepository;
import pm.repository.TeamLeaderApprovalRepository;
import pm.repository.UsersRepository;
import pm.request.CommonTaskDraft;
import pm.request.RaisedRequest;
import pm.response.ApiResponse;
import pm.response.CommonTaskActivityResponse;
import pm.service.ActivityRequestService;
import pm.service.EmailService;
import pm.utils.AuthUserData;

@Service
public class ActivityRequestImpl implements ActivityRequestService {
    @Autowired
    private ActivityRequestRepository activityRequestRepository;

    @Autowired
    private TimeConfig timeConfig;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ProdApprovalHistoryRepository prodApprovalHistoryRepository;

    @Autowired
    private TeamLeaderApprovalRepository teamLeaderApprovalRepository;

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    @Autowired
    private Login_historyRepository loginHistoryRepository;

    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;
    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;

    @Autowired
    private EmailService emailService;

    @Value("${myapp.customProperty}")
    private String portalUrl;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public ResponseEntity<?> sendRequest(RaisedRequest raisedRequest) {

        if (raisedRequest.getRequestDate().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse(true, "Please Enter the Request Date", null));
        }

        // if (!raisedRequest.getReason().matches(".*[a-zA-Z].*")) {
        // return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        // .body(new ApiResponse(true, "Please enter a reason containing letters",
        // null));
        // }
        if (!raisedRequest.getReason().matches(".*[\\s\\S]*[a-zA-Z]+[\\s\\S]*")) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(new ApiResponse(true, "Please enter a reason containing letters", null));
        }

        LocalDateTime currDateTime = LocalDateTime.now();
        int userId = AuthUserData.getUserId();
        String localDates = raisedRequest.getRequestDate();
        List<LocalDate> removedDates = new ArrayList<>();
        // for (String date : localDates) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(localDates, formatter);
        List<String> statuses = Arrays.asList("Pending", "Approved");
        ActivityRequest count = activityRequestRepository.findbyUserIdAndRequestDateAndStatus(userId, localDate,
                statuses);
        Users user = usersRepository.findById(userId).orElse(null);

        if (count == null) {
            ActivityRequest newActivityRequest = new ActivityRequest();
            newActivityRequest.setUserId(userId);
            newActivityRequest.setRequestDate(localDate);
            newActivityRequest.setCreatedat(currDateTime);
            newActivityRequest.setUpdatedat(currDateTime);
            newActivityRequest.setIsdeleted(false);
            newActivityRequest.setStatus(EProductApproStatus.Pending);
            newActivityRequest.setReason(raisedRequest.getReason());

            if (user != null) {
                newActivityRequest.setSendedTo(usersRepository.findByEmpidGetUserId(user.getSupervisor()));
                activityRequestRepository.save(newActivityRequest);
            }
        }

        String message;
        if (count == null) {
            Optional<Users> supervisorEmail = usersRepository
                    .findById(usersRepository.findByEmpidGetUserId(user.getSupervisor()));
            message = "Requests Raised successfully.";
            String htmlContent = "<div>"
                    + "<p style='padding-left:5px'>"
                    + "Your team member, <b>" + user.getName() + "</b>  has raised a request for (<b>" + localDate +
                    "</b>) to fill out the timesheet. We kindly request you to review the details by clicking on the \"View Details\" link provided below:"
                    + "</p>"
                    + "<p style='text-align: center;'><a href='" + portalUrl +
                    "#/members-activity' style='color: #007bff; text-decoration: none;font-weight:bold'>"
                    + "View Details"
                    + "</a></p>"
                    + "</div>";

            emailService.sendEmail(
                    supervisorEmail.get().getEmail(), "Raise request from " + user.getName(),
                    htmlContent);

        } else {
            message = "The following dates are already raised: " + localDates;
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, null));
    }

    // public ResponseEntity<?> getRaisedRequestByUser() {
    // int userId = AuthUserData.getUserId();
    // List<ActivityRequest> activityRequests =
    // activityRequestRepository.findbyUserId(userId);
    // activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());
    // List<Map<String, Object>> mappedRequests = new ArrayList<>();

    // for (ActivityRequest activityRequest : activityRequests) {
    // Map<String, Object> mappedRequest = new HashMap<>();
    // mappedRequest.put("id", activityRequest.getId());
    // mappedRequest.put("remarks", activityRequest.getRemarks());
    // mappedRequest.put("status", activityRequest.getStatus());
    // mappedRequest.put("reason", activityRequest.getReason());
    // mappedRequest.put("requestDate", activityRequest.getRequestDate());
    // Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
    // mappedRequest.put("teamName", user.get().getName());

    // mappedRequests.add(mappedRequest);
    // }
    // String message = "Request Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, mappedRequests));
    // }

    public ResponseEntity<?> getRaisedRequestByUser(int page, int size, boolean filter, LocalDate date, String status) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        Page<ActivityRequest> activityRequests = null;

        int userId = AuthUserData.getUserId();
        // List<ActivityRequest> activityRequests =
        // activityRequestRepository.findbyUserId(userId);
        // activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());

        if (filter) {
            if (date != null && status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbyUserIdAndRequestDateAndStatusAndPageable(userId,
                        date, status, pageable);
            } else if (date != null) {
                activityRequests = activityRequestRepository.findbyUserIdAndRequestDate(userId, date, pageable);
            } else if (status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbyUserIdAndStatus(userId, status, pageable);
            }
        } else {
            activityRequests = activityRequestRepository.findbyUserId(userId, pageable);
        }

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("reason", activityRequest.getReason());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, mappedRequests));
    }

    public ResponseEntity<?> getRaisedRequestToSupervisor(LocalDate date) {
        int userId = AuthUserData.getUserId();
        List<ActivityRequest> activityRequests = activityRequestRepository.findbySendedToAndRequestDate(userId, date);
        activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        // mappedRequests.sort(Comparator.comparing(request -> (Long) request.get("id"),
        // Comparator.reverseOrder()));
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, mappedRequests));
    }

    public ResponseEntity<?> getRaisedRequestToSupervisor(int page, int size, boolean filter, LocalDate date,
            String status, int userid) {

        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        Page<ActivityRequest> activityRequests = null;

        int userId = AuthUserData.getUserId();
        if (filter) {

            if (date != null && status != null && !status.isEmpty() && userid != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndUserIdAndStatusAndPageable(
                        userId, date, userid, status, pageable);

            } else if (date != null && userid != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndUserIdAndPageable(userId,
                        date, userid, pageable);

            } else if (userid != 0 && status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndUserIdAndStatusAndPageable(userId, userid,
                        status, pageable);

            } else if (date != null && status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndStatusAndPageable(userId,
                        date, status, pageable);

            } else if (date != null) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndPageable(userId, date,
                        pageable);

            } else if (status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndStatusAndPageable(userId, status,
                        pageable);

            } else if (userid != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndUserIdAndPageable(userId, userid,
                        pageable);
            }
        } else {
            activityRequests = activityRequestRepository.findbySendedToAndPageable(userId, pageable);
        }

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("reason", activityRequest.getReason());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        // mappedRequests.sort(Comparator.comparing(request -> (Long) request.get("id"),
        // Comparator.reverseOrder()));
        long totalCount = activityRequests.getTotalElements();
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("dataList", mappedRequests);
        responseData.put("totalCount", totalCount);
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, responseData));
    }

    public ResponseEntity<?> getRaisedRequestToSupervisor() {
        int userId = AuthUserData.getUserId();
        List<ActivityRequest> activityRequests = activityRequestRepository.findbySendedToAndRequestDate(userId);
        activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("reason", activityRequest.getReason());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        // mappedRequests.sort(Comparator.comparing(request -> (Long) request.get("id"),
        // Comparator.reverseOrder()));
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, mappedRequests));
    }

    public ResponseEntity<?> getRaisedRequestToSupervisorByStatus() {
        int userId = AuthUserData.getUserId();
        List<ActivityRequest> activityRequests = activityRequestRepository.findbySendedToAndStatus(userId);
        activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("reason", activityRequest.getReason());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        // mappedRequests.sort(Comparator.comparing(request -> (Long) request.get("id"),
        // Comparator.reverseOrder()));
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, mappedRequests));
    }

    public ResponseEntity<?> getRaisedRequestToSupervisorByStatus(int page, int size, boolean filter, LocalDate date,
            String status, int memberId) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        Page<ActivityRequest> activityRequests = null;

        int userId = AuthUserData.getUserId();
        // List<ActivityRequest> activityRequests =
        // activityRequestRepository.findbySendedToAndStatus(userId);
        // activityRequests.sort(Comparator.comparing(ActivityRequest::getId).reversed());
        List<String> statuses = Arrays.asList("Rejected", "Approved");
        if (filter) {

            if (date != null && status != null && !status.isEmpty() && memberId != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndUserIdAndStatusAndPageable(
                        userId, date, memberId, status, pageable);

            } else if (date != null && memberId != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndUserIdAndPageableAndStatus(
                        userId, date, memberId, pageable, statuses);

            } else if (memberId != 0 && status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndUserIdAndStatusAndPageable(userId,
                        memberId, status, pageable);

            } else if (date != null && status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndStatusAndPageable(userId,
                        date, status, pageable);

            } else if (date != null) {
                activityRequests = activityRequestRepository.findbySendedToAndRequestDateAndPageableAndStatus(userId,
                        date, pageable, statuses);

            } else if (status != null && !status.isEmpty()) {
                activityRequests = activityRequestRepository.findbySendedToAndStatusAndPageable(userId, status,
                        pageable);

            } else if (memberId != 0) {
                activityRequests = activityRequestRepository.findbySendedToAndUserIdAndPageableAndStatus(userId,
                        memberId, pageable, statuses);
            }
        } else {
            activityRequests = activityRequestRepository.findbySendedToAndPageableAndStatus(userId, statuses, pageable);
        }

        List<Map<String, Object>> mappedRequests = new ArrayList<>();

        for (ActivityRequest activityRequest : activityRequests) {
            Map<String, Object> mappedRequest = new HashMap<>();
            mappedRequest.put("id", activityRequest.getId());
            mappedRequest.put("remarks", activityRequest.getRemarks());
            mappedRequest.put("reason", activityRequest.getReason());
            mappedRequest.put("status", activityRequest.getStatus());
            mappedRequest.put("requestDate", activityRequest.getRequestDate());
            Optional<Users> user = usersRepository.findById(activityRequest.getUserId());
            mappedRequest.put("teamName", user.get().getName());

            mappedRequests.add(mappedRequest);
        }
        // mappedRequests.sort(Comparator.comparing(request -> (Long) request.get("id"),
        // Comparator.reverseOrder()));
        String message = "Request Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, mappedRequests));
    }

    @Override
    public ResponseEntity<?> getRaisedRequestStatus(LocalDate date) {
        int user_id = AuthUserData.getUserId();
        ActivityRequest teamLeaderApproval = activityRequestRepository.findbyUserIdAndRequestDate(user_id,
                date);

        Long count = taskActivityRepository.countByUserIdAndActivityDate(user_id, date);
        if (teamLeaderApproval != null) {
            ApprovalResponse response = new ApprovalResponse(count, teamLeaderApproval.getStatus());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Request Fetched Successfully.", response));
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(false, "No Data Found", Collections.emptyList()));
    }

    @Override
    public ResponseEntity<?> approveRequest(RaisedRequest raisedRequest, int id) {
        try {
            ActivityRequest request = activityRequestRepository.findById(id).orElse(null);
            EProductApproStatus approvalStatus;
            if ("Approved".equals(raisedRequest.getStatus())) {
                approvalStatus = EProductApproStatus.Approved;
            } else {
                approvalStatus = EProductApproStatus.Rejected;
            }
            request.setStatus(approvalStatus);
            LocalDateTime date = LocalDateTime.now();
            request.setUpdatedat(date);
            request.setRemarks(raisedRequest.getRemarks());
            ActivityRequest activity = activityRequestRepository.save(request);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, raisedRequest.getStatus() + " the Request successfully", activity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Some error occurs", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> createAttendanceSheetByDate(LocalDate date, String status) {
        LocalDateTime localdate = LocalDateTime.now();

        if (attendanceSheetRepository.existsByUseridAndAppliedDate(AuthUserData.getUserId(), date)) {
            AttendanceSheet sheet = attendanceSheetRepository.findByUseridAndAppliedDate(AuthUserData.getUserId(),
                    date);
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body(new ApiResponse(true, "Already Exist This Data", sheet));
        }
        int userId = AuthUserData.getUserId();
        Users user = usersRepository.findById(userId).orElse(null);
        AttendanceSheet attendancedata = attendanceSheetRepository
                .save(new AttendanceSheet(userId, status, date, localdate, localdate));
        if (status.equalsIgnoreCase("leave")) {
            Optional<Users> supervisorEmail = usersRepository
                    .findById(usersRepository.findByEmpidGetUserId(user.getSupervisor()));
            String htmlContent = "<div>"
                    + "<p style='padding-left:5px'>"
                    + "Your team member, <b>" + user.getName()
                    + "</b>  has submitted a leave application through the \"NERAM\" system."
                    + "</p>"
                    + "<p style='text-align: center;'><a href='" + portalUrl +
                    "#/members-activity' style='color: #007bff; text-decoration: none;font-weight: bold'>"
                    + "View Details"
                    + "</a></p>"
                    + "</div>";
            emailService.sendEmail(supervisorEmail.get().getEmail(), user.getName() + " applied Leave on Neram tool",
                    htmlContent);
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "ok Created", attendancedata));
    }

    @Override
    public ResponseEntity<?> approveRequests(RaisedRequest raisedRequest, List<Integer> ids) {
        try {
            List<ActivityRequest> updatedRequests = new ArrayList<>();
            for (int id : ids) {
                // Fetch the request for the given ID
                Optional<ActivityRequest> optionalRequest = activityRequestRepository.findById(id);
                if (optionalRequest.isPresent()) {
                    ActivityRequest request = optionalRequest.get();
                    // Update the status and timestamp
                    EProductApproStatus approvalStatus = ("Approved".equalsIgnoreCase(raisedRequest.getStatus()))
                            ? EProductApproStatus.Approved
                            : EProductApproStatus.Rejected;
                    request.setStatus(approvalStatus);
                    LocalDateTime date = LocalDateTime.now();
                    request.setUpdatedat(date);
                    request.setRemarks(raisedRequest.getRemarks());
                    // Save the updated request
                    ActivityRequest updatedRequest = activityRequestRepository.save(request);
                    updatedRequests.add(updatedRequest);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "No request found for ID: " + id, Collections.emptyList()));
                }
            }
            executorService.execute(() -> raisedRequestApproval(ids, raisedRequest));

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Requests Updated Successfully", updatedRequests));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error occurs", Collections.emptyList()));
        }
    }

    private void raisedRequestApproval(List<Integer> ids, RaisedRequest raisedRequest) {
        for (Integer taskid : ids) {
            Optional<ActivityRequest> optionalRequest = activityRequestRepository.findById(taskid);
            Optional<Users> users = usersRepository.findById(optionalRequest.get().getUserId());
            String body = "<b>" + users.get().getName() + "</b> your timesheet Request for <b>"
                    + optionalRequest.get().getRequestDate() +
                    "</b>  has been approved by the supervisor."
                    + "<p style='text-align: center;'><a href='" + portalUrl +
                    "#/timesheet' style='color: #007bff; text-decoration: none;font-weight:bold'>"
                    + "View Details"
                    + "</a></p>";

            emailService.sendEmail(users.get().getEmail(), "Raised Request was " + raisedRequest.getStatus(), body);
        }
    }

    // @Override
    // public ResponseEntity<?> getTimeSheetByStatus() {
    // Integer userId = AuthUserData.getUserId();
    // LocalDate todate = LocalDate.now();
    // LocalDate fromdate = todate.minusDays(30);
    // List<LocalDate> dateRanges = getDatesInRange(fromdate, todate);

    // Map<LocalDate, String> timeSheetData = new LinkedHashMap<>(); // Using
    // LinkedHashMap to maintain insertion order

    // for (LocalDate dateRange : dateRanges) {
    // CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
    // .findUserIdAndActivityDatesbyloop(userId, dateRange);
    // AttendanceSheet attendanceSheet =
    // attendanceSheetRepository.findByUseridAndAppliedDate(userId, dateRange);

    // if (userIdAndDateList != null) {
    // timeSheetData.put(dateRange, "entered");
    // } else if (attendanceSheet != null &&
    // attendanceSheet.getStatus().equalsIgnoreCase("leave")) {

    // timeSheetData.put(dateRange, "leave");
    // } else {
    // timeSheetData.put(dateRange, "not entered");
    // }
    // }

    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Time sheet data fetched successfully",
    // timeSheetData));
    // }

    // private List<LocalDate> getDatesInRange(LocalDate fromDate, LocalDate toDate)
    // {
    // List<LocalDate> datesInRange = new ArrayList<>();
    // LocalDate currentDate = fromDate;
    // while (!currentDate.isAfter(toDate)) {
    // datesInRange.add(currentDate);
    // currentDate = currentDate.plusDays(1);
    // }
    // return datesInRange;
    // }

    // new code
    @Override
    public ResponseEntity<?> getTimeSheetByStatus(int month, int year) {
        int userId = AuthUserData.getUserId();
        LocalTime dynamicTime = timeConfig.getComparisonTime();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<Date> submittedList = activityRequestRepository.findActivityDatesForUserSubmitted(userId, startDate,
                endDate);
        List<Date> attendanceList = activityRequestRepository.findAttendanceDatesByUserId(userId, startDate, endDate);
        List<Date> raisedRequestList = activityRequestRepository.findRaisedRequestDatesByUserId(userId, startDate,
                endDate);
        List<Date> approvedRequestList = activityRequestRepository.findRaisedRequestApprovedDatesByUserId(userId,
                startDate,
                endDate);
        List<Date> pendingRequestList = activityRequestRepository.findRaisedRequestPendingDatesByUserId(userId, startDate, endDate);

        List<Date> filteredRaisedList = new ArrayList<>(raisedRequestList);
        List<Date> filteredApprovedRaisedList = new ArrayList<>(approvedRequestList);
        List<Date> filteredPendingRaisedList = new ArrayList<>(pendingRequestList);

        Set<Date> submittedSet = new HashSet<>(submittedList);

        filteredRaisedList.removeIf(submittedSet::contains);
        filteredPendingRaisedList.removeIf(submittedSet::contains);
        filteredApprovedRaisedList.removeIf(submittedSet::contains);
        filteredApprovedRaisedList.removeIf(raisedRequestList::contains);
        filteredApprovedRaisedList.removeIf(attendanceList::contains);
        LocalTime currentTime = LocalTime.now(); // Current time

        List<Date> datesInRange = new ArrayList<>();

        // Check if today is Monday
        // String dateStr = "27-07-2024"; // Specific date in dd-MM-yyyy format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate currentDate = LocalDate.now(); // Current date
        // LocalDate currentDate = LocalDate.parse(dateStr, formatter);
        if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            if (currentTime.isBefore(dynamicTime)) {
                // If it's Monday and before 4 PM, include Friday, Saturday, Sunday from the
                // previous week
                LocalDate previousSunday = currentDate.minusDays(1);
                LocalDate previousSaturday = currentDate.minusDays(2);
                LocalDate previousFriday = currentDate.minusDays(3);

                // Add dates to datesInRange
                datesInRange.add(java.sql.Date.valueOf(previousSunday));
                datesInRange.add(java.sql.Date.valueOf(previousSaturday));
                datesInRange.add(java.sql.Date.valueOf(previousFriday));
            }
            // Always include today's date
            datesInRange.add(java.sql.Date.valueOf(currentDate));
        } else {
            // If today is not Monday
            if (currentTime.isBefore(dynamicTime)) {
                // If it's before 4 PM, include yesterday's date
                LocalDate yesterday = currentDate.minusDays(1);
                datesInRange.add(java.sql.Date.valueOf(yesterday));
            }
            // Always include today's date
            datesInRange.add(java.sql.Date.valueOf(currentDate));
        }
        datesInRange.removeAll(attendanceList);

        Map<String, List<Date>> timeSheetData = new HashMap<>();
        timeSheetData.put("SubmittedList", submittedList);
        timeSheetData.put("AttendanceList", attendanceList);
        timeSheetData.put("RaisedRequestList", filteredRaisedList);
        timeSheetData.put("Permission", datesInRange);
        timeSheetData.put("ApprovedRequestList", filteredApprovedRaisedList);
        timeSheetData.put("PendingRequestList", filteredPendingRaisedList);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Time sheet data fetched successfully", timeSheetData));
    }

    private List<LocalDate> getDatesInRange(LocalDate fromDate, LocalDate toDate) {
        List<LocalDate> datesInRange = new ArrayList<>();
        LocalDate currentDate = fromDate;
        while (!currentDate.isAfter(toDate)) {
            datesInRange.add(currentDate);
            currentDate = currentDate.plusDays(1);
        }
        return datesInRange;
    }

    @Override
    public ResponseEntity<?> getAttendanceSheetByUser(LocalDate date, int page, int size) {
        // Check if the date parameter is null
        if (date != null) {
            // If date is present, fetch records for that specific date with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("applied_date").descending());
            Page<AttendanceSheet> attendanceSheetPage = attendanceSheetRepository
                    .findByUseridAndAppliedDatewithpagination(AuthUserData.getUserId(), date, pageable);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Records for the specified date", attendanceSheetPage));
        } else {
            // If date is null, fetch all records for the user with pagination
            Pageable pageable = PageRequest.of(page, size, Sort.by("applied_date").descending());
            Page<AttendanceSheet> attendanceSheetPage = attendanceSheetRepository
                    .findAllByUserid(AuthUserData.getUserId(), pageable);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "All records for the user", attendanceSheetPage));
        }
    }

    @Override
    public ResponseEntity<?> getAttendanceSheetBySupervisor(LocalDate date, int page, int size, Integer userid) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("applied_date").descending());
        Page<Object[]> attendanceSheetPage = null;
        List<AttendanceSheetDto> dtos = new ArrayList<>();
        Page<AttendanceSheetDto> pageOfDtos = null;
        if (date != null && userid != null) {

            // If both date and userid are present
            attendanceSheetPage = attendanceSheetRepository
                    .findByUseridAndAppliedDateforSupervisor(AuthUserData.getEmpid(), userid, date, pageable);
        } else if (userid != null) {

            // If only userid is present
            attendanceSheetPage = attendanceSheetRepository
                    .findByUseridAndAppliedDateforUserandDate(AuthUserData.getEmpid(), userid, pageable);
        } else if (date != null) {

            // If only date is present
            attendanceSheetPage = attendanceSheetRepository
                    .findByUseridAndAppliedDateforSupervisorWithDate(AuthUserData.getEmpid(), date, pageable);
        } else {
            // No userid or date provided
            attendanceSheetPage = attendanceSheetRepository
                    .findByUseridAndAppliedDateforSupervisor(AuthUserData.getEmpid(), pageable);
        }

        // Populate dtos
        for (Object[] row : attendanceSheetPage.getContent()) {
            java.sql.Date sqlDate = (java.sql.Date) row[0];
            String userName = (String) row[1]; // Assuming name is the second column
            String status = (String) row[2]; // Assuming status is the third column

            AttendanceSheetDto dto = new AttendanceSheetDto(sqlDate, userName, status);
            dtos.add(dto);
        }

        // Create PageImpl
        pageOfDtos = new PageImpl<>(dtos, attendanceSheetPage.getPageable(), attendanceSheetPage.getTotalElements());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Records found", pageOfDtos));

    }

    @Override
    public ResponseEntity<?> deleteAttendance(Integer id) {

        attendanceSheetRepository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Attendance deleted successfully", null));
    }

    @Override
    public ResponseEntity<?> getSelfActivity(String month, String year, int id) {

        try {
            int monthe = Integer.parseInt(month);
            int yeare = Integer.parseInt(year);

            YearMonth yearMonth = YearMonth.of(yeare, monthe);

            LocalDate firstDateOfMonth = yearMonth.atDay(1);
            LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
            if (lastDateOfMonth.isAfter(LocalDate.now())) {
                lastDateOfMonth = LocalDate.now();
            }
            List<LocalDate> allDates = new ArrayList<>();
            LocalDate current = firstDateOfMonth;

            while (!current.isAfter(lastDateOfMonth)) {
                if (current.isAfter(LocalDate.now())) {
                    break; // Stop adding dates if current date is today or in the future
                }
                allDates.add(current);
                current = current.plusDays(1);
            }
            List<Map<String, Object>> dataMapList = new ArrayList<>();

            for (LocalDate date : allDates) {
                Map<String, Object> data = new HashMap<>();
                Long pendingCount = commonTimeSheetActivityRepository.countByUserIdAndStatusandDate(id, "Pending",
                        date);
                Long approvalCount = commonTimeSheetActivityRepository.countByUserIdAndStatusandDate(id, "Approved",
                        date);
                Long rejectedCount = commonTimeSheetActivityRepository.countByUserIdAndStatusandDate(id, "Reject",
                        date);
                Long raisedRequestCount = activityRequestRepository.getRaisedRequestCount(date, id);
                Long leaveCount = attendanceSheetRepository.countByUseridandDate(date, id);
                Long notEnteredCount = commonTimeSheetActivityRepository.findYesterdayTimesheetRecord(id, date) > 0 ? 0L
                        : 1L;
                if (raisedRequestCount > 0 || leaveCount > 0) {
                    notEnteredCount = 0L;
                }
                // Long leaveCount = attendanceSheetRepository.countByUseridandDate(date, id);

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDate = date.format(formatter);

                data.put("date", formattedDate);
                data.put("approved", approvalCount);
                data.put("rejected", rejectedCount);
                data.put("pending", pendingCount);
                data.put("notEntered", notEnteredCount);
                data.put("leave", leaveCount);
                data.put("raisedrequest", raisedRequestCount);
                dataMapList.add(data);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Data Fetched Successfully", dataMapList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @Override
    public ResponseEntity<?> getRaisedRequestByUserDate(LocalDate date, int id) {
        List<Object[]> results = activityRequestRepository.findRaisedRequestByUserIdAndDate(id, date);

        if (results.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "No requests found", null), HttpStatus.NOT_FOUND);
        }

        RaisedRequestApproval raisedRequestApproval = mapToRaisedRequestApproval(results.get(0));

        return new ResponseEntity<>(new ApiResponse(true, "Request found", raisedRequestApproval), HttpStatus.OK);
    }

    private RaisedRequestApproval mapToRaisedRequestApproval(Object[] result) {
        RaisedRequestApproval raisedRequestApproval = new RaisedRequestApproval();
        raisedRequestApproval.setId((Integer) result[0]);
        raisedRequestApproval.setActivity_date(convertToDateTime(result[1]).toLocalDate());
        raisedRequestApproval.setStatus((String) result[2]);
        raisedRequestApproval.setDescription((String) result[3]);
        raisedRequestApproval.setUserName((String) result[4]);
        return raisedRequestApproval;
    }

    public ResponseEntity<?> getAttendanceRequestByUserDate(LocalDate date, int id) {
        List<Object[]> results = attendanceSheetRepository.findAttendanceRequestByUserIdAndDate(id, date);

        if (results.isEmpty()) {
            return new ResponseEntity<>(new ApiResponse(false, "No requests found", null), HttpStatus.NOT_FOUND);
        }

        RaisedRequestApproval attendanceRequestApproval = mapToRaisedRequestApproval(results.get(0));

        return new ResponseEntity<>(new ApiResponse(true, "Request found", attendanceRequestApproval), HttpStatus.OK);
    }

    private LocalDateTime convertToDateTime(Object dateObject) {
        if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toLocalDateTime();
        } else if (dateObject instanceof java.sql.Date) {
            return ((java.sql.Date) dateObject).toLocalDate().atStartOfDay();
        } else {
            throw new IllegalArgumentException("Unsupported date type");
        }
    }

    // FOR TEMPORARY API CALL FOR RAISED REQUEST
    // ********************************************************

    @Override
    public ResponseEntity<?> raisedRequestForAll() {

        LocalDateTime currDateTime = LocalDateTime.now();
        String localDates = "2024-08-12";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(localDates, formatter);

        List<Users> users = usersRepository.getAll();

        for (Users user1 : users) {

            ActivityRequest newActivityRequest = new ActivityRequest();
            newActivityRequest.setUserId(user1.getId());
            newActivityRequest.setRequestDate(localDate);
            newActivityRequest.setCreatedat(currDateTime);
            newActivityRequest.setUpdatedat(currDateTime);
            newActivityRequest.setIsdeleted(false);
            newActivityRequest.setStatus(EProductApproStatus.Approved);
            newActivityRequest.setReason("Technical Issue");
            newActivityRequest.setSendedTo(usersRepository.findByEmpidGetUserId(user1.getSupervisor()));
            activityRequestRepository.save(newActivityRequest);
        }

        String message = "Created Successfully..!";

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, null));
    }

    @Override
    public ResponseEntity<?> getDataBasedOnID(int id) {
        Optional<ActivityRequest> activityData = activityRequestRepository.findById(id);
        return ResponseEntity.ok().body(new ApiResponse(true, "", activityData));
    }
    

    @Override
    public ResponseEntity<?> updateRaisedRequest(int id, LocalDate date, String reason) {

        try {
            List<ActivityRequest> updatedActivities = new ArrayList<>();
            Optional<ActivityRequest> requestActivityOptional = activityRequestRepository.findById(id);
            if (requestActivityOptional.isPresent()) {
                ActivityRequest activityRequest = requestActivityOptional.get();
                activityRequest.setReason(reason);
                activityRequest.setRequestDate(date);
                activityRequest.setRemarks("");
                activityRequest.setStatus(EProductApproStatus.Pending);
                ActivityRequest updatedActivity = activityRequestRepository.save(activityRequest);
                updatedActivities.add(updatedActivity);
            }

            if (updatedActivities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "No updates performed", Collections.emptyList()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Request Resubmitted successfully", Collections.emptyList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Something went wrong", e.getMessage()));
        }  
    }

}
