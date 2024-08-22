package pm.serviceImplements;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.ContractPersonDTO;
import pm.dto.ContractUserDataDTO;
import pm.dto.ContractUserDataDTO.commonTimeSheetRecord;
import pm.dto.ReportDTO;
import pm.dto.UserDTO;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.AttendanceSheetRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.TaskRepository;
import pm.repository.UsersRepository;
import pm.repository.MemberRepository;
import pm.response.ApiResponse;
import pm.service.GenerateReport;
import pm.utils.AuthUserData;
import pm.utils.CommonFunct;

import java.sql.Date;
import java.sql.Timestamp;

@Service
public class GenerateReportImpl implements GenerateReport {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetRepo;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;

    @Autowired
    private CommonFunct commonFunct;

    @Autowired
    private MemberRepository memberRepo;

    // @Override
    // public ResponseEntity<?> generateReport(LocalDate date) {
    // int userId = AuthUserData.getUserId();
    // String roleType = "Contract";
    // List<Users> supervisors = usersRepository.findBySupervisorAndRoleType(userId,
    // roleType);
    // List<ContractPersonDTO> supervisorsDTO = new ArrayList<ContractPersonDTO>();
    // String supervisorname = usersRepository.findByUserName(userId);

    // for (Users employee : supervisors) {
    // String commonTimeSheet =
    // commonTimeSheetRepo.findHoursByUserIdAndActivityDate(employee.getId(), date);
    // ContractPersonDTO userdto = new ContractPersonDTO(
    // employee.getId(),
    // employee.getName(),
    // supervisorname,
    // employee.getCreated_at(),
    // employee.getBranch(),
    // commonTimeSheet);
    // supervisorsDTO.add(userdto);
    // }

    // String message = "User Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, supervisorsDTO));

    // }

    // @Override
    // public ResponseEntity<?> getContractPerson(LocalDate date) {
    // int id = AuthUserData.getUserId();
    // String roleType1 = "Contract";
    // Users contractPerson = usersRepository.findByIdAndRoleType(id, roleType1);
    // // finding supervisor name
    // int supervisorId = contractPerson.getSupervisor();
    // Users supervisor = usersRepository.findById(supervisorId).orElse(null);
    // // calculating total work hours
    // String commonTimeSheet =
    // commonTimeSheetRepo.findHoursByUserIdAndActivityDate(id, date);

    // ContractPersonDTO contract = new ContractPersonDTO();

    // contract = new ContractPersonDTO(contractPerson.getId(),
    // contractPerson.getName(),
    // contractPerson.getCreated_at(),
    // contractPerson.getBranch(),
    // supervisor.getName(),
    // commonTimeSheet
    // // activity.getHours()
    // );
    // String message = "User Fetched Successfully.";
    // return ResponseEntity.status(HttpStatus.OK)
    // .body(new ApiResponse(true, message, contract));

    // }

