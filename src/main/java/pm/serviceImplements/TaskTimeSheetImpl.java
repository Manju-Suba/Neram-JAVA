package pm.serviceImplements;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import ch.qos.logback.core.net.SyslogOutputStream;
import jakarta.transaction.Transactional;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException.NotFound;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pm.config.TimeConfig;
import pm.dto.OwnerApprovalDto;
import pm.dto.ProductNameDTO;
import pm.dto.SubmitActivityList;
import pm.dto.TaskActivityResponse;
import pm.dto.TaskActivityResponseDTO;
import pm.dto.TaskActivityResponseList;
import pm.dto.UserTaskActivityResponse;
import pm.exception.ProductNameAlreadyExistsException;
import pm.model.product.EProductApproStatus;
import pm.model.product.Product;
import pm.model.task.ActivityLog;
import pm.model.task.CommonTaskActivityLog;
import pm.model.task.CommonTimeSheetActivity;
import pm.model.task.TaskActivity;
import pm.model.taskapproval.FinalApprovalLog;
import pm.model.taskapproval.OwnerApprovalLog;
import pm.model.users.Users;
import pm.payload.TimeSheetPayload;
import pm.repository.*;
import pm.request.ActivityRequest;
import pm.request.CommonTaskActivityRequest;
import pm.request.CommonTaskDraft;
import pm.request.TaskActivityRequest;
import pm.response.*;
import pm.service.EmailService;
import pm.service.TaskTimeSheetService;
import pm.utils.AuthUserData;

