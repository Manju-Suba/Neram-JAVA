package pm.serviceImplements;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import pm.dto.EmployeeAttendance;
import pm.dto.ResponseWrapper;
import pm.model.Budgie.BudgieApi;
import pm.model.users.Users;
import pm.repository.AttendanceSheetRepository;
import pm.repository.BudgieRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.utils.AuthUserData;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class SwipeInSwipeOut {

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.token}")
    private String token;

    @Value("${external.CITPL.api.url}")
    private String citplUrl;

    private final UsersRepository userRepo;
    private final BudgieRepository budgieRepository;
    private final AttendanceSheetRepository attendanceSheetRepository;
    private final CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;
    private final WebClient webClient;

    public Logger logger = LoggerFactory.getLogger(SwipeInSwipeOut.class);

    public SwipeInSwipeOut(UsersRepository userRepo, BudgieRepository budgieRepository,
                           AttendanceSheetRepository attendanceSheetRepository,
                           CommonTimeSheetActivityRepository commonTimeSheetActivityRepository, WebClient webClient) {
        this.userRepo = userRepo;
        this.budgieRepository = budgieRepository;
        this.attendanceSheetRepository = attendanceSheetRepository;
        this.commonTimeSheetActivityRepository = commonTimeSheetActivityRepository;
        this.webClient = webClient;
    }

    public ResponseEntity<ResponseWrapper> callExternalApi(String paramName, String paramValue,
                                                           boolean checkShortfallHours, boolean checkExcessHours) {
        try {
            // Prepare URL with query parameters
            String url = baseUrl + "/get_onsite_attendance_table";

            // Build the URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

            if (paramName != null) {
                String trimmedParamName = paramName.trim();
                if (!trimmedParamName.isEmpty()) {
                    builder.queryParam("employee", trimmedParamName);
                } else {
                    // If paramName is empty after trimming, set employee parameter to %20
                    builder.queryParam("employee", URLEncoder.encode(" ", StandardCharsets.UTF_8));
                }
            }

            // Add daterange parameter
            builder.queryParam("daterange", paramValue);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make the API call and get the ResponseWrapper
            Mono<ResponseWrapper> responseWrapperMono = webClient.get()
                    .uri(builder.toUriString())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .retryWhen(Retry.fixedDelay(5, Duration.ofSeconds(5)))
                    .doOnError(e -> logger.error("Error during API call: {}", e.getMessage(), e))
                    .onErrorResume(e -> {
                        logger.error("Retries exhausted: {}", e.getMessage(), e);
                        return Mono.empty();
                    });

            // Block and get the response
            ResponseWrapper responseWrapper = responseWrapperMono.block();

            if (responseWrapper == null) {
                // Handle the case when responseWrapper is null
                return ResponseEntity.internalServerError().body(new ResponseWrapper(false, "No response from external API", null));
            }

            // Process the result to calculate and add additional fields
            List<EmployeeAttendance> employeeAttendances = responseWrapper.getResult();

            for (EmployeeAttendance attendance : employeeAttendances) {
                String signIn = attendance.getSignIn();
                String signOut = attendance.getSignOut();

                boolean excess = attendance.isExcess();
                boolean shortfall = attendance.isShortfall();


                // Check conditions to break the loop
                if (checkExcessHours && !excess) {
                    employeeAttendances = Collections.emptyList();
                    break;
                }

                if (checkShortfallHours && !shortfall) {
                    employeeAttendances = Collections.emptyList();
                    break;
                }

                try {
                    // Parse signIn and signOut times into LocalTime
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                    LocalTime signInTime = LocalTime.parse(signIn, formatter);
                    LocalTime signOutTime = LocalTime.parse(signOut, formatter);

                    // Calculate actual worked duration
                    Duration actualWorkDuration = Duration.between(signInTime, signOutTime);

                    Integer userId = userRepo.findByusernameGetUserId(attendance.getEmployeeID());
                    if (userId != null) {
                        DateTimeFormatter formatterPassing = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDate date = LocalDate.parse(attendance.getDate(), formatterPassing);

                        List<Object[]> timevalue = commonTimeSheetActivityRepository.getTimeTotalHours(userId, date);

                        if (!timevalue.isEmpty()) {
                            Object[] responsevalue = timevalue.get(0);
                            if (responsevalue != null) {
                                Time activityTime = (Time) responsevalue[0];
                                LocalTime localActivityTime = activityTime.toLocalTime();
                                attendance.setTimesheetHours(
                                        localActivityTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Hrs");
                            } else {
                                attendance.setTimesheetHours("00:00 Hrs");
                            }
                        } else {
                            attendance.setTimesheetHours("00:00 Hrs");
                        }
                    }

                    // Calculate excess hours and minutes
                    long excessHours = 0;
                    long excessMinutes = 0;
                    if (excess) {
                        if (actualWorkDuration.compareTo(Duration.ofHours(9)) > 0) {
                            Duration excessDuration = actualWorkDuration.minus(Duration.ofHours(9));
                            excessHours = excessDuration.toHours();
                            excessMinutes = excessDuration.toMinutesPart();
                        }
                        String excessHoursMinutes = String.format("%02d:%02d", excessHours, excessMinutes);
                        attendance.setExcessHours(excessHoursMinutes);
                    } else {
                        attendance.setExcessHours("00:00");
                    }

                    // Format hours and minutes
                    String actualHoursMinutes = String.format("%02d:%02d", actualWorkDuration.toHours(),
                            actualWorkDuration.toMinutesPart());

                    // Set calculated hours and excess hours in attendance object
                    attendance.setTotalWorkHours("09:00 Hrs");
                    attendance.setAcutalWorkHours(actualHoursMinutes);

                    if (attendance.isRegularizationStatus()) {
                        System.out.println("Regularization is true");
                        attendance.setShortfallHours("00:00");
                    } else {
                        if (shortfall) {
                            System.out.println("False for Regularization");
                            // Calculate shortfall duration relative to 9 hours
                            Duration shortfallDuration = Duration.ofHours(9).minus(actualWorkDuration);

                            // Handle shortfall hours and minutes
                            long shortfallHours = Math.max(0, shortfallDuration.toHours()); // Ensure shortfallHours is non-negative
                            long shortfallMinutes = shortfallDuration.toMinutesPart();

                            // If actualWorkDuration exceeds 9 hours, set shortfall to 0:00
                            if (actualWorkDuration.compareTo(Duration.ofHours(9)) > 0) {
                                shortfallHours = 0;
                                shortfallMinutes = 0;
                            }
                            String shortfallHoursMinutes = String.format("%02d:%02d", shortfallHours, shortfallMinutes);

                            attendance.setShortfallHours(shortfallHoursMinutes);
                        } else {
                            attendance.setShortfallHours("00:00");
                        }
                    }

                } catch (DateTimeParseException e) {
                    // Handle parsing error
                    logger.error("Error parsing time for attendance: {}", e.getMessage(), e);
                    // Optionally set default values or handle the error accordingly
                }
            }

            // Update the result in responseWrapper if it's not null
            if (responseWrapper != null) {
                responseWrapper.setResult(employeeAttendances);
            }

            // Return the updated responseWrapper or an appropriate response if null
            return responseWrapper != null
                    ? ResponseEntity.ok(responseWrapper)
                    : ResponseEntity.internalServerError().body(new ResponseWrapper(false, "No valid data found", null));

        } catch (Exception e) {
            logger.error("Error calling external API: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new ResponseWrapper());
        }
    }



    // ==============================Supervisor based
    // implementation============================
//    public ResponseEntity<?> membersBasedSwipeInOut(LocalDate date, List<String> empIds, boolean checkShortfallHours,
//                                                    boolean checkExcessHours, int pageNumber, int pageSize) {
//        try {
//            // Fetch authenticated user's empId
//            String empId = AuthUserData.getEmpid();
//            Optional<Users> userdata = userRepo.findByUsernameAndIs_deletedFalse(empId);
//
//            // Format LocalDate to a date string
//            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//            String passingdate = date.format(formatter) + "-" + date.format(formatter);
//
//            // Fetch list of active users
//            List<Users> userlist = (empIds != null && !empIds.isEmpty()) ? userRepo.getUserList(empIds)
//                    : userRepo.findByStatusandActiveEmployees(empId);
//
//            // Initialize pagination variables
//            int totalItems = userlist.size();
//            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
//            int startIndex = pageNumber * pageSize;
//            int endIndex = Math.min(startIndex + pageSize, totalItems);
//
//            // Check if pageNumber is valid
//            if (startIndex >= totalItems && !checkShortfallHours && !checkExcessHours) {
//                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
//            }
//
//            // Subset of users for the current page or all users if conditions met
//            List<Users> paginatedUserList;
//            if (!checkShortfallHours && !checkExcessHours) {
//                paginatedUserList = userlist.subList(startIndex, endIndex); // Paginate
//            } else {
//                paginatedUserList = userlist; // Fetch all users
//            }
//
//            // List to store response maps
//            List<Map<String, Object>> responseMapList = new ArrayList<>();
//
//            // Process each user in the user list
//            for (Users user : paginatedUserList) {
//                ResponseWrapper responseBody = null;
//
//                if (userdata.isPresent() && userdata.get().getEmail().contains("@cavininfotech.com")&& !userdata.get().getUsername().equalsIgnoreCase("DY006")) {
//                    // Fetch BudgieApi based on user and date
//                    BudgieApi budgieApi = budgieRepository.findByEmployeeIdAndDate(user.getUsername(),
//                            convertLocalDateToDate(date));
//                    if (budgieApi != null) {
//                        EmployeeAttendance employeeAttendance = new EmployeeAttendance();
//                        employeeAttendance.setSignIn(budgieApi.getSignIn());
//                        employeeAttendance.setUsername(userRepo.findByUserNamegetName(budgieApi.getEmployeeID()));
//                        employeeAttendance.setSignOut(budgieApi.getSignOut());
//                        employeeAttendance.setEmployeeID(budgieApi.getEmployeeID());
//                        employeeAttendance.setDate(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//
//                        responseBody = convertBudgieApiToResponseWrapper(employeeAttendance, passingdate);
//                    } else {
//                        responseBody = new ResponseWrapper(false, "No data found", null);
//                    }
//                } else {
//                    ResponseEntity<ResponseWrapper> responseEntity = callExternalApi(user.getUsername(), passingdate,
//                            checkShortfallHours, checkExcessHours);
//                    if (responseEntity != null && responseEntity.getBody() != null) {
//                        responseBody = responseEntity.getBody();
//                    } else {
//                        responseBody = new ResponseWrapper(false, "No data found", null);
//                    }
//                }
//
//                // Fetch leave information
//                String leave = attendanceSheetRepository.statusForAppliedDateandUser(date, user.getId());
//                System.out.println(responseBody);
//                // Process attendance and add to responseMapList
//                if (responseBody != null && responseBody.isStatus() && responseBody.getResult() != null
//                        && !responseBody.getResult().isEmpty()) {
//                    EmployeeAttendance sourceAttendance = responseBody.getResult().get(0);
//                    EmployeeAttendance targetAttendance = new EmployeeAttendance();
//                    BeanUtils.copyProperties(sourceAttendance, targetAttendance);
//
//                    // Check conditions based on flags
//                    boolean includeRecord = true;
//                    if (checkShortfallHours && (targetAttendance.getShortfallHours() == null || targetAttendance.getShortfallHours().compareTo("00:00") <= 0)) {
//                        includeRecord = false;
//                    }
//                    if (checkExcessHours && (targetAttendance.getExcessHours() == null || targetAttendance.getExcessHours().compareTo("00:00") <= 0)) {
//                        includeRecord = false;
//                    }
//
//                    if (includeRecord) {
//                        // Create response map and add to responseMapList
//                        Map<String, Object> responseMap = new HashMap<>();
//                        responseMap.put("name", user.getName());
//                        responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : targetAttendance);
//                        responseMapList.add(responseMap);
//                    }
//                } else {
//                    // Include null records when not checking for shortfall or excess hours
//                    if (!checkShortfallHours && !checkExcessHours) {
//                        Map<String, Object> responseMap = new HashMap<>();
//                        responseMap.put("name", user.getName());
//                        responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : null);
//                        responseMapList.add(responseMap);
//                    }
//                }
//            }
//
//            // Create response data
//            Map<String, Object> data = new HashMap<>();
//            if (checkShortfallHours || checkExcessHours) {
//                // Apply pagination for filtered results after fetching all data
//                totalItems = responseMapList.size();
//                totalPages = (int) Math.ceil((double) totalItems / pageSize);
//                startIndex = pageNumber * pageSize;
//                endIndex = Math.min(startIndex + pageSize, totalItems);
//
//
//                if (startIndex > totalItems) {
//                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
//                }
//
//                List<Map<String, Object>> paginatedResponseMapList = responseMapList.subList(startIndex, endIndex);
//
//                data.put("data", paginatedResponseMapList);
//            } else {
//                data.put("data", responseMapList);
//            }
//
//            data.put("totalItems", totalItems);
//            data.put("totalPages", totalPages);
//            System.out.println("4444444");
//
//            // Return ApiResponse containing the responseMapList and pagination info if applicable
//            ApiResponse apiResponse = new ApiResponse(true, "Supervisor Based Members Fetched Successfully", data);
//            return ResponseEntity.ok().body(apiResponse);
//
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//            return ResponseEntity.internalServerError().body(new ApiResponse(false, e.getMessage(), e.getMessage()));
//        }
//    }

    public ResponseEntity<?> membersBasedSwipeInOut(LocalDate date, List<String> empIds, boolean checkShortfallHours,
                                                    boolean checkExcessHours, int pageNumber, int pageSize) {
        try {
            String empId = AuthUserData.getEmpid();
            Optional<Users> userdata = userRepo.findByUsernameAndIs_deletedFalse(empId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
            String passingdate = date.format(formatter) + "-" + date.format(formatter);

            List<Users> userlist = (empIds != null && !empIds.isEmpty()) ? userRepo.getUserList(empIds)
                    : userRepo.findByStatusandActiveEmployees(empId);

            int totalItems = userlist.size();
            int totalPages = (int) Math.ceil((double) totalItems / pageSize);
            int startIndex = pageNumber * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalItems);

            if (startIndex >= totalItems && !checkShortfallHours && !checkExcessHours) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
            }

            List<Users> paginatedUserList;
            if (!checkShortfallHours && !checkExcessHours) {
                paginatedUserList = userlist.subList(startIndex, endIndex);
            } else {
                paginatedUserList = userlist;
            }

            List<Map<String, Object>> responseMapList = Collections.synchronizedList(new ArrayList<>());

            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            List<Callable<Void>> tasks = new ArrayList<>();
            for (Users user : paginatedUserList) {
                tasks.add(() -> {
                    processUser(user, date, passingdate, userdata, checkShortfallHours, checkExcessHours, responseMapList);
                    return null;
                });
            }

            executorService.invokeAll(tasks);
            executorService.shutdown();

            Map<String, Object> data = new HashMap<>();
            if (checkShortfallHours || checkExcessHours) {
                totalItems = responseMapList.size();
                totalPages = (int) Math.ceil((double) totalItems / pageSize);
                startIndex = pageNumber * pageSize;
                endIndex = Math.min(startIndex + pageSize, totalItems);

                if (startIndex > totalItems) {
                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
                }

                List<Map<String, Object>> paginatedResponseMapList = responseMapList.subList(startIndex, endIndex);
                data.put("data", paginatedResponseMapList);
            } else {
                data.put("data", responseMapList);
            }

            data.put("totalItems", totalItems);
            data.put("totalPages", totalPages);

            ApiResponse apiResponse = new ApiResponse(true, "Supervisor Based Members Fetched Successfully", data);
            return ResponseEntity.ok().body(apiResponse);

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body(new ApiResponse(false, e.getMessage(), e.getMessage()));
        }
    }

    private void processUser(Users user, LocalDate date, String passingdate, Optional<Users> userdata,
                             boolean checkShortfallHours, boolean checkExcessHours, List<Map<String, Object>> responseMapList) {
        try {
            ResponseWrapper responseBody = null;

            if (userdata.isPresent() && userdata.get().getEmail().contains("@cavininfotech.com") && !userdata.get().getUsername().equalsIgnoreCase("DY006")) {
                BudgieApi budgieApi = budgieRepository.findByEmployeeIdAndDate(user.getUsername(),
                        convertLocalDateToDate(date));
                if (budgieApi != null) {
                    EmployeeAttendance employeeAttendance = new EmployeeAttendance();
                    employeeAttendance.setSignIn(budgieApi.getSignIn());
                    employeeAttendance.setUsername(userRepo.findByUserNamegetName(budgieApi.getEmployeeID()));
                    employeeAttendance.setSignOut(budgieApi.getSignOut());
                    employeeAttendance.setEmployeeID(budgieApi.getEmployeeID());
                    employeeAttendance.setDate(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

                    responseBody = convertBudgieApiToResponseWrapper(employeeAttendance, passingdate);
                } else {
                    responseBody = new ResponseWrapper(false, "No data found", null);
                }
            } else {
                ResponseEntity<ResponseWrapper> responseEntity = callExternalApi(user.getUsername(), passingdate,
                        checkShortfallHours, checkExcessHours);
                if (responseEntity != null && responseEntity.getBody() != null) {
                    responseBody = responseEntity.getBody();
                } else {
                    responseBody = new ResponseWrapper(false, "No data found", null);
                }
            }

            String leave = attendanceSheetRepository.statusForAppliedDateandUser(date, user.getId());

            if (responseBody != null && responseBody.isStatus() && responseBody.getResult() != null
                    && !responseBody.getResult().isEmpty()) {
                EmployeeAttendance sourceAttendance = responseBody.getResult().get(0);
                EmployeeAttendance targetAttendance = new EmployeeAttendance();
                BeanUtils.copyProperties(sourceAttendance, targetAttendance);

                boolean includeRecord = true;
                if (checkShortfallHours && (targetAttendance.getShortfallHours() == null || targetAttendance.getShortfallHours().compareTo("00:00") <= 0)) {
                    includeRecord = false;
                }
                if (checkExcessHours && (targetAttendance.getExcessHours() == null || targetAttendance.getExcessHours().compareTo("00:00") <= 0)) {
                    includeRecord = false;
                }

                if (includeRecord) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("name", user.getName());
                    responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : targetAttendance);
                    responseMapList.add(responseMap);
                }
            } else {
                if (!checkShortfallHours && !checkExcessHours) {
                    Map<String, Object> responseMap = new HashMap<>();
                    responseMap.put("name", user.getName());
                    responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : null);
                    responseMapList.add(responseMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public ResponseWrapper convertBudgieApiToResponseWrapper(EmployeeAttendance employeeAttendance,
                                                             String formattedDate) {
        ResponseWrapper responseWrapper = new ResponseWrapper();
        responseWrapper.setStatus(true);
        responseWrapper.setResult(processAttendanceTimes(employeeAttendance, formattedDate));
        responseWrapper.setMessage("Records fetched Successfully");
        // Set other properties if any
        return responseWrapper;
    }

    public static Date convertLocalDateToDate(LocalDate localDate) {
        // Format the LocalDate
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String formattedDateStr = localDate.format(formatter);

        // Convert the formatted date string back to LocalDate
        LocalDate formattedLocalDate = LocalDate.parse(formattedDateStr, formatter);

        // Convert the LocalDate to java.sql.Date
        return java.sql.Date.valueOf(formattedLocalDate);
    }

//    public ResponseEntity<ApiResponse> monthBasedSwipeInOut(LocalDate date, String month, String year, boolean checkShortfallHours, boolean excessHours, int pageNumber, int pageSize) {
//        try {
//            String empId = AuthUserData.getEmpid();
//            Optional<Users> userdata = userRepo.findByUsernameAndIs_deletedFalse(empId);
//
//            int monthe = Integer.parseInt(month);
//            int yearr = Integer.parseInt(year);
//
//            YearMonth yearMonth = YearMonth.of(yearr, monthe);
//            LocalDate firstDateOfMonth = yearMonth.atDay(1);
//            LocalDate lastDateOfMonth = yearMonth.atEndOfMonth();
//            if (lastDateOfMonth.isAfter(LocalDate.now())) {
//                lastDateOfMonth = LocalDate.now();
//            }
//
//            List<LocalDate> allDates = new ArrayList<>();
//            LocalDate current = firstDateOfMonth;
//            if (date != null) {
//                allDates.add(date);
//            } else {
//                while (!current.isAfter(lastDateOfMonth)) {
//                    allDates.add(current);
//                    current = current.plusDays(1);
//                }
//            }
//
//            List<Map<String, Object>> responseMapList = new ArrayList<>();
//            List<Map<String, Object>> filteredResponseMapList = new ArrayList<>();
//
//            for (LocalDate currentDate : allDates) {
//                DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
//                String formattedDate = currentDate.format(formatterDate) + "-" + currentDate.format(formatterDate);
//
//                ResponseWrapper responseBody = null;
//                if (userdata.isPresent() && userdata.get().getEmail().contains("@cavininfotech.com") && !userdata.get().getUsername().equalsIgnoreCase("DY006")) {
//                    BudgieApi budgieApi = budgieRepository.findByEmployeeIdAndDate(userdata.get().getUsername(), convertLocalDateToDate(currentDate));
//                    if (budgieApi != null) {
//                        EmployeeAttendance employeeAttendance = new EmployeeAttendance();
//                        employeeAttendance.setSignIn(budgieApi.getSignIn());
//                        employeeAttendance.setUsername(userRepo.findByUserNamegetName(budgieApi.getEmployeeID()));
//                        employeeAttendance.setSignOut(budgieApi.getSignOut());
//                        employeeAttendance.setEmployeeID(budgieApi.getEmployeeID());
//                        employeeAttendance.setDate(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//
//                        responseBody = convertBudgieApiToResponseWrapper(employeeAttendance, formattedDate);
//                    } else {
//                        responseBody = new ResponseWrapper(false, "No data found", null);
//                    }
//                } else {
//                    ResponseEntity<ResponseWrapper> responseEntity = callExternalApi(empId, formattedDate, checkShortfallHours, excessHours);
//                    if (responseEntity != null && responseEntity.getBody() != null) {
//                        responseBody = responseEntity.getBody();
//                    } else {
//                        responseBody = new ResponseWrapper(false, "No data found", null);
//                    }
//                }
//                System.out.println(responseBody);
//
//                String leave = attendanceSheetRepository.statusForAppliedDateandUser(currentDate, userRepo.findByEmpidGetUserId(empId));
//                List<?> resultList = responseBody != null ? responseBody.getResult() : null;
//
//                if (responseBody != null && responseBody.isStatus() && resultList != null && !resultList.isEmpty()) {
//                    EmployeeAttendance sourceAttendance = responseBody.getResult().get(0);
//                    EmployeeAttendance targetAttendance = new EmployeeAttendance();
//                    BeanUtils.copyProperties(sourceAttendance, targetAttendance);
//                    boolean addToResponseList = true;
//
//                    if (checkShortfallHours) {
//                        if (targetAttendance.getShortfallHours() != null && targetAttendance.getShortfallHours().compareTo("00:00") <= 0) {
//                            addToResponseList = false;
//                        }
//                    } else if (excessHours) {
//                        if (targetAttendance.getExcessHours() != null && targetAttendance.getExcessHours().compareTo("00:00") <= 0) {
//                            addToResponseList = false;
//                        }
//                    }
//
//                    Map<String, Object> responseMap = new HashMap<>();
//                    responseMap.put("date", currentDate);
//                    responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : targetAttendance);
//
//                    if (addToResponseList) {
//                        filteredResponseMapList.add(responseMap);
//                    }
//
//                    responseMapList.add(responseMap);
//                } else {
//                    Map<String, Object> responseMap = new HashMap<>();
//                    responseMap.put("date", currentDate);
//                    responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : null);
//                    responseMapList.add(responseMap);
//                }
//            }
//
//            List<Map<String, Object>> paginatedResponseMapList;
//
//            if (checkShortfallHours || excessHours) {
//                int totalFilteredItems = filteredResponseMapList.size();
//                int totalFilteredPages = (int) Math.ceil((double) totalFilteredItems / pageSize);
//                int filteredStartIndex = pageNumber * pageSize;
//                int filteredEndIndex = Math.min(filteredStartIndex + pageSize, totalFilteredItems);
//
//                if (filteredStartIndex >= totalFilteredItems) {
//                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
//                }
//
//                paginatedResponseMapList = filteredResponseMapList.subList(filteredStartIndex, filteredEndIndex);
//
//                Map<String, Object> data = new HashMap<>();
//                data.put("data", paginatedResponseMapList);
//                data.put("totalPages", totalFilteredPages);
//                data.put("totalItems", totalFilteredItems);
//
//                ApiResponse apiResponse = new ApiResponse(true, "Month Based Data Fetched successfully", data);
//                return ResponseEntity.ok().body(apiResponse);
//
//            } else {
//                int totalItems = responseMapList.size();
//                int totalPages = (int) Math.ceil((double) totalItems / pageSize);
//                int startIndex = pageNumber * pageSize;
//                int endIndex = Math.min(startIndex + pageSize, totalItems);
//
//                System.out.println(startIndex);
//                System.out.println(totalItems);
//
//                if (startIndex >= totalItems) {
//                    return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
//                }
//
//                paginatedResponseMapList = responseMapList.subList(startIndex, endIndex);
//
//                Map<String, Object> data = new HashMap<>();
//                data.put("data", paginatedResponseMapList);
//                data.put("totalPages", totalPages);
//                data.put("totalItems", totalItems);
//
//                ApiResponse apiResponse = new ApiResponse(true, "Month Based Data Fetched successfully", data);
//                return ResponseEntity.ok().body(apiResponse);
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Internal Server Issue", e.getMessage()));
//        }
//    }


    public ResponseEntity<ApiResponse> monthBasedSwipeInOut(LocalDate date, String month, String year, boolean checkShortfallHours, boolean excessHours, int pageNumber, int pageSize) {
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        try {
            String empId = AuthUserData.getEmpid();
            Optional<Users> userdata = userRepo.findByUsernameAndIs_deletedFalse(empId);

            int monthe = Integer.parseInt(month);
            int yearr = Integer.parseInt(year);

            YearMonth yearMonth = YearMonth.of(yearr, monthe);
            LocalDate firstDateOfMonth = yearMonth.atDay(1);
            LocalDate lastDateOfMonth = yearMonth.atEndOfMonth().isAfter(LocalDate.now()) ? LocalDate.now() : yearMonth.atEndOfMonth();

            List<LocalDate> allDates = date != null ? List.of(date) : generateDatesList(firstDateOfMonth, lastDateOfMonth);

            // Use ExecutorService to process each date in parallel
            List<Callable<Map<String, Object>>> tasks = new ArrayList<>();
            for (LocalDate currentDate : allDates) {
                tasks.add(() -> getAttendanceForDate(userdata, empId, currentDate, checkShortfallHours, excessHours));
            }

            List<Future<Map<String, Object>>> futures = executorService.invokeAll(tasks);
            List<Map<String, Object>> responseMapList = new ArrayList<>();
            for (Future<Map<String, Object>> future : futures) {
                try {
                    responseMapList.add(future.get());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            executorService.shutdown();

            List<Map<String, Object>> filteredResponseMapList = responseMapList.stream()
                    .filter(map -> filterByHours(map, checkShortfallHours, excessHours))
                    .collect(Collectors.toList());

            System.out.println(filteredResponseMapList);

            return preparePaginatedResponse(checkShortfallHours || excessHours ? filteredResponseMapList : responseMapList, pageNumber, pageSize);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "Internal Server Issue", e.getMessage()));
        }
    }
    private List<LocalDate> generateDatesList(LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        while (!start.isAfter(end)) {
            dates.add(start);
            start = start.plusDays(1);
        }
        return dates;
    }

    private Map<String, Object> getAttendanceForDate(Optional<Users> userdata, String empId, LocalDate currentDate, boolean checkShortfallHours, boolean excessHours) {
        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String formattedDate = currentDate.format(formatterDate) + "-" + currentDate.format(formatterDate);

        ResponseWrapper responseBody;
        if (userdata.isPresent() && userdata.get().getEmail().contains("@cavininfotech.com") && !userdata.get().getUsername().equalsIgnoreCase("DY006")) {
            responseBody = getResponseFromInternalApi(userdata, currentDate, formattedDate);
        } else {
            responseBody = callExternalApi(empId, formattedDate, checkShortfallHours, excessHours).getBody();
        }

        return buildResponseMap(currentDate, responseBody, empId);
    }

    private ResponseWrapper getResponseFromInternalApi(Optional<Users> userdata, LocalDate currentDate, String formattedDate) {
        BudgieApi budgieApi = budgieRepository.findByEmployeeIdAndDate(userdata.get().getUsername(), convertLocalDateToDate(currentDate));
        if (budgieApi != null) {
            EmployeeAttendance employeeAttendance = new EmployeeAttendance();
            employeeAttendance.setSignIn(budgieApi.getSignIn());
            employeeAttendance.setUsername(userRepo.findByUserNamegetName(budgieApi.getEmployeeID()));
            employeeAttendance.setSignOut(budgieApi.getSignOut());
            employeeAttendance.setEmployeeID(budgieApi.getEmployeeID());
            employeeAttendance.setDate(currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            return convertBudgieApiToResponseWrapper(employeeAttendance, formattedDate);
        } else {
            return new ResponseWrapper(false, "No data found", null);
        }
    }

    private boolean filterByHours(Map<String, Object> map, boolean checkShortfallHours, boolean excessHours) {
        if (checkShortfallHours || excessHours) {
            System.out.println("data: " + map);

            Object attendanceObj = map.get("attendance");
            if (attendanceObj instanceof EmployeeAttendance) {
                EmployeeAttendance attendance = (EmployeeAttendance) attendanceObj;

                if (attendance != null) {
                    if (checkShortfallHours && attendance.getShortfallHours() != null && attendance.getShortfallHours().compareTo("00:00") <= 0) {
                        return false;
                    } else if (excessHours && attendance.getExcessHours() != null && attendance.getExcessHours().compareTo("00:00") <= 0) {
                        return false;
                    }
                }
            } else {
                System.out.println("Attendance is not of type EmployeeAttendance, it is: " + attendanceObj);
                // Handle cases where attendance is not of type EmployeeAttendance
                // For example, if attendanceObj is "leave" or any other string
                return false; // or any other logic you want to apply for non-EmployeeAttendance objects
            }
        }
        return true;
    }

    private Map<String, Object> buildResponseMap(LocalDate currentDate, ResponseWrapper responseBody, String empId) {
        Map<String, Object> responseMap = new HashMap<>();
        String leave = attendanceSheetRepository.statusForAppliedDateandUser(currentDate, userRepo.findByEmpidGetUserId(empId));
        List<?> resultList = responseBody != null ? responseBody.getResult() : null;

        if (responseBody != null && responseBody.isStatus() && resultList != null && !resultList.isEmpty()) {
            EmployeeAttendance sourceAttendance = responseBody.getResult().get(0);
            EmployeeAttendance targetAttendance = new EmployeeAttendance();
            BeanUtils.copyProperties(sourceAttendance, targetAttendance);
            responseMap.put("date", currentDate);
            responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : targetAttendance);
        } else {
            responseMap.put("date", currentDate);
            responseMap.put("attendance", (leave != null && !leave.isEmpty()) ? leave : null);
        }
        return responseMap;
    }

    private ResponseEntity<ApiResponse> preparePaginatedResponse(List<Map<String, Object>> responseMapList, int pageNumber, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        if (responseMapList.isEmpty()) {
            data.put("data", Collections.emptyList());
            data.put("totalPages", 0);
            data.put("totalItems", 0);
            return ResponseEntity.ok().body(new ApiResponse(true, "No data found for the given criteria", data));
        }

        int totalItems = responseMapList.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = pageNumber * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalItems);

        if (startIndex >= totalItems) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid page number", null));
        }

        List<Map<String, Object>> paginatedResponseMapList = responseMapList.subList(startIndex, endIndex);

        data.put("data", paginatedResponseMapList);
        data.put("totalPages", totalPages);
        data.put("totalItems", totalItems);

        ApiResponse apiResponse = new ApiResponse(true, "Month Based Data Fetched successfully", data);
        return ResponseEntity.ok().body(apiResponse);
    }




    /// =============================common Method
    /// ==================================

    public List<EmployeeAttendance> processAttendanceTimes(EmployeeAttendance attendance, String formattedDate) {
        String signIn = attendance.getSignIn();
        String signOut = attendance.getSignOut();

        try {

            ResponseWrapper regularization = citplRegularization(attendance.getEmployeeID(), formattedDate);

            if (regularization != null && regularization.isStatus() && regularization.getResult() != null
                    && !regularization.getResult().isEmpty()) {
                signIn = regularization.getResult().get(0).getSignIn();
                signOut = regularization.getResult().get(0).getSignOut();
                attendance.setSignIn(regularization.getResult().get(0).getSignIn());
                attendance.setSignOut(regularization.getResult().get(0).getSignOut());
                attendance.setRegularizationStatus(true);
            }

            // Parse signIn and signOut times into LocalTime
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            LocalTime signInTime = LocalTime.parse(signIn, formatter);
            LocalTime signOutTime = LocalTime.parse(signOut, formatter);

            // Define standard work hours (8 hours 30 minutes)
            Duration standardWorkDuration = Duration.ofHours(8).plusMinutes(30);

            // Calculate actual worked duration
            Duration actualWorkDuration = Duration.between(signInTime, signOutTime);

            // Calculate shortfall duration relative to 8 hours 30 minutes
            Duration shortfallDuration = standardWorkDuration.minus(actualWorkDuration);

            // Handle shortfall hours and minutes
            long shortfallHours = Math.max(0, shortfallDuration.toHours()); // Ensure shortfallHours is non-negative
            long shortfallMinutes = shortfallDuration.toMinutesPart();

            Integer userId = userRepo.findByusernameGetUserId(attendance.getEmployeeID());
            if (userId != null) {
                DateTimeFormatter formatterPassing = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(attendance.getDate(), formatterPassing);

                List<Object[]> timevalue = commonTimeSheetActivityRepository.getTimeTotalHours(userId, date);

                if (!timevalue.isEmpty()) {
                    Object[] responsevalue = timevalue.get(0);
                    if (responsevalue != null) {
                        Time activityTime = (Time) responsevalue[0];
                        LocalTime localActivityTime = activityTime.toLocalTime();
                        attendance.setTimesheetHours(
                                localActivityTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " Hrs");
                    } else {
                        attendance.setTimesheetHours("00:00 Hrs");
                    }
                } else {
                    attendance.setTimesheetHours("00:00 Hrs");
                }
            }

            // If actualWorkDuration exceeds standard work hours, set shortfall to 0:00
            if (actualWorkDuration.compareTo(standardWorkDuration) > 0) {
                shortfallHours = 0;
                shortfallMinutes = 0;
            }

            // Calculate excess hours and minutes
            long excessHours = 0;
            long excessMinutes = 0;

            // If actual work duration exceeds standard work hours, calculate excess hours
            // and minutes
            if (actualWorkDuration.compareTo(standardWorkDuration) > 0) {
                Duration excessDuration = actualWorkDuration.minus(standardWorkDuration);
                excessHours = excessDuration.toHours();
                excessMinutes = excessDuration.toMinutesPart();
            }

            // Format hours and minutes
            String actualHoursMinutes = String.format("%02d:%02d", actualWorkDuration.toHours(),
                    actualWorkDuration.toMinutesPart());
            String shortfallHoursMinutes = String.format("%02d:%02d", shortfallHours, shortfallMinutes);
            String excessHoursMinutes = String.format("%02d:%02d", excessHours, excessMinutes);

            // System.out.println("shortfallHoursMinutes " + shortfallHoursMinutes);
            // if(shortfallHoursMinutes.compareTo("00:00")>0){
            // System.out.println(attendance.getUsername());
            // }
            // Set calculated hours and excess hours in attendance object
            attendance.setTotalWorkHours("08:30 Hrs");
            attendance.setAcutalWorkHours(actualHoursMinutes);
            attendance.setExcessHours(excessHoursMinutes);
            attendance.setShortfallHours(shortfallHoursMinutes);

        } catch (DateTimeParseException e) {
            // Handle parsing error
            logger.error("Error parsing time for attendance: {}", e.getMessage(), e);
            // Optionally set default values or handle the error accordingly
        }

        return List.of(attendance);
    }

    private ResponseWrapper citplRegularization(String employeeID, String date) {
        try {
            String url = citplUrl + "/get_onsite_attendance_table";

            // Build the URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

            String trimmedEmployeeID = employeeID != null ? employeeID.trim() : "";
            if (!trimmedEmployeeID.isEmpty()) {
                builder.queryParam("employee", trimmedEmployeeID);
            } else {
                // If employeeID is empty after trimming, set employee parameter to %20
                builder.queryParam("employee", "%20");
            }

            // Add daterange parameter
            builder.queryParam("daterange", date);

            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Make the API call and get the ResponseWrapper
            RetryBackoffSpec retrySpec = Retry.backoff(3, Duration.ofSeconds(1))
                    .maxBackoff(Duration.ofSeconds(5))
                    .filter(throwable -> throwable instanceof WebClientResponseException || throwable instanceof IOException)
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure());

            // Make the API call and get the ResponseWrapper
            Mono<ResponseWrapper> responseWrapperMono = webClient.get()
                    .uri(builder.toUriString())
                    .headers(httpHeaders -> httpHeaders.addAll(headers))
                    .retrieve()
                    .bodyToMono(ResponseWrapper.class)
                    .retryWhen(retrySpec)
                    .timeout(Duration.ofSeconds(10))
                    .doOnError(e -> logger.error("Error during API call: {}", e.getMessage(), e))
                    .onErrorResume(e -> {
                        logger.error("Retries exhausted or timeout: {}", e.getMessage(), e);
                        return Mono.empty();
                    });

            // Block and get the response
            ResponseWrapper responseWrapper = responseWrapperMono.block();

            // Process the result to calculate and add additional fields
            if (responseWrapper != null && responseWrapper.getResult() != null) {
                List<EmployeeAttendance> employeeAttendances = responseWrapper.getResult();
                // Assuming there are additional calculations or modifications
                responseWrapper.setResult(employeeAttendances);
            } else {
                // Handle the case where responseWrapper or result is null
                responseWrapper = new ResponseWrapper();
                responseWrapper.setStatus(false);
                responseWrapper.setMessage("No data found");
                responseWrapper.setResult(null);
            }
            return responseWrapper;
        } catch (Exception e) {
            logger.error("Error in citplRegularization: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Error processing request", null);
        }
    }

    // public ResponseEntity<ResponseWrapper> cronApi() {
    // HttpHeaders headers = new HttpHeaders();
    // headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    // LocalDate currentDate = LocalDate.now();
    //
    // // Subtract one day from current date
    // LocalDate previousDate = currentDate.minusDays(1);
    // DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    //
    // String yesterday = previousDate.format(formatterr);
    // String passingDate = yesterday + "-" + yesterday;
    // String paramName = " ";
    // // Define the desired date format
    // String url = baseUrl + "/get_onsite_attendance_table";
    //
    // // Build the URL with query parameters
    // UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
    //
    // if (paramName != null) {
    // String trimmedParamName = paramName.trim();
    // if (!trimmedParamName.isEmpty()) {
    // builder.queryParam("employee", trimmedParamName);
    // } else {
    // // If paramName is empty after trimming, set employee parameter to %20
    // try {
    // builder.queryParam("employee", URLEncoder.encode(" ",
    // StandardCharsets.UTF_8.toString()));
    // } catch (UnsupportedEncodingException e) {
    // e.printStackTrace();
    // // Handle encoding exception as per your application's error handling
    // strategy
    // }
    // }
    // }
    //
    // // Add daterange parameter
    // builder.queryParam("daterange", passingDate);
    //
    // // Create HttpEntity with headers
    // HttpEntity<?> entity = new HttpEntity<>(headers);
    //
    // // Make the API call and get the ResponseEntity<ResponseWrapper>
    // ResponseEntity<ResponseWrapper> response =
    // restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity,
    // ResponseWrapper.class);
    //
    // // Get the ResponseWrapper object
    // ResponseWrapper responseWrapper = response.getBody();
    // // Process the result to calculate and add additional fields
    // List<EmployeeAttendance> employeeAttendances = responseWrapper.getResult();
    //
    // for (EmployeeAttendance attendance : employeeAttendances) {
    // String signIn = attendance.getSignIn();
    // String signOut = attendance.getSignOut();
    //
    // try {
    // // Parse signIn and signOut times into LocalTime
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    //
    // LocalTime signInTime = LocalTime.parse(signIn, formatter);
    //
    // LocalTime signOutTime = LocalTime.parse(signOut, formatter);
    //
    // // Define standard work hours
    // LocalTime standardWorkHours = LocalTime.parse("09:00:00", formatter);
    //
    // // Calculate actual worked duration
    // Duration actualWorkDuration = Duration.between(signInTime, signOutTime);
    //
    // // Calculate shortfall duration relative to 9 hours
    // Duration shortfallDuration = Duration.ofHours(9).minus(actualWorkDuration);
    //
    // // Handle shortfall hours and minutes
    // long shortfallHours = Math.max(0, shortfallDuration.toHours()); // Ensure
    // shortfallHours is non-negative
    //
    // long shortfallMinutes = shortfallDuration.toMinutesPart();
    //
    // // If actualWorkDuration exceeds 9 hours, set shortfall to 0:00
    // if (actualWorkDuration.compareTo(Duration.ofHours(9)) > 0) {
    // shortfallHours = 0;
    // shortfallMinutes = 0;
    // }
    //
    // // Calculate excess hours and minutes
    // long excessHours = 0;
    // long excessMinutes = 0;
    //
    // // If actual work duration exceeds 9 hours, calculate excess hours and
    // minutes
    // if (actualWorkDuration.compareTo(Duration.ofHours(9)) > 0) {
    // Duration excessDuration = actualWorkDuration.minus(Duration.ofHours(9));
    // excessHours = excessDuration.toHours();
    // excessMinutes = excessDuration.toMinutesPart();
    // }
    //
    // // Format hours and minutes
    // String actualHoursMinutes = String.format("%02d:%02d",
    // actualWorkDuration.toHours(), actualWorkDuration.toMinutesPart());
    // String shortfallHoursMinutes = String.format("%02d:%02d", shortfallHours,
    // shortfallMinutes);
    // String excessHoursMinutes = String.format("%02d:%02d", excessHours,
    // excessMinutes);
    //
    // // Set calculated hours and excess hours in attendance object
    // attendance.setTotalWorkHours("09:00 Hrs");
    // attendance.setAcutalWorkHours(actualHoursMinutes);
    // attendance.setExcessHours(excessHoursMinutes);
    // attendance.setShortfallHours(shortfallHoursMinutes);
    //
    // convertFromEmployeeAttendance(attendance);
    ////
    //
    // } catch (DateTimeParseException e) {
    // // Handle parsing error
    // System.err.println("Error parsing time for attendance: " + e.getMessage());
    // // Optionally set default values or handle the error accordingly
    // }
    // }
    //
    // // Update the result in responseWrapper
    // responseWrapper.setResult(employeeAttendances);
    // System.out.println(responseWrapper.getResult());
    // // Return the updated responseWrapper
    // return ResponseEntity.ok(responseWrapper);
    // }

}