    @Override
    public ResponseEntity<?> getUsersDataList(int id, LocalDate date) {

        List<Object[]> userList = usersRepository.getUsersDataWithIdAndDate(id, date);

        String commonTimeSheet = commonTimeSheetRepo.findHoursByPersonIdAndActivityDate(id, date);
        // List<List<commonTimeSheetRecord>> detailedDTOLists =
        // mapToDetailedDTOList(userList);
        Long approvedCount = commonTimeSheetRepo.countApprovedRecords(id, date);
        List<String> finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDate(id, date);
        String finalApproved;
        if (finalApproveList.isEmpty()) {
            finalApproved = null;
        } else if (approvedCount == finalApproveList.size()) {
            finalApproved = "Approved";
        } else {
            finalApproved = "Not yet";
        }
        ContractUserDataDTO summaryDTOList = mapToSummaryDTOList(userList, commonTimeSheet, id, date, finalApproved);
        if (summaryDTOList == null) {
            summaryDTOList = new ContractUserDataDTO();
            Users userdata = usersRepository.findById(id).orElse(null);
            String preset = attendanceSheetRepository.findStatusByUserId(id, date);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            summaryDTOList.setName(userdata.getName());
            // if (detailedDTOLists == null || detailedDTOLists.isEmpty()) {
            // summaryDTOList.setAttendanceStatus("Leave");
            // } else {
            // summaryDTOList.setAttendanceStatus("Present");
            // }
            summaryDTOList.setAttendanceStatus(preset);
            summaryDTOList.setDoj(userdata.getCreated_at().toLocalDate());
            summaryDTOList.setDay(dayOfWeek.toString());
            summaryDTOList.setEmail(userdata.getEmail());
            summaryDTOList.setDate(date);
            summaryDTOList.setFinalApprove(finalApproved);
        }

        List<List<commonTimeSheetRecord>> detailedDTOList = mapToDetailedDTOList(userList);
        String message = "User Fetched Successfully.";
        Map<String, Object> response = new HashMap<>();
        response.put("Basicdetail", summaryDTOList);
        response.put("Productdetail", detailedDTOList);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, response));
    }

    @Override
    public ResponseEntity<?> getUsersDataListdaterange(int id, LocalDate fromdate, LocalDate todate) {
        List<Object[]> userList = usersRepository.getUsersDataWithIdAndDatebetween(id, fromdate, todate);
        // Map<LocalDate, List<commonTimeSheetRecord>> detailedDTOMap =
        // mapToDetailedDTOMap(userList);
        List<List<commonTimeSheetRecord>> detailedDTOList = mapToDetailedDTOList(userList);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "User Fetched Successfully.", detailedDTOList));
    }

    private Map<LocalDate, List<commonTimeSheetRecord>> mapToDetailedDTOMap(List<Object[]> userList) {
        Map<LocalDate, List<commonTimeSheetRecord>> detailedDTOMap = new HashMap<>();

        for (Object[] userData : userList) {
            java.sql.Date sqlDate = (java.sql.Date) userData[5]; // Assuming the 6th element of the array is the
            // java.sql.Date
            LocalDate date = sqlDate.toLocalDate(); // Convert java.sql.Date to LocalDate

            commonTimeSheetRecord record = (commonTimeSheetRecord) userData[1]; // Assuming the second element is your
            // commonTimeSheetRecord object

            // Check if the map already contains the date as a key
            if (detailedDTOMap.containsKey(date)) {
                // If the date exists, add the record to the list associated with that date
                detailedDTOMap.get(date).add(record);
            } else {
                // If the date doesn't exist, create a new list and add the record to it
                List<commonTimeSheetRecord> records = new ArrayList<>();
                records.add(record);
                detailedDTOMap.put(date, records);
            }
        }

        return detailedDTOMap;
    }

    private ContractUserDataDTO mapToSummaryDTOList(List<Object[]> rawResults, String commonTimeSheet, int id,
                                                    LocalDate date, String finalApproved) {
        List<ContractUserDataDTO> contractUserDataDTOList = rawResults.stream()
                .map(result -> mapToBasicDetail(result, commonTimeSheet, id, date, finalApproved))
                .collect(Collectors.toList());

        if (!contractUserDataDTOList.isEmpty()) {
            return contractUserDataDTOList.get(0);
        } else {
            return null; // Handle the case when the list is empty
        }
    }

    private ContractUserDataDTO mapToBasicDetail(Object[] result, String commonTimeSheet, int id, LocalDate date,
                                                 String finalApproved) {
        List<Object[]> userList = usersRepository.getUsersDataWithIdAndDate(id, date);
        List<List<commonTimeSheetRecord>> detailedDTOList = mapToDetailedDTOList(userList);

        ContractUserDataDTO dto = new ContractUserDataDTO();

        dto.setName((String) result[1]);
        dto.setEmail((String) result[2]);
        dto.setFinalApprove(finalApproved);
        Optional.ofNullable(result[3])
                .map(timestamp -> ((Timestamp) timestamp).toLocalDateTime())
                .ifPresent(dateTime -> {
                    dto.setDoj(dateTime.toLocalDate()); // Extracting date part and setting it in DTO
                });

        String preset = attendanceSheetRepository.findStatusByUserId(id, date);
        if (detailedDTOList != null && !detailedDTOList.isEmpty()) {

            dto.setAttendanceStatus("Present");
        } else if (detailedDTOList == null) {
            dto.setAttendanceStatus("Leave");
        }

        Optional.ofNullable(result[5]).map(sqlDate -> ((Date) sqlDate).toLocalDate())
                .ifPresent(dto::setDate);
        // dto.setAttendanceStatus((String) result[4]);

        Optional.ofNullable(result[5])
                .map(sqlDate -> ((Date) sqlDate).toLocalDate())
                .map(LocalDate::getDayOfWeek)
                .ifPresent(day -> dto.setDay(day.toString()));
        dto.setTotal_work_hours(commonTimeSheet);
        return dto;
    }

    private List<List<commonTimeSheetRecord>> mapToDetailedDTOList(List<Object[]> rawResults) {
        return rawResults.stream()
                .map(this::mapToProductDetails)
                .collect(Collectors.toList());
    }

    ContractUserDataDTO contractUserDataDTO = new ContractUserDataDTO();

    private List<commonTimeSheetRecord> mapToProductDetails(Object[] result) {

        List<commonTimeSheetRecord> productList = new ArrayList<>();
        commonTimeSheetRecord dto = contractUserDataDTO.new commonTimeSheetRecord();

        dto.setUsername((String) result[1]);
        dto.setProductName((String) result[4]);
        Optional.ofNullable(result[5]).map(sqlDate -> ((Date) sqlDate).toLocalDate())
                .ifPresent(dto::setActivity_date);
        dto.setTask((String) result[6]);
        dto.setHours((String) result[7]);
        dto.setUserstatus((String) result[8]);
        dto.setDescription((String) result[9]);
        dto.setApproveStatus((String) result[10]);
        dto.setRemarks((String) result[11]);
        if ("ON Role".equals(result[14])) {
            dto.setFinalApprove((String) result[13]);
            String ownerName = memberRepo.findByMemberAndProdIdAndGetAssignedBy((int) result[21], (int) result[20]);
            if (ownerName != null) {
                dto.setFinalName(ownerName);
            } else {
                if ("Technical".equals(result[17])) {
                    if (result[19] != null) {
                        String value = (String) result[19];
                        String[] parts = value.split(",");
                        int id = Integer.parseInt(parts[0].trim());
                        String name = usersRepository.findByUserName(id);
                        dto.setFinalName(name);
                    }
                }else if ("Data".equals(result[17])) {
                    if (result[22] != null) {
                        String value = (String) result[22];
                        String[] parts = value.split(",");
                        int id = Integer.parseInt(parts[0].trim());
                        String name = usersRepository.findByUserName(id);
                        dto.setFinalName(name);
                    }
                }else if ("HOW".equals(result[17])) {
                    if (result[23] != null) {
                        String value = (String) result[23];
                        String[] parts = value.split(",");
                        int id = Integer.parseInt(parts[0].trim());
                        String name = usersRepository.findByUserName(id);
                        dto.setFinalName(name);
                    }
                } else {
                    if (result[18] != null) {
                        String value = (String) result[18];
                        String[] parts = value.split(",");
                        int id = Integer.parseInt(parts[0].trim());
                        String name = usersRepository.findByUserName(id);
                        dto.setFinalName(name);
                    }
                }

            }
        } else {
            dto.setFinalApprove((String) result[12]);
            dto.setFinalName(usersRepository.findByUserNameGetName((String) result[16]));
        }
        dto.setApproverName(usersRepository.findByUserNameGetName((String) result[15]));
        productList.add(dto);
        return productList;
    }

    @Override
    public List<Map<String, Object>> pdfExcelReport(int id, LocalDate date) {
        // TODO Auto-generated method stub
        return null;
    }

    // @Override
    // public List<Map<String, Object>> pdfExcelReport(int id, LocalDate date) {
    // List<Object[]> userList = taskRepository.getUsersDataWithIdAndDate(id, date);
    // String commonTimeSheet =
    // commonTimeSheetRepo.findHoursByUserIdAndActivityDate(id, date);
    //
    // List<Map<String, Object>> reports = mapToReports(userList, commonTimeSheet);
    // return reports;
    // }
    //
    // private List<Map<String, Object>> mapToReports(List<Object[]> rawResults,
    // String commonTimeSheet) {
    // List<Map<String, Object>> reports = new ArrayList<>();
    //
    // // Assume there is at least one user in rawResults
    // Object[] firstUser = rawResults.get(0);
    // Map<String, Object> userReport = new HashMap();
    // ReportDTO summaryDTO = mapToBasicDetail(firstUser, commonTimeSheet);
    // userReport.put("UserDetail", summaryDTO);
    //
    // List<ProductDetailDTO> allDetailedDTOList = new ArrayList<>();
    //
    // for (Object[] result : rawResults) {
    // List<ProductDetailDTO> detailedDTOList = mapToProductDetails(result);
    // allDetailedDTOList.addAll(detailedDTOList);
    // }
    //
    // userReport.put("TaskDetail", allDetailedDTOList);
    // reports.add(userReport);
    //
    // return reports;
    // }

    @Override
    public ResponseEntity<?> generateReport(LocalDate date) {
        // int userId = AuthUserData.getUserId();
        String userId = AuthUserData.getEmpid();
        String roleType = "Contract";
        List<Users> supervisors = usersRepository.findBySupervisorAndRoleType(userId, roleType);
        List<ContractPersonDTO> supervisorsDTO = new ArrayList<ContractPersonDTO>();
        String supervisorname = usersRepository.findByUserNamegetName(userId);

        for (Users employee : supervisors) {
            String commonTimeSheet = commonTimeSheetRepo.findHoursByUserIdAndActivityDate(employee.getId(), date);
            Long approvedCount = commonTimeSheetRepo.countApprovedRecords(employee.getId(), date);
            List<String> finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDate(employee.getId(), date);
            String finalApproved;
            if (finalApproveList.isEmpty()) {
                finalApproved = null;
            } else if (approvedCount == finalApproveList.size()) {
                finalApproved = "Approved";
            } else {
                finalApproved = "Not yet";
            }

            ContractPersonDTO userdto = new ContractPersonDTO(
                    employee.getId(),
                    employee.getName(),
                    supervisorname,
                    employee.getCreated_at(),
                    employee.getBranch(),
                    commonTimeSheet, finalApproved);
            supervisorsDTO.add(userdto);
        }

        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, supervisorsDTO));

    }

    @Override
    // supervior report based on date range
    public ResponseEntity<?> generateReportDateRange(LocalDate fromdate, LocalDate todate, int userId, int page,
                                                     int size, String roleType) {

        List<Users> supervisors = new ArrayList<>();
        List<ContractPersonDTO> supervisorsDTO = new ArrayList<>();
        String supervisorname = null;
        String loggedInUserId = AuthUserData.getEmpid();
        supervisorname = usersRepository.findByUserNamegetName(loggedInUserId);

        if (userId != 0) {
            Users employee = null;
            if (roleType.equalsIgnoreCase("all")) {
                employee = usersRepository.findByUserId(userId);
            } else {
                employee = usersRepository.findByIdAndRoleType(userId, roleType);

            }

            if (employee != null) {
                supervisors.add(employee);
            }
        } else {
            if (roleType.equalsIgnoreCase("all")) {
                supervisors = usersRepository.findBySupervisorwithoutRoleType(loggedInUserId);
            } else {
                supervisors = usersRepository.findBySupervisorAndRoleType(loggedInUserId, roleType);
            }
        }
        List<LocalDate> datesInRange = getDatesInRange(fromdate, todate);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, datesInRange.size());

        for (int i = startIndex; i < endIndex; i++) {
            LocalDate date = datesInRange.get(i);

            for (Users employee : supervisors) {
                String commonTimeSheet = commonTimeSheetRepo.findHoursByUserIdAndActivityDate(employee.getId(), date);


                List<String> supervisorStatusList = commonTimeSheetRepo.findByUserIdAndActivityDateSupervisorStaus(employee.getId(), date);
                Long superviorApprovedStatus = commonTimeSheetRepo.countApprovedbySupervisorApproved(employee.getId(), date);
                Long superviorRejectStatus = commonTimeSheetRepo.countApprovedbySupervisorReject(employee.getId(), date);

                Long approvedCount;
                List<String> finalApproveList;
                Long rejectedCount;
                if (employee.getRoleType().equalsIgnoreCase("Contract")) {
                    approvedCount = commonTimeSheetRepo.countApprovedRecords(employee.getId(), date);
                    finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDate(employee.getId(), date);
                    rejectedCount = commonTimeSheetRepo.countRejectRecords(employee.getId(), date);

                } else {
                    approvedCount = commonTimeSheetRepo.countApprovedRecordsOwner(employee.getId(), date);
                    finalApproveList = commonTimeSheetRepo
                            .findByUserIdAndActivityDateSupervisorApproved(employee.getId(), date);
                    rejectedCount = commonTimeSheetRepo.countRejectRecordsOwner(employee.getId(), date);

                }
                String supervisorStatus;
                if (supervisorStatusList.isEmpty()) {
                    supervisorStatus = null;
                } else if (supervisorStatusList.size() == superviorApprovedStatus) {
                    supervisorStatus = "Approved";
                } else if (supervisorStatusList.size() == superviorRejectStatus) {
                    supervisorStatus = "Reject";
                } else {
                    supervisorStatus = "Pending";
                }

                String finalApproved;
                if (finalApproveList.isEmpty()) {
                    finalApproved = null;
                } else if (approvedCount == finalApproveList.size()) {
                    finalApproved = "Approved";
                } else if (rejectedCount == finalApproveList.size()) {
                    finalApproved = "Reject";
                } else {
                    assert supervisorStatus != null;
                    if (supervisorStatus.equalsIgnoreCase("Approved")) {
                        finalApproved = "Pending";

                    } else {
                        finalApproved = "Not yet";
                    }
                }

                ContractPersonDTO userdto = new ContractPersonDTO(
                        employee.getId(),
                        employee.getName(),
                        supervisorname,
                        employee.getCreated_at(),
                        employee.getBranch(),
                        commonTimeSheet,
                        finalApproved,
                        date,supervisorStatus);
                supervisorsDTO.add(userdto);
            }
        }
        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, supervisorsDTO));
    }

    public ResponseEntity<?> generateReportDateRange(LocalDate fromdate, LocalDate todate, List<Integer> users_id) {
        String roleType = "Contract";
        List<Users> supervisors = new ArrayList<>();
        List<ContractPersonDTO> supervisorsDTO = new ArrayList<>();
        String supervisorname = null;

        List<LocalDate> datesInRange = getDatesInRange(fromdate, todate);
        for (LocalDate date : datesInRange) {
            for (Integer id : users_id) {
                Users employee = usersRepository.findById(id).orElse(null);
                String commonTimeSheet = commonTimeSheetRepo.findHoursByUserIdAndActivityDate(id, date);
                Long approvedCount = commonTimeSheetRepo.countApprovedRecords(id, date);
                List<String> finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDate(id, date);
                String supervisorStatus;
                Long rejectedCount;
                List<String> supervisorStatusList = commonTimeSheetRepo.findByUserIdAndActivityDateSupervisorStaus(employee.getId(), date);
                Long superviorApprovedStatus = commonTimeSheetRepo.countApprovedbySupervisorApproved(employee.getId(), date);
                Long superviorRejectStatus = commonTimeSheetRepo.countApprovedbySupervisorReject(employee.getId(), date);
                rejectedCount = commonTimeSheetRepo.countRejectRecords(employee.getId(), date);

                if (supervisorStatusList.isEmpty()) {
                    supervisorStatus = null;
                } else if (supervisorStatusList.size() == superviorApprovedStatus) {
                    supervisorStatus = "Approved";
                } else if (supervisorStatusList.size() == superviorRejectStatus) {
                    supervisorStatus = "Reject";
                } else {
                    supervisorStatus = "Pending";
                }

                String finalApproved;
                if (finalApproveList.isEmpty()) {
                    finalApproved = null;
                } else if (approvedCount == finalApproveList.size()) {
                    finalApproved = "Approved";
                } else if (rejectedCount == finalApproveList.size()) {
                    finalApproved = "Reject";
                } else {
                    assert supervisorStatus != null;
                    if (supervisorStatus.equalsIgnoreCase("Approved")) {
                        finalApproved = "Pending";

                    } else {
                        finalApproved = "Not yet";
                    }
                }
                ContractPersonDTO userdto = new ContractPersonDTO(
                        employee.getId(),
                        employee.getName(),
                        supervisorname,
                        employee.getCreated_at(),
                        employee.getBranch(),
                        commonTimeSheet,
                        finalApproved,
                        date,supervisorStatus);
                supervisorsDTO.add(userdto);
            }

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Successfully", supervisorsDTO));
    }

    private List<LocalDate> getDatesInRange(LocalDate fromdate, LocalDate todate) {
        List<LocalDate> datesInRange = new ArrayList<>();
        LocalDate currentDate = fromdate;
        while (!currentDate.isAfter(todate)) {
            datesInRange.add(currentDate);
            if (currentDate.isEqual(todate)) {
                break; // Break the loop if the current date is equal to the end date
            }
            currentDate = currentDate.plusDays(1);
        }
        return datesInRange;
    }

    @Override
    public ResponseEntity<?> getContractPerson(LocalDate fromdate, LocalDate todate, int page, int size) {

        Users contractPerson = usersRepository.findByUsername(AuthUserData.getEmpid()).orElse(null);
        String supervisorId = contractPerson.getSupervisor();
        Users supervisor = usersRepository.findByUserNameGetAll(supervisorId).orElse(null);
        String supervisorName = "";
        if (supervisor != null) {
            supervisorName = supervisor.getName();
        }

        // Calculate the offset
        int offset = (page - 1) * size;

        // Fetch the data from the repository with pagination
        List<ContractPersonDTO> contractPersonsList = new ArrayList<>();
        List<LocalDate> dateRange = getDatesInRange(fromdate, todate);

        // Iterate over the date range with pagination
        int count = 0;
        for (LocalDate date : dateRange) {
            if (count >= offset && contractPersonsList.size() < size) {
                String commonTimeSheet = commonTimeSheetRepo.findHoursByUserIdAndActivityDate(AuthUserData.getUserId(),
                        date);

                List<String> supervisorStatusList = commonTimeSheetRepo.findByUserIdAndActivityDateSupervisorStaus(contractPerson.getId(), date);
                Long superviorApprovedStatus = commonTimeSheetRepo.countApprovedbySupervisorApproved(contractPerson.getId(), date);
                Long superviorRejectStatus = commonTimeSheetRepo.countApprovedbySupervisorReject(contractPerson.getId(), date);

                List<String> finalApproveList;
                Long approvedCount;
                Long rejectedCount;
                if (contractPerson.getRoleType().equalsIgnoreCase("Contract")) {
                    finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDate(contractPerson.getId(),
                            date);
                    approvedCount = commonTimeSheetRepo.countApprovedRecords(contractPerson.getId(), date);
                    rejectedCount = commonTimeSheetRepo.countRejectRecords(contractPerson.getId(), date);

                } else {
                    finalApproveList = commonTimeSheetRepo.findByUserIdAndActivityDateOnRole(contractPerson.getId(), date);
                    approvedCount = commonTimeSheetRepo.countApprovedRecordsOwner(contractPerson.getId(), date);
                    rejectedCount = commonTimeSheetRepo.countRejectRecordsOwner(contractPerson.getId(), date);
                }

                String supervisorStatus;
                if (supervisorStatusList.isEmpty()) {
                    supervisorStatus = null;
                } else if (supervisorStatusList.size() == superviorApprovedStatus) {
                    supervisorStatus = "Approved";
                } else if (supervisorStatusList.size() == superviorRejectStatus) {
                    supervisorStatus = "Reject";
                } else {
                    supervisorStatus = "Pending";
                }

                String finalApproved;
                if (finalApproveList.isEmpty()) {
                    finalApproved = null;
                } else if (approvedCount == finalApproveList.size()) {
                    finalApproved = "Approved";
                } else if (rejectedCount == finalApproveList.size()) {
                    finalApproved = "Reject";
                } else {
                    assert supervisorStatus != null;
                    if (supervisorStatus.equalsIgnoreCase("Approved")) {
                        finalApproved = "Pending";

                    } else {
                        finalApproved = "Not yet";
                    }
                }
                ContractPersonDTO userdto = new ContractPersonDTO(
                        contractPerson.getId(),
                        contractPerson.getName(),
                        supervisorName,
                        contractPerson.getCreated_at(),
                        contractPerson.getBranch(),
                        commonTimeSheet, finalApproved, date, supervisorStatus);

                // Add the DTO to the list
                contractPersonsList.add(userdto);
            }
            count++;
        }

        String message = "Contract Persons Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, contractPersonsList));
    }

    @Override
    public ResponseEntity<?> getContractPersonList(String roletype) {
        List<Users> users = null;
        String userid = AuthUserData.getEmpid();
        if (roletype.equalsIgnoreCase("All")) {
            users = usersRepository.findBySupervisorIdwithoutRoleType(userid);
        } else {
            users = usersRepository.findBySupervisorIdandRoleType(userid, roletype);

        }
        List<UserDTO> userDTO = new ArrayList<UserDTO>();

        for (Users user : users) {
            UserDTO userdto = new UserDTO();
            userdto.setId(user.getId());
            userdto.setName(user.getName());
            List<String> roleNamesTeachead = user.getRole_id().stream().map(Roles::getName)
                    .collect(Collectors.toList());
            String concatenatedteachead = String.join(",", roleNamesTeachead);
            userdto.setDesignation(concatenatedteachead);
            userdto.setRole(user.getRoleType());
            userdto.setProfile_pic(user.getProfile_pic());
            userDTO.add(userdto);
        }

        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(userDTO);

        // Sort the userDTO list in descending order based on user ID
        modifiedUserDTOs.sort(Comparator.comparing(UserDTO::getId, Comparator.reverseOrder()));

        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, modifiedUserDTOs));
    }

}
