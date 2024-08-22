package pm.serviceImplements;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Service;
import pm.dto.*;
import pm.model.flow.Flow;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;
import pm.model.task.CommonTimeSheetActivity;
import pm.model.users.RoleWiseWidgets;
import pm.model.users.UserWidgets;
import pm.model.users.Users;
import pm.repository.*;
import pm.request.DashboardSetDefault;
import pm.request.DashboardUpdateRequest;
import pm.response.ApiResponse;
import pm.response.ApiResponsePageable;
import pm.response.OwnerDetails;
import pm.response.ProductListResponse;
import pm.service.DashboardService;
import pm.utils.AuthUserData;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private FlowsRepository flowsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private TaskCategoryRepository taskCategoryRepository;

    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;

    @Autowired
    private BussinessCategoriesRepository bussinessCategoriesRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProdApprovalHistoryRepository prodApprovalHistoryRepository;

    @Autowired
    private RoleWiseWidgetsRepository roleWiseWidgetsRepository;

    @Autowired
    private UserWidgetsRepository userWidgetsRepository;

    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;

    @Override
    public ResponseEntity<?> designationMemberCount() {
        Long totalCount = usersRepository.countByMember();
        Long employeeCount = usersRepository.countByDesignation("Employee");
        Long approverCount = usersRepository.countByDesignation("Approver");
        Long ownerCount = usersRepository.countByDesignation("Owner");
        Long headCount = usersRepository.countByDesignation("Head");

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Designation Member Count", new DesignationMemberCountDTO(
                        totalCount,
                        employeeCount, approverCount, ownerCount, headCount)));
    }

    @Override
    public ResponseEntity<?> memberStatusCount() {
        Long memberCount = usersRepository.countByMemberisdeletefalse();
        Long activeCount = usersRepository.countByStatus(true);
        Long inactiveCount = usersRepository.countByStatus(false);

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", memberCount);
        response.put("activeCount", activeCount);
        response.put("inactiveCount", inactiveCount);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Active/Inactive Members Count", response));
    }

    @Override
    public ResponseEntity<?> flowsStatusWiseCount() {
        Long flowsCount = flowsRepository.countByFlows();
        Long flowActiveCount = flowsRepository.countByActiveData();
        Long flowInactiveCount = flowsRepository.countByInactiveData();

        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", flowsCount);
        response.put("activeCount", flowActiveCount);
        response.put("inactiveCount", flowInactiveCount);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Active/Inactive Flows Count", response));

    }

    @Override
    public ResponseEntity<?> designationCount() {
        Long totalCount = rolesRepository.countByRoles();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Designation Count", response));
    }

    @Override
    public ResponseEntity<?> businessCategoryCount() {
        Long totalCount = bussinessCategoriesRepository.countByBusiness();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Business Category Count", response));
    }

    @Override
    public ResponseEntity<?> taskGroupCount() {
        Long totalCount = taskCategoryRepository.countByTaskGroup();
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", totalCount);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Total Task Groups Count", response));
    }

    @Override
    public ResponseEntity<?> teamMemberCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            String authEmpId = AuthUserData.getEmpid();
            String supervisorId = usersRepository.getsupervisorcount(authEmpId);
            if ("true".equalsIgnoreCase(supervisorId)) {
                Long totalMemberCount = usersRepository.countBySupervisorId(authEmpId);
                Long onroleMemberCount = usersRepository.countBySupervisorIdAndRole(authEmpId, "ON Role");
                Long contractMemberCount = usersRepository.countBySupervisorIdAndRole(authEmpId, "Contract");

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("totalCount", totalMemberCount);
                response.put("onroleMemberCount", onroleMemberCount);
                response.put("contractMemberCount", contractMemberCount);
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Under Supervisor Member List Count", response));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> teamMemberActivityStatus() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            String authEmpId = AuthUserData.getEmpid();
            String supervisorId = usersRepository.getsupervisorcount(authEmpId);

            if ("true".equalsIgnoreCase(supervisorId)) {
                Long totalCount = commonTimeSheetActivityRepository.countBySupervisorIdAndStatusAll(authEmpId);
                Long pendingActCount = commonTimeSheetActivityRepository.countBySupervisorIdAndStatus(authEmpId,
                        "Pending");
                Long approvedActCount = commonTimeSheetActivityRepository.countBySupervisorIdAndStatus(authEmpId,
                        "Approved");

                Long rejectedActCount = commonTimeSheetActivityRepository.countBySupervisorIdAndStatus(authEmpId,
                        "Reject");

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("totalCount", totalCount);
                response.put("pendingCount", pendingActCount);
                response.put("approvedCount", approvedActCount);
                response.put("rejectedCount", rejectedActCount);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Under Supervisor Member List Status wise Activity Count",
                                response));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> teamMemberSecondLevelActivity() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            String authEmpId = AuthUserData.getEmpid();
            String supervisorId = usersRepository.getsupervisorcount(authEmpId);

            if ("true".equalsIgnoreCase(supervisorId)) {
                Long totalCount = commonTimeSheetActivityRepository
                        .countBySupervisorIdAndContractMemberAndStatusAll(authEmpId);
                Long pendingActCount = commonTimeSheetActivityRepository
                        .countBySupervisorIdAndContractMemberAndStatus(authEmpId, "TL Approved");
                Long approvedActCount = commonTimeSheetActivityRepository
                        .countBySupervisorIdAndContractMemberAndStatus(authEmpId, "Approved");
                Long rejectedActCount = commonTimeSheetActivityRepository
                        .countBySupervisorIdAndContractMemberAndStatus(authEmpId, "Reject");

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("totalCount", totalCount);
                response.put("pendingCount", pendingActCount);
                response.put("approvedCount", approvedActCount);
                response.put("rejectedCount", rejectedActCount);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Under Supervisor Contract Member List Status wise Activity Count",
                                response));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> submittedActivityStatusCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Long totalCount = commonTimeSheetActivityRepository.countByUserIdAndStatusAll(userId);
            Long pendingActCount = commonTimeSheetActivityRepository.countByUserIdAndStatus(userId, "Pending");
            Long approvedActCount = commonTimeSheetActivityRepository.countByUserIdAndStatus(userId, "Approved");
            Long rejectedActCount = commonTimeSheetActivityRepository.countByUserIdAndStatus(userId, "Reject");

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalCount", totalCount);
            response.put("pendingCount", pendingActCount);
            response.put("approvedCount", approvedActCount);
            response.put("rejectedCount", rejectedActCount);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Submitted Activity Status Count", response));

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> secLevelSubmittedActivityStatusCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            Long totalCount = 0L;
            Long pendingActiCount = 0L;
            Long approvedActiCount = 0L;
            Long rejectedActiCount = 0L;
            String message = "";

            if ("ON Role".equals(user.getRoleType())) { // Correct way to compare strings in Java

                totalCount = commonTimeSheetActivityRepository.countByUserIdAndOnroleAndStatusAll(userId);
                pendingActiCount = commonTimeSheetActivityRepository.countByUserIdAndOnroleAndStatus(userId,
                        "Supervisor Approved");
                approvedActiCount = commonTimeSheetActivityRepository.countByUserIdAndOnroleAndStatus(userId,
                        "Approved");
                rejectedActiCount = commonTimeSheetActivityRepository.countByUserIdAndOnroleAndStatus(userId, "Reject");

                message = "User Submitted Activity Second Level Status Count";
            } else {
                totalCount = commonTimeSheetActivityRepository.countByUserIdAndContractMemberAndStatusAll(userId);
                pendingActiCount = commonTimeSheetActivityRepository.countByUserIdAndContractMemberAndStatus(userId,
                        "TL Approved");
                approvedActiCount = commonTimeSheetActivityRepository.countByUserIdAndContractMemberAndStatus(userId,
                        "Approved");
                rejectedActiCount = commonTimeSheetActivityRepository.countByUserIdAndContractMemberAndStatus(userId,
                        "Reject");

                message = "Contract User Submitted Activity Second Level Status Count";
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalCount", totalCount);
            response.put("pendingCount", pendingActiCount);
            response.put("approvedCount", approvedActiCount);
            response.put("rejectedCount", rejectedActiCount);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, response));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> productStatusWiseCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {

            Users user = userOptional.get();
            if ("Head".equals(user.getDesignation())) {

                Long totalCount = productRepository.countByProductAndStatusAll(userId);
                Long pendingProductCount = productRepository.countByPendingProductAndStatus(userId);
                Long approvedProductCount = productRepository.countByApprovedProductAndStatus(userId);
                Long rejectedProductCount = productRepository.countByRejectedProductAndStatus(userId);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("totalCount", totalCount);
                response.put("pendingCount", pendingProductCount);
                response.put("approvedCount", approvedProductCount);
                response.put("rejectedCount", rejectedProductCount);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Submitted Activity Status Count", response));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> assignedToMemberCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();
            List<String> techOwners = new ArrayList<>(); // Initialize the list

            if ("Technical".equals(user.getBranch())) {
                techOwners = productRepository.findTechOwnersByUserId(userId);
            } else if ("Product".equals(user.getBranch())) {
                techOwners = productRepository.findProductOwnersByUserId(userId);
            } else if ("Data".equals(user.getBranch())) {
                techOwners = productRepository.findDataOwnersByUserId(userId);
            } else if ("HOW".equals(user.getBranch())) {
                techOwners = productRepository.findHowOwnersByUserId(userId);
            }

            // Using a Map to store the count of each number
            Map<String, Long> techOwnerCountMap = new HashMap<>();
            techOwnerCountMap.put("totalCount", 0L); // Initialize the count

            for (String techOwner : techOwners) {
                techOwnerCountMap.put("totalCount", techOwnerCountMap.get("totalCount") + 1);

                String[] numbers = techOwner.split(",");
                for (String number : numbers) {
                    Integer num = Integer.parseInt(number.trim());

                    Optional<Users> userName = usersRepository.findById(num);
                    if (userName.isPresent()) {
                        Users usern = userName.get();
                        String key = usern.getName(); // Use user name as the key
                        techOwnerCountMap.put(key, techOwnerCountMap.getOrDefault(key, 0L) + 1);
                    } else {
                        System.out.println("User not found for ID: " + num);
                    }
                }
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Assigned Owners Product Count", techOwnerCountMap));

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }

    }

    @Override
    public ResponseEntity<?> assignedUnassignedProductCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();

            Long assignedProductCount = 0L;
            Long unassignedProductCount = 0L;
            Long totalProductCount = 0L;

            if ("Technical".equals(user.getBranch())) {
                assignedProductCount = productRepository.findAssignedProductCount(userId);
                unassignedProductCount = productRepository.findUnAssignedTechProductCount(userId);
                totalProductCount = productRepository.findTotalTechProductCount(userId);

            } else if ("Product".equals(user.getBranch())) {
                assignedProductCount = productRepository.findAssignedProdProductCount(userId);
                unassignedProductCount = productRepository.findUnAssignedProdProductCount(userId);
                totalProductCount = productRepository.findTotalProdProductCount(userId);
            }

            if ("Data".equals(user.getBranch())) {
                assignedProductCount = productRepository.findAssignedProductCountData(userId);
                unassignedProductCount = productRepository.findUnAssignedDataCount(userId);
                totalProductCount = productRepository.findTotalTechDataCount(userId);

            } else if ("HOW".equals(user.getBranch())) {
                assignedProductCount = productRepository.findAssignedProdHowCount(userId);
                unassignedProductCount = productRepository.findUnAssignedProdHowCount(userId);
                totalProductCount = productRepository.findTotalProdHowCount(userId);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("totalCount", totalProductCount);
            response.put("assignedCount", assignedProductCount);
            response.put("unassignedCount", unassignedProductCount);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Assigned/Unassigned Product Count", response));

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }

    }

    @Override
    public ResponseEntity<?> approverStatusWiseProductCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();

            if ("Approver".equals(user.getDesignation())) {

                Long totalCount = prodApprovalHistoryRepository.countByApproverDataAndStatusAll(userId);
                Long pendingCount = prodApprovalHistoryRepository.countByApproverDataAndStatus(userId, "Pending");
                Long approvedCount = prodApprovalHistoryRepository.countByApproverDataAndStatus(userId, "Approved");
                Long rejectedCount = prodApprovalHistoryRepository.countByApproverDataAndStatus(userId, "Rejected");

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("totalCount", totalCount);
                response.put("pendingCount", pendingCount);
                response.put("approvedCount", approvedCount);
                response.put("rejectedCount", rejectedCount);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Approver: Product Status Wise Count", response));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> myTimesheetDetails(int filterValue, LocalDate date) {
        try {
            int authUserId = AuthUserData.getUserId();
            List<Object[]> timeSheetActivity;

            if (filterValue == 0) {
                timeSheetActivity = commonTimeSheetActivityRepository.findMyTimesheetData(authUserId, date);
            } else {
                timeSheetActivity = commonTimeSheetActivityRepository.findMyTimesheetDataAndProduct(authUserId, date,
                        filterValue);
            }

            List<Map<String, Object>> timeSheetActivityList = new ArrayList<>();
            for (Object[] row : timeSheetActivity) {
                Map<String, Object> mappedRow = new HashMap<>();
                mappedRow.put("product", row[3]);
                mappedRow.put("hours", row[0]);
                mappedRow.put("description", row[1]);
                mappedRow.put("task", row[2]);
                timeSheetActivityList.add(mappedRow);
            }

            if (!timeSheetActivityList.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Timesheet data fetched successfully", timeSheetActivityList));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "No data available", Collections.emptyList()));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(true, "Not Found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> roleBasedTimesheetDetails(int page, int size, boolean search, String vaule,
                                                       String memberType) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Object[]> activity;

            if ("On Role".equals(memberType) && search) {
                activity = commonTimeSheetActivityRepository.getOverallTimeSheetDataAndSearch(vaule, pageable);
            } else if ("Contract".equals(memberType) && search) {
                activity = commonTimeSheetActivityRepository.getOverallContractPersonTimeSheetDataAndSearch(vaule,
                        pageable);
            } else if ("Contract".equals(memberType)) {
                activity = commonTimeSheetActivityRepository.getOverallContractPersonTimeSheetData(pageable);
            } else if ("On Role".equals(memberType)) {
                activity = commonTimeSheetActivityRepository.getOverallTimeSheetData(pageable);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(true, "Member Type Not Found", Collections.emptyList()));
            }

            List<Map<String, Object>> timeSheetActivityList = activity.stream().map(row -> {
                Map<String, Object> mappedRow = new HashMap<>();
                mappedRow.put("member", row[0]);
                mappedRow.put("date", row[1]);
                mappedRow.put("supervisor", row[2]);
                mappedRow.put("productHead", "");
                mappedRow.put("1stApproval", row[3]);
                mappedRow.put("1stRemarks", row[4]);
                mappedRow.put("2ndApproval", row[5]);
                mappedRow.put("2ndRemarks", "");
                return mappedRow;
            }).collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", timeSheetActivityList));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(true, "Not Found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> widgetSetToEmployee(boolean status, String empid) {
        List<Users> userData = new ArrayList<>();
        if (status) {
            userData = findByUserNameGetAll(empid);
        } else {
            userData = usersRepository.findAll();
        }
        for (Users user : userData) {

            List<Map<String, Object>> allMappedRows = new ArrayList<>();
            List<String> roles = new ArrayList<>();

            Boolean checkSup = usersRepository.existsBySupervisor(user.getUsername());
            if (Boolean.TRUE.equals(checkSup) && "Employee".equals(user.getDesignation())) {
                roles = Arrays.asList(user.getDesignation(), "Supervisor");
            } else if (Boolean.TRUE.equals(checkSup)) {
                roles = Arrays.asList(user.getDesignation(), "Employee", "Supervisor");
            } else if ("Employee".equals(user.getDesignation())) {
                roles = Arrays.asList(user.getDesignation());
            } else {
                roles = Arrays.asList(user.getDesignation(), "Employee");
            }
            List<Object[]> roleWidgets = roleWiseWidgetsRepository.findByRoles(roles);

            for (Object[] s : roleWidgets) {
                Map<String, Object> mappedRow = new HashMap<>();
                mappedRow.put("widgetCount", s[2]);
                mappedRow.put("widgetTable", s[3]);
                allMappedRows.add(mappedRow);
            }

            Map<String, String> combinedData = combineWidgetCounts(allMappedRows);
            String combinedCounts = combinedData.get("widgetCount");
            String combinedTables = combinedData.get("widgetTable");

            String[] widgetCount = combinedCounts.trim().split("\\s*,\\s*");
            List<String> widgetList = Arrays.asList(widgetCount);

            List<String> firstThree;
            if (widgetList.size() >= 3) {
                firstThree = new ArrayList<>(widgetList.subList(0, 3));
            } else {
                firstThree = new ArrayList<>(widgetList);
            }
            List<String> afterFirstThree = new ArrayList<>(widgetList.subList(firstThree.size(), widgetList.size()));
            String[] widgetTable = combinedTables.trim().split("\\s*,\\s*");
            List<String> widgetList2 = Arrays.asList(widgetTable);

            List<String> firstTwoTable;
            if (widgetList.size() < 3) {
                int setSize = 3 - widgetList.size();
                int setSize2 = 2 - setSize;
                int setSize3 = setSize2 + 2;

                if (widgetList2.size() >= 2) {
                    firstTwoTable = new ArrayList<>(widgetList2.subList(0, setSize3));
                } else {
                    firstTwoTable = new ArrayList<>(widgetList2);
                }
            } else if (widgetList2.size() >= 2) {
                firstTwoTable = new ArrayList<>(widgetList2.subList(0, 2));
            } else {
                firstTwoTable = new ArrayList<>(widgetList2);
            }

            List<String> afterfirstTwoTable = new ArrayList<>(
                    widgetList2.subList(firstTwoTable.size(), widgetList2.size()));

            // *** GET TOTAL WIDGETS IN LIST ARRAY
            // *****************************************************//
            List<String> totalWidgets = new ArrayList<>(Arrays.asList(widgetCount));
            // Merge widgetList2 into widgetList3
            totalWidgets.addAll(widgetList2);

            // *** GET TOTAL ASSIGNED WIDGETS
            // *****************************************************//
            // Merge firstThree and firstTwoTable
            List<String> assignedWidgets = new ArrayList<>(firstThree);
            assignedWidgets.addAll(firstTwoTable);

            // *** GET REMAINING WIDGETS
            // *****************************************************//
            // Create a list of remaining widgets by subtracting assigned widgets from total
            // widgets
            List<String> remainingWidgets = new ArrayList<>(totalWidgets);
            remainingWidgets.removeAll(assignedWidgets);

            // Create a new UserWidgets object
            UserWidgets widgets = new UserWidgets();
            widgets.setEmp_id(user.getUsername());
            widgets.setRole(user.getDesignation());
            widgets.setWidget_count(firstThree.toString());
            widgets.setWidget_table(firstTwoTable.toString());
            widgets.setRemaining_widget_count(afterFirstThree.toString());
            widgets.setRemaining_widget_table(afterfirstTwoTable.toString());
            widgets.setRemaining_widget(remainingWidgets.toString());
            widgets.setTotal_widget(totalWidgets.toString());
            userWidgetsRepository.save(widgets); // Use save() instead of saveAll()
        }

        Map<String, Object> response = new HashMap<>();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Widgets Assigned Successfully To The Users..", response));
    }

    public List<Users> findByUserNameGetAll(String empid) {
        List<Users> users = new ArrayList<>();
        Optional<Users> userData = usersRepository.findByUserNameGetAll(empid);
        users.add(userData.get());
        return users;
    }

    public static Map<String, String> combineWidgetCounts(List<Map<String, Object>> dataList) {
        StringBuilder combinedCounts = new StringBuilder();
        StringBuilder combinedTables = new StringBuilder();

        for (Map<String, Object> data : dataList) {
            String widgetCountsString = (String) data.get("widgetCount");
            if (widgetCountsString != null && !widgetCountsString.isEmpty()) {
                // Remove curly braces and append widget counts
                widgetCountsString = widgetCountsString.replaceAll("[{}]", "").trim();
                combinedCounts.append(widgetCountsString).append(", ");
            }

            String widgetTablesString = (String) data.get("widgetTable");
            if (widgetTablesString != null && !widgetTablesString.isEmpty()) {
                // Remove curly braces and append widget counts
                widgetTablesString = widgetTablesString.replaceAll("[{}]", "").trim();
                combinedTables.append(widgetTablesString).append(", ");
            }
        }

        // Remove trailing comma and space
        if (combinedCounts.length() > 0) {
            combinedCounts.setLength(combinedCounts.length() - 2);
        }
        if (combinedTables.length() > 0) {
            combinedTables.setLength(combinedTables.length() - 2);
        }

        Map<String, String> combinedData = new HashMap<>();
        combinedData.put("widgetCount", combinedCounts.toString());
        combinedData.put("widgetTable", combinedTables.toString());

        return combinedData;

    }

    @Override
    public ResponseEntity<?> dashboardSequenceList() {
        try {
            Integer userId = AuthUserData.getUserId();
            Optional<Users> userOptional = usersRepository.findById(userId);

            List<Map<String, Object>> allMappedRows = new ArrayList<>();

            if (userOptional.isPresent()) {
                Users user = userOptional.get();
                List<Object[]> userWidgets = userWidgetsRepository.findBYEmp_idAndStatusActive(user.getUsername());

                ObjectMapper objectMapper = new ObjectMapper();

                for (Object[] s : userWidgets) {
                    Map<String, Object> mappedRow = new HashMap<>();
                    // String[] splitarray = s[3].toString().replace("[", "").replace("]",
                    // "").replace("\"", "").split(",");
                    // for (int i = 0; i < splitarray.length; i++) {
                    // splitarray[i] = splitarray[i].trim();
                    // }
                    // String
                    // countkey=userWidgetsRepository.checkCountPresentorNot(user.getDesignation());
                    //
                    // String tableName =
                    // userWidgetsRepository.checkTablePresentorNot(user.getDesignation());
                    // String countString = countkey.replace("[", "").replace("]", "").replace("\"",
                    // "");
                    // String tableString = tableName.replace("[", "").replace("]",
                    // "").replace("\"", "").replace("{","").replace("}", "");
                    // System.out.println(tableString);
                    //// Split the strings into arrays
                    // String[] countArray = countString.split(", ");
                    //
                    // String[] tableArray = tableString.split(",");
                    //
                    //
                    // StringBuilder countBuilder = new StringBuilder();
                    // StringBuilder tableBuilder = new StringBuilder();
                    //
                    // for (String split : splitarray) {
                    // // Check if the widget name is present in the count array
                    // for (String count : countArray) {
                    // if (count.equalsIgnoreCase(split)) {
                    // // Insert into the count
                    // countBuilder.append(split).append(",");
                    // }
                    // }
                    // // Check if the widget name is present in the table array
                    // for (String table : tableArray) {
                    // if (table.equalsIgnoreCase(split)) {
                    // // Insert into the table
                    // tableBuilder.append(split).append(",");
                    // }
                    // }
                    // }
                    //
                    //// Append the values if they are present
                    // String countValues = countBuilder.toString().trim();
                    // String tableValues = tableBuilder.toString().trim();
                    //
                    // List<String> countValuesList = new ArrayList<>();
                    // List<String> tableValuesList = new ArrayList<>();
                    //
                    //// Split countBuilder and tableBuilder strings by comma and add them to lists
                    // String[] countValuesArray = countValues.split(",");
                    // String[] tableValuesArray = tableValues.split(",");
                    //
                    //// Add count values to the list
                    // for (String value : countValuesArray) {
                    // if (!value.isEmpty()) {
                    // countValuesList.add(value);
                    // }
                    // }
                    //
                    //// Add table values to the list
                    // for (String value : tableValuesArray) {
                    // if (!value.isEmpty()) {
                    // tableValuesList.add(value);
                    // }
                    // }
                    //
                    // mappedRow.put("Remaingcount", countValuesList);
                    // mappedRow.put("Remaingtable", tableValuesList);

                    mappedRow.put("widgetCount",
                            objectMapper.readValue((String) s[0], new TypeReference<List<String>>() {
                            }));
                    mappedRow.put("widgetTable",
                            objectMapper.readValue((String) s[1], new TypeReference<List<String>>() {
                            }));
                    mappedRow.put("totalWidget",
                            objectMapper.readValue((String) s[2], new TypeReference<List<String>>() {
                            }));
                    mappedRow.put("remainingWidget",
                            objectMapper.readValue((String) s[3], new TypeReference<List<String>>() {
                            }));
                    // mappedRow.put("remainingWidgetCount",
                    // objectMapper.readValue((String) s[4], new TypeReference<List<String>>() {
                    // }));
                    if (s[4] != null && s[4] instanceof String) {
                        String jsonString = (String) s[4];
                        List<String> remainingWidgetCount = objectMapper.readValue(jsonString,
                                new TypeReference<List<String>>() {
                                });

                        // Check if the list is empty or not
                        if (remainingWidgetCount.isEmpty()) {
                            System.out.println("The list is empty.");
                            mappedRow.put("remainingWidgetCount", Collections.emptyList());

                        } else {
                            mappedRow.put("remainingWidgetCount", remainingWidgetCount);
                        }
                    } else {
                        System.err.println("Value at index 4 is not a valid JSON string or is null");
                    }

                    mappedRow.put("remainingWidgetTable",
                            objectMapper.readValue((String) s[5], new TypeReference<List<String>>() {
                            }));
                    allMappedRows.add(mappedRow);
                }
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "User Widgets Fetched Successfully", allMappedRows));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User not found", Collections.emptyList()));
            }

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(true, "Not Found", e.getMessage()));
        }
    }

    @Override
    public ResponseEntity<?> timeSheetDaysCountPerMonth(int month, int year) {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            YearMonth yearMonth = YearMonth.of(year, month);
            int totalDaysInMonth = yearMonth.lengthOfMonth();
            Long daysCount = commonTimeSheetActivityRepository.countDistinctDaysInMonth(userId, month, year);
            int leaveCount = attendanceSheetRepository.countByUseridAndMonth(userId, month, year);
            Long notEnteredCount = totalDaysInMonth - (daysCount != null ? daysCount : 0);
            Map<String, Object> response = new HashMap<>();
            response.put("totalDays", daysCount);
            response.put("leaveCount", leaveCount);
            response.put("notEnteredCount", notEnteredCount);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Time Sheet Days Count Per Month Fetched Successfully", response));

        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> flowAccessCountHead() {
        int userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();

            if ("Head".equals(user.getDesignation())) {
                List<Flow> record = flowsRepository.findAll();

                int num = 0;
                int noAccess = 0;
                int totalCount = 0;
                for (Flow s : record) {
                    totalCount = totalCount + 1;
                    List<Integer> accessTo = s.getAccess_to();
                    if (accessTo.contains(3)) {
                        num = num + 1;
                    } else {
                        noAccess = noAccess + 1;
                    }
                }

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("assignedCount", num);
                response.put("unassignedCount", noAccess);
                response.put("totalCount", totalCount);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Flow Access count for Head Fetched Successfully", response));

            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Access Denied", Collections.emptyList()));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> dashboardSequenceUpdate(String empId, String replacementType, String replaceFrom,
                                                     String replaceTo) {
        Optional<UserWidgets> userOptional = userWidgetsRepository.findByEmp_id(empId);

        if (userOptional.isPresent()) {
            UserWidgets userWidgets = userOptional.get();

            /// ************ ********************* ************ //////
            if ("widgetCount".equals(replacementType)) {
                String remaingCWidgets = userWidgets.getRemaining_widget_count();

                // Step 1: Remove the square brackets and quotes, and split the string into
                // elements
                String trimmed2 = remaingCWidgets.substring(1, remaingCWidgets.length() - 1);
                List<String> elements2 = new ArrayList<>(Arrays.asList(trimmed2.split(", ")));

                // Remove the quotes around each element
                elements2 = elements2.stream()
                        .map(element -> element.replaceAll("^\"|\"$", ""))
                        .collect(Collectors.toList());

                // Step 2: Replace elements at the exact place
                for (int i = 0; i < elements2.size(); i++) {
                    if (elements2.get(i).equals(replaceTo)) {
                        elements2.set(i, replaceFrom);
                    }
                }

                // Step 3: Convert the list back to a string representation with quotes and
                // square brackets
                String result2 = elements2.stream()
                        .map(element -> "\"" + element + "\"")
                        .collect(Collectors.joining(", ", "[", "]"));

                // Step 4: Print the result string
                System.out.println(result2);

                // next process to update widget count column
                String widgetCount = userWidgets.getWidget_count();

                // Step 1: Remove the square brackets and quotes, and split the string into
                // elements
                String trimmed = widgetCount.substring(1, widgetCount.length() - 1);
                List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                // Remove the quotes around each element
                elements = elements.stream()
                        .map(element -> element.replaceAll("^\"|\"$", ""))
                        .collect(Collectors.toList());

                // Step 2: Replace elements at the exact place
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).equals(replaceFrom)) {
                        elements.set(i, replaceTo);
                    }
                }

                // Step 3: Convert the list back to a string representation with quotes and
                // square brackets
                String result = elements.stream()
                        .map(element -> "\"" + element + "\"")
                        .collect(Collectors.joining(", ", "[", "]"));

                // Step 4: Print the result string
                System.out.println(result);

                UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                existingWidgets.setWidget_count(result);
                existingWidgets.setRemaining_widget_count(result2);
                userWidgetsRepository.save(existingWidgets);

            } else if ("widgetTable".equals(replacementType)) {

                String remaingTWidgets = userWidgets.getRemaining_widget_table();

                // Step 1: Remove the square brackets and quotes, and split the string into
                // elements
                String trimmed2 = remaingTWidgets.substring(1, remaingTWidgets.length() - 1);
                List<String> elements2 = new ArrayList<>(Arrays.asList(trimmed2.split(", ")));

                // Remove the quotes around each element
                elements2 = elements2.stream()
                        .map(element -> element.replaceAll("^\"|\"$", ""))
                        .collect(Collectors.toList());

                // Step 2: Replace elements at the exact place
                for (int i = 0; i < elements2.size(); i++) {
                    if (elements2.get(i).equals(replaceTo)) {
                        elements2.set(i, replaceFrom);
                    }
                }

                // Step 3: Convert the list back to a string representation with quotes and
                // square brackets
                String result2 = elements2.stream()
                        .map(element -> "\"" + element + "\"")
                        .collect(Collectors.joining(", ", "[", "]"));

                // Step 4: Print the result string
                System.out.println(result2);

                String widgetTable = userWidgets.getWidget_table();

                // Step 1: Remove the square brackets and quotes, and split the string into
                // elements
                String trimmed = widgetTable.substring(1, widgetTable.length() - 1);
                List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                // Remove the quotes around each element
                elements = elements.stream()
                        .map(element -> element.replaceAll("^\"|\"$", ""))
                        .collect(Collectors.toList());

                // Step 2: Replace elements at the exact place
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).equals(replaceFrom)) {
                        elements.set(i, replaceTo);
                    }
                }

                // Step 3: Convert the list back to a string representation with quotes and
                // square brackets
                String result = elements.stream()
                        .map(element -> "\"" + element + "\"")
                        .collect(Collectors.joining(", ", "[", "]"));

                // Step 4: Print the result string
                System.out.println(result);

                UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                existingWidgets.setWidget_table(result);
                existingWidgets.setRemaining_widget_table(result2);
                userWidgetsRepository.save(existingWidgets);
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Dashboard Sequence Updated Successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> dashboardSequenceUpdateList(DashboardUpdateRequest dashboardUpdateRequestList,
                                                         String key) {
        try {
            // for (DashboardUpdateRequest dashboardUpdateRequest :
            // dashboardUpdateRequestList) {
            String empId = dashboardUpdateRequestList.getEmpId();
            String replacementType = dashboardUpdateRequestList.getReplacementType();
            String replaceFrom = dashboardUpdateRequestList.getReplaceFrom();
            String replaceTo = dashboardUpdateRequestList.getReplaceTo();
            Optional<UserWidgets> userOptional = userWidgetsRepository.findByEmp_id(empId);

            if (userOptional.isPresent()) {
                UserWidgets userWidgets = userOptional.get();
                if (key != null && key.equalsIgnoreCase("inner")) {

                    if ("widgetCount".equals(replacementType)) {

                        String widgetCount = userWidgets.getWidget_count();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed = widgetCount.substring(1, widgetCount.length() - 1);
                        List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                        // Remove the quotes around each element
                        elements = elements.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements.size(); i++) {
                            if (elements.get(i).equals(replaceFrom)) {
                                elements.set(i, replaceTo);
                            } else if (elements.get(i).equals(replaceTo)) {
                                elements.set(i, replaceFrom);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result = elements.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                        existingWidgets.setWidget_count(result);
                        userWidgetsRepository.save(existingWidgets);

                    } else if ("widgetTable".equals(replacementType)) {

                        String widgetTable = userWidgets.getWidget_table();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed = widgetTable.substring(1, widgetTable.length() - 1);
                        List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                        // Remove the quotes around each element
                        elements = elements.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements.size(); i++) {
                            if (elements.get(i).equals(replaceFrom)) {
                                elements.set(i, replaceTo);
                            } else if (elements.get(i).equals(replaceTo)) {
                                elements.set(i, replaceFrom);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result = elements.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        // Step 4: Print the result string

                        UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                        existingWidgets.setWidget_table(result);
                        userWidgetsRepository.save(existingWidgets);

                    }
                } else {

                    /// ************ ********************* ************ //////
                    if ("widgetCount".equals(replacementType)) {
                        String remaingCWidgets = userWidgets.getRemaining_widget_count();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed2 = remaingCWidgets.substring(1, remaingCWidgets.length() - 1);
                        List<String> elements2 = new ArrayList<>(Arrays.asList(trimmed2.split(", ")));

                        // Remove the quotes around each element
                        elements2 = elements2.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements2.size(); i++) {
                            if (elements2.get(i).equals(replaceTo)) {
                                elements2.set(i, replaceFrom);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result2 = elements2.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        // Step 4: Print the result string
                        System.out.println(result2);

                        // next process to update widget count column
                        String widgetCount = userWidgets.getWidget_count();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed = widgetCount.substring(1, widgetCount.length() - 1);
                        List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                        // Remove the quotes around each element
                        elements = elements.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements.size(); i++) {
                            if (elements.get(i).equals(replaceFrom)) {
                                elements.set(i, replaceTo);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result = elements.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        // Step 4: Print the result string
                        System.out.println(result);

                        UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                        existingWidgets.setWidget_count(result);
                        existingWidgets.setRemaining_widget_count(result2);
                        userWidgetsRepository.save(existingWidgets);

                    } else if ("widgetTable".equals(replacementType)) {

                        String remaingTWidgets = userWidgets.getRemaining_widget_table();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed2 = remaingTWidgets.substring(1, remaingTWidgets.length() - 1);
                        List<String> elements2 = new ArrayList<>(Arrays.asList(trimmed2.split(", ")));

                        // Remove the quotes around each element
                        elements2 = elements2.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements2.size(); i++) {
                            if (elements2.get(i).equals(replaceTo)) {
                                elements2.set(i, replaceFrom);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result2 = elements2.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        // Step 4: Print the result string
                        System.out.println(result2);

                        String widgetTable = userWidgets.getWidget_table();

                        // Step 1: Remove the square brackets and quotes, and split the string into
                        // elements
                        String trimmed = widgetTable.substring(1, widgetTable.length() - 1);
                        List<String> elements = new ArrayList<>(Arrays.asList(trimmed.split(", ")));

                        // Remove the quotes around each element
                        elements = elements.stream()
                                .map(element -> element.replaceAll("^\"|\"$", ""))
                                .collect(Collectors.toList());

                        // Step 2: Replace elements at the exact place
                        for (int i = 0; i < elements.size(); i++) {
                            if (elements.get(i).equals(replaceFrom)) {
                                elements.set(i, replaceTo);
                            }
                        }

                        // Step 3: Convert the list back to a string representation with quotes and
                        // square brackets
                        String result = elements.stream()
                                .map(element -> "\"" + element + "\"")
                                .collect(Collectors.joining(", ", "[", "]"));

                        // Step 4: Print the result string
                        System.out.println(result);

                        UserWidgets existingWidgets = userWidgetsRepository.findByEmp_id(empId).orElse(null);
                        existingWidgets.setWidget_table(result);
                        existingWidgets.setRemaining_widget_table(result2);
                        userWidgetsRepository.save(existingWidgets);
                    }

                }
            } else {
                // User not found, handle the error
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User not found for empId: " + empId, null));
            }
            // }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Dashboard updated successfully", Collections.emptyList()));
        } catch (Exception e) {
            // Handle any unexpected exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred: " + e.getMessage(), null));
        }
    }

    public ResponseEntity<?> dashboardContractUserAndSupervisorList() {
        // Get counts
        Long contractMemberCount = usersRepository.countByContractMember();
        Long supervisorCount = usersRepository.countBySupervisor();

        // Get username list
        List<Object[]> usernameList = usersRepository.getListOfMemeberAndSupervisorName();

        // Create a custom response object to encapsulate counts and usernames
        DashboardResponse dashboardResponse = new DashboardResponse();
        dashboardResponse.setContractMemberCount(contractMemberCount);
        dashboardResponse.setSupervisorCount(supervisorCount);

        // Populate the username list
        List<DashBoardName> usernames = new ArrayList<>();
        for (Object[] row : usernameList) {
            DashBoardName response = new DashBoardName();
            response.setUsername((String) row[0]);
            response.setSupervisorName((String) row[1]); // Assuming row[1] is the username
            usernames.add(response);
        }
        dashboardResponse.setUsernames(usernames);

        // Return the custom response object
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Dashboard", dashboardResponse));
    }

    public ResponseEntity<?> dashboardWeeklyBasedTimeline(LocalDate fromdate, LocalDate todate) {
        List<Object[]> result;
        int userId = AuthUserData.getUserId();
        LocalDate currentDate = LocalDate.now();
        List<CommonResponse> commmonresponse = new ArrayList<>();
        List<Map<String, Object>> responseDataList = new ArrayList<>();
        if (fromdate != null && todate != null) {

            List<LocalDate> dateList = new ArrayList<>();
            dateList.add(fromdate);
            // Start from the day after fromDate and end on toDate
            LocalDate currentDateminus = fromdate.plusDays(1);
            while (!currentDateminus.isAfter(todate)) {
                dateList.add(currentDateminus);
                currentDateminus = currentDateminus.plusDays(1);
            }

            for (LocalDate date : dateList) {
                Map<String, Object> responseData = new HashMap<>();

                List<Object[]> response = commonTimeSheetActivityRepository.getWeeklyBasedTimeline(userId, date);
                if (response.size() >= 1) {
                    for (Object[] responsevalue : response) {
                        // Check if the response array has at least one element
                        Time activityTime = (Time) responsevalue[0]; // Access the first element of the response array
                        LocalTime localActivityTime = activityTime.toLocalTime();
                        System.out.println(localActivityTime);
                        responseData.put("time", localActivityTime);
                        if (activityTime.toLocalTime().getHour() >= 4) {
                            responseData.put("color", "#00AB55B2");
                        } else {
                            responseData.put("color", "#e90b20");

                        }
                    }
                } else {
                    // Handle the case where the response array is empty
                    responseData.put("time", "");
                    responseData.put("color", "");

                }
                responseData.put("Date", date);

                // Add date and corresponding responses to the list
                responseDataList.add(responseData);
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Dashboard", responseDataList));

            // result = commonTimeSheetActivityRepository.getWeeklyBasedTimeline(userId,
            // currentDate, startDate);

        } else {

            for (int i = 0; i < 7; i++) {
                Map<String, Object> responseData = new HashMap<>();

                LocalDate dateValue = currentDate.minusDays(i);
                // Add the date to the list
                System.out.println(dateValue);
                List<Object[]> response = commonTimeSheetActivityRepository.getWeeklyBasedTimeline(userId, dateValue);
                System.out.println(response.size());

                if (response.size() >= 1) {
                    for (Object[] responsevalue : response) {
                        // Check if the response array has at least one element
                        Time activityTime = (Time) responsevalue[0]; // Access the first element of the response array
                        LocalTime localActivityTime = activityTime.toLocalTime();
                        System.out.println(localActivityTime);
                        responseData.put("time", localActivityTime);
                        if (activityTime.toLocalTime().getHour() >= 4) {
                            responseData.put("color", "#00AB55B2");
                        } else {
                            responseData.put("color", "#e90b20");

                        }
                    }
                } else {
                    // Handle the case where the response array is empty
                    responseData.put("time", "");
                    responseData.put("color", "");

                }

                responseData.put("Date", dateValue);

                // Add date and corresponding responses to the list
                responseDataList.add(responseData);
            }

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Dashboard", responseDataList));

        }

    }

    public ResponseEntity<?> masterCategoryCount() {
        Map<String, Object> response = new HashMap<>();
        Long bussinessCount = bussinessCategoriesRepository.countByBusiness();
        Long roleCount = rolesRepository.countByRoles();
        Long taskcategoryCount = taskCategoryRepository.countByTaskGroup();
        Long userCount = usersRepository.countByMemberisdeletefalse();
        response.put("bussinessCount", bussinessCount);
        response.put("roleCount", roleCount);
        response.put("taskcategoryCount", taskcategoryCount);
        response.put("userCount", userCount);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Master Category Count", response));

    }

    public ResponseEntity<?> dashboardDefaultPageUpdate(List<DashboardSetDefault> dashboardSetDefaults) {
        try {
            String empId = AuthUserData.getEmpid();

            Optional<UserWidgets> userOptional = userWidgetsRepository.findByEmp_id(empId);
            if (userOptional.isPresent()) {
                UserWidgets userWidgets = userOptional.get();

                System.out.println(dashboardSetDefaults + "       dashboardSetDefaults");

                // Assuming dashboardSetDefaults has at least one element
                if (!dashboardSetDefaults.isEmpty()) {
                    DashboardSetDefault defaultSettings = dashboardSetDefaults.get(0);

                    // Convert List<String> to String representations

                    // Set converted strings to userWidgets
                    userWidgets.setWidget_count(dashboardSetDefaults.get(0).getWidgetCount().toString());
                    userWidgets.setWidget_table(dashboardSetDefaults.get(0).getWidgetTable().toString());
                    userWidgets.setTotal_widget(dashboardSetDefaults.get(0).getTotalWidget().toString());
                    userWidgets.setRemaining_widget_count(
                            dashboardSetDefaults.get(0).getRemainingWidgetCount().toString());
                    userWidgets.setRemaining_widget_table(
                            dashboardSetDefaults.get(0).getRemainingWidgetTable().toString());
                    userWidgets.setRemaining_widget(dashboardSetDefaults.get(0).getRemainingWidget().toString());

                    userWidgets = userWidgetsRepository.save(userWidgets);

                    return ResponseEntity.status(HttpStatus.OK)
                            .body(new ApiResponse(true, "Default settings updated successfully", userWidgets));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "No default settings provided"));
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User with empId " + empId + " not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error occurred while updating default settings", e.getMessage()));
        }
    }

    private LocalDateTime convertToDateTime(Object dateObject) {
        if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toLocalDateTime();
        } else if (dateObject instanceof java.sql.Date) {
            return ((Date) dateObject).toLocalDate().atStartOfDay();
        } else {
            throw new IllegalArgumentException("Unsupported date type");
        }
    }

    public ResponseEntity<ApiResponsePageable> userBasedMonthdata(String month, String year, int page, int size) {
        try {
            String empid = AuthUserData.getEmpid();
            Pageable pageable = PageRequest.of(page, size);
            Page<Users> userPage = usersRepository.findBySupervisor(empid, pageable);

            int monthe = Integer.parseInt(month);
            int yearr = Integer.parseInt(year);

            LocalDate firstDate = LocalDate.of(yearr, monthe, 1);

            LocalDate today = LocalDate.now();
            LocalDate lastDate;
            if (today.getYear() == yearr && today.getMonthValue() == monthe) {
                lastDate = today;
            } else {
                YearMonth yearMonth = YearMonth.of(yearr, monthe);
                lastDate = yearMonth.atEndOfMonth();
            }
            long totalDays = ChronoUnit.DAYS.between(firstDate, lastDate) + 1;
            List<Map<String, Object>> dashboardMonthListAll = new ArrayList<>();

            for (Users user : userPage.getContent()) {
                Map<String, Object> dashboardMonthList = new HashMap<>();
                Long enteredCount = 0L;
                Long notEnterCount = 0L;

                for (LocalDate date = firstDate; !date.isAfter(lastDate); date = date.plusDays(1)) {
                    Long countEntries = commonTimeSheetActivityRepository.countEntriesOnDate(user.getId(), date);

                    if (countEntries > 0) {
                        enteredCount++;
                    } else {
                        notEnterCount++;
                    }
                }

                dashboardMonthList.put("name", user.getName());
                dashboardMonthList.put("totalDays", totalDays);
                dashboardMonthList.put("entered", enteredCount);
                dashboardMonthList.put("notEntered", notEnterCount);
                dashboardMonthListAll.add(dashboardMonthList);
            }

            ApiResponsePageable apiResponse = new ApiResponsePageable();
            apiResponse.setData(dashboardMonthListAll);
            apiResponse.setMessage("User-based month data retrieved successfully");
            apiResponse.setStatus(true);
            apiResponse.setTotalElements(userPage.getTotalElements());
            apiResponse.setTotalPages(userPage.getTotalPages());
            apiResponse.setCurrentPage(userPage.getNumber());

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponsePageable(false, "Error occurred while retrieving user-based month data", e.getMessage()));
        }
    }

    public ResponseEntity<?> dashboardAddDashboard(String designation, String replacementType, String value) {
        List<Users> users = new ArrayList<>();
        List<String> supervisors = new ArrayList<>();
        switch (designation) {
            case "head" -> users = usersRepository.findByDesignation("Head");
            case "supervisor" -> supervisors = usersRepository.findAllSupervisor();
            case "owner" -> users = usersRepository.findByDesignation("Owner");
            default ->
                    users = usersRepository.findByDesignationall(List.of("Employee", "Head", "Owner", "Admin", "Approver"));
        }

        if (designation.equals("supervisor")) {
            users = usersRepository.findByUsernames(supervisors);
        }

        for (Users user : users) {
            System.out.println(user.getName());
            List<Object[]> widgetInfoList = userWidgetsRepository.findByEmpId(user.getUsername());
            if (widgetInfoList != null && !widgetInfoList.isEmpty()) {
                for (Object[] widgetInfo : widgetInfoList) {
                    String str = "";
                    String count = "";
                    String table = "";

                        str = widgetInfo[0] != null ? widgetInfo[0].toString() : "";
                    count = widgetInfo[1] != null ? widgetInfo[1].toString() : "";
                    table = widgetInfo[2] != null ? widgetInfo[2].toString() : "[]";


                    List<String> strList = parseStringToList(str);
                        strList.add("\"" + value + "\"");
                        str = strList.toString();
                    System.out.println(str);
                    if (replacementType.equals("count")) {


                            if (count.length() > 3) {
                                List<String> cardList = parseStringToList(count);
                                cardList.add("\"" + value + "\"");
                                count = cardList.toString();
                            }else{

                            }
                            System.out.println(count);


                    } else {

                            List<String> tableList = parseStringToList(table);
                            tableList.add("\"" + value + "\"");
                            table = tableList.toString();
                            System.out.println(table);

                    }
                      userWidgetsRepository.updateWidgetInfo(user.getUsername(), str, count, table);

                }

                // Update the userWidgetsRepository with the new data

            }
        }

        return ResponseEntity.status(HttpStatus.OK).body("success");
    }

    // Helper method to parse a string to a list
    private List<String> parseStringToList(String str) {
        str = str.substring(1, str.length() - 1); // Remove the brackets
        return new ArrayList<>(Arrays.asList(str.split(", ")));
    }

}
