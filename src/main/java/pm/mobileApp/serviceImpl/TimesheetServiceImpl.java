package pm.mobileApp.serviceImpl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.ProductNamesDTO;
import pm.mobileApp.service.TimesheetService;
import pm.mobileApp.dto.AllReportDto;
import pm.mobileApp.dto.InputDto;
import pm.mobileApp.dto.MyReportDetailDto;
import pm.mobileApp.dto.MyTeamReportDto;
import pm.mobileApp.dto.MyTeamTimeSheetDto;
import pm.mobileApp.dto.MyteamFilterDto;
import pm.mobileApp.dto.TaskDetails;
import pm.mobileApp.dto.TaskListDto;
import pm.mobileApp.dto.UserNameDto;
import pm.mobileApp.dto.productActivityDto;
import pm.mobileAppDto.MembersActivityResponse;
import pm.mobileAppDto.MembersActivityUserDto;
import pm.model.users.Users;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.utils.AuthUserData;

@Service
public class TimesheetServiceImpl implements TimesheetService {

    private final CommonTimeSheetActivityRepository activityRepository;

    @Autowired
    private UsersRepository usersRepository;

    public TimesheetServiceImpl(CommonTimeSheetActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public ResponseEntity<ApiResponse> getDraftProductListWithFilters(int page, int size, LocalDate date,
                                                                      String filter, String status) {
        try {
            int userId = AuthUserData.getUserId();
//            Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size);
            Page<Object[]> products;
            boolean draft;
            if (status.equalsIgnoreCase("draft")) {
                draft = true;
            } else {
                draft = false;
            }
            if (date != null && filter != null) {
                products = activityRepository.findProdIdByUserIdAndDraftIsTrue(userId, date, filter, pageable, draft);
            } else if (date != null && filter == null) {
                products = activityRepository.findProdIdByUserIdAndDraftIsTrue(userId, pageable, date, draft);
            } else if (date == null && filter != null) {
                products = activityRepository.findProdIdByUserIdAndDraftIsTrue(userId, pageable, filter, draft);
            } else {
                products = activityRepository.findProdIdByUserIdAndDraftIsTrue(userId, pageable, draft);
            }
            List<ProductNamesDTO> productNamesDTOs = products.getContent().stream()
                    .map(obj -> new ProductNamesDTO((Integer) obj[0], (String) obj[1]))
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", productNamesDTOs));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> getDraftTaskListWithFilters(int page, int size, LocalDate date, int prodId,
                                                                   String status) {
        try {
            int userId = AuthUserData.getUserId();
            // Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size);
            Page<Object[]> products;
            boolean draft;
            if (status.equalsIgnoreCase("draft")) {
                draft = true;
            } else {
                draft = false;
            }
            if (date != null) {
                products = activityRepository.getTaskByProdIdAndUserIdAndDate(userId, pageable, date, prodId, draft);
            } else {
                products = activityRepository.getTaskByProdIdAndUserIdAndDate(userId, pageable, prodId, draft);
            }
            List<TaskListDto> taskListDtos = products.getContent().stream()
                    .map(obj -> new TaskListDto((Integer) obj[0], (String) obj[1], (String) obj[2], (String) obj[3],
                            (String) obj[4], convertToDateTime(obj[5]).toLocalDate()))
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task Time sheet data fetched successfully", taskListDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> getMembersActivityListWithFilters(int page, int size, LocalDate fromdate, LocalDate todate, int userId, String status, String filter, int prodId) {
        String supervisorId = AuthUserData.getEmpid();
        int supapproveId = AuthUserData.getUserId();
    //    Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> activity;
        List<MembersActivityUserDto> userdata;
        //date Based List of members Activity
        if (fromdate != null && todate != null && userId == 0 && status == null && filter != null && prodId == 0) {
            System.out.println("22222222");
            activity = activityRepository.getusersDataByDate(supervisorId, fromdate, todate, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // All  List of members Activity
        } else if (fromdate != null && todate != null && userId == 0 && status == null && filter != null && prodId != 0) {
            System.out.println("212121221");
            activity = activityRepository.getusersDataByDatewithProductId(supervisorId, fromdate, todate, prodId, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // All  List of members Activity
        } else if (fromdate == null && todate == null && userId == 0 && status == null && filter == null) {
            System.out.println("11111111");

            activity = activityRepository.getusersDataAll(supervisorId, pageable);
            userdata = groupbyUser(activity);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // User and date  Based List of members Activity
        } else if (fromdate != null && todate != null && userId != 0 && status == null && filter == null && prodId==0) {
            System.out.println("333333");

            activity = activityRepository.findTasksBySupervisorAndUserWithDate(supervisorId, userId, fromdate, todate, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
//  user id based Based List of members Activity
        }else if (fromdate == null && todate == null && userId != 0 && status == null && filter == null && prodId != 0) {
            System.out.println("323232323232");

            activity = activityRepository.findTasksBySupervisorAndUserWithProdId(supervisorId, userId, prodId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }
        else if (fromdate != null && todate != null && userId != 0 && status == null && filter == null && prodId != 0) {
            System.out.println("323232323232");

            activity = activityRepository.findTasksBySupervisorAndUserWithDateandProdId(supervisorId, userId,fromdate,todate, prodId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }
        else if (fromdate == null && todate == null && userId != 0 && status == null && filter == null && prodId ==0) {
            System.out.println("4444444444");

            activity = activityRepository.findTasksBySupervisorAndUser(supervisorId, userId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
//Approved Activity task List
        } else if (fromdate == null && todate == null && userId == 0 && status != null && filter == null) {
            System.out.println("5555555");

            System.out.println(" status");
            activity = activityRepository.findTasksByApprovedSupervisorByStatus(supapproveId, status, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
//user Based on task Activity Approved or Rejeject   List
        } else if (fromdate == null && todate == null && userId != 0 && status != null && filter == null && prodId ==0) {
            System.out.println("66666666");

            activity = activityRepository.findTasksBySupervisorByUserAndStatus(supapproveId, userId, status, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }else if (fromdate == null && todate == null && userId != 0 && status != null && filter == null && prodId !=0) {
            System.out.println("656556565");

            activity = activityRepository.findTasksBySupervisorByUserAndStatusandProdId(supapproveId, userId, status,prodId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }

        else if (fromdate != null && todate != null && userId != 0 && status != null && filter == null && prodId ==0) {
            System.out.println("66666666");

            activity = activityRepository.findTasksBySupervisorByUserAndStatusandDate(supapproveId, userId, status,fromdate,todate, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }else if (fromdate != null && todate != null && userId != 0 && status != null && filter == null && prodId !=0 ) {
            System.out.println("66666666");

            activity = activityRepository.findTasksBySupervisorByUserAndStatusandDateAndProdId(supapproveId, userId, status,fromdate,todate,prodId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }

        else if (fromdate != null && todate != null && userId == 0 && status != null && filter == null) {
            System.out.println("77777777");

            activity = activityRepository.findTasksByApprovedSupervisorBasedonDate(supapproveId, fromdate, todate, status, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // product based search filter
        } else if (fromdate == null && todate == null && userId == 0 && status == null && filter != null && prodId != 0) {
            System.out.println("8888888");

            activity = activityRepository.findByProductName(supervisorId, prodId, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
// user Based search filter
        } else if (fromdate == null && todate == null && userId != 0 && status == null && filter != null && prodId == 0) {
            System.out.println("999999999");

            activity = activityRepository.findByUserNameByUser(supervisorId, userId, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // user based on status based filter
        } else if (fromdate == null && todate == null && userId != 0 && status == null && filter != null && prodId != 0) {
            System.out.println("1010101010");

            activity = activityRepository.findByUserNameByUserAndProductFilter(supervisorId, userId, prodId, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // user based on status based filter
        }
        // else if (fromdate == null  && todate==null  && userId != 0 && status == null && filter != null && prodId!=0) {
        //     System.out.println("1010101010");

        //     activity = activityRepository.findByUserNameByUser(supervisorId, userId, pageable);
        //     userdata = groupbyUser(activity);
        //     return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        //     // user based on status based filter
        // }
        else if (fromdate != null && todate != null && userId != 0 && status == null && filter != null && prodId == 0) {
            System.out.println("3535353553535");

            activity = activityRepository.findByUserNameByUserandProduct(supervisorId, userId, fromdate, todate, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // user based on status based filter
        } else if (fromdate != null && todate != null && userId != 0 && status == null && filter != null && prodId != 0) {
            System.out.println("414141414141");

            activity = activityRepository.findByUserNameByUseranddatewithProduct(supervisorId, userId, fromdate, todate, prodId, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
            // user based on status based filter
        } else if (fromdate == null && todate == null && userId != 0 && status != null && filter != null && prodId != 0) {
            System.out.println("44444444444444444");
            activity = activityRepository.findByUserIdStatusProductId(supapproveId, userId, prodId, status, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (fromdate == null && todate == null && userId != 0 && status != null && filter != null && prodId ==0) {
            System.out.println("20202020");

            activity = activityRepository.findByUserNameByUserandStatus(supapproveId, userId, status, pageable );
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (fromdate == null && todate == null && userId == 0 && status != null && filter != null && prodId != 0) {
            System.out.println("3131131313113");
            activity = activityRepository.findByProductNameByStatus(supapproveId, prodId, status, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid request parameters", null));
        }

    }


//    private Map<String, List<MembersActivityResponse>> groupTasksByProduct(Page<Object[]> activity) {
//        Map<String, List<MembersActivityResponse>> groupedTasks = new LinkedHashMap<>();
//        for (Object[] result : activity) {
//            String productName = (String) result[1];
//            MembersActivityResponse taskActivity = mapToCommonTaskActivity(result);
//            groupedTasks.computeIfAbsent(productName, k -> new ArrayList<>()).add(taskActivity);
//        }
//        return groupedTasks;
//    }


    private List<Map<String, Object>> groupTasksByProduct(Page<Object[]> activity) {
        Map<String, List<MembersActivityResponse>> groupedTasks = new LinkedHashMap<>();
        for (Object[] result : activity) {
            String productName = (String) result[1]; // Assuming project name is at index 0
            MembersActivityResponse taskActivity = mapToCommonTaskActivity(result);
            groupedTasks.computeIfAbsent(productName, k -> new ArrayList<>()).add(taskActivity);
        }

        List<Map<String, Object>> responseData = new ArrayList<>();
        for (Map.Entry<String, List<MembersActivityResponse>> entry : groupedTasks.entrySet()) {
            Map<String, Object> projectData = new LinkedHashMap<>();
            projectData.put("project", entry.getKey());
            projectData.put("data", entry.getValue());
            responseData.add(projectData);
        }
        return responseData;
    }

    private Map<String, List<MembersActivityResponse>> groupTasksByProductForContract(Page<Object[]> activity) {
        Map<String, List<MembersActivityResponse>> groupedTasks = new LinkedHashMap<>();
        for (Object[] result : activity) {
            String productName = (String) result[1];
            MembersActivityResponse taskActivity = mapToCommonTaskActivityForContract(result);
            groupedTasks.computeIfAbsent(productName, k -> new ArrayList<>()).add(taskActivity);
        }
        return groupedTasks;
    }

    private List<MembersActivityUserDto> groupbyUser(Page<Object[]> activity) {
        List<MembersActivityUserDto> groupedTasks = new ArrayList<>();
        for (Object[] result : activity) {
            MembersActivityUserDto taskActivity = maptouseBasedList(result);
            groupedTasks.add(taskActivity);
        }
        return groupedTasks;
    }

    private MembersActivityResponse mapToCommonTaskActivity(Object[] result) {
        return new MembersActivityResponse(
                result[0].toString(),
                (Integer) result[2], // username
                result[1].toString(), // prodname// id
                convertToDateTime(result[3]).toLocalDate(), // activeDate
                ((Timestamp) result[4]).toLocalDateTime(), // createdOn
                result[5].toString(), // description
                result[7].toString(), // hours
                result[11].toString(), // task
                result[21].toString(), // supervisorStatus
                result[16].toString()
        );
    }

    private MembersActivityResponse mapToCommonTaskActivityForContract(Object[] result) {
        String finalApproveStatus = result[16].toString();

        if ("TL Approved".equals(finalApproveStatus)) {
            finalApproveStatus = "Pending";
        }
        return new MembersActivityResponse(
                result[0].toString(),
                (Integer) result[2], // username
                result[1].toString(), // prodname// id
                convertToDateTime(result[3]).toLocalDate(), // activeDate
                ((Timestamp) result[4]).toLocalDateTime(), // createdOn
                result[5].toString(), // description
                result[7].toString(), // hours
                result[11].toString(), // task
                result[21].toString(), // supervisorStatus
                finalApproveStatus); // finalApproveStatus
    }

    private MembersActivityUserDto maptouseBasedList(Object[] result) {

        return new MembersActivityUserDto(result[0].toString(), (Integer) result[1]);
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

    @Override
    public ResponseEntity<?> getContractMembersActivityListWithFilters(int page, int size, LocalDate date,
                                                                       int userId, String status, boolean filter, int prodId) {
        String finalApprovalId = AuthUserData.getEmpid();
        // Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> activity;
        if (status.equals("Rejected")) {
            status = "Reject";
        }
        List<String> statusList = getStatusList(status);
        List<MembersActivityUserDto> userdata;
        if (date != null && userId == 0 && prodId == 0 && filter) {
            System.out.println("111111");

            activity = activityRepository.getusersDataByDateForApproval(finalApprovalId, date, pageable, statusList);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId != 0 && prodId == 0 && filter) {
            System.out.println("22222222");
            activity = activityRepository.getusersByDateanduseridteForPending(finalApprovalId, date, pageable, statusList, userId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId != 0 && prodId == 0 && filter) {
            System.out.println("565656565");
            activity = activityRepository.getusersByanduseridteForPending(finalApprovalId, pageable, statusList, userId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId == 0 && prodId > 0 && filter) {
            System.out.println("33333333");

            activity = activityRepository.getusersDataByDateForApprovalbyproductid(finalApprovalId, date, pageable, statusList, prodId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId == 0 && prodId > 0 && filter) {
            System.out.println("3131331313131331");
            activity = activityRepository.getusersDataByForApprovalbyproductid(finalApprovalId, pageable, statusList, prodId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));

        } else if (date != null && userId != 0 && prodId > 0 && filter) {
            System.out.println("4444444");
            activity = activityRepository.getusersByDateanduseridandProdIDForPending(finalApprovalId, date, pageable, statusList, userId, prodId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId != 0 && prodId > 0 && filter) {
            System.out.println("55555");
            activity = activityRepository.getusersByuseridandProdIDForPending(finalApprovalId, pageable, statusList, userId, prodId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } 
        else if (date == null && userId > 0 && prodId >0 && !filter) {
            System.out.println("10-10-10-10-10-10-10");
            activity = activityRepository.findTaskByProdIdAndWithoutFilter(finalApprovalId, prodId, pageable, statusList,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }
        else if (date == null && userId == 0) {
            System.out.println("66666666");
            activity = activityRepository.getusersDataAllForApproval(finalApprovalId, pageable, statusList);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        }
        else if (date != null && userId > 0 && prodId > 0 && !filter) {
            System.out.println("999999999");

            activity = activityRepository.findTaskByDateAndProductId(finalApprovalId, date, prodId, pageable, statusList,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
            
        }
        else if (date != null && userId != 0 && !filter) {
            System.out.println("7777777");

            activity = activityRepository.findTasksByFinalApproverAndUserWithDate(finalApprovalId, userId, date,
                    pageable, statusList);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        } else if (date == null && userId != 0 && !filter) {
            System.out.println("8888888");

            activity = activityRepository.findTasksByFinalApproverAndUser(finalApprovalId, userId, pageable,
                    statusList);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }
        else if (date != null && userId > 0 && prodId == 0 && !filter) {
            System.out.println("11-11-11-11-11-11-11");
            activity = activityRepository.findTheTaskByDate(finalApprovalId, date, pageable, statusList,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));

        }
        else {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid request parameters", null));
        }

    }

    @Override
    public ResponseEntity<?> getProductsMembersActivityListWithFilters(int page, int size, LocalDate date, int userId, String status, String filter, int prodId) {
        int authId = AuthUserData.getUserId();
        System.out.println(authId + " : authId");
        String branch = AuthUserData.getBranch();

        // Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size);
        List<String> statusList = null;
        if (status != null){
             statusList = getStatusListOwner(status);
                }
        List<MembersActivityUserDto> userdata;
        Page<Object[]> activity;


        if (date == null && userId <= 0 && filter == null && status == null && prodId == 0) {
            System.out.println("111111111");
            activity = activityRepository.getOwnerBasedData(authId, branch, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId == 0 && filter != null && status == null && prodId > 0) {
            System.out.println("22222222");
            activity = activityRepository.getOwnerBasedDataByUserIdAndProductIdDate(authId, branch, date, prodId, pageable);
            userdata = groupbyUser(activity);
            System.out.println(userdata);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));

        }
        else if (date != null && userId > 0 && filter == null && status == null && prodId > 0) {
            System.out.println("21-21-21-21-21");
            activity = activityRepository.getOwnerBasedDataByDateAndProductId(authId, branch, date, prodId, pageable, userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }

        else if (date != null && userId > 0 && filter == null && status != null && prodId > 0) {
            System.out.println("22-22-22-22-22");
            activity = activityRepository.getOwnerDataWithDateAndProductIdWithTheStatus(authId, branch, date, prodId, statusList, pageable,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
            
        }

        else if (date == null && userId > 0 && filter == null && status == null && prodId > 0) {
            System.out.println("23-23-23-23-23-23");
            activity = activityRepository.getOwnerDataByProductIdOnly(authId, branch, prodId, pageable,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }

        else if (date == null && userId > 0 && filter == null && status != null && prodId > 0) {
            System.out.println("24-24-24-24-24");
            activity = activityRepository.getOwnerDataByTheProductIdWithStatus(authId, branch, prodId, statusList, pageable, userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        }

        else if (date != null && userId > 0 && filter == null && status == null && prodId == 0) {
            System.out.println("25-25-25-25-25");
            activity = activityRepository.getOwnerDataByDateWithoutFilter(authId, branch, date, pageable,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));  
        }

        else if (date != null && userId > 0 && filter == null && status != null && prodId == 0) {
            System.out.println("26-26-26-26-26");
            activity = activityRepository.getOwnerDataByDateWithStatus(authId, branch, date, statusList, pageable,userId);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
            .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));  
        }

        else if (date != null && userId <= 0 && filter != null && status == null && prodId == 0) {
            System.out.println("333333333");
            activity = activityRepository.getOwnerBasedDatawithDate(authId, branch, date, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId > 0 && filter == null && status == null) {
            System.out.println("4444444");
            activity = activityRepository.getOwnerBasedDataByUserId(authId, branch, userId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        } else if (date != null && userId > 0 && prodId > 0 && filter != null && status == null) {
            System.out.println("5555555");
            activity = activityRepository.getOwnerBasedDataByUserIdAndProductIdAndDate(authId, branch, prodId, userId, date, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId > 0 && prodId > 0 && filter != null && status == null) {
            System.out.println("6666666");

            activity = activityRepository.getOwnerBasedDataByUserIdAndProductId(authId, branch, prodId, userId, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId > 0 && filter != null && status == null) {
            System.out.println("7777777");

            activity = activityRepository.getOwnerBasedDataWithFilterUserId(authId, branch, userId, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId <= 0 && prodId > 0 && filter != null && status == null) {
            System.out.println("8888888");

            activity = activityRepository.getOwnerBasedDataByProductId(authId, branch, prodId, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId > 0 && status != null && filter != null && prodId > 0) {
            System.out.println("9999999");

            activity = activityRepository.getOwnerBasedDataWithStatusWithDAteAndFilter(authId, branch, userId, date, prodId, statusList, pageable);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date == null && userId <= 0 && status != null && filter == null && prodId ==0) {
            System.out.println("10101010");

            activity = activityRepository.getOwnerBasedDataWithStatus(authId, branch, pageable,statusList);

            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId <= 0 && status != null) {
            System.out.println("11-111-11-11-11-11");

            activity = activityRepository.getOwnerBasedDataWithStatusWithDate(authId, branch, date, pageable,statusList);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        }else if (date == null && userId != 0 && status != null && filter!=null) {
            System.out.println("12-12-12-12-12-12");

            activity = activityRepository.getOwnerBasedDataWithStatusWithuserId(authId, branch,userId, pageable,statusList);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        }


        else if (date == null && userId <= 0 && status != null && prodId>0) {
            System.out.println("13-13-13-13-13-13");

            activity = activityRepository.getOwnerBasedDataWithStatusWithProductId(authId, branch, pageable,statusList,prodId);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        }

        else if (date == null && userId > 0 && filter == null && status!=null) {
            System.out.println("daya");
            System.out.println("14-14-14-14-14-14");
            activity = activityRepository.getownerBasedDataByUserIdWithStatus(authId, branch, userId, pageable);
            List<Map<String, Object>> groupedTasks = groupTasksByProduct(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", groupedTasks));
        } else if (date == null && userId <= 0 && status != null && prodId==0) {
            System.out.println("15-15-15-15-15-15");
            activity = activityRepository.getownerBasedDataByUserIdWithStatuswithFilter(authId, branch, statusList, pageable);
            userdata = groupbyUser(activity);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        } else if (date != null && userId > 0 && status == null && filter != null && prodId == 0) {
            System.out.println("16-16-16-16-16-16");

            activity = activityRepository.getOwnerBasedDataUserIdAndDate(authId, branch, userId, date, pageable);
            userdata = groupbyUser(activity);
            // System.out.println(userdata);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));

        } else if (date != null && userId > 0 && status != null && filter != null && prodId == 0) {
            System.out.println("17-17-17-17-17-17");

            activity = activityRepository.getOwnerBasedDataWithUserIdStatusWithDAteAndFilter(authId, branch, userId, date, statusList, pageable);
            userdata = groupbyUser(activity);
            System.out.println(userdata);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", userdata));
        }

        return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid request parameters", null));
    }

    private List<String> getStatusList(String status) {
        if ("both".equalsIgnoreCase(status)) {
            return List.of("Reject", "Approved");
        } else if ("Pending".equalsIgnoreCase(status)) {
            return List.of("TL Approved");
        } else {
            return List.of(status);
        }
    }

    private List<String> getStatusListOwner(String status) {
        if ("both".equalsIgnoreCase(status)) {
            return List.of("Reject", "Approved");
        } else {
            return List.of(status);
        }
    }


    @Override
    public ResponseEntity<?> getMyReportDetails(int id, LocalDate date) {
        int userId = id;
        LocalDate activityDate = date;

        List<Object[]> activityDetails = activityRepository.findActivityDetailsByUserIdAndActivityDate(userId, activityDate);

        Map<String, List<TaskDetails>> productDetailsMap = new HashMap<>();

        for (Object[] activityDetail : activityDetails) {
            String productName = (String) activityDetail[6]; // Assuming product name is at index 6
            TaskDetails detail = new TaskDetails(
                    (String) activityDetail[1],  // taskName
                    (String) activityDetail[2],  // hours
                    (String) activityDetail[3],  // description
                    (String) activityDetail[5]   // approverRemarks
            );

            productDetailsMap.computeIfAbsent(productName, k -> new ArrayList<>()).add(detail);
        }

        List<productActivityDto> productActivities = productDetailsMap.entrySet().stream()
                .map(entry -> new productActivityDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        MyReportDetailDto reportDetailDto = new MyReportDetailDto(productActivities);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Task timesheet data fetched successfully", reportDetailDto));
    }

    // @Override
    // public ResponseEntity<?> getMyTeamReportList(InputDto inputDto) {
    //     int superVisorId = AuthUserData.getUserId();
    //     LocalDate fromDate = inputDto.getFromDate();
    //     LocalDate toDate = inputDto.getToDate();
    //     String userName = inputDto.getUserName();

    //     // List<UserNameDto> activityList  = new ArrayList<>();

    //     if(fromDate == null &&  toDate == null && userName == null ){

    //         Optional<Users> supervisor = usersRepository.findById(superVisorId);
    //         String supervisorName = supervisor.get().getUsername();

    //         List<String> activityList = activityRepository.getMyTeamReportDetailBySupervisorId(supervisorName);

    //         return ResponseEntity.status(HttpStatus.OK)
    //             .body(new ApiResponse(true, "data fetched succesfully", activityList));

    //     }

    //     if(fromDate != null &&  toDate != null && userName == null){
    //         Optional<Users> supervisor = usersRepository.findById(superVisorId);
    //         String supervisorName = supervisor.get().getUsername();

    //         List<String> activityList = activityRepository.findDistinctUserNamesBySupervisorAndDateRange(supervisorName, fromDate, toDate);

    //         return ResponseEntity.status(HttpStatus.OK)
    //             .body(new ApiResponse(true, "data fetched succesfully", activityList));
    //     }

    //     if(fromDate != null &&  toDate != null && userName != null){
    //         Optional<Users> supervisor = usersRepository.findById(superVisorId);
    //         String supervisorName = supervisor.get().getUsername();

    //         Optional<Users> user = usersRepository.findByUsername(userName);
    //         int  userId = user.get().getId();

    //         List<String> activityList = activityRepository.findDistinctUserNamesBySupervisorDateRangeAndUserId(supervisorName, fromDate, toDate, userId);

    //         return ResponseEntity.status(HttpStatus.OK)
    //             .body(new ApiResponse(true, "data fetched succesfully", activityList));

    //     }

    //     if(fromDate == null &&  toDate == null && userName != null){

    //         Optional<Users> user = usersRepository.findByUsername(userName);
    //         int  userId = user.get().getId();

    //         List<String> activityList = activityRepository.findDistinctUserNamesByUserId(userId);

    //         return ResponseEntity.status(HttpStatus.OK)
    //             .body(new ApiResponse(true, "data fetched succesfully", activityList));

    //     }

    //     return null;

    // }

    @Override
    public ResponseEntity<?> getMyTeamReportList(InputDto inputDto) {
        int superVisorId = AuthUserData.getUserId();
        LocalDate fromDate = inputDto.getFromDate();
        LocalDate toDate = inputDto.getToDate();
        String userName = inputDto.getUserName();

        List<UserNameDto> activityList = new ArrayList<>();

        if (fromDate == null && toDate == null && userName == null) {

            Optional<Users> supervisor = usersRepository.findById(superVisorId);
            String supervisorName = supervisor.get().getUsername();

            List<Object[]> results = activityRepository.getMyTeamReportDetailBySupervisorId(supervisorName);
            for (Object[] result : results) {
                int userId = (int) result[0];
                String name = (String) result[1];
                activityList.add(new UserNameDto(userId, name));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "data fetched succesfully", activityList));

        }

        if (fromDate != null && toDate != null && userName == null) {
            Optional<Users> supervisor = usersRepository.findById(superVisorId);
            String supervisorName = supervisor.get().getUsername();

            List<Object[]> results = activityRepository.findDistinctUserNamesBySupervisorAndDateRange(supervisorName, fromDate, toDate);
            for (Object[] result : results) {
                int userId = (int) result[0];
                String name = (String) result[1];
                activityList.add(new UserNameDto(userId, name));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "data fetched succesfully", activityList));
        }

        if (fromDate != null && toDate != null && userName != null) {
            Optional<Users> supervisor = usersRepository.findById(superVisorId);
            String supervisorName = supervisor.get().getUsername();

            Optional<Users> user = usersRepository.findByUsername(userName);
            int userId = user.get().getId();

            List<Object[]> results = activityRepository.findDistinctUserNamesBySupervisorDateRangeAndUserId(supervisorName, fromDate, toDate, userId);
            for (Object[] result : results) {
                int empId = (int) result[0];
                String name = (String) result[1];
                activityList.add(new UserNameDto(empId, name));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "data fetched succesfully", activityList));

        }

        if (fromDate == null && toDate == null && userName != null) {

            Optional<Users> user = usersRepository.findByUsername(userName);
            int userIds = user.get().getId();

            List<Object[]> results = activityRepository.findDistinctUserNamesByUserId(userIds);
            for (Object[] result : results) {
                int userId = (int) result[0];
                String name = (String) result[1];
                activityList.add(new UserNameDto(userId, name));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "data fetched succesfully", activityList));

        }

        return null;

    }


    @Override
    public ResponseEntity<?> getMyteamSingleMemberReport(MyteamFilterDto inputDto) {
        int superVisorId = AuthUserData.getUserId();
        LocalDate fromDate = inputDto.getFromDate();
        LocalDate toDate = inputDto.getToDate();
        int userId = inputDto.getUserId();

        if (fromDate == null && toDate == null) {
            Optional<Users> supervisor = usersRepository.findById(superVisorId);
            String supervisorName = supervisor.get().getUsername();

            List<Object[]> activityList = activityRepository.getMyteamSingleMemberReportBySupervisorId(supervisorName, userId);

            Map<LocalDate, Map<String, List<TaskDetails>>> groupedByDateAndProduct = activityList.stream()
                    .sorted(Comparator.comparing((Object[] activityDetail) -> ((java.sql.Date) activityDetail[7]).toLocalDate()).reversed())  // Convert to LocalDate and sort by activityDate in descending order
                    .collect(Collectors.groupingBy(
                            activityDetail -> ((Date) activityDetail[7]).toLocalDate(),  // Group by activityDate
                            LinkedHashMap::new,  // Maintain insertion order
                            Collectors.groupingBy(
                                    activityDetail -> (String) activityDetail[6],  // Group by productName within each date
                                    LinkedHashMap::new,  // Maintain insertion order
                                    Collectors.mapping(activityDetail -> new TaskDetails(
                                            (String) activityDetail[1],  // taskName
                                            (String) activityDetail[2],  // hours
                                            (String) activityDetail[3],  // description
                                            (String) activityDetail[5]   // approverRemarks
                                    ), Collectors.toList())
                            )
                    ));

            List<AllReportDto> allReportList = new ArrayList<>();

            groupedByDateAndProduct.forEach((date, productDetailsMap) -> {
                List<MyTeamTimeSheetDto> productActivities = new ArrayList<>();
                productDetailsMap.forEach((productName, taskDetailsList) -> {
                    MyTeamTimeSheetDto timeSheetDto = new MyTeamTimeSheetDto(productName, taskDetailsList);
                    productActivities.add(timeSheetDto);
                });
                allReportList.add(new AllReportDto(date, productActivities));
            });

            MyTeamReportDto reportDetailDto = new MyTeamReportDto(allReportList);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", reportDetailDto));
        }

        if (fromDate != null && toDate != null) {
            Optional<Users> supervisor = usersRepository.findById(superVisorId);
            String supervisorName = supervisor.get().getUsername();

            List<Object[]> activityList = activityRepository.getMyteamSingleMemberReportBySupervisorIdByDateRange(userId, fromDate, toDate, supervisorName);

            Map<LocalDate, Map<String, List<TaskDetails>>> groupedByDateAndProduct = activityList.stream()
                    .sorted(Comparator.comparing((Object[] activityDetail) -> ((java.sql.Date) activityDetail[7]).toLocalDate()).reversed())  // Convert to LocalDate and sort by activityDate in descending order
                    .collect(Collectors.groupingBy(
                            activityDetail -> ((Date) activityDetail[7]).toLocalDate(),  // Group by activityDate
                            LinkedHashMap::new,  // Maintain insertion order
                            Collectors.groupingBy(
                                    activityDetail -> (String) activityDetail[6],  // Group by productName within each date
                                    LinkedHashMap::new,  // Maintain insertion order
                                    Collectors.mapping(activityDetail -> new TaskDetails(
                                            (String) activityDetail[1],  // taskName
                                            (String) activityDetail[2],  // hours
                                            (String) activityDetail[3],  // description
                                            (String) activityDetail[5]   // approverRemarks
                                    ), Collectors.toList())
                            )
                    ));

            List<AllReportDto> allReportList = new ArrayList<>();

            groupedByDateAndProduct.forEach((date, productDetailsMap) -> {
                List<MyTeamTimeSheetDto> productActivities = new ArrayList<>();
                productDetailsMap.forEach((productName, taskDetailsList) -> {
                    MyTeamTimeSheetDto timeSheetDto = new MyTeamTimeSheetDto(productName, taskDetailsList);
                    productActivities.add(timeSheetDto);
                });
                allReportList.add(new AllReportDto(date, productActivities));
            });

            MyTeamReportDto reportDetailDto = new MyTeamReportDto(allReportList);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Task timesheet data fetched successfully", reportDetailDto));

        }

        return null;
    }


}