@Service
public class TaskTimeSheetImpl implements TaskTimeSheetService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private TaskActivityRepository taskActivityRepository;

    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;

    @Autowired
    private FinalApprovalLogRepository finalApprovalLogRepository;

    @Autowired
    private CommonTaskActivityLogRepository commonTaskActivityLogRepository;

    @Autowired
    OwnerApprovalLogRepository ownerApprovalLogRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    private UsersRepository usersRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ActivityRequestImpl activityRequestImpl;

    @Autowired
    private ActivityRequestRepository activityRequestRepository;

    @Value("${myapp.customProperty}")
    private String portalUrl;
    @Autowired
    private TimeConfig timeConfig;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public ResponseEntity<?> getDraft(LocalDate date, String status) {
        boolean draft = "Drafts".equalsIgnoreCase(status);
        int id = AuthUserData.getUserId();
        Optional<Users> user = usersRepository.findById(id);
        List<TaskActivity> activityLogs = taskActivityRepository.findByUserAndActivityDateAndDraftNative(date, draft,
                id);
        activityLogs = activityLogs.stream().sorted(Comparator.comparing(TaskActivity::getId).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Fetched Successfully", activityLogs));
    }

    @Override
    public ResponseEntity<?> updateTaskActivity(int id, TaskActivity taskActivity) {
        Optional<TaskActivity> taskActivityOptional = taskActivityRepository.findById(id);
        if (taskActivityOptional.isPresent()) {
            TaskActivity existingTaskActivity = taskActivityOptional.get();
            existingTaskActivity.setActivity_date(taskActivity.getActivity_date());
            existingTaskActivity.setHours(taskActivity.getHours());
            existingTaskActivity.setDescription(taskActivity.getDescription());
            existingTaskActivity.setStatus(taskActivity.getStatus());
            existingTaskActivity.setDraft(taskActivity.isDraft());
            existingTaskActivity.setTask(taskActivity.getTask());
            existingTaskActivity.setUser(taskActivity.getUser());
            existingTaskActivity.setProduct(taskActivity.getProduct());
            taskActivityRepository.save(existingTaskActivity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Updated Successfully", "Task Time sheet data updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Not Found", Collections.emptyList()));
        }
    }

    // @Override
    // public ResponseEntity<?> getTaskList(LocalDate date) {
    // int id = AuthUserData.getUserId();
    // List<Users> user = usersRepository.findBySupervisor(id);
    // List<TaskActivityResponse> activityLogs = new ArrayList<>();
    //
    // for (Users u : user) {
    // List<TaskActivityResponse> userActivityLogs = taskActivityRepository
    // .findTaskDTOsByUserIdAndActivityDate(u.getId(), date)
    // .stream()
    // .map(row -> new TaskActivityResponse(
    // (Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (Integer) row[2],
    // (String) row[3],
    // (String) row[4],
    // (boolean) row[5],
    // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(),
    // (boolean) row[8],
    // (String) row[9],
    // (String) row[10],
    // (String) row[11], (String) row[12], (String) row[13]))
    // .collect(Collectors.toList());
    //
    // // List<TaskActivity> userActivityLogs =
    // // taskActivityRepository.findByUserIdAndActivityDate(u.getId(),
    // // date);
    // activityLogs.addAll(userActivityLogs);
    // }
    // activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogs));
    // }
    //
    // @Override
    // public ResponseEntity<?> getTaskListdata() {
    // int id = AuthUserData.getUserId();
    // List<Users> user = usersRepository.findBySupervisor(id);
    // List<TaskActivityResponse> activityLogs = new ArrayList<>();
    //
    // for (Users u : user) {
    // List<TaskActivityResponse> userActivityLogs = taskActivityRepository
    // .findTaskDTOsByUserId(u.getId())
    // .stream()
    // .map(row -> new TaskActivityResponse(
    // (Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (Integer) row[2],
    // (String) row[3],
    // (String) row[4],
    // (boolean) row[5],
    // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(),
    // (boolean) row[8],
    // (String) row[9],
    // (String) row[10],
    // (String) row[11], (String) row[12], (String) row[13]))
    // .collect(Collectors.toList());
    //
    // // List<TaskActivity> userActivityLogs =
    // // taskActivityRepository.findByUserIdAndActivityDate(u.getId(),
    // // date);
    // activityLogs.addAll(userActivityLogs);
    // }
    // activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogs));
    // }

    @Override
    public ResponseEntity<?> getTaskList(String date) {
        int id = AuthUserData.getUserId();
        List<Users> user = usersRepository.findBySupervisor(usersRepository.findByIdGetUsername(id));
        List<TaskActivityResponse> activityLogs = new ArrayList<>();

        for (Users u : user) {
            List<TaskActivityResponse> userActivityLogs;
            if (date.equalsIgnoreCase("nodate")) {

                userActivityLogs = taskActivityRepository.findTaskDTOsByUserId(u.getId()).stream()
                        .map(row -> new TaskActivityResponse((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                                (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                                ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                                (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                                (String) row[13], null))
                        .collect(Collectors.toList());

            } else {

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate localDate = LocalDate.parse(date, formatter);
                userActivityLogs = taskActivityRepository.findTaskDTOsByUserIdAndActivityDate(u.getId(), localDate)
                        .stream()
                        .map(row -> new TaskActivityResponse((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                                (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                                ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                                (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                                (String) row[13], null))
                        .collect(Collectors.toList());
            }

            activityLogs.addAll(userActivityLogs);
        }
        activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    @Override
    public ResponseEntity<?> getApprovedorRejectListdata(LocalDate date, String status) {
        return null;
    }

    @Override
    public ResponseEntity<?> getApprovedorRejectListall(String status) {
        int id = AuthUserData.getUserId();
        List<TaskApprovalResonse> taskApprovalResonses = new ArrayList<>();
        List<Object[]> userActivityLogs1 = taskActivityRepository.getActivityLogDetailsByCreatedByDetail(id, status);
        for (Object[] row : userActivityLogs1) {
            int user_id = (int) row[11];
            Optional<Users> user = usersRepository.findById(user_id);
            int pro_id = (int) row[14];
            boolean userExists = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
            // Boolean assignedStatus = userExists ? true : false;
            TaskApprovalResonse taskApprovalResponse = new TaskApprovalResonse((Integer) row[0], (String) row[1],
                    convertToDateTime(row[2]).toLocalDate(), (String) row[3], (String) row[4], (String) row[5],
                    (boolean) row[6], ((Timestamp) row[7]).toLocalDateTime(), ((Timestamp) row[8]).toLocalDateTime(),
                    (String) row[9], (String) row[10], (int) row[11], (String) row[12], (String) row[13], (int) row[14],
                    (String) row[15], userExists);
            taskApprovalResonses.add(taskApprovalResponse);
        }
        taskApprovalResonses.sort(Comparator.comparing(TaskApprovalResonse::getId).reversed());
        if (taskApprovalResonses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponse(true, "No Task Time sheet data Found ", Collections.emptyList()));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", taskApprovalResonses));
    }

    @Override
    public ResponseEntity<?> getCommonApprovedorRejectListall(String status) {
        int id = AuthUserData.getUserId();
        List<TaskApprovalResonse> taskApprovalResonses = new ArrayList<>();
        List<Object[]> userActivityLogs1 = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByDetail(id,
                status);
        for (Object[] row : userActivityLogs1) {
            int user_id = (int) row[11];
            Optional<Users> user = usersRepository.findById(user_id);
            int pro_id = (int) row[14];
            boolean userExists = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
            // Boolean assignedStatus = userExists ? true : false;
            TaskApprovalResonse taskApprovalResponse = new TaskApprovalResonse((Integer) row[0], (String) row[1],
                    convertToDateTime(row[2]).toLocalDate(), (String) row[3], (String) row[4], (String) row[5],
                    (boolean) row[6], ((Timestamp) row[7]).toLocalDateTime(), ((Timestamp) row[8]).toLocalDateTime(),
                    (String) row[9], (String) row[10], (int) row[11], (String) row[12], (String) row[13], (int) row[14],
                    (String) row[15], userExists);
            taskApprovalResonses.add(taskApprovalResponse);
        }
        taskApprovalResonses.sort(Comparator.comparing(TaskApprovalResonse::getId).reversed());
        if (taskApprovalResonses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "No data Found ", Collections.emptyList()));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", taskApprovalResonses));
    }

    // Approved Activity
    @Override
    public ResponseEntity<?> getCommonApprovedorRejectListall(int page, int size, String category, int productId,
            int memberId, String status) {
        try {
            Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size, sortByDescId);

            int authUserId = AuthUserData.getUserId();

            category = category.toLowerCase();

            Page<Object[]> activityLogDetailsPage = handleApproved(authUserId, category, productId, memberId, status,
                    pageable);

            List<TaskApprovalMemberResonse> taskApprovalResponses = activityLogDetailsPage.getContent().stream()
                    .map(row -> {

                        int userExists = memberRepository.countByUserIdAndProdId((Integer) row[14], (Integer) row[15]);

                        Boolean assignedStatus = userExists != 0;

                        return new TaskApprovalMemberResonse((Integer) row[0], (String) row[1],
                                convertToDateTime(row[2]).toLocalDate(), (String) row[3], (String) row[4],
                                (String) row[5], (boolean) row[6], ((Timestamp) row[7]).toLocalDateTime(),
                                ((Timestamp) row[8]).toLocalDateTime(), (String) row[9], (String) row[10],
                                (String) row[11], (String) row[12], (String) row[13], assignedStatus);
                    }).collect(Collectors.toList());

            if (taskApprovalResponses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "No data Found ", Collections.emptyList()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", taskApprovalResponses));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred", null));
        }
    }

    private Page<Object[]> handleApproved(int authUserId, String category, int productId, int memberId, String status,
            Pageable pageable) {

        Page<Object[]> product = null;
        switch (category) {

            case "product" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndProduct(authUserId, status,
                        productId, pageable);
                break;
            }

            case "all" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndProductByAndMemberId(
                        authUserId, status, productId, memberId, pageable);
                break;
            }

            case "default" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedBy(authUserId, status,
                        pageable);
                break;
            }

            case "member" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndMemberId(authUserId,
                        status,
                        memberId, pageable);
                break;
            }
            default -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedBy(authUserId, status,
                        pageable);
                break;
            }
        }
        return product;
    }

    @Override
    public ResponseEntity<?> getOwnerCommonApprovedorRejectListall() {
        int id = AuthUserData.getUserId();
        List<TaskApprovalResonse> taskApprovalResonses = new ArrayList<>();
        List<Object[]> userActivityLogs1 = commonTaskActivityLogRepository
                .getActivityLogDetailsByCreatedByOwnerDetail(id);
        for (Object[] row : userActivityLogs1) {
            int user_id = (int) row[11];
            Optional<Users> user = usersRepository.findById(user_id);
            int pro_id = (int) row[14];
            boolean userExists = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
            // Boolean assignedStatus = userExists ? true : false;
            TaskApprovalResonse taskApprovalResponse = new TaskApprovalResonse((Integer) row[0], (String) row[1],
                    convertToDateTime(row[2]).toLocalDate(), (String) row[3], (String) row[4], (String) row[5],
                    (boolean) row[6], ((Timestamp) row[7]).toLocalDateTime(), ((Timestamp) row[8]).toLocalDateTime(),
                    (String) row[9], (String) row[10], (int) row[11], (String) row[12], (String) row[13], (int) row[14],
                    (String) row[15], userExists);
            taskApprovalResonses.add(taskApprovalResponse);
        }
        taskApprovalResonses.sort(Comparator.comparing(TaskApprovalResonse::getId).reversed());
        if (taskApprovalResonses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "No data Found ", Collections.emptyList()));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", taskApprovalResonses));
    }

    @Override
    public ResponseEntity<?> taskApprovedList(LocalDate date) {
        // int id = AuthUserData.getUserId();
        // List<Users> user = usersRepository.findBySupervisor(id);
        // List<TaskActivityResponse> activityLogs = new ArrayList<>();
        //
        // for (Users u : user) {
        // List<TaskActivityResponse> userActivityLogs = taskActivityRepository
        // .findTaskDTOsByUserIdAndActivityDateAndApproved(u.getId(), date)
        // .stream()
        // .map(row -> new TaskActivityResponse(
        // (Integer) row[0],
        // convertToDateTime(row[1]).toLocalDate(),
        // (Integer) row[2],
        // (String) row[3],
        // (String) row[4],
        // (boolean) row[5],
        // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
        // ((java.sql.Timestamp) row[7]).toLocalDateTime(),
        // (boolean) row[8],
        // (String) row[9],
        // (String) row[10],
        // (String) row[11], (String) row[12]))
        // .collect(Collectors.toList());
        //
        //
        //
        // }
        return null;
    }

    @Override
    public ResponseEntity<?> getTaskListByFinalApprove(LocalDate date) {
        int id = AuthUserData.getUserId();
        List<Users> user = usersRepository.findByFinalApprove(usersRepository.findByIdGetUsername(id));
        List<TaskActivityResponse> activityLogs = new ArrayList<>();
        for (Users u : user) {
            List<TaskActivityResponse> userActivityLogs = taskActivityRepository
                    .findTaskDTOsByUserIdAndActivityDateAndFinalApprove(u.getId(), date).stream()
                    .map(row -> new TaskActivityResponse((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                            (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                            ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                            (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                            (String) row[13], null))
                    .collect(Collectors.toList());

            // List<TaskActivity> userActivityLogs =
            // taskActivityRepository.findByUserIdAndActivityDate(u.getId(),
            // date);
            activityLogs.addAll(userActivityLogs);
        }
        activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    private LocalDateTime convertToDateTime(Object dateObject) {
        if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toLocalDateTime();
        } else if (dateObject instanceof Date) {
            return ((Date) dateObject).toLocalDate().atStartOfDay();
        } else {
            throw new IllegalArgumentException("Unsupported date type");
        }
    }

    // // Common Time sheet Member Activity Creation
    @Override
    public ResponseEntity<?> createMemberActivity(CommonTaskActivityRequest commontaskActivity) {
        try {
            List<TimeSheetPayload> taskActivitiesList = commontaskActivity.getCommonTimeSheetActivities();
            int month = 0;
            int year = 0;
            LocalDate activityDate = null;
            int user_id = AuthUserData.getUserId();

            for (TimeSheetPayload commontaskActivitytimecheck : taskActivitiesList) {
                activityDate = commontaskActivitytimecheck.getActivity_date();
                month = activityDate.getMonthValue();
                year = activityDate.getYear();
                break;
            }
            Date javaSqlDate = Date.valueOf(activityDate);
            ResponseEntity<?> responseEntity = activityRequestImpl.getTimeSheetByStatus(month, year);
            List<Date> permissionDates = null;

            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                // Assuming ApiResponse contains the timeSheetData map
                ApiResponse apiResponse = (ApiResponse) responseEntity.getBody();
                System.out.println("apiResponse: " + apiResponse.getData());

                if (apiResponse != null && apiResponse.getData() instanceof Map) {
                    Map<String, Object> timeSheetData = (Map<String, Object>) apiResponse.getData();

                    if (timeSheetData.containsKey("Permission")) {
                        permissionDates = (List<Date>) timeSheetData.get("Permission");

                        // Now you can use permissionDates as needed
                        System.out.println("Permission Dates: " + permissionDates);
                    } else {
                        System.out.println("Permission key not found in timeSheetData");
                    }
                } else {
                    System.out.println("Invalid response format");
                }
            } else {
                System.out.println("Error fetching time sheet data: " + responseEntity.getStatusCode());
            }

            pm.model.activityrequest.ActivityRequest activityRequest = activityRequestRepository
                    .findbyUserIdAndRequestDate(user_id, activityDate);

            boolean isAllowed = (permissionDates != null && permissionDates.contains(javaSqlDate))
                    || (activityRequest != null && activityRequest.getStatus() == EProductApproStatus.Approved);

            if (isAllowed) {

                // Your logic here
                List<CommonTimeSheetActivity> createdActivities = new ArrayList<>();
                if (commontaskActivity.getStatus() != null
                        && commontaskActivity.getStatus().toLowerCase().contains("draft")) {
                    int totalHours = 0;
                    int totalMinutes = 0;
                    boolean hoursCheck = true;
                    String hours = null;

                    for (TimeSheetPayload activity : taskActivitiesList) {

                        if (hoursCheck) {
                            hours = commonTimeSheetActivityRepository.findbyhours(activity.getActivity_date(), user_id);
                            if (hours != null) {
                                String[] hoursSplit = hours.split(":");
                                totalHours = Integer.parseInt(hoursSplit[0]);

                                int minute = Integer.parseInt(hoursSplit[1]);
                                totalMinutes += minute;
                            }
                            hoursCheck = false;

                        }
                        if (hours != null) {
                            String[] newHoursSplit = activity.getHours().split(":");

                            int newHour = Integer.parseInt(newHoursSplit[0]);
                            int newMinute = Integer.parseInt(newHoursSplit[1]);
                            totalHours += newHour;
                            totalMinutes += newMinute;

                        } else {
                            String[] hoursSplit = activity.getHours().split(":");
                            int hour = Integer.parseInt(hoursSplit[0]);
                            int minute = Integer.parseInt(hoursSplit[1]);
                            totalHours += hour;
                            totalMinutes += minute;
                        }
                    }
                    totalHours += totalMinutes / 60; // Convert extra minutes to hours
                    totalMinutes %= 60; // Keep the remaining minutes

                    // Check if total hours exceed 16
                    if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse(false, "Total hours cannot exceed 9:00.", null));
                    }
                }

                if (commontaskActivity.getStatus() != null
                        && !commontaskActivity.getStatus().toLowerCase().contains("draft")) {
                    int totalHours = 0;
                    int totalMinutes = 0;
                    boolean hoursCheck = true;
                    String hours = null;

                    // Check if total hours exceed 16 before calculating
                    for (TimeSheetPayload activity : taskActivitiesList) {

                        if (hoursCheck) {
                            hours = commonTimeSheetActivityRepository.findbyhours(activity.getActivity_date(), user_id);
                            if (hours != null) {
                                String[] hoursSplit = hours.split(":");
                                totalHours = Integer.parseInt(hoursSplit[0]);

                                int minute = Integer.parseInt(hoursSplit[1]);
                                totalMinutes += minute;

                            }
                            hoursCheck = false;

                        }
                        if (hours != null) {
                            String[] newHoursSplit = activity.getHours().split(":");

                            int newHour = Integer.parseInt(newHoursSplit[0]);
                            int newMinute = Integer.parseInt(newHoursSplit[1]);
                            totalHours += newHour;
                            totalMinutes += newMinute;
                            // totalHours += newHour;
                            // totalMinutes += newMinute;
                        } else {
                            String[] hoursSplit = activity.getHours().split(":");
                            int hour = Integer.parseInt(hoursSplit[0]);
                            int minute = Integer.parseInt(hoursSplit[1]);
                            totalHours += hour;
                            totalMinutes += minute;
                        }
                    }
                    totalHours += totalMinutes / 60; // Convert extra minutes to hours
                    totalMinutes %= 60; // Keep the remaining minutes

                    // Check if total hours exceed 16
                    if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse(false, "Total hours cannot exceed 09:00.", null));
                    }
                }

                try {
                    // int user_id = AuthUserData.getUserId();
                    LocalDateTime currDateTime = LocalDateTime.now();
                    Optional<Users> createdByUser = usersRepository.findById(user_id);

                    String contractStatus;
                    if (createdByUser.get().getSupervisor().equals(createdByUser.get().getFinalApprove())) {
                        contractStatus = "finalApproval";
                    } else {
                        contractStatus = null; // This is allowed but ensure it's intentional
                    }

                    for (TimeSheetPayload activitydata : taskActivitiesList) {

                        CommonTimeSheetActivity activity = new CommonTimeSheetActivity();
                        BeanUtils.copyProperties(activitydata, activity);
                        activity.setProduct(productRepository.findById(activitydata.getProduct()).get());
                        activity.setCreated_at(currDateTime);
                        activity.setUpdated_at(currDateTime);
                        activity.set_deleted(false);
                        activity.setUser(createdByUser.get());
                        activity.setDraft(commontaskActivity.getStatus().toLowerCase().contains("draft"));
                        activity.setFinalApprove("Not Yet");
                        activity.setSupervisorStatus("Pending");
                        activity.setOwnerStatus("Pending");
                        activity.setContractStatus(contractStatus);
                        activity.set_approved(false);
                        activity.setBranch(createdByUser.get().getBranch());
                        activity.setSupervisor(createdByUser.get().getSupervisor());
                        CommonTimeSheetActivity exActivity = commonTimeSheetActivityRepository.save(activity);
                        createdActivities.add(exActivity);
                    }

                    String message = (commontaskActivity.getStatus().equalsIgnoreCase("Created"))
                            ? "Activity Submitted Successfully"
                            : "Activity Drafted Successfully";

                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ApiResponse(true, message, Collections.emptyList()));
                } catch (Exception e) {
                    // Log the exception
                    e.printStackTrace();
                    // Return an error response
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)

                            .body(new ApiResponse(false, "An error occurred while processing time sheet activities",
                                    e.getMessage()));
                }
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ApiResponse(false, "Activity Date not allowed : " + activityDate, Collections.emptyList()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ApiResponse(false, "An error occurred while processing time sheet activities", e.getMessage()));
        }
    }

    // delete the draft activity

    @Transactional
    @Override
    public ResponseEntity<?> deleteActivity(List<Integer> idList) {
        List<String> deletedIds = new ArrayList<>();
        List<String> failedToDeleteIds = new ArrayList<>();
        List<String> notFoundIds = new ArrayList<>();

        for (Integer id : idList) {
            try {
                int rowsAffected = commonTimeSheetActivityRepository.deleteTimesheetRecord(id);
                if (rowsAffected > 0) {
                    deletedIds.add(id.toString());
                } else {
                    notFoundIds.add(id.toString());
                }
            } catch (EmptyResultDataAccessException ex) {
                notFoundIds.add(id.toString());
            } catch (Exception e) {
                failedToDeleteIds.add(id.toString());
            }
        }

        if (!deletedIds.isEmpty()) {
            // If some deletions were successful
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Activities deleted successfully", Map.of("deletedIds", deletedIds)));
        }

        if (!notFoundIds.isEmpty()) {
            // If no records were found for some IDs
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No records found for This IDs", Map.of("notFoundIds", notFoundIds)));
        }

        if (!failedToDeleteIds.isEmpty()) {
            // If some deletions failed
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false,
                    "Failed to delete some activities", Map.of("failedToDeleteIds", failedToDeleteIds)));
        }

        // If all IDs were processed successfully (this should ideally not happen if IDs
        // are passed correctly)
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "No activities found to delete", Collections.emptyList()));
    }

    // common Time Sheet for particular user activity based on supervisor based on
    // date
    @Override
    public ResponseEntity<?> getCommonTimeSheet(LocalDate date) {

        int id = AuthUserData.getUserId();
        List<Users> user = usersRepository.findBySupervisor(usersRepository.findByIdGetUsername(id));

        List<CommonTaskActivityResponse> activityLogs = new ArrayList<>();

        for (Users u : user) {
            List<CommonTaskActivityResponse> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityDate(u.getId(), date).stream()
                    .map(row -> new CommonTaskActivityResponse((Integer) row[0],
                            convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                            (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                            ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                            (String) row[11], (String) row[12], (String) row[13], (boolean) row[14], (String) row[15]))
                    .collect(Collectors.toList());
            if (userActivityLogs.size() >= 1) {
                activityLogs.addAll(userActivityLogs);

            }
        }
        activityLogs.sort(Comparator.comparing(CommonTaskActivityResponse::getId).reversed());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    // ===============================================================================Time
    // sheet date grage List WIthout Product Names
    // =====================================================
    // @Override
    // public ResponseEntity<?> getCommonTimeSheetDateRange(LocalDate fromdate,
    // LocalDate todate) {
    // int id = AuthUserData.getUserId();
    // List<Users> user = usersRepository.findBySupervisor(id);
    //
    // List<CommonTaskActivityResponse> activityLogsall = new ArrayList<>();
    // for (Users u : user) {
    // List<CommonTaskActivityResponse> userActivityLogs =
    // commonTimeSheetActivityRepository
    // .findTaskDTOsByUserIdAndActivityDatebetween(u.getId(), fromdate,
    // todate).stream()
    // .map(row -> new CommonTaskActivityResponse((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3],
    // (String) row[4],
    // (boolean) row[5], ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String)
    // row[9],
    // (String) row[10], (String) row[11], (String) row[12], (String) row[13],
    // (boolean) row[14],
    // (String) row[15]))
    // .collect(Collectors.toList());
    // if (userActivityLogs.size() > 0) {
    // activityLogsall.addAll(userActivityLogs);
    //
    // }
    // activityLogsall.sort(Comparator.comparing(CommonTaskActivityResponse::getId).reversed());
    //
    // }
    //
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogsall));
    // }

    // product based
    @Override
    public ResponseEntity<?> getCommonTimeSheetDateRangebyProduct(LocalDate fromDate, LocalDate toDate) {
        String supervisorId = AuthUserData.getEmpid();

        List<ProductNameDTO> productNameDTO = new ArrayList<>();
        List<ProductNameDTO> MemberList = new ArrayList<>();

        // List<CommonTaskActivityDTO> activityLogsAll = new ArrayList<>();
        Map<String, Object> responseData = new HashMap<>();
        // Set<Integer> uniqueIds = new HashSet<>();

        List<Object[]> productlist = commonTimeSheetActivityRepository.findDistinctProductNames(fromDate, toDate,
                supervisorId);
        for (Object[] rowdata : productlist) {
            ProductNameDTO productNameDTO1 = new ProductNameDTO();
            productNameDTO1.setId((Integer) rowdata[0]);
            productNameDTO1.setName((String) rowdata[1]);

            productNameDTO.add(productNameDTO1);
        }

        List<Object[]> memberList = commonTimeSheetActivityRepository.findDisnictProductAndMemberNames(fromDate, toDate,
                supervisorId);

        for (Object[] memberdata : memberList) {
            Integer userId = (Integer) memberdata[0];

            ProductNameDTO productNameDTO1 = new ProductNameDTO();
            productNameDTO1.setId(userId);
            productNameDTO1.setName((String) memberdata[1]);
            productNameDTO1.setProfilepic((String) memberdata[2]);
            Optional<Users> user = usersRepository.findById(userId);
            List<String> roles = user.get().getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.toList());
            productNameDTO1.setRole(roles);
            MemberList.add(productNameDTO1);
        }

        responseData.put("productName", productNameDTO);
        responseData.put("memberName", MemberList);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", responseData));
    }

    @Override
    public ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(LocalDate fromdate, LocalDate todate,
            Integer prodId) {
        String supervisorId = AuthUserData.getEmpid();
        List<ProductNameDTO> productNameDTO = new ArrayList<>();
        Map<String, Object> responseData = new HashMap<>();

        List<Object[]> productlist = commonTimeSheetActivityRepository
                .findByUserIdAndActivityDatebetweenmembername(prodId, fromdate, todate, supervisorId);
        for (Object[] rowdata : productlist) {
            Integer userId = (Integer) rowdata[0];

            ProductNameDTO productNameDTO1 = new ProductNameDTO();
            productNameDTO1.setId(userId);
            productNameDTO1.setName((String) rowdata[1]);
            productNameDTO1.setProfilepic((String) rowdata[2]);
            Optional<Users> user = usersRepository.findById(userId);
            List<String> roles = user.get().getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.toList());
            productNameDTO1.setRole(roles);
            productNameDTO.add(productNameDTO1);
        }
        responseData.put("memberlist", productNameDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", responseData));
    }

    @Override
    public ResponseEntity<?> getCommonTimeSheetDateRangebyProductidlist(Integer userid, LocalDate fromdate,
            LocalDate todate, Integer prodId) {

        // int supervisorId = AuthUserData.getUserId();
        String supervisorId = AuthUserData.getEmpid();
        List<Users> users = usersRepository.findBySupervisor(supervisorId);
        List<CommonTaskActivityDTO> activityLogsAll = new ArrayList<>();
        Map<String, Object> responseData = new HashMap<>();
        for (Users user : users) {

            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityDatebetweenBasedProdnameDetail(user.getId(), fromdate, todate,
                            prodId);

            for (Object[] row : userActivityLogs) {
                int productId = (int) row[12];
                boolean userAssignedTo = memberRepository.existsByMemberAndProdId(user, productId);
                // Boolean assignedStatus = userAssignedTo ? true : false;
                CommonTaskActivityDTO activityDTO = new CommonTaskActivityDTO((Integer) row[0],
                        convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                        (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                        ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                        (String) row[11], (int) row[12], (String) row[13], (String) row[14], (boolean) row[15],
                        (String) row[16], userAssignedTo);
                activityLogsAll.add(activityDTO);
            }
        }

        responseData.put("timesheetlist", activityLogsAll);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", responseData));
    }

    @Override
    public ResponseEntity<?> getCommonTimeSheet() {
        // int id = AuthUserData.getUserId();
        String id = AuthUserData.getEmpid();
        List<Users> user = usersRepository.findBySupervisor(id);
        List<CommonTaskActivityDTO> activityLogs = new ArrayList<>();
        for (Users u : user) {
            List<Object[]> userActivityLogs1 = commonTimeSheetActivityRepository.findTaskDTOsByUserId(u.getId());
            List<CommonTaskActivityDTO> taskDataList = userActivityLogs1.stream().map(row -> {

                boolean userExists = memberRepository.existsByMemberAndProdId(u, (Integer) row[12]);
                // Boolean assignedStatus = userExists ? true : false;

                return new CommonTaskActivityDTO((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                        (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                        ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                        (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (Integer) row[12],
                        (String) row[13], (String) row[14], (boolean) row[15], (String) row[16], userExists);
            }).collect(Collectors.toList());
            activityLogs.addAll(taskDataList);
        }
        activityLogs.sort(Comparator.comparing(CommonTaskActivityDTO::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    // supervisor Based Activity Collection
    // @Override
    // public ResponseEntity<?> getCommonTimeSheet(int page,
    // int size, String category, int productId, int memberId, LocalDate startDate,
    // LocalDate endDate,
    // String status) {
    // try {
    // Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
    // Pageable pageable = PageRequest.of(page, size, sortByDescId);
    // String id = AuthUserData.getEmpid();
    // int userId = AuthUserData.getUserId();
    // List<CommonTaskActivityDTO> activityLogs = new ArrayList<>();
    // category = category.toLowerCase();
    //
    // Page<Object[]> userActivityLogs1 = handlePending(id, category, productId,
    // memberId, startDate, endDate,
    // pageable, status, userId);
    // List<CommonTaskActivityDTO> taskDataList = userActivityLogs1.stream().map(row
    // -> {
    //
    // int userExists = memberRepository.countByUserIdAndProdId((Integer) row[17],
    // (Integer) row[12]);
    // Boolean assignedStatus = userExists != 0;
    //
    // return new CommonTaskActivityDTO((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // ((Timestamp) row[6]).toLocalDateTime(),
    // ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
    // (String) row[10], (String) row[11], (Integer) row[12], (String) row[13],
    // (String) row[14],
    // (boolean) row[15], (String) row[16], assignedStatus);
    // }).collect(Collectors.toList());
    // activityLogs.addAll(taskDataList);
    //
    // activityLogs.sort(Comparator.comparing(CommonTaskActivityDTO::getId).reversed());
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogs));
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse(true, "Error", Collections.emptyList()));
    // }
    // }

    @Override
    public ResponseEntity<?> getCommonTimeSheet(int page, int size, String category, int productId, int memberId,
            LocalDate startDate, LocalDate endDate, String status) {
        try {
            Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size, sortByDescId);
            String id = AuthUserData.getEmpid();
            int userId = AuthUserData.getUserId();
            category = category.toLowerCase();

            Page<Object[]> userActivityLogs1 = handlePending(id, category, productId, memberId, startDate, endDate,
                    pageable, status, userId);
            List<CommonTaskActivityDTO> activityLogs = new ArrayList<>(userActivityLogs1.getNumberOfElements());
            List<CommonTaskActivityDTO> taskDataList = userActivityLogs1.stream().map(row -> {

                int userExists = memberRepository.countByUserIdAndProdId((Integer) row[17], (Integer) row[12]);
                Boolean assignedStatus = userExists != 0;

                return new CommonTaskActivityDTO((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                        (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                        ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                        (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (Integer) row[12],
                        (String) row[13], (String) row[14], (boolean) row[15], (String) row[16], assignedStatus);
            }).collect(Collectors.toList());
            activityLogs.addAll(taskDataList);
            long totalCount = userActivityLogs1.getTotalElements();
            activityLogs.sort(Comparator.comparing(CommonTaskActivityDTO::getId).reversed());
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("dataList", activityLogs);
            responseData.put("totalCount", totalCount);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", responseData));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    private Page<Object[]> handlePending(String authUserId, String category, int productId, int memberId,
            LocalDate startDate, LocalDate endDate, Pageable pageable, String status, int userId) {

        Page<Object[]> product = null;
        switch (category) {
            case "default" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOs(authUserId, pageable);
                break;
            }
            case "date" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsDate(authUserId, startDate, endDate, pageable);
                break;
            }
            case "product" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsProduct(authUserId, productId, pageable);
                break;
            }
            case "member" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsMember(authUserId, memberId, pageable);
                break;
            }
            case "dateandproduct" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsDateAndProduct(authUserId, startDate, endDate,
                        productId, pageable);
                break;
            }
            case "dateandmember" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsDateAndMember(authUserId, startDate, endDate,
                        memberId, pageable);
                break;
            }
            case "memberandproduct" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsProductAndMember(authUserId, productId,
                        memberId,
                        pageable);
                break;
            }
            case "all" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsAll(authUserId, startDate, endDate, productId,
                        memberId, pageable);
                break;
            }
            case "approveddefault" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedBy(userId, status, pageable);
                break;
            }

            case "approveddefaultdate" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndDateRange(userId, status,
                        startDate, endDate, pageable);
                break;
            }

            case "approvedmember" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndMemberId(userId, status,
                        memberId, pageable);
                break;
            }
            case "approvedall" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndProductByAndMemberId(
                        userId,
                        status, productId, memberId, pageable);
                break;
            }
            case "approvedproduct" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByAndProduct(userId, status,
                        productId, pageable);
                break;
            }
            default -> {
                product = commonTimeSheetActivityRepository.findTaskDTOs(authUserId, pageable);
                break;
            }
        }
        return product;
    }

    // final Approve List for Common TimeSheet
    @Override
    public ResponseEntity<?> getCommonTimeSheetFinal(LocalDate date) {

        int id = AuthUserData.getUserId();
        List<Users> user = usersRepository.findByFinalApprove(usersRepository.findByIdGetUsername(id));
        List<TaskActivityResponse> activityLogs = new ArrayList<>();
        for (Users u : user) {

            List<TaskActivityResponse> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityDateAndFinalApprove(u.getId(), date).stream()
                    .map(row -> new TaskActivityResponse((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                            (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                            ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                            (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                            (String) row[13], null))
                    .collect(Collectors.toList());
            activityLogs.addAll(userActivityLogs);
        }

        activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    // //new code
    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalall() {
        // int id = AuthUserData.getUserId();
        String id = AuthUserData.getEmpid();
        List<Users> user = usersRepository.findByFinalApprove(id);
        List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
        for (Users u : user) {
            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(u.getId());

            for (Object[] row : userActivityLogs) {
                int productId = (int) row[12];
                boolean userAssignedTo = memberRepository.existsByMemberAndProdId(u, productId);
                // Boolean assignedStatus = userAssignedTo ? true : false;

                TaskActivityResponseDTO activityDTO = new TaskActivityResponseDTO((Integer) row[0],
                        convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                        (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                        ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                        (String) row[11], (Integer) row[12], (String) row[13], (String) row[14], userAssignedTo);

                activityLogs.add(activityDTO);

            }
        }

        activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    // @Override
    // public ResponseEntity<?> getCommonTimeSheetFinalall(int page, int size,
    // String category, int productId,
    // String status, int memberId, LocalDate startDate, LocalDate endDate) {
    // try {
    // Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
    // Pageable pageable = PageRequest.of(page, size, sortByDescId);
    // int id = AuthUserData.getUserId();
    // String supervisorId = AuthUserData.getEmpid();
    //
    // List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
    // category = category.toLowerCase();
    //
    // Page<Object[]> userActivityLogs = handlePendingForFinal(supervisorId,
    // category, productId, memberId,
    // startDate, endDate, status, pageable);
    //
    // List<TaskActivityResponseDTO> activityDTO = userActivityLogs.stream().map(row
    // -> {
    //
    // int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer) id,
    // (Integer) row[12]);
    // Boolean assignedStatus = userAssignedTo == 0;
    //
    // return new TaskActivityResponseDTO((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // ((Timestamp) row[6]).toLocalDateTime(),
    // ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
    // (String) row[10], (String) row[11], (Integer) row[12],
    // (String) row[13], (String) row[14], assignedStatus);
    //
    // }).collect(Collectors.toList());
    // activityLogs.addAll(activityDTO);
    //
    // activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogs));
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse(true, "Error", Collections.emptyList()));
    // }
    // }
    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalall(int page, int size, String category, int productId,
            String status, int memberId, LocalDate startDate, LocalDate endDate) {
        try {
            Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size, sortByDescId);
            int id = AuthUserData.getUserId();
            String supervisorId = AuthUserData.getEmpid();

            List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
            category = category.toLowerCase();

            Page<Object[]> userActivityLogs = handlePendingForFinal(supervisorId, category, productId, memberId,
                    startDate, endDate, status, pageable);

            List<TaskActivityResponseDTO> activityDTO = userActivityLogs.stream().map(row -> {

                int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer) id, (Integer) row[12]);
                Boolean assignedStatus = userAssignedTo == 0;

                return new TaskActivityResponseDTO((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                        (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                        ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                        (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (Integer) row[12],
                        (String) row[13], (String) row[14], assignedStatus);

            }).collect(Collectors.toList());
            activityLogs = new ArrayList<>(activityDTO.size());
            activityLogs.addAll(activityDTO);

            activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    private Page<Object[]> handlePendingForFinal(String authUserId, String category, int productId, int memberId,
            LocalDate startDate, LocalDate endDate, String status, Pageable pageable) {

        Page<Object[]> product = null;
        switch (category) {
            // case "default" -> {
            // product =
            // commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(authUserId,
            // pageable);
            // break;
            // }
            case "date" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDate(authUserId, startDate,
                                endDate,
                                pageable);
                break;
            }
            case "product" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProduct(authUserId, productId,
                                pageable);
                break;
            }
            case "member" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndMember(authUserId, memberId,
                                pageable);
                break;
            }
            case "status" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndStatus(authUserId, status,
                                pageable);
                break;
            }
            case "dateandproduct" -> {

                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndProduct(authUserId,
                                startDate,
                                endDate, productId, pageable);
                break;
            }
            case "dateandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndStatus(authUserId,
                                startDate,
                                endDate, status, pageable);
                break;
            }
            case "productandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProductAndStatus(authUserId,
                                productId, status, pageable);
                break;
            }
            case "memberandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndMemberAndStatus(authUserId,
                                memberId,
                                status, pageable);
                break;
            }

            case "dateandmember" -> {

                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndDateAndMember(authUserId,
                                startDate,
                                endDate, memberId, pageable);

                break;
            }
            case "memberandproduct" -> {

                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetailAndProductAndMember(authUserId,
                                productId, memberId, pageable);
                break;
            }
            case "all" -> {

                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndFinalApproveallDetailAndAll(
                        authUserId,
                        startDate, endDate, productId, memberId, status, pageable);
                break;
            }
            case "dateandproductandmember" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndFinalApproveallDetailAnddpm(
                        authUserId,
                        startDate, endDate, productId, memberId, pageable);
                break;
            }
            case "dateandproductandstatus" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndFinalApproveallDetailAnddps(
                        authUserId,
                        startDate, endDate, productId, status, pageable);
                break;
            }
            case "productandmemberandstatus" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndFinalApproveallDetailAndpms(
                        authUserId,
                        productId, memberId, status, pageable);
                break;
            }
            case "dateandmemberandstatus" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndFinalApproveallDetailAnddms(
                        authUserId,
                        startDate, endDate, memberId, status, pageable);
                break;
            }

            default -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(authUserId, pageable);
                break;
            }
        }
        return product;
    }

    // // new code
    @Override
    public ResponseEntity<?> getFinalApproveddaterangeProductandMember(LocalDate fromdate, LocalDate todate) {
        String id = AuthUserData.getEmpid();
        List<ProductNameDTO> productNameDTO = new ArrayList<>();
        List<ProductNameDTO> memberNameDTO = new ArrayList<>();
        Map<String, Object> mapdata = new HashMap<>();

        List<Object[]> productlist = commonTimeSheetActivityRepository.findDistinctProductNamesforFinalapprove(fromdate,
                todate, id);
        for (Object[] rowdata : productlist) {
            ProductNameDTO productNameDTO1 = new ProductNameDTO();
            productNameDTO1.setId((Integer) rowdata[0]);
            productNameDTO1.setName((String) rowdata[1]);
            productNameDTO.add(productNameDTO1);
        }

        List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                .findDisnictProductAndMemberNamesforFinalapprove(fromdate, todate, id);

        for (Object[] memberdata : userActivityLogs) {
            Integer userId = (Integer) memberdata[0];
            ProductNameDTO productNameDTO1 = new ProductNameDTO();
            productNameDTO1.setId(userId);
            productNameDTO1.setName((String) memberdata[1]);
            productNameDTO1.setProfilepic((String) memberdata[2]);
            Optional<Users> user = usersRepository.findById(userId);
            List<String> roles = user.get().getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.toList());
            productNameDTO1.setRole(roles);
            memberNameDTO.add(productNameDTO1);
        }
        mapdata.put("productNames", productNameDTO);
        mapdata.put("memberNames", memberNameDTO);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", mapdata));
    }

    // // New code
    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproduct(LocalDate fromdate, LocalDate todate,
            Integer productid) {
        // int id = AuthUserData.getUserId();
        String id = AuthUserData.getEmpid();
        List<Users> user = usersRepository.findByFinalApprove(id);
        List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
        Map<String, Object> mapdata = new HashMap<>();
        List<ProductNameDTO> productNameDTO = new ArrayList<>();
        for (Users u : user) {

            List<Object[]> productlist = commonTimeSheetActivityRepository
                    .findByUserIdAndActivityDatebetweenmemberusername(u.getId(), productid, fromdate, todate);
            Set<Integer> uniqueProductIds = new HashSet<>();

            for (Object[] rowdata : productlist) {
                Integer productId = (Integer) rowdata[0];

                // Check if the product ID is not already in the set
                if (!uniqueProductIds.contains(productId)) {
                    // Add the product ID to the set to mark it as processed
                    Optional<Users> users = usersRepository.findById(productId);
                    uniqueProductIds.add(productId);
                    ProductNameDTO productNameDTO1 = new ProductNameDTO();
                    productNameDTO1.setId(productId);
                    productNameDTO1.setName((String) rowdata[1]);
                    productNameDTO1.setProfilepic((String) rowdata[2]);
                    List<String> roles = users.get().getRole_id().stream().map(role -> role.getName())
                            .collect(Collectors.toList());
                    productNameDTO1.setRole(roles);
                    productNameDTO.add(productNameDTO1);
                }
            }

            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDateAndFinalApproveDateRangeByproductIdDetail(u.getId(), fromdate, todate,
                            productid);

            for (Object[] row : userActivityLogs) {
                int productId = (int) row[12];
                boolean userAssignedTo = memberRepository.existsByMemberAndProdId(u, productId);
                // Boolean assignedStatus = userAssignedTo ? true : false;
                TaskActivityResponseDTO activityDTO = new TaskActivityResponseDTO((Integer) row[0],
                        convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                        (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                        ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                        (String) row[11], (Integer) row[12], (String) row[13], (String) row[14], userAssignedTo);
                activityLogs.add(activityDTO);
            }
        }
        mapdata.put("userNames", productNameDTO);
        mapdata.put("data", activityLogs);
        activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", mapdata));
    }

    // // New Code
    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalApproveddaterangebyproductByUser(LocalDate fromdate,
            LocalDate todate, Integer productid, Integer userid) {

        Optional<Users> user = usersRepository.findById(userid);
        List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
        Map<String, Object> responseData = new HashMap<>();

        List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                .findUserIdAndActivityDateAndFinalApproveDateRangeByproductIdDetail(userid, fromdate, todate,
                        productid);

        for (Object[] row : userActivityLogs) {
            int productId = (int) row[12];
            boolean userAssignedTo = memberRepository.existsByMemberAndProdId(user.get(), productId);
            // Boolean assignedStatus = userAssignedTo ? true : false;
            TaskActivityResponseDTO activityDTO = new TaskActivityResponseDTO((Integer) row[0],
                    convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                    (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                    (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (Integer) row[12],
                    (String) row[13], (String) row[14], userAssignedTo);
            activityLogs.add(activityDTO);
        }

        responseData.put("timesheetlist", activityLogs);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", responseData));
    }

    // // Old Code
    // @Override
    // public ResponseEntity<?>
    // getCommonTimeSheetFinalApproveddaterangebyproductByUser(LocalDate fromdate,
    // LocalDate todate, Integer productid, Integer userid) {
    // List<TaskActivityResponse> userActivityLogs =
    // commonTimeSheetActivityRepository
    // .findUserIdAndActivityDateAndFinalApproveDateRangeByproductId(userid,
    // fromdate, todate, productid)
    // .stream()
    // .map(row -> new TaskActivityResponse((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String)
    // row[9],
    // (String) row[10], (String) row[11], (String) row[12], (String) row[13]))
    // .collect(Collectors.toList());

    // // activityLogs.addAll(userActivityLogs);

    // userActivityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());

    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // userActivityLogs));
    // }

    // Get Members Activity Based on On role Persons For 2 nd level Approlal for
    // Owner
    @Override
    public ResponseEntity<?> getCommonTimeSheetbyProductOwnerList() {
        List<Integer> product_id = productRepository.findByOwnerInTechOwnerOrProdOwner(AuthUserData.getUserId());
        List<OwnerApprovalDto> activityLogs = new ArrayList<>();

        List<Object[]> userActivityLogs = commonTimeSheetActivityRepository.getProductidmemberList(product_id,
                AuthUserData.getUserId());

        for (Object[] row : userActivityLogs) {
            Integer pro_id = (Integer) row[15];
            Integer user_id = (Integer) row[16];
            Optional<Users> user = usersRepository.findById(user_id);
            boolean userAssignedTo = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
            // Boolean assignedStatus = userAssignedTo ? true : false;
            OwnerApprovalDto activityDTO = new OwnerApprovalDto((Integer) row[0],
                    convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                    (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                    (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                    (String) row[13], (String) row[14], (Integer) row[15], (Integer) row[16], userAssignedTo);
            activityLogs.add(activityDTO);

        }
        Collections.sort(activityLogs, Comparator.comparing(OwnerApprovalDto::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    // Date range Based On Productname and Members Active for Owner Approval
    // public ResponseEntity<?> productownerapprovaldaterange(LocalDate fromdate,
    // LocalDate todate) {
    //
    // List<Integer> product_id =
    // productRepository.findByOwnerInTechOwnerOrProdOwner(AuthUserData.getUserId());
    // List<OwnerApprovalDto> activityLogs = new ArrayList<>();
    // Set<Integer> uniqueProductNames = new HashSet<>();
    // List<ProductNameDTO> productNameDTO = new ArrayList<>();
    // Map<String, Object> mapdata = new HashMap<>();
    // List<OwnerApprovalDto> userActivityLogs =
    // commonTimeSheetActivityRepository.getdProductidmemberListByDateRange(product_id,
    // fromdate, todate)
    // .stream()
    // .map(row -> {
    // ProductNameDTO productNameDTO1 = new ProductNameDTO();
    // productNameDTO1.setId((Integer) row[15]);
    // productNameDTO1.setName((String) row[11]);
    // if (uniqueProductNames.add(productNameDTO1.getId())) {
    // productNameDTO.add(productNameDTO1);
    // }
    // return new OwnerApprovalDto(
    // (Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2],
    // (String) row[3],
    // (String) row[4],
    // (boolean) row[5],
    // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(),
    // (boolean) row[8],
    // (String) row[9],
    // (String) row[10],
    // (String) row[11],
    // (String) row[12],
    // (String) row[13],
    // (String) row[14],
    // (Integer) row[15],
    // (Integer) row[16]
    // );
    // })
    // .collect(Collectors.toList());
    // Collections.sort(userActivityLogs,
    // Comparator.comparing(OwnerApprovalDto::getId).reversed());
    // activityLogs.addAll(userActivityLogs);
    // mapdata.put("productNames", productNameDTO);
    // mapdata.put("data", activityLogs);
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // mapdata));
    // }

    // Get Members Activity Based on On role Persons For 2 nd level Approlal for
    // Owner by daterange and productid/
    @Override
    public ResponseEntity<?> productownerapprovaldaterangeandprodId(LocalDate fromdate, LocalDate todate,
            Integer productid, Integer userid) {
        List<Integer> product_id = productRepository.findByOwnerInTechOwnerOrProdOwner(AuthUserData.getUserId());
        List<ProductNameDTO> productNameDTO = new ArrayList<>();
        List<ProductNameDTO> memberNameDTO = new ArrayList<>();
        Map<String, Object> mapdata = new HashMap<>();
        String id = AuthUserData.getEmpid();

        if (fromdate != null && todate != null && productid == null && (userid == null || userid == 0)) {
            List<Object[]> productlist = commonTimeSheetActivityRepository
                    .getdProductidmemberListByDateRange(product_id, fromdate, todate);
            for (Object[] rowdata : productlist) {
                ProductNameDTO productNameDTO1 = new ProductNameDTO();
                productNameDTO1.setId((Integer) rowdata[0]);
                productNameDTO1.setName((String) rowdata[1]);
                productNameDTO.add(productNameDTO1);
            }

            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findDisnictProductAndMemberNamesforOwner(fromdate, todate, product_id);

            for (Object[] memberdata : userActivityLogs) {
                Integer userId = (Integer) memberdata[0];
                ProductNameDTO productNameDTO1 = new ProductNameDTO();
                productNameDTO1.setId(userId);
                productNameDTO1.setName((String) memberdata[1]);
                productNameDTO1.setProfilepic((String) memberdata[2]);
                Optional<Users> user = usersRepository.findById(userId);
                List<String> roles = user.get().getRole_id().stream().map(role -> role.getName())
                        .collect(Collectors.toList());
                productNameDTO1.setRole(roles);
                memberNameDTO.add(productNameDTO1);
            }

        } else if (fromdate != null && todate != null && productid != null && (userid == null || userid == 0)) {
            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .getdProductidmemberListByDateRangeandproductid(fromdate, todate, productid);
            for (Object[] memberdata : userActivityLogs) {
                Integer userId = (Integer) memberdata[0];
                ProductNameDTO productNameDTO1 = new ProductNameDTO();
                productNameDTO1.setId(userId);
                productNameDTO1.setName((String) memberdata[1]);
                productNameDTO1.setProfilepic((String) memberdata[2]);
                Optional<Users> user = usersRepository.findById(userId);
                List<String> roles = user.get().getRole_id().stream().map(role -> role.getName())
                        .collect(Collectors.toList());
                productNameDTO1.setRole(roles);
                memberNameDTO.add(productNameDTO1);
            }
        }

        mapdata.put("productNames", productNameDTO);
        mapdata.put("memberNames", memberNameDTO);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", mapdata));

    }

    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalallbyApproved() {

        int id = AuthUserData.getUserId();

        List<Users> user = usersRepository.findByFinalApprove(usersRepository.findByIdGetUsername(id));

        List<TaskActivityResponseList> activityLogs = new ArrayList<>();
        for (Users u : user) {
            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityAndFinalApproveallApprovedList(u.getId());

            List<TaskActivityResponseList> taskDataList = userActivityLogs.stream().map(row -> {
                int pro_id = (int) row[12];

                boolean userExists = memberRepository.existsByMemberAndProdId(u, pro_id);
                // Boolean assignedStatus = userExists ? true : false;
                return new TaskActivityResponseList((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                        (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                        ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                        (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (int) row[12],
                        (String) row[13], (String) row[14], (String) row[15], userExists);
            }).collect(Collectors.toList());
            activityLogs.addAll(taskDataList);
        }

        activityLogs.sort(Comparator.comparing(TaskActivityResponseList::getId).reversed());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet Approved  data fetched successfully", activityLogs));
    }

    // by user
    @Override
    public ResponseEntity<?> getCommonTimeSheetUser(LocalDate date) {
        try {
            int id = AuthUserData.getUserId();
            Optional<Users> user = usersRepository.findById(id);

            List<UserTaskActivityResponse> activityLogs = new ArrayList<>();
            List<Object[]> userActivityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityDatepermisionList(user.get().getId(), date);
            for (Object[] row : userActivityLogs) {
                int pro_id = (int) row[12];

                boolean userExists = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
                // Boolean assignedStatus = userExists ? true : false;

                String ownerstatus = "";
                String supervisorstatus = "";
                String finalApprovalStatus = "";

                if ((row[17] != null) && ((String) row[17]).equalsIgnoreCase("Supervisor Approved")) {
                    ownerstatus = "Pending";
                } else if ((row[16] != null) && !((String) row[16]).equalsIgnoreCase("Approved")) {
                    ownerstatus = "Not Yet";
                    finalApprovalStatus = "Not Yet";
                } else {
                    ownerstatus = (String) row[17];
                }

                if ((row[13] != null) && ((String) row[13]).equalsIgnoreCase("TL Approved")) {
                    finalApprovalStatus = "Pending";
                } else {
                    finalApprovalStatus = (String) row[13];
                }

                UserTaskActivityResponse taskResponse = new UserTaskActivityResponse((Integer) row[0],
                        convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                        (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                        ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                        (String) row[11], (int) row[12], finalApprovalStatus, (String) row[14], false, (String) row[16],
                        ownerstatus, userExists);
                activityLogs.add(taskResponse);

            }
            activityLogs.sort(Comparator.comparing(UserTaskActivityResponse::getId).reversed());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Something went wrong", e.getMessage()));
        }
    }

    // update Common Task Activity
    @Override
    public ResponseEntity<?> updateCommonTaskActivities(List<CommonTaskDraft> taskActivities, String draft) {
        try {
            List<CommonTimeSheetActivity> updatedActivities = new ArrayList<>();
            LocalTime dynamicTime = timeConfig.getComparisonTime();

            Integer userId = AuthUserData.getUserId();
            LocalDate currentDate = LocalDate.now();
            // if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            LocalDate currentMinusThree = currentDate.minusDays(3);
            LocalDate currentMinusTwo = currentDate.minusDays(2);
            // System.out.println("Today is Monday.");
            // }
            LocalDate yesterday = currentDate.minusDays(1);
            Optional<Users> createdByUser = usersRepository.findById(userId);

            // int totalHours = 0; // Reset totalHours for each activity
            // int totalMinutes = 0; // Reset totalMinutes for each activity

            // for (CommonTaskDraft activity : taskActivities) {
            // // Fetch hours for the current activity date
            // String hours =
            // commonTimeSheetActivityRepository.findbyhours(activity.getActivity_date(),
            // userId);
            // if (hours != null) {
            // String[] hoursSplit = hours.split(":");
            // totalHours += Integer.parseInt(hoursSplit[0]);
            // totalMinutes += Integer.parseInt(hoursSplit[1]);
            // }

            // // Fetch hours for the new activity
            // String activityHours = activity.getHours();
            // String[] activityHoursSplit = activityHours.split(":");
            // totalHours += Integer.parseInt(activityHoursSplit[0]);
            // totalMinutes += Integer.parseInt(activityHoursSplit[1]);

            // // Adjust total hours and minutes
            // totalHours += totalMinutes / 60;
            // totalMinutes %= 60;
            // System.out.println(totalHours);
            // System.out.println(totalMinutes);

            // if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
            // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new
            // ApiResponse(false,
            // "Total hours cannot exceed 09:00 for date: " + activity.getActivity_date(),
            // null));
            // }
            // }

            Map<LocalDate, Integer> totalHoursByDate = new HashMap<>();
            Map<LocalDate, Integer> totalMinutesByDate = new HashMap<>();
            Set<LocalDate> processedDates = new HashSet<>();

            for (CommonTaskDraft activity : taskActivities) {
                LocalDate activityDate = activity.getActivity_date();

                // Initialize total hours and minutes for the date if not already done
                totalHoursByDate.putIfAbsent(activityDate, 0);
                totalMinutesByDate.putIfAbsent(activityDate, 0);

                // Fetch hours for the current activity date
                if (!processedDates.contains(activityDate)) {
                    String hours = commonTimeSheetActivityRepository.findbyhours(activityDate, userId);
                    if (hours != null) {
                        String[] hoursSplit = hours.split(":");
                        totalHoursByDate.put(activityDate,
                                totalHoursByDate.get(activityDate) + Integer.parseInt(hoursSplit[0]));
                        totalMinutesByDate.put(activityDate,
                                totalMinutesByDate.get(activityDate) + Integer.parseInt(hoursSplit[1]));
                    }
                    // Mark this date as processed
                    processedDates.add(activityDate);
                }

                // System.out.println(totalHoursByDate);
                // Fetch hours for the new activity
                String activityHours = activity.getHours();
                String[] activityHoursSplit = activityHours.split(":");
                totalHoursByDate.put(activityDate,
                        totalHoursByDate.get(activityDate) + Integer.parseInt(activityHoursSplit[0]));
                totalMinutesByDate.put(activityDate,
                        totalMinutesByDate.get(activityDate) + Integer.parseInt(activityHoursSplit[1]));

                // Adjust total hours and minutes
                totalHoursByDate.put(activityDate,
                        totalHoursByDate.get(activityDate) + totalMinutesByDate.get(activityDate) / 60);
                totalMinutesByDate.put(activityDate, totalMinutesByDate.get(activityDate) % 60);

                System.out.println(totalHoursByDate);
                System.out.println(totalMinutesByDate);
                // Check if total hours exceed 09:00 for this date
                if (totalHoursByDate.get(activityDate) > 9 ||
                        (totalHoursByDate.get(activityDate) == 9 && totalMinutesByDate.get(activityDate) > 0)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false,
                            "Total hours cannot exceed 09:00 for date: " + activityDate, null));
                }
            }

            for (CommonTaskDraft activity : taskActivities) {

                LocalDate activityDate = activity.getActivity_date();
                pm.model.activityrequest.ActivityRequest activityRequest = activityRequestRepository
                        .findbyUserIdAndRequestDate(userId, activityDate);

                // Check if the activity date is yesterday and before 11 AM
                if (activityRequest != null && activityRequest.getStatus() == EProductApproStatus.Approved) {
                    // Retrieve the approval time from the activity request
                    LocalDateTime approvalTime = activityRequest.getUpdatedat(); // Assume getApprovalTime() returns
                    // LocalDateTime
                    LocalDateTime currentTime = LocalDateTime.now();
                    // Check if more than one day has passed since the approval time
                    if (approvalTime.plusDays(1).isBefore(currentTime)) {
                        // More than one day has passed, do not set the activity date
                        // Continue to the next activity
                        System.out.println("allowed Raised request");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false,
                                "Allowed raised request time has exceeded", null));
                    } else {
                        // Set the activity date to the approved date
                        activity.setActivity_date(activityDate);
                    }
                } else if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY && (activityDate.equals(currentMinusThree)
                        || activityDate.equals(currentMinusTwo) || activityDate.equals(yesterday))
                        && LocalTime.now().isBefore(dynamicTime)) {
                } else if (activityDate.equals(yesterday) && LocalTime.now().isBefore(dynamicTime)) {
                    // Allow the activity date to be set as yesterday
                } else if (currentDate.equals(activityDate)) {
                    // Otherwise, set the activity date to today
                    System.out.println("current time screnarioo");
                    activity.setActivity_date(currentDate);
                } else {
                    // Otherwise, set the activity date to today
                    System.out.println("Need to raise the request");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false,
                            "Need to raise the request for date: " + activityDate, null));
                    // activity.setActivity_date(activityDate);
                }

                Optional<CommonTimeSheetActivity> taskActivityOptional = commonTimeSheetActivityRepository
                        .findById(activity.getId());
                if (taskActivityOptional.isPresent()) {
                    CommonTimeSheetActivity existingTaskActivity = taskActivityOptional.get();
                    existingTaskActivity.setActivity_date(activity.getActivity_date());
                    existingTaskActivity.setHours(activity.getHours());
                    existingTaskActivity.setDescription(activity.getDescription());
                    existingTaskActivity.setStatus(activity.getStatus());
                    existingTaskActivity.setDraft(draft != null && draft.equalsIgnoreCase("draft"));
                    existingTaskActivity.setSupervisorStatus("Pending");
                    existingTaskActivity.setTask(activity.getTask());
                    existingTaskActivity.setSupervisor(createdByUser.get().getSupervisor());
                    Product product = productRepository.findById(activity.getProduct()).orElse(null);
                    existingTaskActivity.setProduct(product);

                    CommonTimeSheetActivity updatedActivity = commonTimeSheetActivityRepository
                            .save(existingTaskActivity);
                    updatedActivities.add(updatedActivity);
                }
            }

            if (updatedActivities.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "No updates performed", Collections.emptyList()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Activity Updated successfully", Collections.emptyList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Something went wrong", e.getMessage()));
        }
    }

    // public ResponseEntity<?> updateCommonTaskActivities(List<CommonTaskDraft>
    // taskActivities, String draft) {
    // try {
    // List<CommonTimeSheetActivity> updatedActivities = new ArrayList<>();
    // int totalHours = 0;
    // int totalMinutes = 0;
    // String hours = null;
    // Integer user_id = AuthUserData.getUserId();
    //
    // LocalDate currentDate = LocalDate.now(); // Current date
    // int currentMonth = currentDate.getMonthValue();
    // List<Date> datesInRange = new ArrayList<>();
    // Optional<Users> createdByUser = usersRepository.findById(user_id);
    //
    //// if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
    //// // If today is Monday, use the current date as the end date (Monday)
    //// LocalDate currentMonday = currentDate;
    //// // Calculate the start date (previous Monday)
    //// LocalDate prevMonday = currentMonday.minusDays(7);
    ////
    //// // Add dates from the previous Monday to the current Monday
    //// LocalDate tempDate = prevMonday;
    //// while (!tempDate.isAfter(currentMonday)) {
    //// datesInRange.add(Date.valueOf(tempDate));
    //// tempDate = tempDate.plusDays(1);
    //// }
    //// } else {
    //// // If today is not Monday, find the previous Monday
    //// LocalDate prevMonday =
    // currentDate.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
    //// // Add dates from the previous Monday to the current date
    //// LocalDate tempDate = prevMonday;
    //// while (!tempDate.isAfter(currentDate)) {
    //// datesInRange.add(Date.valueOf(tempDate));
    //// tempDate = tempDate.plusDays(1);
    //// }
    //// }
    //
    // LocalDate tempDate = LocalDate.of(currentDate.getYear(),
    // currentDate.getMonth(), 1);
    // while (!tempDate.isAfter(currentDate)) {
    // datesInRange.add(Date.valueOf(tempDate));
    // tempDate = tempDate.plusDays(1);
    // }
    //
    // // Check if total hours exceed 16 before calculating
    // String activityHours = null; // Initialize outside the loop
    //
    // // Iterate over taskActivities to set activity date and calculate total hours
    // Set<LocalDate> uniqueActivityDates = new HashSet<>();
    //
    // for (CommonTaskDraft activity : taskActivities) {
    // LocalDate activityDate = activity.getActivity_date();
    // System.out.println(activityDate);
    // Date sqlDate = Date.valueOf(activityDate);
    // uniqueActivityDates.add(activityDate); // Add activity date to the set
    //
    // // Set the activity date based on its presence in datesInRange
    // if (datesInRange.stream().anyMatch(date -> date.equals(sqlDate))) {
    // activity.setActivity_date(activityDate);
    // System.out.println("111111");
    //
    // } else {
    // activity.setActivity_date(currentDate);
    // System.out.println("2222");
    //
    // }
    //
    //// int activityMonth = activity.getActivity_date().getMonthValue();
    //// if (activityMonth != currentMonth) {
    //// activity.setActivity_date(currentDate);
    //// System.out.println("true"+activityDate);
    //// } else {
    //// activity.setActivity_date(activityDate);
    //// System.out.println("false"+activityDate);
    //// }
    //
    // // Fetch hours for the current activity date
    // hours =
    // commonTimeSheetActivityRepository.findbyhours(activity.getActivity_date(),
    // user_id);
    // System.out.println(hours);
    // if (hours != null) {
    // String[] hoursSplit = hours.split(":");
    // totalHours = Integer.parseInt(hoursSplit[0]);
    //
    // int minute = Integer.parseInt(hoursSplit[1]);
    // totalMinutes += minute;
    //
    // }
    // // Store activity hours to use later
    // activityHours = activity.getHours();
    //
    // // Calculate total hours based on activity hours
    // if (hours != null) {
    // String[] newHoursSplit = activityHours.split(":");
    // int newHour = Integer.parseInt(newHoursSplit[0]);
    // int newMinute = Integer.parseInt(newHoursSplit[1]);
    // totalHours += newHour;
    // totalMinutes += newMinute;
    // } else {
    // String[] hoursSplit = activityHours.split(":");
    // int hour = Integer.parseInt(hoursSplit[0]);
    // int minute = Integer.parseInt(hoursSplit[1]);
    // totalHours += hour;
    // totalMinutes += minute;
    // }
    //
    // // Calculate total hours
    // totalHours += totalMinutes / 60; // Convert extra minutes to hours
    // totalMinutes %= 60; // Keep the remaining minutes
    // System.out.println(totalHours);
    // if (totalHours < 9 || (totalHours == 9 && totalMinutes < 0)) {
    //
    // Optional<CommonTimeSheetActivity> taskActivityOptional =
    // commonTimeSheetActivityRepository
    // .findById(activity.getId());
    // if (taskActivityOptional.isPresent()) {
    // CommonTimeSheetActivity existingTaskActivity = taskActivityOptional.get();
    //
    // if (datesInRange.stream().anyMatch(date -> date.equals(sqlDate))) {
    // existingTaskActivity.setActivity_date(activity.getActivity_date());
    // } else {
    // existingTaskActivity.setActivity_date(currentDate);
    // }
    //
    // existingTaskActivity.setHours(activity.getHours());
    // existingTaskActivity.setDescription(activity.getDescription());
    // existingTaskActivity.setStatus(activity.getStatus());
    // if (draft != null && draft.equalsIgnoreCase("draft")) {
    // existingTaskActivity.setDraft(true);
    //
    // } else {
    // existingTaskActivity.setDraft(false);
    //
    // }
    // existingTaskActivity.setSupervisorStatus("Pending");
    // existingTaskActivity.setTask(activity.getTask());
    // existingTaskActivity.setSupervisor(createdByUser.get().getSupervisor());
    //
    // // existingTaskActivity.setUser(taskActivity.getUser());
    // Product product =
    // productRepository.findById(activity.getProduct()).orElse(null);
    //
    // existingTaskActivity.setProduct(product);
    // totalHours=0;
    // CommonTimeSheetActivity updatedActivity = commonTimeSheetActivityRepository
    // .save(existingTaskActivity);
    // updatedActivities.add(updatedActivity);
    // }
    // } else if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new
    // ApiResponse(false,
    // "Total hours cannot exceed 09:00. " + activity.getActivity_date(), null));
    // }
    // }
    //
    // if (updatedActivities.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND)
    // .body(new ApiResponse(false, "No updates performed",
    // Collections.emptyList()));
    // }
    //
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Activity Updated successfully",
    // Collections.emptyList()));
    // } catch (Exception e) {
    //
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse(false, "Something went wrong", e.getMessage()));
    // }
    // }

    // approval or reject requests
    // @Override
    // public ResponseEntity<?> updateActivity(ActivityRequest activityRequest) {
    // int user_id = AuthUserData.getUserId();
    // LocalDateTime currDateTime = LocalDateTime.now();
    // CommonTaskActivityLog activityLog = new CommonTaskActivityLog();
    // activityLog.setTaskActivity(activityRequest.getId());
    // activityLog.setStatus(activityRequest.getStatus());
    // activityLog.setRemarks(activityRequest.getRemarks());
    // activityLog.setCreatedBy(user_id);
    // activityLog.setCreated_at(currDateTime);
    // activityLog.setIsDeleted(false);
    // commonTaskActivityLogRepository.save(activityLog);
    // List<Integer> data = activityRequest.getId();
    // for (int i = 0; i < data.size(); i++) {
    // CommonTaskActivityLog taskActivity =
    // commonTaskActivityLogRepository.findById(data.get(i)).orElse(null);
    // taskActivity.set_approved(true);
    // taskActivity.setStatus(activityRequest.getStatus());
    // commonTaskActivityLogRepository.save(taskActivity);
    // }
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // "Success", null));
    //
    // }

    @Override
    public ResponseEntity<?> updateActivity(ActivityRequest activityRequest) {
        int user_id = AuthUserData.getUserId();
        LocalDateTime currDateTime = LocalDateTime.now();
        List<Integer> data = activityRequest.getId();

        for (int i = 0; i < data.size(); i++) {
            Integer activityId = data.get(i);
            CommonTaskActivityLog activityLog = new CommonTaskActivityLog();
            activityLog.setTaskActivity(activityId);
            activityLog.setStatus(activityRequest.getStatus());
            // Validate remarks
            String remarks = activityRequest.getRemarks();
            if (!remarks.matches(".*[\\s\\S]*[a-zA-Z]+[\\s\\S]*")
                    && !activityRequest.getStatus().equalsIgnoreCase("Approved")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Remarks must contain alphabets.", null));
            } else if (remarks.trim().isEmpty() && !activityRequest.getStatus().equalsIgnoreCase("Approved")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Remarks cannot be empty or contain only spaces.", null));
            }
            activityLog.setRemarks(activityRequest.getRemarks());
            activityLog.setCreatedBy(user_id);
            activityLog.setCreated_at(currDateTime);
            activityLog.setIsDeleted(false);
            commonTaskActivityLogRepository.save(activityLog);
            CommonTimeSheetActivity taskActivity = commonTimeSheetActivityRepository.findById(activityId).orElse(null);

            if (taskActivity != null) {
                taskActivity.setSupervisorStatus(activityRequest.getStatus());
                if ("Contract".equalsIgnoreCase(taskActivity.getUser().getRoleType())) {

                    if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                        taskActivity.setFinalApprove("TL Approved");

                    } else {
                        taskActivity.setFinalApprove("Not Yet");
                    }

                    // if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                    // if
                    // (taskActivity.getSupervisor().equals(taskActivity.getUser().getFinalApprove()))
                    // {
                    // taskActivity.set_approved(true);
                    // taskActivity.setFinalApprove("Approved");
                    // } else {
                    // taskActivity.setFinalApprove("TL Approved");
                    // }
                    // } else if ("Rejected".equalsIgnoreCase(activityRequest.getStatus())) {

                    // if
                    // (taskActivity.getSupervisor().equals(taskActivity.getUser().getFinalApprove()))
                    // {
                    // taskActivity.set_approved(true);
                    // taskActivity.setFinalApprove("Rejected");
                    // } else {
                    // taskActivity.setFinalApprove("Not Yet");
                    // }
                    // } else {
                    // taskActivity.setFinalApprove("Not Yet");

                    // }
                } else if ("ON Role".equalsIgnoreCase(taskActivity.getUser().getRoleType())) {

                    Optional<Users> findSupervisorId = usersRepository
                            .findByUsername(taskActivity.getUser().getSupervisor());

                    if (taskActivity.getUser().getBranch().equals("Product")) {

                        // Assuming prodOwner contains the format "161,4"
                        String prodOwner = taskActivity.getProduct().getProdOwner();
                        String supervisorId = String.valueOf(findSupervisorId.get().getId()); // convert supervisor ID
                                                                                              // to string

                        String[] prodOwners = prodOwner != null ? prodOwner.split(",") : new String[0];
                        boolean isSupervisorOwner = Arrays.asList(prodOwners).contains(supervisorId);

                        if (isSupervisorOwner) {
                            if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Approved");
                                taskActivity.setOwnerApproved(true);
                                taskActivity.set_approved(true);
                            } else if ("Reject".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Reject");
                                taskActivity.setOwnerApproved(true);
                                taskActivity.set_approved(true);
                            } else {
                                if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                    taskActivity.setOwnerStatus("Supervisor Approved");
                                } else {
                                    taskActivity.setOwnerStatus("Supervisor Not Approved");
                                }
                            }
                        } else {
                            if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Supervisor Approved");
                            } else {
                                taskActivity.setOwnerStatus("Supervisor Not Approved");

                            }
                        }
                    } else if (taskActivity.getUser().getBranch().equals("Technical")) {
                        // if(taskActivity.getProduct().getTechOwner().equals(findSupervisorId.get().getId())){

                        String techOwner = taskActivity.getProduct().getTechOwner();
                        String supervisorId = String.valueOf(findSupervisorId.get().getId()); // convert supervisor ID
                                                                                              // to string
                        String[] techOwners = techOwner != null ? techOwner.split(",") : new String[0]; // Split the
                                                                                                        // techOwner
                                                                                                        // string by
                                                                                                        // comma

                        // Check if any of the techOwner values match the supervisorId
                        boolean isSupervisorOwner = Arrays.asList(techOwners).contains(supervisorId);

                        if (isSupervisorOwner) {
                            // Check the queries
                            if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Approved");
                                taskActivity.setOwnerApproved(true);
                                taskActivity.set_approved(true);
                            } else if ("Reject".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Reject");
                                taskActivity.setOwnerApproved(true);
                                taskActivity.set_approved(true);
                            } else {
                                if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                    taskActivity.setOwnerStatus("Supervisor Approved");
                                } else {
                                    taskActivity.setOwnerStatus("Supervisor Not Approved");

                                }
                            }
                        } else {
                            if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                                taskActivity.setOwnerStatus("Supervisor Approved");
                            } else {
                                taskActivity.setOwnerStatus("Supervisor Not Approved");
                            }
                        }
                    } else {
                        if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                            taskActivity.setOwnerStatus("Supervisor Approved");
                        } else {
                            taskActivity.setOwnerStatus("Supervisor Not Approved");
                        }
                    }
                }
                taskActivity.setSupervisorApproved(true);

                Optional<Product> product_data = productRepository.findById(taskActivity.getProduct().getId());
                // if (product_data != null && ((product_data.get().getProdOwner() != null
                // && product_data.get().getProdOwner().contains(String.valueOf(user_id)))
                // || (product_data.get().getTechOwner() != null
                // && product_data.get().getTechOwner().contains(String.valueOf(user_id))))) {
                // taskActivity.setOwnerApproved(true);
                // }
                commonTimeSheetActivityRepository.save(taskActivity);
            }
        }
        Users supervisorName = usersRepository.findById(user_id).orElse(null);

        // if (activityRequest.getStatus().equalsIgnoreCase("Reject")) {
        // executorService.execute(() -> rejectedTimesheetByTL(data,
        // supervisorName.getName(),activityRequest.getStatus()));
        executorService.execute(() -> {
            try {
                System.out.println("Starting email task...");
                rejectedTimesheetByTL(data, supervisorName.getName(), activityRequest.getStatus());
                System.out.println("Email task completed successfully.");
            } catch (Exception e) {
                System.err.println("Exception occurred during email task: " + e.getMessage());
                e.printStackTrace();
            }
        });
        // }
        String message;
        if (activityRequest.getStatus().equalsIgnoreCase("Approved")) {
            message = "Activity Approved Successfully";
        } else {
            message = "Activity Rejected Successfully";
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));

    }

    // @Override
    // public ResponseEntity<?> updateActivity(ActivityRequest activityRequest) {
    // int user_id = AuthUserData.getUserId();
    // LocalDateTime currDateTime = LocalDateTime.now();
    // List<Integer> data = activityRequest.getId();
    // for (int i = 0; i < data.size(); i++) {
    // Integer activityId = data.get(i);
    // CommonTaskActivityLog activityLog = new CommonTaskActivityLog();
    // activityLog.setTaskActivity(activityId);
    // activityLog.setStatus(activityRequest.getStatus());
    // // Validate remarks
    // String remarks = activityRequest.getRemarks();
    // if (!remarks.matches(".*[\\s\\S]*[a-zA-Z]+[\\s\\S]*")
    // && !activityRequest.getStatus().equalsIgnoreCase("Approved")) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    // .body(new ApiResponse(false, "Remarks must contain alphabets.", null));
    // } else if (remarks.trim().isEmpty() &&
    // !activityRequest.getStatus().equalsIgnoreCase("Approved")) {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
    // .body(new ApiResponse(false, "Remarks cannot be empty or contain only
    // spaces.", null));
    // }
    // activityLog.setRemarks(activityRequest.getRemarks());
    // activityLog.setCreatedBy(user_id);
    // activityLog.setCreated_at(currDateTime);
    // activityLog.setIsDeleted(false);
    // commonTaskActivityLogRepository.save(activityLog);
    // CommonTimeSheetActivity taskActivity =
    // commonTimeSheetActivityRepository.findById(activityId).orElse(null);

    // if (taskActivity != null) {
    // taskActivity.setSupervisorStatus(activityRequest.getStatus());
    // if ("Contract".equalsIgnoreCase(taskActivity.getUser().getRoleType())) {
    // if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
    // taskActivity.setFinalApprove("TL Approved");

    // } else {
    // taskActivity.setFinalApprove("Not Yet");

    // }
    // } else if ("ON Role".equalsIgnoreCase(taskActivity.getUser().getRoleType()))
    // {
    // if ("Approved".equalsIgnoreCase(activityRequest.getStatus())) {
    // taskActivity.setOwnerStatus("Supervisor Approved");
    // } else {
    // taskActivity.setOwnerStatus("Supervisor Not Approved");

    // }
    // }
    // // taskActivity.set_approved(true);
    // // if(!activityRequest.getStatus().equalsIgnoreCase("Reject")) {
    // taskActivity.setSupervisorApproved(true);
    // // }

    // Optional<Product> product_data =
    // productRepository.findById(taskActivity.getProduct().getId());
    // // if (product_data != null && ((product_data.get().getProdOwner() != null
    // // && product_data.get().getProdOwner().contains(String.valueOf(user_id)))
    // // || (product_data.get().getTechOwner() != null
    // // && product_data.get().getTechOwner().contains(String.valueOf(user_id)))))
    // {
    // // taskActivity.setOwnerApproved(true);
    // // }
    // commonTimeSheetActivityRepository.save(taskActivity);
    // }
    // }
    // // if (activityRequest.getStatus().equalsIgnoreCase("Reject")) {
    // Users supervisorName = usersRepository.findById(user_id).orElse(null);
    // // executorService.execute(() -> rejectedTimesheetByTL(data,
    // // supervisorName.getName(),activityRequest.getStatus()));
    // executorService.execute(() -> {
    // try {
    // System.out.println("Starting email task...");
    // rejectedTimesheetByTL(data, supervisorName.getName(),
    // activityRequest.getStatus());
    // System.out.println("Email task completed successfully.");
    // } catch (Exception e) {
    // System.err.println("Exception occurred during email task: " +
    // e.getMessage());
    // e.printStackTrace();
    // }
    // });
    // // }
    // String message;
    // if (activityRequest.getStatus().equalsIgnoreCase("Approved")) {
    // message = "Activity Approved Successfully";
    // } else {
    // message = "Activity Rejected Successfully";
    // }
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // message, null));

    // }

    private void rejectedTimesheetByTL(List<Integer> ids, String supervisorName, String status) {
        int index = 1;
        for (Integer ts_id : ids) {
            CommonTimeSheetActivity taskActivity = commonTimeSheetActivityRepository.findById(ts_id).orElse(null);
            String htmlContent = "<div>" + "<p style='padding-left:5px'>" + "Your timesheet for <b>"
                    + taskActivity.getProduct().getName() + "</b> on <b>" + taskActivity.getActivity_date()
                    + "</b> has been " + status + " by <b>" + supervisorName
                    + "</b>. We kindly request you to review the details by clicking on the \"View Details\" link provided below:"
                    + "</p>" + "<p style='text-align: center;'><a href='" + portalUrl
                    + "' style='color: #007bff; text-decoration: none;font-weight:bold'>" + "View Details" + "</a></p>"
                    + "</div>";
            emailService.sendEmail(taskActivity.getUser().getEmail(),
                    "Timesheet " + status + " by " + supervisorName + " on Neram tool", htmlContent);
            System.out.println(index);
            index++;
        }
    }

    @Override
    public ResponseEntity<?> productownerapproval(ActivityRequest activityRequest) {
        int user_id = AuthUserData.getUserId();
        LocalDateTime currDateTime = LocalDateTime.now();
        List<Integer> data = activityRequest.getId();
        for (int i = 0; i < data.size(); i++) {
            Integer activityId = data.get(i);
            OwnerApprovalLog activityLog = new OwnerApprovalLog();
            activityLog.setTaskActivity(activityId);
            activityLog.setStatus(activityRequest.getStatus());
            activityLog.setRemarks(activityRequest.getRemarks());
            activityLog.setCreatedBy(user_id);
            activityLog.setCreated_at(currDateTime);
            activityLog.setIsDeleted(false);
            ownerApprovalLogRepository.save(activityLog);
            CommonTimeSheetActivity taskActivity = commonTimeSheetActivityRepository.findById(activityId).orElse(null);

            if (taskActivity != null && "Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                taskActivity.set_approved(true);

                taskActivity.setOwnerApproved(true);
                taskActivity.setOwnerStatus("Approved");

                commonTimeSheetActivityRepository.save(taskActivity);
            } else {
                taskActivity.setOwnerApproved(true);
                taskActivity.setOwnerStatus(activityRequest.getStatus());
                commonTimeSheetActivityRepository.save(taskActivity);

            }
        }
        String message;
        if (activityRequest.getStatus().equalsIgnoreCase("Approved")) {
            message = "Activity Approved Successfully";
        } else {
            message = "Activity Rejected Successfully";
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));

    }

    @Override
    public ResponseEntity<?> updateFinalApproved(ActivityRequest activityRequest, String type) {
        int user_id = AuthUserData.getUserId();
        LocalDateTime currDateTime = LocalDateTime.now();
        List<Integer> data = activityRequest.getId();
        for (int i = 0; i < data.size(); i++) {
            Integer activityId = data.get(i);
            FinalApprovalLog activityLog = new FinalApprovalLog();
            activityLog.setTaskActivity(activityId);
            activityLog.setStatus(activityRequest.getStatus());
            activityLog.setRemarks(activityRequest.getRemarks());
            activityLog.setCreatedBy(user_id);
            activityLog.setCreated_at(currDateTime);
            activityLog.setIsDeleted(false);
            activityLog.setSheetType(type);
            finalApprovalLogRepository.save(activityLog);
            if ("Common".equalsIgnoreCase(type)) {
                CommonTimeSheetActivity taskActivity = commonTimeSheetActivityRepository.findById(activityId)
                        .orElse(null);

                if (Objects.equals(taskActivity.getContractStatus(), "finalApproval")) {
                    taskActivity.setSupervisorApproved(true);
                    taskActivity.setSupervisorStatus(activityRequest.getStatus());
                }

                if (taskActivity != null && "Approved".equalsIgnoreCase(activityRequest.getStatus())) {
                    taskActivity.set_approved(true);
                    // taskActivity.setStatus(activityRequest.getStatus());
                }
                taskActivity.setFinalApprove(activityRequest.getStatus());
                commonTimeSheetActivityRepository.save(taskActivity);
            } else {
                TaskActivity taskActivity = taskActivityRepository.findById(activityId).orElse(null);
                if (taskActivity != null) {
                    taskActivity.set_approved(true);
                    taskActivity.setFinalApprove(activityRequest.getStatus());
                    // taskActivity.setStatus(activityRequest.getStatus());
                    taskActivityRepository.save(taskActivity);
                }
            }

        }
        String message;
        if (activityRequest.getStatus().equalsIgnoreCase("Approved")) {
            message = "Activity Approved Successfully";
        } else {
            message = "Activity Rejected Successfully";
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));

    }

    // show list of tasks
    @Override
    public ResponseEntity<?> getDraftsubmit(Boolean status, LocalDate date) {

        int id = AuthUserData.getUserId();
        // Optional<Users> user = usersRepository.findById(id);
        List<CommonTimeSheetActivity> activityLogs = new ArrayList<CommonTimeSheetActivity>();
        if (date != null) {
            activityLogs = commonTimeSheetActivityRepository.findByUserAndActivityDateAndDraftwithDate(status, id,
                    date);
        } else {
            activityLogs = commonTimeSheetActivityRepository.findByUserAndActivityDateAndDraft(status, id);
        }
        activityLogs = activityLogs.stream().sorted(Comparator.comparing(CommonTimeSheetActivity::getId).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Activity Fetched Successfully", activityLogs));
    }

    @Override
    public ResponseEntity<?> getApprovedorRejectList(LocalDate date, String status) {
        int id = AuthUserData.getUserId();
        // Optional<Users> user = usersRepository.findById(id);
        List<CommonTaskActivityLog> activityLogs = commonTaskActivityLogRepository
                .findByUserAndActivityDateAndDraftNative(date, status, id);
        activityLogs = activityLogs.stream().sorted(Comparator.comparing(CommonTaskActivityLog::getId).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Activity Fetched Successfully", activityLogs));
    }

    @Override
    public ResponseEntity<?> updateTaskActivityDraft(TaskActivityRequest taskActivities) {
        List<String> successMessages = new ArrayList<>();
        List<String> notFoundMessages = new ArrayList<>();

        for (TaskActivity taskActivity : taskActivities.getTaskActivities()) {
            Optional<TaskActivity> taskActivityOptional = taskActivityRepository.findById(taskActivity.getId());

            if (taskActivityOptional.isPresent()) {
                TaskActivity existingTaskActivity = taskActivityOptional.get();
                int user_id = AuthUserData.getUserId();
                Optional<Users> createdByUser = usersRepository.findById(user_id);
                existingTaskActivity.setActivity_date(taskActivity.getActivity_date());
                existingTaskActivity.setHours(taskActivity.getHours());
                existingTaskActivity.setDescription(taskActivity.getDescription());
                existingTaskActivity.setStatus(taskActivity.getStatus());
                existingTaskActivity.setFinalApprove("Not Yet");
                existingTaskActivity.setDraft(false);
                existingTaskActivity.setTask(taskActivity.getTask());
                existingTaskActivity.setUser(createdByUser.get());
                existingTaskActivity.setProduct(taskActivity.getProduct());

                taskActivityRepository.save(existingTaskActivity);
                successMessages.add("Updated Successfully for TaskActivity");
            } else {
                notFoundMessages.add("Not Found for TaskActivity ID: " + taskActivity.getId());
            }
        }

        if (!successMessages.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, String.join(", ", successMessages),
                    "Task Time sheet data updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, String.join(", ", notFoundMessages), Collections.emptyList()));
        }
    }

    // @Override
    // public ResponseEntity<?> getCommonTimeSheetbyProductOwner() {
    //
    // List<TaskActivityResponse> activityLogs = new ArrayList<>();
    // List<TaskActivityResponse> userActivityLogs =
    // commonTimeSheetActivityRepository.findTaskDTOsByAndActivityDate()
    // .stream()
    // .map(row -> new TaskActivityResponse((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // ((Timestamp) row[6]).toLocalDateTime(),
    // ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
    // (String) row[10], (String) row[11], (String) row[12], (String) row[13],
    // null))
    // .collect(Collectors.toList());
    // activityLogs.addAll(userActivityLogs);
    // activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet data fetched successfully",
    // activityLogs));
    // }

    public ResponseEntity<?> getCommonTimeSheetbyProductOwner() {
        List<TaskActivityResponse> activityLogs = new ArrayList<>();
        List<Object[]> rows = commonTimeSheetActivityRepository.findTaskDTOsByAndActivityDate();

        for (Object[] row : rows) {
            int id = (int) row[0];
            LocalDate activityDate = convertToDateTime(row[1]).toLocalDate();
            String hours = (String) row[2];
            String description = (String) row[3];
            String status = (String) row[4];
            boolean draft = (boolean) row[5];
            LocalDateTime created_at = ((Timestamp) row[6]).toLocalDateTime();
            LocalDateTime updated_at = ((Timestamp) row[7]).toLocalDateTime();
            boolean is_deleted = (boolean) row[8];
            String userName = (String) row[9];
            String taskName = (String) row[10];
            String productName = (String) row[11];
            String finalApproveStatus = (String) row[12];
            String branch = (String) row[13];
            TaskActivityResponse activityResponse = new TaskActivityResponse(id, activityDate, hours, description,
                    status, draft, created_at, updated_at, is_deleted, userName, taskName, productName,
                    finalApproveStatus, branch, null);

            activityLogs.add(activityResponse);
        }

        activityLogs.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    @Override
    public ResponseEntity<?> getTaskListdata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseEntity<?> viewActivity(int id) {
        Optional<CommonTimeSheetActivity> activityData = commonTimeSheetActivityRepository.findById(id);
        CommonTaskActivityResponse taskResponse = new CommonTaskActivityResponse();
        taskResponse.setActivity_date(activityData.get().getActivity_date());
        taskResponse.setDescription(activityData.get().getDescription());
        taskResponse.setHours(activityData.get().getHours());
        taskResponse.setStatus(activityData.get().getStatus());
        taskResponse.setProductName(activityData.get().getProduct().getName());
        taskResponse.setProductId(activityData.get().getProduct().getId());
        taskResponse.setFinalApproveStatus(activityData.get().getFinalApprove());
        taskResponse.setTaskName(activityData.get().getTask());
        taskResponse.setId(activityData.get().getId());
        return ResponseEntity.ok().body(new ApiResponse(true, "", taskResponse));
    }

    @Override
    public ResponseEntity<?> updateActivity(CommonTaskDraft taskActivity, int id) {
        Optional<CommonTimeSheetActivity> taskData = commonTimeSheetActivityRepository.findById(id);
        if (taskData.isPresent()) {
            int totalHours = 0;
            int totalMinutes = 0;
            CommonTimeSheetActivity existingData = taskData.get();
            int user_id = AuthUserData.getUserId();
            String hours = commonTimeSheetActivityRepository.findbyhoursAndId(taskActivity.getActivity_date(), user_id,
                    id);
            if (hours != null) {

                String[] hoursSplit = hours.split(":");
                String[] newHoursSplit = taskActivity.getHours().split(":");
                int hour = Integer.parseInt(hoursSplit[0]);
                int minute = Integer.parseInt(hoursSplit[1]);
                int newHour = Integer.parseInt(newHoursSplit[0]);
                int newMinute = Integer.parseInt(newHoursSplit[1]);
                totalHours = hour + newHour;
                totalMinutes = minute + newMinute;
                totalHours += totalMinutes / 60;
                totalMinutes %= 60;

            }
            if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Total hours cannot exceed 09:00.", null));
            }
            existingData.setActivity_date(taskActivity.getActivity_date());
            existingData.setDescription(taskActivity.getDescription());
            existingData.setHours(taskActivity.getHours());
            existingData.setTask(taskActivity.getTask());
            existingData.setDraft(false);
            Optional<Users> createdByUser = usersRepository.findById(user_id);
            existingData.setSupervisor(createdByUser.get().getSupervisor());
            if ((existingData.getUser().getRoleType().equalsIgnoreCase("Contract"))) {
                if (!existingData.getFinalApprove().equalsIgnoreCase("Reject")) {
                    existingData.setFinalApprove("Not Yet");
                    existingData.setSupervisorStatus("Resubmit");
                    existingData.set_approved(false);
                    existingData.setSupervisorApproved(false);
                }
                if (existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                    existingData.setFinalApprove("TL Approved");

                }
            } else if ((existingData.getUser().getRoleType().equalsIgnoreCase("ON Role"))) {

                if (existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                    existingData.setOwnerStatus("Supervisor Approved");
                    existingData.setOwnerApproved(false);

                }
                if (!existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                    existingData.set_approved(false);
                    existingData.setSupervisorApproved(false);
                    existingData.setSupervisorStatus("Pending");

                }
            }

            existingData.setStatus(taskActivity.getStatus());

            Product product = productRepository.findById(taskActivity.getProduct()).get();
            existingData.setProduct(product);
            commonTimeSheetActivityRepository.save(existingData);
            return ResponseEntity.ok()
                    .body(new ApiResponse(true, "TimeSheet Updated Successfully", Collections.emptyList()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(true, "Record Not Found", Collections.emptyList()));

        }
    }

    @Override
    public ResponseEntity<?> updateActivity(List<CommonTaskDraft> taskActivities, int id) {
        if (taskActivities == null || taskActivities.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "No activity data provided", null));
        }

        int userId = AuthUserData.getUserId();
        Optional<Users> existinguser = usersRepository.findById(userId);

        // Calculate total hours and minutes from the input array

        for (int i = 0; i < taskActivities.size(); i++) {
            CommonTaskDraft taskActivity = taskActivities.get(i);
            int totalHours = 0; // Reset totalHours for each activity
            int totalMinutes = 0; // Reset totalMinutes for each activity
            String hours = commonTimeSheetActivityRepository.findbyHoursBasedonreSubmit(taskActivity.getActivity_date(),
                    userId, taskActivity.getId());
            if (hours != null) {
                String[] hoursSplit = hours.split(":");
                totalHours += Integer.parseInt(hoursSplit[0]);
                totalMinutes += Integer.parseInt(hoursSplit[1]);
            }
            System.out.println(hours);
            // Fetch hours for the new activity
            String activityHours = taskActivity.getHours();
            String[] activityHoursSplit = activityHours.split(":");
            totalHours += Integer.parseInt(activityHoursSplit[0]);
            totalMinutes += Integer.parseInt(activityHoursSplit[1]);

            // Adjust total hours and minutes
            totalHours += totalMinutes / 60;
            totalMinutes %= 60;
            System.out.println(totalHours);
            System.out.println(totalMinutes);

            if (totalHours > 9 || (totalHours == 9 && totalMinutes > 0)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false,
                        "Total hours cannot exceed 09:00 for date: " + taskActivity.getActivity_date(), null));
            }

            if (i == 0) {
                // Update the existing record with the first item in the list
                Optional<CommonTimeSheetActivity> taskData = commonTimeSheetActivityRepository.findById(id);
                if (taskData.isPresent()) {
                    CommonTimeSheetActivity existingData = taskData.get();
                    existingData.setActivity_date(taskActivity.getActivity_date());
                    existingData.setDescription(taskActivity.getDescription());
                    existingData.setHours(taskActivity.getHours());
                    existingData.setTask(taskActivity.getTask());
                    existingData.setDraft(false);
                    existingData.setUpdated_at(LocalDateTime.now());
                    existingData.setSupervisor(usersRepository.findById(userId).get().getSupervisor());

                    if (existingData.getUser().getRoleType().equalsIgnoreCase("Contract")) {
                        // if (!existingData.getFinalApprove().equalsIgnoreCase("Reject")) {
                        existingData.setFinalApprove("Not Yet");
                        existingData.setSupervisorStatus("Resubmit");
                        existingData.set_approved(false);
                        existingData.setSupervisorApproved(false);
                        // }
                        // if (existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                        // existingData.setFinalApprove("Not Yet");
                        // existingData.setSupervisorStatus("Resubmit");
                        // existingData.set_approved(false);
                        // existingData.setSupervisorApproved(false);
                        // }
                    } else if (existingData.getUser().getRoleType().equalsIgnoreCase("ON Role")) {
                        // if (existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                        existingData.set_approved(false);
                        existingData.setSupervisorApproved(false);
                        existingData.setSupervisorStatus("Resubmit");
                        existingData.setOwnerApproved(false);
                        existingData.setOwnerStatus("pending");
                        // }
                        // if (!existingData.getSupervisorStatus().equalsIgnoreCase("Approved")) {
                        // existingData.set_approved(false);
                        // existingData.setSupervisorApproved(false);
                        // existingData.setSupervisorStatus("Pending");
                        // }
                    }

                    existingData.setStatus(taskActivity.getStatus());
                    existingData.setProduct(productRepository.findById(taskActivity.getProduct()).get());
                    commonTimeSheetActivityRepository.save(existingData);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse(false, "Record Not Found", null));
                }
            } else {

                // Save new entries for subsequent items in the list
                CommonTimeSheetActivity newActivity = new CommonTimeSheetActivity();
                newActivity.setActivity_date(taskActivity.getActivity_date());
                newActivity.setDescription(taskActivity.getDescription());
                newActivity.setHours(taskActivity.getHours());
                newActivity.setTask(taskActivity.getTask());
                newActivity.setDraft(false);
                newActivity.setCreated_at(LocalDateTime.now());
                newActivity.setUpdated_at(LocalDateTime.now());
                newActivity.setSupervisor(usersRepository.findById(userId).get().getSupervisor());
                newActivity.setUser(usersRepository.findById(userId).get());
                newActivity.setStatus(taskActivity.getStatus());
                newActivity.setProduct(productRepository.findById(taskActivity.getProduct()).get());

                if (existinguser.get().getRoleType().equalsIgnoreCase("Contract")) {
                    newActivity.setFinalApprove("Not Yet");
                    newActivity.setSupervisorStatus("Resubmit");
                    newActivity.set_approved(false);
                    newActivity.setSupervisorApproved(false);
                } else if (existinguser.get().getRoleType().equalsIgnoreCase("ON Role")) {
                    newActivity.setOwnerStatus("Not Yet");
                    newActivity.setFinalApprove("Not Yet");
                    newActivity.setOwnerApproved(false);
                    newActivity.set_approved(false);
                    newActivity.setSupervisorApproved(false);
                    newActivity.setSupervisorStatus("Pending");
                }

                commonTimeSheetActivityRepository.save(newActivity);
            }
        }

        return ResponseEntity.ok()
                .body(new ApiResponse(true, "TimeSheet Updated Successfully", Collections.emptyList()));
    }

    @Override
    public String getEmailByDraft() {
        try {
            // final Integer user_id = AuthUserData.getUserId();
            List<Users> userdata = usersRepository.findAll();
            for (Users user : userdata) {
                Optional<Users> createdByUser = usersRepository.findById(user.getId());
                List<CommonTimeSheetActivity> commonTimeSheetActivities = commonTimeSheetActivityRepository
                        .findbyUserIdAndRequestDateanddraftTrue(user.getId(), LocalDate.now());

                if (!commonTimeSheetActivities.isEmpty()) {
                    String recipientEmail = "emayavarman.e@hepl.com";
                    String subject = "Task Pending Activity";

                    // Create the HTML content for the email body
                    String body = "<html><body><p><h2>Hi " + createdByUser.get().getName() + ",</h2></p>"
                            + "<p>You have " + commonTimeSheetActivities.size()
                            + " TimeSheet(s) in draft list. Kindly submit it before the end of the day.</p>"
                            + "<p>We kindly request you to review the details by clicking on the \"View Details\" link provided below:</p>"
                            + "<p style='text-align: center;'><a href='" + portalUrl
                            + "' style='color: #007bff; text-decoration: none;font-weight:bold'>" + "View Details"
                            + "</a></p>" + "</body></html>";

                    // Send the HTML email
                    emailService.sendEmail(recipientEmail, subject, body);
                }
            }
            return "Success";
        } catch (Exception e) {
            // Handle the exception
            e.printStackTrace(); // or log the exception
            return "Error: " + e.getMessage(); // or return an appropriate

        }
    }

    @Override
    public ResponseEntity<?> getDraftsubmitDetail(Boolean status, LocalDate date) {
        int id = AuthUserData.getUserId();
        Optional<Users> user = usersRepository.findById(id);
        List<CommonTimeSheetActivity> activityLogs = commonTimeSheetActivityRepository
                .findByUserAndActivityDateAndDraftDetail(status, id, date);
        activityLogs = activityLogs.stream().sorted(Comparator.comparing(CommonTimeSheetActivity::getId).reversed())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Fetched Successfully", activityLogs));
    }

    public ResponseEntity<?> getSumbittedActivity(int page, int size, LocalDate date, String approverType,
            String status, boolean filter) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            int id = AuthUserData.getUserId();
            Optional<Users> user = usersRepository.findById(id);

            if (!user.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
            }

            List<UserTaskActivityResponse> activityLogs;
            if (filter) {
                activityLogs = getFilteredActivityLogs(user.get(), date, approverType, status, pageable);
            } else {

                activityLogs = getAllActivityLogs(user.get(), pageable);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
        } catch (Exception e) {
            // Handle exceptions appropriately
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred", null));
        }
    }

    private List<UserTaskActivityResponse> getFilteredActivityLogs(Users user, LocalDate date, String approverType,
            String status, Pageable pageable) {

        String role = user.getRoleType();

        if (status != null && status.equalsIgnoreCase("Rejected")) {
            status = "Reject";
        }
        if (approverType != null && approverType.equalsIgnoreCase("Level1")) {
            if (date != null) {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDatepermisionSuperviser(user.getId(), date, status, pageable)
                        .stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());

            } else {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndpermisionSuperviser(user.getId(), status, pageable).stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());

            }
        }

        if (date != null) {
            if (approverType != null && approverType.equalsIgnoreCase("Level2") && role.equalsIgnoreCase("On Role")
                    && status.equalsIgnoreCase("Pending")) {
                String ownerstatus = "Supervisor Approved";
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDatepermisionOwnerStatus(user.getId(), date, ownerstatus,
                                pageable)
                        .stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            } else if (approverType != null && approverType.equalsIgnoreCase("Level2")
                    && role.equalsIgnoreCase("On Role") && !status.equalsIgnoreCase("Pending")) {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDatepermisionOwnerStatus(user.getId(), date, status, pageable)
                        .stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            }

        } else {

            if (approverType != null && approverType.equalsIgnoreCase("Level2") && role.equalsIgnoreCase("On Role")
                    && status.equalsIgnoreCase("Pending")) {
                String ownerstatus = "Supervisor Approved";
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndpermisionOwnerStatus(user.getId(), ownerstatus, pageable).stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            } else if (approverType != null && approverType.equalsIgnoreCase("Level2")
                    && role.equalsIgnoreCase("On Role") && !status.equalsIgnoreCase("Pending")) {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndpermisionOwnerStatus(user.getId(), status, pageable).stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            }

        }

        if (date != null) {
            if (approverType != null && approverType.equalsIgnoreCase("Level2") && role.equalsIgnoreCase("Contract")
                    && status.equalsIgnoreCase("Pending")) {
                String finalStatus = "TL Approved";
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDatepermisionFinalApprover(user.getId(), date, finalStatus,
                                pageable)
                        .stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            } else if (approverType != null && approverType.equalsIgnoreCase("Level2")
                    && role.equalsIgnoreCase("Contract") && !status.equalsIgnoreCase("Pending")) {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDatepermisionFinalApprover(user.getId(), date, status, pageable)
                        .stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            }
        } else {
            if (approverType != null && approverType.equalsIgnoreCase("Level2") && role.equalsIgnoreCase("Contract")
                    && status.equalsIgnoreCase("Pending")) {
                String finalStatus = "TL Approved";
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdpermisionFinalApprover(user.getId(), finalStatus, pageable).stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            } else if (approverType != null && approverType.equalsIgnoreCase("Level2")
                    && role.equalsIgnoreCase("Contract") && !status.equalsIgnoreCase("Pending")) {
                return commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdpermisionFinalApprover(user.getId(), status, pageable).stream()
                        .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
            }
        }
        // return null;

        return commonTimeSheetActivityRepository
                .findTaskDTOsByUserIdAndActivityDatepermision(user.getId(), date, pageable).stream()
                .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
    }

    private List<UserTaskActivityResponse> getAllActivityLogs(Users user, Pageable pageable) {
        return commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivity(user.getId(), pageable).stream()
                .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());
    }

    private UserTaskActivityResponse mapToUserTaskActivityResponse(Object[] row, Users user) {
        int pro_id = (int) row[12];
        boolean userExists = memberRepository.existsByMemberAndProdId(user, pro_id);
        String ownerstatus = "";
        String finalApprovalStatus = "";

        if ((row[17] != null) && ((String) row[17]).equalsIgnoreCase("Supervisor Approved")) {
            ownerstatus = "Pending";
        } else if ((row[16] != null) && !((String) row[16]).equalsIgnoreCase("Approved")) {
            ownerstatus = "Not Yet";
            finalApprovalStatus = "Not Yet";
        } else {
            ownerstatus = (String) row[17];
        }

        if ((row[13] != null) && ((String) row[13]).equalsIgnoreCase("TL Approved")) {
            finalApprovalStatus = "Pending";
        } else {
            finalApprovalStatus = (String) row[13];
        }

        return new UserTaskActivityResponse((Integer) row[0], convertToDateTime(row[1]).toLocalDate(), (String) row[2],
                (String) row[3], (String) row[4], (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(),
                ((Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9], (String) row[10],
                (String) row[11], pro_id, finalApprovalStatus, (String) row[14], false, (String) row[16], ownerstatus,
                userExists);
    }

    @Override
    public ResponseEntity<?> getCommonTimeSheetFinalallbyApproved(int page, int size, String category, int productId,
            String status, int memberId, LocalDate date) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            int id = AuthUserData.getUserId();
            String userid = AuthUserData.getEmpid();

            List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
            category = category.toLowerCase();

            Page<Object[]> userActivityLogs = handleApprovedForFinal(userid, category, productId, memberId, date,
                    status, pageable);

            List<TaskActivityResponseDTO> activityDTO = userActivityLogs.stream().map(row -> {

                int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer) id, (Integer) row[12]);
                Boolean assignedStatus = userAssignedTo == 0;

                return new TaskActivityResponseDTO((Integer) row[0], convertToDateTime(row[1]).toLocalDate(),
                        (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
                        ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                        (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (Integer) row[12],
                        (String) row[13], (String) row[14], assignedStatus);

            }).collect(Collectors.toList());

            activityLogs = new ArrayList<>(activityDTO.size());
            activityLogs.addAll(activityDTO);

            activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet Approved data fetched successfully", activityLogs));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    // public ResponseEntity<?> getCommonTimeSheetFinalallbyApproved(int page, int
    // size, String category, int productId,
    // String status, int memberId, LocalDate date) {
    // try {
    // Pageable pageable = PageRequest.of(page, size);
    // int id = AuthUserData.getUserId();
    // String userid = AuthUserData.getEmpid();
    //
    // // List<Users> user = usersRepository.findByFinalApprove(id);
    // List<TaskActivityResponseDTO> activityLogs = new ArrayList<>();
    // category = category.toLowerCase();
    //
    // Page<Object[]> userActivityLogs = handleApprovedForFinal(userid, category,
    // productId, memberId, date,
    // status, pageable);
    //
    // // List<TaskActivityResponseDTO> activityDTO =
    // userActivityLogs.stream().map(row
    // // -> {
    // // int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer)
    // // row[15], (Integer) row[16]);
    // // Boolean assignedStatus = userAssignedTo == 0;
    // // return new TaskActivityResponseDTO((Integer) row[0],
    // // convertToDateTime(row[1]).toLocalDate(),
    // // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // // ((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8],
    // (String)
    // // row[9],
    // // (String) row[10], (String) row[11], (String) row[12], (String) row[13],
    // // (String) row[14], assignedStatus);
    // // }).collect(Collectors.toList());
    // List<TaskActivityResponseDTO> activityDTO = userActivityLogs.stream().map(row
    // -> {
    //
    // int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer) id,
    // (Integer) row[12]);
    // Boolean assignedStatus = userAssignedTo == 0;
    //
    // return new TaskActivityResponseDTO((Integer) row[0],
    // convertToDateTime(row[1]).toLocalDate(),
    // (String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
    // ((java.sql.Timestamp) row[6]).toLocalDateTime(),
    // ((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String)
    // row[9],
    // (String) row[10], (String) row[11], (Integer) row[12],
    // (String) row[13], (String) row[14], assignedStatus);
    //
    // }).collect(Collectors.toList());
    // activityLogs.addAll(activityDTO);
    //
    // activityLogs.sort(Comparator.comparing(TaskActivityResponseDTO::getId).reversed());
    //
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Task Time sheet Approved data fetched
    // successfully", activityLogs));
    //
    // } catch (Exception e) {
    // e.printStackTrace();
    // return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
    // .body(new ApiResponse(true, "Error", Collections.emptyList()));
    // }
    // }

    private Page<Object[]> handleApprovedForFinal(String authUserId, String category, int productId, int memberId,
            LocalDate date, String status, Pageable pageable) {

        Page<Object[]> product = null;
        switch (category) {
            case "default" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndFinalApproveallApproved(authUserId, pageable);

                break;
            }
            case "date" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityAndApprovedAndDate(
                        authUserId,
                        date, pageable);
                break;
            }
            case "product" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityAndApprovedAndProduct(
                        authUserId,
                        productId, pageable);
                break;
            }
            case "member" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityAndApprovedAndMember(
                        authUserId,
                        memberId, pageable);
                break;
            }
            case "status" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityAndApprovedAndStatus(
                        authUserId,
                        status, pageable);
                break;
            }
            case "dateandproduct" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProduct(authUserId, date, productId,
                                pageable);
                break;
            }
            case "dateandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndStatus(authUserId, date, status, pageable);
                break;
            }
            case "productandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndStatus(
                                authUserId, productId, status, pageable);
                break;
            }
            case "memberandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndMemberAndStatus(
                                authUserId, memberId, status, pageable);
                break;
            }
            case "dateandmember" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndMember(authUserId, date, memberId,
                                pageable);
                break;
            }
            case "memberandproduct" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndMember(
                                authUserId, productId, memberId, pageable);
                break;
            }
            case "all" -> {
                product = commonTimeSheetActivityRepository.findTaskDTOsByUserIdAndActivityAndApprovedAndAll(authUserId,
                        date, productId, memberId, status, pageable);
                break;
            }
            case "dateandproductandmember" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProductAndMember(authUserId, date,
                                productId,
                                memberId, pageable);
                break;
            }
            case "dateandproductandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndProductAndStatus(authUserId, date,
                                productId,
                                status, pageable);
                break;
            }
            case "productandmemberandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndProductAndMemberAndStatus(authUserId, productId,
                                memberId, status, pageable);
                break;
            }
            case "dateandmemberandstatus" -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityAndApprovedAndDateAndMemberAndStatus(authUserId, date, memberId,
                                status, pageable);
                break;
            }

            default -> {
                product = commonTimeSheetActivityRepository
                        .findTaskDTOsByUserIdAndActivityDateAndFinalApproveallDetail(authUserId, pageable);
                break;
            }
        }
        return product;
    }

    @Override
    public ResponseEntity<?> getCommonTimeSheetbyProductOwnerList(int page, int size, String category, int productId,
            int memberId, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = PageRequest.of(page, size);
        List<Integer> productIds = productRepository.findByOwnerInTechOwnerOrProdOwner(AuthUserData.getUserId());
        List<OwnerApprovalDto> activityLogs = new ArrayList<>();
        int userId = AuthUserData.getUserId();
        String branch = AuthUserData.getBranch();
        Page<Object[]> userActivityLogs = handlePendingCommonTimeSheet(userId, category, productId, memberId, startDate,
                endDate, productIds, branch, pageable);

        for (Object[] row : userActivityLogs) {
            Integer pro_id = (Integer) row[15];
            Integer user_id = (Integer) row[16];
            Optional<Users> user = usersRepository.findById(user_id);
            boolean userAssignedTo = memberRepository.existsByMemberAndProdId(user.get(), pro_id);
            // Boolean assignedStatus = userAssignedTo ? true : false;
            OwnerApprovalDto activityDTO = new OwnerApprovalDto((Integer) row[0],
                    convertToDateTime(row[1]).toLocalDate(), (String) row[2], (String) row[3], (String) row[4],
                    (boolean) row[5], ((Timestamp) row[6]).toLocalDateTime(), ((Timestamp) row[7]).toLocalDateTime(),
                    (boolean) row[8], (String) row[9], (String) row[10], (String) row[11], (String) row[12],
                    (String) row[13], (String) row[14], (Integer) row[15], (Integer) row[16], userAssignedTo);
            activityLogs.add(activityDTO);

        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", activityLogs));
    }

    private Page<Object[]> handlePendingCommonTimeSheet(int authUserId, String category, int productId, int memberId,
            LocalDate startDate, LocalDate endDate, List<Integer> productIds, String branch, Pageable pageable) {

        Page<Object[]> product = null;
        switch (category) {
            case "default" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberList(productIds, authUserId, branch,
                        pageable);

                break;
            }
            case "date" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndDate(productIds, authUserId,
                        branch,
                        startDate, endDate, pageable);
                break;
            }
            case "product" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndProduct(productIds, productId,
                        branch,
                        authUserId, pageable);
                break;
            }
            case "member" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndMember(productIds, authUserId,
                        branch,
                        memberId, pageable);
                break;
            }
            case "dateandproduct" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndProductAndDate(productIds,
                        authUserId,
                        branch, startDate, endDate, productId, pageable);
                break;
            }
            case "dateandmember" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndMemberAndDate(productIds,
                        authUserId,
                        branch, startDate, endDate, memberId, pageable);
                break;
            }
            case "memberandproduct" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAndMemberAndProduct(productIds,
                        authUserId, branch, productId, memberId, pageable);
                break;
            }
            case "all" -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberListAll(productIds, authUserId, branch,
                        startDate, endDate, productId, memberId, pageable);
                break;
            }

            default -> {

                product = commonTimeSheetActivityRepository.getProductIdMemberList(productIds, AuthUserData.getUserId(),
                        branch, pageable);
                break;
            }
        }
        return product;
    }

    @Override
    public ResponseEntity<?> getOwnerCommonApprovedorRejectListall(int page, int size, String category, int productId,
            int memberId, String status, LocalDate date) {
        int id = AuthUserData.getUserId();
        String branch = AuthUserData.getBranch();
        Pageable pageable = PageRequest.of(page, size);

        List<String> statuses = getStatus(status.toLowerCase());

        List<TaskApprovalMemberResonse> userActivityLogs = handleCommonApprovedorRejectList(id, category, productId,
                memberId, statuses, date, branch, pageable).stream().map(row -> {
                    int userAssignedTo = memberRepository.countByUserIdAndProdId((Integer) row[14], (Integer) row[15]);
                    Boolean assignedStatus = userAssignedTo != 0;
                    return new TaskApprovalMemberResonse((Integer) row[0], (String) row[1],
                            convertToDateTime(row[2]).toLocalDate(), (String) row[3], (String) row[4], (String) row[5],
                            (boolean) row[6], ((Timestamp) row[7]).toLocalDateTime(),
                            ((Timestamp) row[8]).toLocalDateTime(), (String) row[9], (String) row[10], (String) row[11],
                            (String) row[12], (String) row[13], assignedStatus);
                }).collect(Collectors.toList());

        if (userActivityLogs.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "No data Found ", Collections.emptyList()));

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task Time sheet data fetched successfully", userActivityLogs));
    }

    private List<String> getStatus(String status) {
        List<String> statuses;
        statuses = switch (status) {
            case "all" -> Arrays.asList("Approved", "Reject");
            case "rejected" -> Arrays.asList("Reject");
            case "approved" -> Arrays.asList("Approved");
            default -> Arrays.asList("Approved", "Reject");
        };
        return statuses;
    }

    private List<Object[]> handleCommonApprovedorRejectList(int authUserId, String category, int productId,
            int memberId, List<String> status, LocalDate date, String branch, Pageable pageable) {

        List<Object[]> product = null;
        switch (category) {

            case "product" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndProduct(authUserId,
                        branch, status, productId, pageable);
                break;
            }

            case "default" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwner(authUserId, branch,
                        status,
                        pageable);

                break;
            }

            case "member" -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndMember(authUserId,
                        branch,
                        status, memberId, pageable);
                break;
            }

            case "date" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndDate(authUserId,
                        branch,
                        status, date, pageable);
                break;
            }

            case "dateandproduct" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndDateAndProduct(
                        authUserId,
                        branch, status, date, productId, pageable);
                break;
            }
            case "dateandmember" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndDateAndMember(
                        authUserId,
                        status, date, memberId, pageable);
                break;
            }
            case "memberandproduct" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAndMemberAndProduct(
                        authUserId, branch, status, productId, memberId, pageable);
                break;
            }
            case "all" -> {

                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwnerAll(authUserId, branch,
                        status, date, productId, memberId, pageable);
                break;
            }
            default -> {
                product = commonTaskActivityLogRepository.getActivityLogDetailsByCreatedByOwner(authUserId, branch,
                        status,
                        pageable);
                break;
            }
        }
        return product;
    }

    @Override
    public ResponseEntity<ApiResponse> idBasedCommonTaskActivityList(int id) {

        try {
            Optional<Users> userOptional = usersRepository.findById(AuthUserData.getUserId());
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User not found", Collections.emptyList()));
            }

            Users user = userOptional.get();
            List<UserTaskActivityResponse> activityLogs = commonTimeSheetActivityRepository
                    .findTaskDTOsByUserIdAndActivityRedirect(id).stream()
                    .map(row -> mapToUserTaskActivityResponse(row, user)).collect(Collectors.toList());

            ApiResponse response = new ApiResponse(true, "Activity found", activityLogs);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> deleteTimesheet(List<Integer> ids) {
        try {
            for (Integer id : ids) {
                if (commonTimeSheetActivityRepository.existsById(id)) {
                    commonTimeSheetActivityRepository.deleteById(id);
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body("Timesheet with ID " + id + " not found");
                }
            }
            return ResponseEntity.ok().body("Timesheets deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete timesheets");
        }
    }

    @Override
    public ResponseEntity<?> deleteTimesheetdaterange(int id, LocalDate fromdate, LocalDate todate) {

        commonTimeSheetActivityRepository.deleteByUserIdAndDateRange(id, fromdate, todate);
        return null;
    }

    @Override
    public ResponseEntity<?> insertActivityRequest(int userId, LocalDate fromDate, LocalDate toDate, int sender) {
        try {
            LocalDate currentDate = fromDate;

            while (!currentDate.isAfter(toDate)) {
                activityRequestRepository.insertActivityRequest(currentDate, sender, userId);
                currentDate = currentDate.plusDays(1);
            }

            return ResponseEntity.ok("Activity requests inserted successfully for the date range.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to insert activity requests");
        }
    }

    @Override
    public ResponseEntity<ApiResponse> notEnteredTimesheetMoreThanTwoDays() {

        try {
            String id = AuthUserData.getEmpid();
            int userId = AuthUserData.getUserId();

            LocalDate currentDate = LocalDate.now();
            LocalDate yesterday = currentDate.minusDays(1);
            System.out.println("currentDate : " + currentDate);
            System.out.println("yesterday : " + yesterday);

            List<String> allUsers = usersRepository.findAllUserBySupervisor(id);

            List<String> enteredTimesheets = commonTimeSheetActivityRepository
                    .findEnteredTimesheetsByUserToSuperviserIdAndDateRange(id, yesterday, currentDate);

            System.out.println("enteredTimesheets : " + enteredTimesheets);
            List<String> notEnteredTimeSheet = allUsers.stream()
                    .filter(user -> !enteredTimesheets.contains(user))
                    .collect(Collectors.toList());

            TimeSheetData timeSheetData = new TimeSheetData();
            timeSheetData.setTotalCount(allUsers.size());
            timeSheetData.setActiveCount(enteredTimesheets.size());
            timeSheetData.setInactiveCount(notEnteredTimeSheet.size());
            timeSheetData.setActiveMembers(enteredTimesheets);
            timeSheetData.setInactiveMembers(notEnteredTimeSheet);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Success", timeSheetData));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Failed to get the data", null));
        }

    }

}
