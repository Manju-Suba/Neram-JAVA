package pm.mobileApp.serviceImpl;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.mobileApp.service.RaisedService;
import pm.mobileAppDto.MembersActivityResponse;
import pm.mobileAppDto.RaisedRequestResponse;
import pm.repository.ActivityRequestRepository;
import pm.response.ApiResponse;
import pm.utils.AuthUserData;

@Service
public class RaisedServiceImpl implements RaisedService {

    private final ActivityRequestRepository activityRequestRepository;

    public RaisedServiceImpl(ActivityRequestRepository activityRequestRepository) {
        this.activityRequestRepository = activityRequestRepository;
    }

    @Override
    public ResponseEntity<ApiResponse> getRequestedListForSupervisor(int page, int size, LocalDate date, int memberId,
            String status) {
        try {
            int userId = AuthUserData.getUserId();
            Pageable pageable = createPageRequest(page, size);
            Page<Object[]> requestList = fetchRequestList(userId, date, memberId, List.of(status), pageable);

            List<Map<String, Object>> groupedByDate = groupTasksByProduct(requestList);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Requested List fetched Successfully", groupedByDate));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<ApiResponse> getRaisedRequestedListForSupervisor(int page, int size, LocalDate date,
            int memberId, String status) {
        try {
            int userId = AuthUserData.getUserId();
            Pageable pageable = createPageRequest(page, size);
            List<String> statusList = getStatusList(status);
            Page<Object[]> requestList = fetchRequestList(userId, date, memberId, statusList, pageable);

            List<Map<String, Object>> groupedByDate = groupTasksByProduct(requestList);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Raised Request List fetched Successfully", groupedByDate));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(true, "Error", Collections.emptyList()));
        }
    }

    private Pageable createPageRequest(int page, int size) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "request_date");
        return PageRequest.of(page, size, sortByDescId);
    }

    private List<String> getStatusList(String status) {
        if ("both".equalsIgnoreCase(status)) {
            return List.of("Rejected", "Approved");
        } else {
            return List.of(status);
        }
    }

    private Page<Object[]> fetchRequestList(int userId, LocalDate date, int memberId, List<String> statusList,
            Pageable pageable) {
        if (date != null && memberId != 0) {
            return activityRequestRepository.findbySendedToAndRequestDateAndStatusAndUserIdAndPageableToSupervisor(
                    userId, date, statusList, memberId, pageable);
        } else if (date != null) {
            return activityRequestRepository.findbySendedToAndRequestDateAndStatusAndPageableToSupervisor(userId, date,
                    statusList, pageable);
        } else if (memberId != 0) {
            return activityRequestRepository.findbySendedToAndStatusAndUserIdAndPageableToSupervisor(userId, statusList,
                    memberId, pageable);
        } else {
            return activityRequestRepository.findbySendedToAndStatusAndPageableToSupervisor(userId, statusList,
                    pageable);
        }
    }

    private Map<String, List<Map<String, Object>>> groupRequestsByDate(Page<Object[]> requestList) {
        return requestList.getContent().stream()
                .map(req -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", req[0]);
                    map.put("userName", req[1]);
                    map.put("requestDate", req[2]);
                    map.put("reason", req[3]);
                    map.put("status", req[4]);
                    return map;
                })
                .collect(Collectors.groupingBy(req -> req.get("requestDate").toString()));
    }


    private List<Map<String, Object>> groupTasksByProduct(Page<Object[]> activity) {
        Map<String, List<RaisedRequestResponse>> groupedTasks = new LinkedHashMap<>();
        for (Object[] result : activity) {
            String activityDate = String.valueOf(convertToDateTime(result[2]).toLocalDate());
            RaisedRequestResponse taskActivity = mapToCommonTaskActivity(result);
            groupedTasks.computeIfAbsent(activityDate, k -> new ArrayList<>()).add(taskActivity);
        }

        List<Map<String, Object>> responseData = new ArrayList<>();
        for (Map.Entry<String, List<RaisedRequestResponse>> entry : groupedTasks.entrySet()) {
            Map<String, Object> projectData = new LinkedHashMap<>();
            projectData.put("date", entry.getKey());
            projectData.put("data", entry.getValue());
            responseData.add(projectData);
        }
        return responseData;
    }

    private RaisedRequestResponse mapToCommonTaskActivity(Object[] result) {
        return new RaisedRequestResponse(
                (Integer) result[0],
                result[1].toString(), // prodname// id
                convertToDateTime(result[2]).toLocalDate(), // activeDate
                result[3].toString(), // description
                result[4].toString() // hours

        );
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
}
