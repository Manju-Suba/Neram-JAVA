package pm.serviceImplements;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.FinalApproveMemberListDTO;
import pm.dto.UserDTO;
import pm.dto.UserTaskActivityResponse;
import pm.dto.UserTaskActivityResponseStatus;
import pm.model.attendanceSheet.AttendanceSheet;
import pm.model.task.CommonTimeSheetActivity;
import pm.model.users.Users;
import pm.repository.AttendanceSheetRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.MemberRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.service.MemberSupervisorService;
import pm.utils.AuthUserData;
import pm.utils.CommonFunct;

@Service
public class MemberSupervisorServiceImp implements MemberSupervisorService {

    @Autowired
    private UsersRepository userRepo;
    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;
    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;
    @Value("${fileBasePath}")
    private String fileBasePath;

    @Autowired
    private CommonFunct commonFunct;

    @Autowired
    private MemberRepository memberRepo;

    @Override
    public ResponseEntity<?> findAllSupervisorIds(String company) {

        List<String> supervisorIds = new ArrayList<>(); // Initialize the list
        List<String> supervisors = userRepo.findAllSupervisor();
        if (company != null) {
            if (company.equalsIgnoreCase("hepl")) {
                for (String companysupervisor : supervisors) {

                    supervisorIds.add(userRepo.findAllSupervisorbasedCompany(companysupervisor));

                }

            } else {
                for (String companysupervisor : supervisors) {
                    // if(!companysupervisor.contains("0")) {
                    supervisorIds.add(userRepo.findAllSupervisorbasedCompanywithothepl(companysupervisor));
                    // }
                }
            }

        } else {
            supervisorIds = supervisors;
        }
        List<UserDTO> supervisorsDTO = new ArrayList<>();

        for (String supervisorId : supervisorIds) {
            Users supervisor = userRepo.findByUsername(supervisorId).orElse(null);

            if (supervisor != null) {
                UserDTO dto = new UserDTO(supervisor.getId(),
                        supervisor.getName(),
                        supervisor.getRole_id().stream()
                                .map(role -> role.getName())
                                .collect(Collectors.joining(", ")),
                        supervisor.getEmail(),
                        supervisor.getProfile_pic(),
                        supervisor.getBranch());
                supervisorsDTO.add(dto);
            }
        }

        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(supervisorsDTO);

        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, modifiedUserDTOs));
    }

    // @Override
    // public ResponseEntity<?> getMembersUnderSupervisor(Integer supervisorId) {
    // List<Users> supervisorMembers = userRepo.findBySupervisor(supervisorId);

    // // Convert entities to DTOs
    // List<UserDTO> userDTOs = convertEntitiesToDTOs(supervisorMembers);

    // String message = "Members under Supervisor Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, userDTOs));
    // }

    // new concept dev
    @Override
    public ResponseEntity<?> getMembersUnderSupervisor(int supervisorId) {
        List<Users> supervisorMembers = userRepo
                .findByStatusandActiveEmployees(userRepo.findByIdGetUsername(supervisorId));
        // Convert entities to DTOs
        List<UserDTO> userDTOs = convertEntitiesToDTOs(supervisorMembers);

        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(userDTOs);

        // Check if the profile picture exists for each user and update the DTO
        // accordingly
        // for (UserDTO userDTO : userDTOs) {
        // String profilePicPath = fileBasePath + userDTO.getProfile_pic(); // Assuming
        // profilePic is the file name
        // String profilegetPath = userDTO.getProfile_pic(); // direct file name passed
        // here...
        // Path filePath = Paths.get(profilePicPath);

        // // Check if the profile picture exists
        // if (Files.exists(filePath)) {
        // userDTO.setProfile_pic(profilegetPath);
        // } else {
        // userDTO.setProfile_pic(null);
        // }
        // }

        String message = "Members under Supervisor Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, modifiedUserDTOs));
    }

    @Override
    public ResponseEntity<?> getMembersUnderSupervisorBasedCompany(int supervisorId) {
        List<Users> supervisorMembers = userRepo
                .findByStatusandActiveEmployees(userRepo.findByIdGetUsername(supervisorId));
        List<HashMap<String, Object>> members = new ArrayList<>();
        for (Users member : supervisorMembers) {
            HashMap<String, Object> memberMap = new HashMap<>();
            memberMap.put("name", member.getName());
            memberMap.put("userName", member.getUsername());
            String profilePicPath = fileBasePath + member.getProfile_pic(); // Assuming profilePic is the file name
            String profilegetPath = member.getProfile_pic(); // direct file name passed here...
            Path filePath = Paths.get(profilePicPath);

            // Check if the profile picture exists
            if (Files.exists(filePath)) {
                memberMap.put("profile_pic", profilegetPath);
            } else {
                memberMap.put("profile_pic", null);

            }
            String companyName = "";
            if (!member.getEmail().isEmpty()) {
                String email = member.getEmail();
                String emailDomain = email.substring(email.lastIndexOf("@") + 1);
                companyName = emailDomain.substring(0, emailDomain.lastIndexOf("."));
            }
            memberMap.put("company", companyName);
            members.add(memberMap);
        }

        String message = "Members under Supervisor Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, members));
    }

    @Override
    public ResponseEntity<?> getMembersUnderFinalApprove(Integer finalApproveId) {
        List<Users> supervisorMembers = userRepo.findByFinalApproveAndIsActive(AuthUserData.getEmpid());
        List<UserDTO> UserList = new ArrayList<>();
        for (Users user : supervisorMembers) {
            UserDTO finalapprove = new UserDTO();
            finalapprove.setId(user.getId());
            finalapprove.setName(user.getName());
            // userDTO.setName(user.getUsername());
            finalapprove.setProfile_pic(user.getProfile_pic());
            finalapprove.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            finalapprove.setEmail(user.getEmail());
            UserList.add(finalapprove);
        }

        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(UserList);

        // Convert entities to DTOs
        // List<UserDTO> userDTOs = convertEntitiesToDTOs(supervisorMembers);

        String message = "Members under finalApprove Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, modifiedUserDTOs));
    }

    private List<UserDTO> convertEntitiesToDTOs(List<Users> usersList) {

        List<UserDTO> userDTOs = new ArrayList<>();
        for (Users user : usersList) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            // userDTO.setName(user.getUsername());
            userDTO.setProfile_pic(user.getProfile_pic());
            userDTO.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            userDTO.setEmail(user.getEmail());
            userDTO.setBranch(user.getBranch());
            userDTO.setUserName(user.getUsername());
            // map other fields
            userDTOs.add(userDTO);
        }
        return userDTOs;
    }

    @Override
    public ResponseEntity<?> getReport(Integer id, LocalDate date, String status) {
        // Retrieve user data
        Optional<Users> userDataOptional = userRepo.findById(id);
        if (userDataOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
        }
        Users userdata = userDataOptional.get();

        CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                .findUserIdAndActivityDate(date, id);
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

        if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
                ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {

            UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
            datadto.setId(userdata.getId());
            datadto.setUserName(userdata.getName());
            datadto.setBranch(userdata.getBranch());
            datadto.setRoleType(userdata.getRoleType());
            datadto.setSupervisorName(
                    userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor()) : null);
            datadto.setStatus(userIdAndDateList == null ? "Not Entered" : "Entered");
            datadto.setActivity_date(date);

            // If data is entered, fetch additional details
            if (userIdAndDateList != null) {
                String hours = commonTimeSheetActivityRepository.findbyhours(date, id);
                // activity
                datadto.setActivity_date(userIdAndDateList.getActivity_date());
                datadto.setId(userIdAndDateList.getId());
                datadto.setUserName(userIdAndDateList.getUser().getName());
                datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                datadto.setBranch(userIdAndDateList.getBranch());
                datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
                datadto.setSupervisorName(userRepo.findByUserNameGetName(userIdAndDateList.getUser().getSupervisor()));
                datadto.setHours(hours);
            }

            dataList.add(datadto);
        }

        String message = "Data Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, dataList));
    }

    // daterange mani code
    // public ResponseEntity<?> getReportDetail(Integer id, LocalDate fromDate,
    // LocalDate toDate, String status) {
    // Optional<Users> userDataOptional = userRepo.findById(id);
    // if (userDataOptional.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new
    // ApiResponse(false, "User not found", null));
    // }
    // Users userdata = userDataOptional.get();
    // List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();
    //
    // List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
    // for (LocalDate date : dateRange) {
    // List<CommonTimeSheetActivity> userIdAndDateList =
    // commonTimeSheetActivityRepository
    // .findUserIdAndActivityDate(date, id);
    //
    // if (("entered".equalsIgnoreCase(status) && !userIdAndDateList.isEmpty()) ||
    // ("not entered".equalsIgnoreCase(status) && userIdAndDateList.isEmpty())) {
    //
    // UserTaskActivityResponseStatus datadto = new
    // UserTaskActivityResponseStatus();
    // datadto.setId(userdata.getId());
    // datadto.setUserName(userdata.getName());
    // datadto.setBranch(userdata.getBranch());
    // datadto.setRoleType(userdata.getRoleType());
    // datadto.setSupervisorName(userdata.getSupervisor() != 0
    // ? userRepo.findById(userdata.getSupervisor()).orElse(null).getName()
    // : null);
    // datadto.setStatus(userIdAndDateList.isEmpty() ? "Not Entered" : "Entered");
    // datadto.setActivity_date(date);
    //
    // // If data is entered, fetch additional details
    // if (!userIdAndDateList.isEmpty()) {
    // String hours = commonTimeSheetActivityRepository.findbyhours(date, id);
    // CommonTimeSheetActivity firstActivity = userIdAndDateList.get(0); // Assuming
    // you only need the
    // // first activity
    // datadto.setActivity_date(firstActivity.getActivity_date());
    // datadto.setId(firstActivity.getId());
    // datadto.setUserName(firstActivity.getUser().getName());
    // datadto.setSupervisorStatus(firstActivity.getSupervisorStatus());
    // datadto.setBranch(firstActivity.getBranch());
    // datadto.setRoleType(firstActivity.getUser().getRoleType());
    // datadto.setSupervisorName(
    // userRepo.findById(firstActivity.getUser().getSupervisor()).orElse(null).getName());
    // datadto.setHours(hours);
    // }
    //
    // dataList.add(datadto);
    // }
    // }
    //
    // String message = "Data Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // message, dataList));
    // }

    // public ResponseEntity<?> getReportDetail(Integer id, LocalDate fromDate,
    // LocalDate toDate, String status) {
    // Optional<Users> userDataOptional = userRepo.findById(id);
    // if (userDataOptional.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new
    // ApiResponse(false, "User not found", null));
    // }
    // Users userdata = userDataOptional.get();
    // Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = new
    // LinkedHashMap<>(); // LinkedHashMap to maintain insertion order
    //
    // List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
    // for (LocalDate date : dateRange) {
    // List<CommonTimeSheetActivity> userIdAndDateList =
    // commonTimeSheetActivityRepository
    // .findUserIdAndActivityDate(date, id);
    // List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();
    //
    // if (("entered".equalsIgnoreCase(status) && !userIdAndDateList.isEmpty()) ||
    // ("not entered".equalsIgnoreCase(status) && userIdAndDateList.isEmpty())) {
    //
    // UserTaskActivityResponseStatus datadto = new
    // UserTaskActivityResponseStatus();
    // datadto.setId(userdata.getId());
    // datadto.setUserName(userdata.getName());
    // datadto.setBranch(userdata.getBranch());
    // datadto.setRoleType(userdata.getRoleType());
    // datadto.setSupervisorName(userdata.getSupervisor() != null ?
    // userRepo.findByUserNameGetName(userdata.getSupervisor()):null);
    // datadto.setStatus(userIdAndDateList.isEmpty() ? "Not Entered" : "Entered");
    // datadto.setActivity_date(date);
    //
    // // If data is entered, fetch additional details
    // if (!userIdAndDateList.isEmpty()) {
    // String hours = commonTimeSheetActivityRepository.findbyhours(date, id);
    // CommonTimeSheetActivity firstActivity = userIdAndDateList.get(0); // Assuming
    // you only need the first activity
    // datadto.setActivity_date(firstActivity.getActivity_date());
    // datadto.setId(firstActivity.getId());
    // datadto.setUserName(firstActivity.getUser().getName());
    // datadto.setSupervisorStatus(firstActivity.getSupervisorStatus());
    // datadto.setBranch(firstActivity.getBranch());
    // datadto.setRoleType(firstActivity.getUser().getRoleType());
    // datadto.setSupervisorName(
    // userRepo.findByUserNameGetName(firstActivity.getUser().getSupervisor()));
    // datadto.setHours(hours);
    // }
    //
    // dataList.add(datadto);
    // }
    // // Add the list of data to the map with the date as the key
    // dataMap.put(date, dataList);
    // }
    //
    // String message = "Data Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // message, dataMap));
    // }

    public ResponseEntity<?> getReportDetail(Integer id, LocalDate fromDate, LocalDate toDate, String status) {
        Optional<Users> userDataOptional = userRepo.findById(id);
        if (userDataOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found", null));
        }
        Users userdata = userDataOptional.get();
        Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = new HashMap<>(); // LinkedHashMap to maintain
        // insertion order

        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
        for (LocalDate date : dateRange) {
            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDate(date, id);
            AttendanceSheet attendanceSheet = attendanceSheetRepository.findByUseridAndAppliedDate(id, date);
            List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

            UserTaskActivityResponseStatus datadto = null;

            // If data is entered, fetch additional details
            if ("all".equalsIgnoreCase(status)) {
                datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                datadto.setSupervisorName(
                        userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                : null);
                datadto.setActivity_date(date);
                if (userIdAndDateList != null) {
                    String hours = commonTimeSheetActivityRepository.findbyhours(date, id);
                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                    datadto.setHours(hours);
                    datadto.setStatus("Entered");
                } else if (attendanceSheet != null && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                    datadto.setStatus("Leave");
                } else {
                    datadto.setStatus("Not Entered");
                }
            } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                datadto.setSupervisorName(
                        userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                : null);
                datadto.setActivity_date(date);
                String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                datadto.setHours(hours);
                datadto.setStatus("Entered");
            } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null && attendanceSheet == null) {
                datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                datadto.setSupervisorName(
                        userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                : null);
                datadto.setActivity_date(date);
                datadto.setStatus("Not Entered");
            }

            if (datadto != null) {
                dataMap.computeIfAbsent(date, k -> new ArrayList<>()).add(datadto);
            }
        }
        // Add the list of data to the map with the date as the key

        String message = "Data Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, dataMap));
    }

    private UserTaskActivityResponseStatus createUserTaskDTO(CommonTimeSheetActivity activity, LocalDate date) {
        UserTaskActivityResponseStatus dto = new UserTaskActivityResponseStatus();
        dto.setId(activity.getId());
        dto.setActivity_date(activity.getActivity_date());
        dto.setUserName(activity.getUser().getName());
        dto.setSupervisorStatus(activity.getSupervisorStatus());
        dto.setBranch(activity.getBranch());
        dto.setRoleType(activity.getUser().getRoleType());
        dto.setStatus("Entered");
        Users supervisor = userRepo.findByUserNameGetAll(activity.getUser().getSupervisor()).orElse(null);
        if (supervisor != null) {
            dto.setSupervisorName(supervisor.getName());
        }
        return dto;
    }

    private UserTaskActivityResponseStatus createUserTaskDTO(Users user, LocalDate date) {
        UserTaskActivityResponseStatus userDTO = new UserTaskActivityResponseStatus();
        userDTO.setId(user.getId());
        userDTO.setUserName(user.getName());
        userDTO.setActivity_date(date);
        userDTO.setBranch(user.getBranch());
        userDTO.setRoleType(user.getRoleType());
        Users supervisor = userRepo.findByUserNameGetAll(user.getSupervisor()).orElse(null);
        if (supervisor != null) {
            userDTO.setSupervisorName(supervisor.getName());
        }
        return userDTO;
    }

    // return null;

    // @Override
    // public ResponseEntity<?> getMembersUnderSupervisor(Integer supervisorId,
    // LocalDate date) {
    // List<Users> supervisorMembers = userRepo.findBySupervisor(supervisorId);
    // List<Integer> memberIds = new ArrayList<>();
    // for (Users user : supervisorMembers) {
    // memberIds.add(user.getId());
    // }
    //
    // List<CommonTimeSheetActivity>
    // userIdAndDateList=commonTimeSheetActivityRepository.findUserIdAndActivityDates(memberIds,date);
    // List< UserTaskActivityResponseStatus> supervisorsDTO = new ArrayList<
    // UserTaskActivityResponseStatus>();
    // if (!userIdAndDateList.isEmpty()) {
    // for (CommonTimeSheetActivity activity : userIdAndDateList) {
    // UserTaskActivityResponseStatus dto = new UserTaskActivityResponseStatus(
    // activity.getId(),
    // activity.getActivity_date(),
    // activity.getUser().getName(),
    // activity.getSupervisorStatus(),
    // activity.getBranch(),
    // activity.getStatus());
    //
    // // Fetch supervisor name based on supervisor ID
    // Integer supervisorId1 = activity.getUser().getSupervisor();
    // if (supervisorId1 != null) {
    // Users supervisor = userRepo.findById(supervisorId1).orElse(null);
    // if (supervisor != null) {
    // supervisor.getName();
    // dto.setSupervisorName(supervisor.getName());
    // //Integer supervisorId123 = supervisor.getId();
    //
    // for (Users user : supervisorMembers) {
    // String hours = commonTimeSheetActivityRepository.findbyhours(date,
    // user.getId());
    // if (hours != null) {
    // dto.setHours(hours);
    // }
    // }
    // }
    // }
    // supervisorsDTO.add(dto);
    // }
    // String message = "User Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, supervisorsDTO));
    //
    // }
    // else {
    // List<Users> supervisorMembers1 = userRepo.findBySupervisor(supervisorId);
    // List<UserDTO> userDTOs = new ArrayList<UserDTO>();
    // for (Users user : supervisorMembers1) {
    // UserDTO userDTO = new UserDTO();
    // userDTO.setId(user.getId());
    // userDTO.setName(user.getName());
    // userDTO.setBranch(user.getBranch());
    // userDTOs.add(userDTO);
    //
    // }
    // String message = "User Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, userDTOs));
    //
    // }
    //
    // }
    @Override
    public ResponseEntity<?> getMembersUnderSupervisor(Integer supervisorId, LocalDate date, String status,
            int pageNumber, int pageSize) {
        List<Users> supervisorMembers = userRepo.findBySupervisorAndIsActive(userRepo.findByUserName(supervisorId));
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

        // Iterate through the supervisor members and populate the dataList
        for (Users userdata : supervisorMembers) {
            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);

            if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
                    ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {

                UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                datadto.setSupervisorName(
                        userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                : null);
                datadto.setStatus(userIdAndDateList != null ? "Entered" : "Not Entered");
                datadto.setActivity_date(date);

                if (userIdAndDateList != null) {
                    String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                    datadto.setActivity_date(userIdAndDateList.getActivity_date());
                    datadto.setId(userIdAndDateList.getId());
                    datadto.setUserName(userIdAndDateList.getUser().getName());
                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                    datadto.setBranch(userIdAndDateList.getBranch());
                    datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
                    datadto.setSupervisorName(
                            userRepo.findByUserNameGetAll(userIdAndDateList.getUser().getSupervisor()).orElse(null)
                                    .getName());
                    datadto.setStatus("Entered");
                    datadto.setHours(hours);
                }

                dataList.add(datadto);
            }
        }

        // Slice the dataList to paginate based on the requested page number and page
        // size
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, dataList.size());

        // Ensure endIndex does not exceed the size of the dataList
        if (startIndex >= dataList.size()) {
            startIndex = endIndex = dataList.size();
        } else {
            endIndex = Math.min(startIndex + pageSize, dataList.size());
        }

        // Extract the sublist based on pagination
        List<UserTaskActivityResponseStatus> paginatedList = dataList.subList(startIndex, endIndex);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", paginatedList));
    }

    // download data
    @Override
    public ResponseEntity<?> getMembersUnderSupervisordownload(Integer supervisorId, LocalDate date, String status) {
        List<Users> supervisorMembers = userRepo
                .findBySupervisorAndIsActive(userRepo.findByIdGetUsername(supervisorId));
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

        for (Users userdata : supervisorMembers) {
            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);

            if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
                    ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {

                UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                datadto.setSupervisorName(
                        userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                : null);
                datadto.setStatus(userIdAndDateList != null ? "Entered" : "Not Entered");
                datadto.setActivity_date(date);

                if (userIdAndDateList != null) {
                    String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                    datadto.setActivity_date(userIdAndDateList.getActivity_date());
                    datadto.setId(userIdAndDateList.getId());
                    datadto.setUserName(userIdAndDateList.getUser().getName());
                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                    datadto.setBranch(userIdAndDateList.getBranch());
                    datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
                    datadto.setSupervisorName(
                            userRepo.findByUserNameGetAll(userIdAndDateList.getUser().getSupervisor()).orElse(null)
                                    .getName());
                    datadto.setStatus("Entered");
                    datadto.setHours(hours);
                }

                dataList.add(datadto);
            }
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", dataList));
    }

    // daterange mani code
    // public ResponseEntity<?> getMembersUnderSupervisordownloadDetail(Integer
    // supervisorId, LocalDate fromDate,
    // LocalDate toDate, String status) {
    // List<Users> supervisorMembers =
    // userRepo.findBySupervisorAndIsActive(supervisorId);
    // List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();
    //
    // List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
    // for (LocalDate date : dateRange) {
    // for (Users userdata : supervisorMembers) {
    // CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
    // .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
    //
    // if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
    // ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {
    //
    // UserTaskActivityResponseStatus datadto = new
    // UserTaskActivityResponseStatus();
    // datadto.setId(userdata.getId());
    // datadto.setUserName(userdata.getName());
    // datadto.setBranch(userdata.getBranch());
    // datadto.setRoleType(userdata.getRoleType());
    // datadto.setSupervisorName(userdata.getSupervisor() != -1
    // ? userRepo.findById(userdata.getSupervisor()).orElse(null).getName()
    // : null);
    // datadto.setStatus(userIdAndDateList != null ? "Entered" : "Not Entered");
    // datadto.setActivity_date(date);
    //
    // if (userIdAndDateList != null) {
    // String hours = commonTimeSheetActivityRepository.findbyhours(date,
    // userdata.getId());
    // datadto.setActivity_date(userIdAndDateList.getActivity_date());
    // datadto.setId(userIdAndDateList.getId());
    // datadto.setUserName(userIdAndDateList.getUser().getName());
    // datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
    // datadto.setBranch(userIdAndDateList.getBranch());
    // datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
    // datadto.setSupervisorName(
    // userRepo.findById(userIdAndDateList.getUser().getSupervisor()).orElse(null).getName());
    // datadto.setStatus("Entered");
    // datadto.setHours(hours);
    // }
    //
    // dataList.add(datadto);
    // }
    // }
    // }
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Data Fetched Successfully", dataList));
    // }

    // public ResponseEntity<?> getMembersUnderSupervisordownloadDetail(Integer
    // supervisorId, LocalDate fromDate,
    // LocalDate toDate, String status) {
    // List<Users> supervisorMembers =
    // userRepo.findBySupervisorAndIsActive(supervisorId);
    // Map<LocalDate, List<UserTaskActivityResponseStatus>> enteredDataMap = new
    // LinkedHashMap<>();
    //
    // List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
    // for (LocalDate date : dateRange) {
    // List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();
    // for (Users userdata : supervisorMembers) {
    // CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
    // .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
    //
    // if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
    // ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {
    //
    // UserTaskActivityResponseStatus datadto = new
    // UserTaskActivityResponseStatus();
    // datadto.setId(userdata.getId());
    // datadto.setUserName(userdata.getName());
    // datadto.setBranch(userdata.getBranch());
    // datadto.setRoleType(userdata.getRoleType());
    // datadto.setSupervisorName(userdata.getSupervisor() != null ?
    // userRepo.findByUserNameGetName(userdata.getSupervisor()):null);
    // datadto.setStatus(userIdAndDateList != null ? "Entered" : "Not Entered");
    // datadto.setActivity_date(date);
    //
    // if (userIdAndDateList != null) {
    // String hours = commonTimeSheetActivityRepository.findbyhours(date,
    // userdata.getId());
    // datadto.setActivity_date(userIdAndDateList.getActivity_date());
    // datadto.setId(userIdAndDateList.getId());
    // datadto.setUserName(userIdAndDateList.getUser().getName());
    // datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
    // datadto.setBranch(userIdAndDateList.getBranch());
    // datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
    // datadto.setSupervisorName(
    // userRepo.findByUserNameGetAll(userIdAndDateList.getUser().getSupervisor()).orElse(null).getName());
    // datadto.setStatus("Entered");
    // datadto.setHours(hours);
    // }
    //
    // dataList.add(datadto);
    // }
    // }
    // enteredDataMap.put(date, dataList);
    // }
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, "Data Fetched Successfully", enteredDataMap));
    // }

    // Entered and Not Enterd Excel Downlode with supervisor id based
    public ResponseEntity<?> getMembersUnderSupervisordownloadDetail(Integer supervisorId, LocalDate fromDate,
            LocalDate toDate, String status) {
        List<Users> supervisorMembers = userRepo
                .findBySupervisorAndIsActive(userRepo.findByIdGetUsername(supervisorId));

        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
        Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = dateRange.stream()
                .collect(Collectors.toMap(date -> date, date -> supervisorMembers.stream().map(user -> {

                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                            .findUserIdAndActivityDatesbyloop(user.getId(), date);
                    AttendanceSheet attendanceSheet = attendanceSheetRepository.findByUseridAndAppliedDate(user.getId(),
                            date);
                    UserTaskActivityResponseStatus userResponse = null;

                    if ("all".equalsIgnoreCase(status)) {
                        userResponse = new UserTaskActivityResponseStatus();
                        userResponse.setId(user.getId());
                        userResponse.setUserName(user.getName());
                        userResponse.setBranch(user.getBranch());
                        userResponse.setRoleType(user.getRoleType());
                        if (user.getSupervisor() != null) {
                            Users supervisor = userRepo.findByUsername(user.getSupervisor()).orElse(null);
                            userResponse.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                        }
                        userResponse.setActivity_date(date);
                        if (userIdAndDateList != null) {
                            String hours = commonTimeSheetActivityRepository.findbyhours(date, user.getId());

                            userResponse.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());

                            userResponse.setHours(hours);
                            userResponse.setStatus("Entered");
                        } else if (attendanceSheet != null && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                            userResponse.setStatus("leave");
                        } else {
                            userResponse.setStatus("Not Entered");
                        }
                    } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                        userResponse = new UserTaskActivityResponseStatus();
                        userResponse.setId(user.getId());
                        userResponse.setUserName(user.getName());
                        userResponse.setBranch(user.getBranch());
                        userResponse.setRoleType(user.getRoleType());
                        if (user.getSupervisor() != null) {
                            Users supervisor = userRepo.findByUsername(user.getSupervisor()).orElse(null);
                            userResponse.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                        }
                        userResponse.setActivity_date(date);
                        String hours = commonTimeSheetActivityRepository.findbyhours(date, user.getId());

                        userResponse.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());

                        userResponse.setHours(hours);
                        userResponse.setStatus("Entered");
                    } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                            && attendanceSheet == null) {
                        userResponse = new UserTaskActivityResponseStatus();
                        userResponse.setId(user.getId());
                        userResponse.setUserName(user.getName());
                        userResponse.setBranch(user.getBranch());
                        userResponse.setRoleType(user.getRoleType());
                        if (user.getSupervisor() != null) {
                            Users supervisor = userRepo.findByUsername(user.getSupervisor()).orElse(null);
                            userResponse.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                        }
                        userResponse.setActivity_date(date);
                        userResponse.setStatus("Not Entered");
                    }
                    return userResponse;

                }).filter(Objects::nonNull)
                        .collect(Collectors.toList())));
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", dataMap));
    }

    @Override
    public ResponseEntity<?> getMembersUnderSupervisorbyall(LocalDate date, String status, int pageNumber,
            int pageSize) {
        // Retrieve the data and populate the dataList
        List<UserTaskActivityResponseStatus> dataList = retrieveData(date, status);
        // Calculate the start and end indices for pagination
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, dataList.size());

        // Ensure endIndex does not exceed the size of the dataList
        if (startIndex >= dataList.size()) {
            startIndex = endIndex = dataList.size();
        } else {
            endIndex = Math.min(startIndex + pageSize, dataList.size());
        }

        // Extract the sublist based on pagination
        List<UserTaskActivityResponseStatus> paginatedList = dataList.subList(startIndex, endIndex);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", paginatedList));
    }

    // Helper method to retrieve data based on date and status
    private List<UserTaskActivityResponseStatus> retrieveData(LocalDate date, String status) {
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

        // Process each user
        for (Users userdata : supervisorMembers) {
            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);

            if (("entered".equalsIgnoreCase(status) && userIdAndDateList != null) ||
                    ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null)) {

                UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                datadto.setId(userdata.getId());
                datadto.setUserName(userdata.getName());
                datadto.setBranch(userdata.getBranch());
                datadto.setRoleType(userdata.getRoleType());
                if (userdata.getSupervisor() != null) {
                    datadto.setSupervisorName(
                            userdata.getSupervisor() != null ? userRepo.findByUserNameGetName(userdata.getSupervisor())
                                    : null);
                }
                datadto.setStatus(userIdAndDateList != null ? "Entered" : "Not Entered");
                datadto.setActivity_date(date);

                if (userIdAndDateList != null) {
                    String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                    datadto.setActivity_date(userIdAndDateList.getActivity_date());
                    datadto.setId(userIdAndDateList.getId());
                    datadto.setUserName(userIdAndDateList.getUser().getName());
                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                    datadto.setBranch(userIdAndDateList.getBranch());
                    datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
                    if (userdata.getSupervisor() != null) {

                        datadto.setSupervisorName(
                                userRepo.findByUserNameGetAll(userIdAndDateList.getUser().getSupervisor()).orElse(null)
                                        .getName());
                    }
                    datadto.setStatus("Entered");
                    datadto.setHours(hours);
                }

                dataList.add(datadto);
            }
        }

        return dataList;
    }

    // downlode for all
    public ResponseEntity<?> getMembersUnderSupervisorbyalldownlode(LocalDate date, String status) {
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<UserTaskActivityResponseStatus> enterreddataList = new ArrayList<>();
        List<UserTaskActivityResponseStatus> notEnterreddataList = new ArrayList<>();

        for (Users userdata : supervisorMembers) {
            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);

            if (userIdAndDateList != null && status.equalsIgnoreCase("entered")) {

                String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                datadto.setActivity_date(userIdAndDateList.getActivity_date());
                datadto.setId(userIdAndDateList.getId());
                datadto.setUserName(userIdAndDateList.getUser().getName());
                datadto.setRoleType(userIdAndDateList.getUser().getRoleType());
                datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                datadto.setBranch(userIdAndDateList.getBranch());
                if (userIdAndDateList.getUser().getSupervisor() != null) {
                    Users supervisor = userRepo.findByUserNameGetAll(userIdAndDateList.getUser().getSupervisor())
                            .orElse(null);
                    datadto.setSupervisorName(supervisor.getName());
                }

                datadto.setStatus("Entered");
                datadto.setHours(hours);

                enterreddataList.add(datadto);
            } else {
                Optional<Users> user = userRepo.findById(userdata.getId());
                UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                AttendanceSheet attendanceSheet = attendanceSheetRepository
                        .findByUseridAndAppliedDate(user.get().getId(), date);
                if (attendanceSheet == null) {

                    if (!user.isEmpty()) {
                        datadto.setId(user.get().getId());
                        datadto.setUserName(user.get().getName());
                        datadto.setBranch(user.get().getBranch());
                        datadto.setRoleType(user.get().getRoleType());

                        if (user.get().getSupervisor() != null) {
                            Users supervisor = userRepo.findByUserNameGetAll(user.get().getSupervisor()).orElse(null);
                            datadto.setSupervisorName(supervisor.getName());
                        }

                        datadto.setStatus("Not Entered");
                        datadto.setActivity_date(date);
                    }
                    notEnterreddataList.add(datadto);

                }
            }
        }
        if (status.equalsIgnoreCase("entered")) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Data Fetched Successfully", enterreddataList));
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Data Fetched Successfully", notEnterreddataList));
        }
    }

    public ResponseEntity<?> getMembersalldownlode(LocalDate fromDate, LocalDate toDate, String status) {
        List<Users> activeMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);

        Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = dateRange.stream()
                .collect(Collectors.toMap(
                        date -> date,
                        date -> activeMembers.stream()
                                .map(userData -> {
                                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                            .findUserIdAndActivityDatesbyloop(userData.getId(), date);
                                    AttendanceSheet attendanceSheet = attendanceSheetRepository
                                            .findByUseridAndAppliedDate(userData.getId(), date);

                                    UserTaskActivityResponseStatus datadto = null;
                                    if ("all".equalsIgnoreCase(status)) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setActivity_date(date);
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        if (userIdAndDateList != null) {
                                            String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                                    userData.getId());
                                            datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                            datadto.setHours(hours);
                                            datadto.setStatus("Entered");
                                        } else if (attendanceSheet != null
                                                && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                                            datadto.setStatus("leave");
                                        } else {
                                            datadto.setStatus("Not Entered");
                                        }
                                    } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        datadto.setActivity_date(date);
                                        String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                                userData.getId());
                                        datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                        datadto.setHours(hours);
                                        datadto.setStatus("Entered");
                                    } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                                            && attendanceSheet == null) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        datadto.setActivity_date(date);
                                        datadto.setStatus("Not Entered");
                                    }
                                    return datadto;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(ArrayList::new))));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", dataMap));
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
    public ResponseEntity<?> getMembersUnderSupervisorbyallDetail(LocalDate fromDate, LocalDate toDate, String status,
            int pageNumber, int pageSize, Integer supervisorId, Integer memberId, String company, String roletype) {
        Page<UserTaskActivityResponseStatus> dataList = null;
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize); // Page numbers are 0-based
        if (company != null && supervisorId == null && memberId == null && roletype == null) {
            dataList = retrieveDatascompany(company, fromDate, toDate, status, pageable);
        } else if (company == null && supervisorId == null && memberId == null && roletype == null) {
            dataList = retrieveDatasall(fromDate, toDate, status, pageable);
        } else if (supervisorId != null && memberId == null && roletype == null) {
            dataList = retrieveDatas(supervisorId, fromDate, toDate, status, pageable);
        } else if (supervisorId != null && memberId != null && roletype == null) {
            dataList = retrieveDatasForMembers(memberId, fromDate, toDate, status, pageable);
        } else if (roletype != null && company != null) {
            System.out.println("11111");
            dataList = retrieveDataRoleBased(company, roletype, fromDate, toDate, status, pageable);

        } else if (roletype != null && company == null)
            dataList = retrieveDataRoleBasedWithoutCompany(roletype, fromDate, toDate, status, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", dataList));
    }

    // downlode for roletype based on company
    public ResponseEntity<?> downloadRetrieveDataRoleBasedWithoutCompany(String roleType,
            LocalDate fromDate, LocalDate toDate, String company) {
        // Retrieve active employees
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<LocalDate> dates = getDatesInRange(fromDate, toDate);

        // Create a map to store the data
        Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = new HashMap<>();

        for (LocalDate date : dates) {
            List<UserTaskActivityResponseStatus> dailyDataList = supervisorMembers.stream()
                    .filter(user -> {
                        boolean isCompanyMatch = false;
                        boolean isRoleTypeMatch = user.getRoleType().equalsIgnoreCase(roleType);

                        // Check company match if company is provided
                        if (company != null) {
                            if (company.equalsIgnoreCase("hepl")) {
                                isCompanyMatch = user.getEmail().contains("@hepl.com")
                                        || user.getUsername().equalsIgnoreCase("Dy006");
                            } else if (company.equalsIgnoreCase("citpl")) {
                                isCompanyMatch = user.getEmail().contains("@cavininfotech.com")
                                        && !user.getUsername().equalsIgnoreCase("Dy006");
                            }
                        }

                        // Continue if the company does not match
                        if (company != null && !isCompanyMatch) {
                            return false;
                        }

                        // Continue if the role type does not match
                        return isRoleTypeMatch;
                    })
                    .map(user -> {
                        UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();
                        datadto.setActivity_date(date);
                        datadto.setBranch(user.getBranch());
                        datadto.setId(user.getId());
                        datadto.setRoleType(roleType);
                        datadto.setUserName(user.getName());

                        // Set supervisor name if available
                        if (user.getSupervisor() != null) {
                            datadto.setSupervisorName(userRepo.findByUserNameGetName(user.getSupervisor()));
                        }

                        // Set final approve name if role type is Contract
                        if (user.getRoleType().equals("Contract")) {
                            String nameFinal = userRepo.findByUserNameGetName(user.getFinalApprove());
                            datadto.setFinalName(nameFinal);
                        }

                        // Fetch activity details
                        CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                .findUserIdAndActivityDatesbyloop(user.getId(), date);
                        if (userIdAndDateList != null) {
                            String hours = commonTimeSheetActivityRepository.findbyhours(date, user.getId());
                            datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                            datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                            datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                            datadto.setHours(hours);
                        }

                        return datadto;
                    })
                    .collect(Collectors.toList());

            dataMap.put(date, dailyDataList);
        }

        // Return the response entity with the data map
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "generated", dataMap));
    }

    public Page<UserTaskActivityResponseStatus> retrieveDataRoleBased(String company, String roleType,
            LocalDate fromDate, LocalDate toDate, String status, Pageable pageable) {

        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<Users> memberIds = new ArrayList<>();
        List<LocalDate> dates = getDatesInRange(fromDate, toDate);
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>(); // Instantiate the list

        for (LocalDate date : dates) {
            for (Users user : supervisorMembers) {
                boolean isCompanyMatch = false;
                boolean isRoleTypeMatch = user.getRoleType().equalsIgnoreCase(roleType);
                System.out.println(isRoleTypeMatch);
                if (company.equalsIgnoreCase("hepl")) {
                    isCompanyMatch = user.getEmail().contains("@hepl.com")
                            || user.getUsername().equalsIgnoreCase("Dy006");
                } else if (company.equalsIgnoreCase("citpl")) {
                    isCompanyMatch = user.getEmail().contains("@cavininfotech.com")
                            && !user.getUsername().equalsIgnoreCase("Dy006");
                }

                if (isCompanyMatch && isRoleTypeMatch) {
                    memberIds.add(user);
                    System.out.println(user.getName());

                    UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus(); // Instantiate the
                    // object here

                    datadto.setActivity_date(date);
                    datadto.setBranch(user.getBranch());
                    datadto.setId(user.getId());
                    datadto.setRoleType(roleType);
                    datadto.setUserName(user.getName());
                    if (user.getSupervisor() != null) {
                        datadto.setSupervisorName(userRepo.findByUserNameGetName(user.getSupervisor()));
                    }
                    if (user.getRoleType().equals("Contract")) {
                        String nameFinal = userRepo.findByUserNameGetName(user.getFinalApprove());
                        datadto.setFinalName(nameFinal);
                    }
                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                            .findUserIdAndActivityDatesbyloop(user.getId(), date);

                    if (userIdAndDateList != null) {
                        String hours = commonTimeSheetActivityRepository.findbyhours(date, user.getId());
                        datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                        datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                        datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                        datadto.setHours(hours);
                    }

                    dataList.add(datadto);
                } // Add the object to the list
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dataList.size());
        List<UserTaskActivityResponseStatus> pageList = new ArrayList<>();

        if (start < end) {
            pageList = dataList.subList(start, end);
        }

        return new PageImpl<>(pageList, pageable, dataList.size());
    }

    public Page<UserTaskActivityResponseStatus> retrieveDataRoleBasedWithoutCompany(String roleType, LocalDate fromDate,
            LocalDate toDate, String status, Pageable pageable) {
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<Users> memberIds = new ArrayList<>();
        List<LocalDate> dates = getDatesInRange(fromDate, toDate);
        List<UserTaskActivityResponseStatus> dataList = new ArrayList<>();

        for (LocalDate date : dates) {
            for (Users user : supervisorMembers) {
                boolean isRoleTypeMatch = user.getRoleType().equalsIgnoreCase(roleType);
                System.out.println(isRoleTypeMatch);

                if (isRoleTypeMatch) {
                    memberIds.add(user);
                    System.out.println(user.getName());

                    UserTaskActivityResponseStatus datadto = new UserTaskActivityResponseStatus();

                    datadto.setActivity_date(date);
                    datadto.setBranch(user.getBranch());
                    datadto.setId(user.getId());
                    datadto.setRoleType(roleType);
                    datadto.setUserName(user.getName());
                    if (user.getSupervisor() != null) {
                        datadto.setSupervisorName(userRepo.findByUserNameGetName(user.getSupervisor()));
                    }
                    if (user.getRoleType().equals("Contract")) {
                        String nameFinal = userRepo.findByUserNameGetName(user.getFinalApprove());
                        datadto.setFinalName(nameFinal);
                    }
                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                            .findUserIdAndActivityDatesbyloop(user.getId(), date);

                    if (userIdAndDateList != null) {
                        String hours = commonTimeSheetActivityRepository.findbyhours(date, user.getId());
                        datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                        datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                        datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                        datadto.setHours(hours);
                    }

                    dataList.add(datadto);
                }
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), dataList.size());
        List<UserTaskActivityResponseStatus> pageList = new ArrayList<>();

        if (start < end) {
            pageList = dataList.subList(start, end);
        }

        return new PageImpl<>(pageList, pageable, dataList.size());
    }

    // Company Based List
    private Page<UserTaskActivityResponseStatus> retrieveDatascompany(String company, LocalDate fromDate,
            LocalDate toDate, String status, Pageable pageable) {
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<Users> memberIds = new ArrayList<>();
        if (company.equalsIgnoreCase("hepl")) {
            for (Users user : supervisorMembers) {
                if (user.getEmail().contains("@hepl.com") || user.getUsername().equalsIgnoreCase("Dy006")) { // only for
                    // aishwarya
                    memberIds.add(user);
                }
            }

        } else if (company.equalsIgnoreCase("citpl")) {
            for (Users user : supervisorMembers) {
                if (user.getEmail().contains("@cavininfotech.com") && !user.getUsername().equalsIgnoreCase("Dy006")) { // only
                    // for
                    // aishwarya
                    memberIds.add(user);
                }
            }
        }
        long totalCount = (long) getDatesInRange(fromDate, toDate).size() * memberIds.size();

        List<UserTaskActivityResponseStatus> dataList = getDatesInRange(fromDate, toDate).stream()
                .flatMap(date -> memberIds.stream()
                        .map(userdata -> {
                            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
                            AttendanceSheet attendanceSheet = attendanceSheetRepository
                                    .findByUseridAndAppliedDate(userdata.getId(), date);

                            UserTaskActivityResponseStatus datadto = null;

                            if ("all".equalsIgnoreCase(status)) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                if (userdata.getRoleType().equals("Contract")) {
                                    String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                    datadto.setFinalName(nameFinal);
                                }
                                datadto.setActivity_date(date);
                                if (userIdAndDateList != null) {
                                    String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                            userdata.getId());
                                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                    datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                    datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                    datadto.setHours(hours);
                                    datadto.setStatus("Entered");
                                    if (userdata.getRoleType().equals("ON Role")) {
                                        String ownerName = memberRepo
                                                .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                        userIdAndDateList.getProduct().getId());
                                        if (ownerName != null) {
                                            datadto.setFinalName(ownerName);
                                        } else {
                                            if ("Technical".equals(userdata.getBranch())) {
                                                if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getTechOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            } else {
                                                if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getProdOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            }

                                        }
                                    }
                                } else if (attendanceSheet != null
                                        && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                                    datadto.setStatus("Leave");
                                } else {
                                    datadto.setStatus("Not Entered");
                                }
                            } else if ("entered".equalsIgnoreCase(status)) {
                                if (userIdAndDateList != null) {
                                    datadto = new UserTaskActivityResponseStatus();
                                    datadto.setId(userdata.getId());
                                    datadto.setUserName(userdata.getName());
                                    datadto.setBranch(userdata.getBranch());
                                    datadto.setRoleType(userdata.getRoleType());
                                    if (userdata.getSupervisor() != null) {
                                        datadto.setSupervisorName(
                                                userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                    }
                                    if (userdata.getRoleType().equals("Contract")) {
                                        String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                        datadto.setFinalName(nameFinal);
                                    } else {
                                        String ownerName = memberRepo
                                                .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                        userIdAndDateList.getProduct().getId());
                                        if (ownerName != null) {
                                            datadto.setFinalName(ownerName);
                                        } else {
                                            if ("Technical".equals(userdata.getBranch())) {
                                                if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getTechOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            } else {
                                                if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getProdOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            }

                                        }
                                    }
                                    datadto.setActivity_date(date);
                                    String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                            userdata.getId());
                                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                    datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                    datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                    datadto.setHours(hours);
                                    datadto.setStatus("Entered");
                                }
                            } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                                    && attendanceSheet == null) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                datadto.setActivity_date(date);
                                datadto.setStatus("Not Entered");
                            }

                            return datadto;
                        }))
                .filter(Objects::nonNull) // Filter out null values
                .collect(Collectors.toList());

        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), dataList.size());

        List<UserTaskActivityResponseStatus> paginatedList = dataList.subList(startIndex, endIndex);

        return new PageImpl<>(paginatedList, pageable, totalCount);
    }

    private Page<UserTaskActivityResponseStatus> retrieveDatasall(LocalDate fromDate, LocalDate toDate, String status,
            Pageable pageable) {
        List<Users> supervisorMembers = userRepo.getActiveEmployeeswithoutDelete();
        long totalCount = (long) getDatesInRange(fromDate, toDate).size() * supervisorMembers.size();

        List<UserTaskActivityResponseStatus> dataList = getDatesInRange(fromDate, toDate).stream()
                .flatMap(date -> supervisorMembers.stream()
                        .map(userdata -> {
                            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
                            AttendanceSheet attendanceSheet = attendanceSheetRepository
                                    .findByUseridAndAppliedDate(userdata.getId(), date);

                            UserTaskActivityResponseStatus datadto = null;

                            if ("all".equalsIgnoreCase(status)) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                if (userdata.getRoleType().equals("Contract")) {
                                    String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                    datadto.setFinalName(nameFinal);
                                }
                                datadto.setActivity_date(date);
                                if (userIdAndDateList != null) {
                                    String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                            userdata.getId());
                                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                    datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                    datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                    datadto.setHours(hours);
                                    datadto.setStatus("Entered");
                                    if (userdata.getRoleType().equals("ON Role")) {
                                        String ownerName = memberRepo
                                                .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                        userIdAndDateList.getProduct().getId());
                                        if (ownerName != null) {
                                            datadto.setFinalName(ownerName);
                                        } else {
                                            if ("Technical".equals(userdata.getBranch())) {
                                                if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getTechOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            } else {
                                                if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getProdOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            }

                                        }
                                    }
                                } else if (attendanceSheet != null
                                        && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                                    datadto.setStatus("Leave");
                                } else {
                                    datadto.setStatus("Not Entered");
                                }
                            } else if ("entered".equalsIgnoreCase(status)) {
                                if (userIdAndDateList != null) {
                                    datadto = new UserTaskActivityResponseStatus();
                                    datadto.setId(userdata.getId());
                                    datadto.setUserName(userdata.getName());
                                    datadto.setBranch(userdata.getBranch());
                                    datadto.setRoleType(userdata.getRoleType());
                                    if (userdata.getSupervisor() != null) {
                                        datadto.setSupervisorName(
                                                userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                    }
                                    if (userdata.getRoleType().equals("Contract")) {
                                        String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                        datadto.setFinalName(nameFinal);
                                    } else {
                                        String ownerName = memberRepo
                                                .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                        userIdAndDateList.getProduct().getId());
                                        if (ownerName != null) {
                                            datadto.setFinalName(ownerName);
                                        } else {
                                            if ("Technical".equals(userdata.getBranch())) {
                                                if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getTechOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            } else {
                                                if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getProdOwner();
                                                    String[] parts = value.split(",");
                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            }

                                        }
                                    }
                                    datadto.setActivity_date(date);
                                    String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                            userdata.getId());
                                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                    datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                    datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                    datadto.setHours(hours);
                                    datadto.setStatus("Entered");
                                }
                            } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                                    && attendanceSheet == null) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                datadto.setActivity_date(date);
                                datadto.setStatus("Not Entered");
                            }

                            return datadto;
                        }))
                .filter(Objects::nonNull) // Filter out null values
                .collect(Collectors.toList());

        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), dataList.size());

        List<UserTaskActivityResponseStatus> paginatedList = dataList.subList(startIndex, endIndex);

        return new PageImpl<>(paginatedList, pageable, totalCount);
    }

    private Page<UserTaskActivityResponseStatus> retrieveDatas(Integer supervisorId, LocalDate fromDate,
            LocalDate toDate, String status, Pageable pageable) {

        List<Users> supervisorMembers = userRepo
                .findBySupervisorAndIsActive(userRepo.findByIdGetUsername(supervisorId));
        long totalCount = (long) getDatesInRange(fromDate, toDate).size() * supervisorMembers.size();

        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);

        List<UserTaskActivityResponseStatus> dataList = dateRange.stream()
                .flatMap(date -> supervisorMembers.stream()
                        .map(userdata -> {
                            CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                    .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
                            AttendanceSheet attendanceSheet = attendanceSheetRepository
                                    .findByUseridAndAppliedDate(userdata.getId(), date);

                            UserTaskActivityResponseStatus datadto = null;

                            if ("all".equalsIgnoreCase(status)) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                if (userdata.getRoleType().equals("Contract")) {
                                    String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                    datadto.setFinalName(nameFinal);
                                }
                                datadto.setActivity_date(date);
                                if (userIdAndDateList != null) {
                                    String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                            userdata.getId());
                                    datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                    datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                    datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                    datadto.setHours(hours);
                                    datadto.setStatus("Entered");
                                    if (userdata.getRoleType().equals("ON Role")) {
                                        String ownerName = memberRepo
                                                .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                        userIdAndDateList.getProduct().getId());
                                        System.out.println(ownerName + "ownerName=");
                                        if (ownerName != null) {
                                            datadto.setFinalName(ownerName);
                                        } else {
                                            System.out.println(
                                                    userIdAndDateList.getProduct().getTechOwner() + "Tech=");
                                            System.out.println(userIdAndDateList.getProduct().getProdOwner() + "Prod=");

                                            if ("Technical".equals(userdata.getBranch())) {
                                                if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getTechOwner();
                                                    String[] parts = value.split(",");
                                                    System.out.println(parts + "parts=");

                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            } else {
                                                if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                    String value = userIdAndDateList.getProduct().getProdOwner();
                                                    String[] parts = value.split(",");
                                                    System.out.println(parts + "parts=");

                                                    int id = Integer.parseInt(parts[0].trim());
                                                    String name = userRepo.findByUserName(id);
                                                    datadto.setFinalName(name);
                                                }
                                            }

                                        }
                                    }
                                } else if (attendanceSheet != null
                                        && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                                    datadto.setStatus("Leave");
                                } else {
                                    datadto.setStatus("Not Entered");
                                }
                            } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                if (userdata.getRoleType().equals("Contract")) {
                                    String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                                    datadto.setFinalName(nameFinal);
                                } else {
                                    String ownerName = memberRepo
                                            .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                    userIdAndDateList.getProduct().getId());
                                    if (ownerName != null) {
                                        datadto.setFinalName(ownerName);
                                    } else {
                                        if ("Technical".equals(userdata.getBranch())) {
                                            if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                                String value = userIdAndDateList.getProduct().getTechOwner();
                                                String[] parts = value.split(",");
                                                int id = Integer.parseInt(parts[0].trim());
                                                String name = userRepo.findByUserName(id);
                                                datadto.setFinalName(name);
                                            }
                                        } else {
                                            if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                                String value = userIdAndDateList.getProduct().getProdOwner();
                                                String[] parts = value.split(",");
                                                int id = Integer.parseInt(parts[0].trim());
                                                String name = userRepo.findByUserName(id);
                                                datadto.setFinalName(name);
                                            }
                                        }

                                    }
                                }
                                datadto.setActivity_date(date);
                                String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                                datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                                datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                                datadto.setHours(hours);
                                datadto.setStatus("Entered");
                            } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                                    && attendanceSheet == null) {
                                datadto = new UserTaskActivityResponseStatus();
                                datadto.setId(userdata.getId());
                                datadto.setUserName(userdata.getName());
                                datadto.setBranch(userdata.getBranch());
                                datadto.setRoleType(userdata.getRoleType());
                                if (userdata.getSupervisor() != null) {
                                    datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                                }
                                datadto.setActivity_date(date);
                                datadto.setStatus("Not Entered");
                            }

                            return datadto;
                        }))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageable.getPageSize(), dataList.size());

        List<UserTaskActivityResponseStatus> paginatedList = dataList.subList(startIndex, endIndex);

        return new PageImpl<>(paginatedList, pageable, totalCount);
    }

    private Page<UserTaskActivityResponseStatus> retrieveDatasForMembers(Integer memberId, LocalDate fromDate,
            LocalDate toDate, String status, Pageable pageable) {

        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);

        Page<UserTaskActivityResponseStatus> page = dateRange.stream()
                .flatMap(date -> {
                    Optional<Users> userDataOptional = userRepo.findById(memberId);
                    if (userDataOptional.isEmpty()) {
                        return Stream.empty();
                    }
                    Users userdata = userDataOptional.get();

                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                            .findUserIdAndActivityDatesbyloop(userdata.getId(), date);
                    AttendanceSheet attendanceSheet = attendanceSheetRepository
                            .findByUseridAndAppliedDate(userdata.getId(), date);

                    UserTaskActivityResponseStatus datadto = null;

                    if ("all".equalsIgnoreCase(status)) {
                        datadto = new UserTaskActivityResponseStatus();
                        datadto.setId(userdata.getId());
                        datadto.setUserName(userdata.getName());
                        datadto.setBranch(userdata.getBranch());
                        datadto.setRoleType(userdata.getRoleType());
                        if (userdata.getSupervisor() != null) {
                            datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                        }
                        if (userdata.getRoleType().equals("Contract")) {
                            String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                            datadto.setFinalName(nameFinal);
                        }

                        datadto.setActivity_date(date);
                        if (userIdAndDateList != null) {
                            String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                            datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                            datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                            datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                            datadto.setHours(hours);
                            datadto.setStatus("Entered");
                            if (userdata.getRoleType().equals("ON Role")) {
                                String ownerName = memberRepo
                                        .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                                userIdAndDateList.getProduct().getId());
                                if (ownerName != null) {
                                    datadto.setFinalName(ownerName);
                                } else {
                                    if ("Technical".equals(userdata.getBranch())) {
                                        if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                            String value = userIdAndDateList.getProduct().getTechOwner();
                                            String[] parts = value.split(",");
                                            int id = Integer.parseInt(parts[0].trim());
                                            String name = userRepo.findByUserName(id);
                                            datadto.setFinalName(name);
                                        }
                                    } else {
                                        if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                            String value = userIdAndDateList.getProduct().getProdOwner();
                                            String[] parts = value.split(",");
                                            int id = Integer.parseInt(parts[0].trim());
                                            String name = userRepo.findByUserName(id);
                                            datadto.setFinalName(name);
                                        }
                                    }

                                }
                            }
                        } else if (attendanceSheet != null && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                            datadto.setStatus("Leave");
                        } else {
                            datadto.setStatus("Not Entered");
                        }
                    } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                        datadto = new UserTaskActivityResponseStatus();
                        datadto.setId(userdata.getId());
                        datadto.setUserName(userdata.getName());
                        datadto.setBranch(userdata.getBranch());
                        datadto.setRoleType(userdata.getRoleType());
                        if (userdata.getSupervisor() != null) {
                            datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                        }
                        if (userdata.getRoleType().equals("Contract")) {
                            String nameFinal = userRepo.findByUserNameGetName(userdata.getFinalApprove());
                            datadto.setFinalName(nameFinal);
                        } else {
                            String ownerName = memberRepo
                                    .findByMemberAndProdIdAndGetAssignedBy(userdata.getId(),
                                            userIdAndDateList.getProduct().getId());
                            if (ownerName != null) {
                                datadto.setFinalName(ownerName);
                            } else {
                                if ("Technical".equals(userdata.getBranch())) {
                                    if (userIdAndDateList.getProduct().getTechOwner() != null) {
                                        String value = userIdAndDateList.getProduct().getTechOwner();
                                        String[] parts = value.split(",");
                                        int id = Integer.parseInt(parts[0].trim());
                                        String name = userRepo.findByUserName(id);
                                        datadto.setFinalName(name);
                                    }
                                } else {
                                    if (userIdAndDateList.getProduct().getProdOwner() != null) {
                                        String value = userIdAndDateList.getProduct().getProdOwner();
                                        String[] parts = value.split(",");
                                        int id = Integer.parseInt(parts[0].trim());
                                        String name = userRepo.findByUserName(id);
                                        datadto.setFinalName(name);
                                    }
                                }

                            }
                        }
                        datadto.setActivity_date(date);
                        String hours = commonTimeSheetActivityRepository.findbyhours(date, userdata.getId());
                        datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                        datadto.setFinalApproveStatus(userIdAndDateList.getFinalApprove());
                        datadto.setOwnerStatus(userIdAndDateList.getOwnerStatus());
                        datadto.setHours(hours);
                        datadto.setStatus("Entered");
                    } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                            && attendanceSheet == null) {
                        datadto = new UserTaskActivityResponseStatus();
                        datadto.setId(userdata.getId());
                        datadto.setUserName(userdata.getName());
                        datadto.setBranch(userdata.getBranch());
                        datadto.setRoleType(userdata.getRoleType());
                        if (userdata.getSupervisor() != null) {
                            datadto.setSupervisorName(userRepo.findByUserNameGetName(userdata.getSupervisor()));
                        }
                        datadto.setActivity_date(date);

                        datadto.setStatus("Not Entered");
                    }

                    return Stream.of(datadto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), PageImpl::new));

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), page.getContent().size());

        return new PageImpl<>(page.getContent().subList(start, end), pageable, page.getTotalElements());
    }

    public ResponseEntity<?> getMembersalldownlodecompany(LocalDate fromDate, LocalDate toDate, String status,
            String company) {
        List<Users> activeMembers = userRepo.getActiveEmployeeswithoutDelete();
        List<LocalDate> dateRange = getDatesInRange(fromDate, toDate);
        List<Users> memberIds = new ArrayList<>();
        if (company.equalsIgnoreCase("hepl")) {
            for (Users user : activeMembers) {
                if (user.getEmail().contains("@hepl.com") || user.getUsername().equalsIgnoreCase("Dy006")) { // only for
                    // aishwarya
                    memberIds.add(user);
                }
            }

        } else if (company.equalsIgnoreCase("citpl")) {
            for (Users user : activeMembers) {
                if (user.getEmail().contains("@cavininfotech.com") && !user.getUsername().equalsIgnoreCase("Dy006")) { // only
                    // for
                    // aishwarya
                    memberIds.add(user);
                }
            }
        }
        Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = dateRange.stream()
                .collect(Collectors.toMap(
                        date -> date,
                        date -> memberIds.stream()
                                .map(userData -> {
                                    CommonTimeSheetActivity userIdAndDateList = commonTimeSheetActivityRepository
                                            .findUserIdAndActivityDatesbyloop(userData.getId(), date);
                                    AttendanceSheet attendanceSheet = attendanceSheetRepository
                                            .findByUseridAndAppliedDate(userData.getId(), date);

                                    UserTaskActivityResponseStatus datadto = null;
                                    if ("all".equalsIgnoreCase(status)) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setActivity_date(date);
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        if (userIdAndDateList != null) {
                                            String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                                    userData.getId());
                                            datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                            datadto.setHours(hours);
                                            datadto.setStatus("Entered");
                                        } else if (attendanceSheet != null
                                                && attendanceSheet.getStatus().equalsIgnoreCase("leave")) {
                                            datadto.setStatus("leave");
                                        } else {
                                            datadto.setStatus("Not Entered");
                                        }
                                    } else if ("entered".equalsIgnoreCase(status) && userIdAndDateList != null) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        datadto.setActivity_date(date);
                                        String hours = commonTimeSheetActivityRepository.findbyhours(date,
                                                userData.getId());
                                        datadto.setSupervisorStatus(userIdAndDateList.getSupervisorStatus());
                                        datadto.setHours(hours);
                                        datadto.setStatus("Entered");
                                    } else if ("not entered".equalsIgnoreCase(status) && userIdAndDateList == null
                                            && attendanceSheet == null) {
                                        datadto = new UserTaskActivityResponseStatus();
                                        datadto.setId(userData.getId());
                                        datadto.setUserName(userData.getName());
                                        datadto.setBranch(userData.getBranch());
                                        datadto.setRoleType(userData.getRoleType());
                                        if (userData.getSupervisor() != null) {
                                            Users supervisor = userRepo.findByUsername(userData.getSupervisor())
                                                    .orElse(null);
                                            datadto.setSupervisorName(supervisor != null ? supervisor.getName() : null);
                                        }
                                        datadto.setActivity_date(date);
                                        datadto.setStatus("Not Entered");
                                    }
                                    return datadto;
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toCollection(ArrayList::new))));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Data Fetched Successfully", dataMap));

    }
}
