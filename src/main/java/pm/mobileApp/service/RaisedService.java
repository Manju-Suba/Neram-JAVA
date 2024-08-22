package pm.mobileApp.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import pm.response.ApiResponse;

public interface RaisedService {

    ResponseEntity<ApiResponse> getRequestedListForSupervisor(int page, int size, LocalDate date, int memberId,
            String status);

    ResponseEntity<ApiResponse> getRaisedRequestedListForSupervisor(int page, int size, LocalDate date, int memberId,
            String status);

}
