package pm.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.request.RaisedRequest;

@Service
public interface ActivityRequestService {
    ResponseEntity<?> sendRequest(RaisedRequest raisedRequest);
    ResponseEntity<?> raisedRequestForAll();
    

    // ResponseEntity<?> getRaisedRequestByUser();

    ResponseEntity<?> getRaisedRequestByUser(int page, int size, boolean filter, LocalDate date, String status);

    ResponseEntity<?> getRaisedRequestToSupervisor(LocalDate date);

    ResponseEntity<?> getRaisedRequestToSupervisor(int page, int size, boolean filter, LocalDate date, String status,
            int userid);

    ResponseEntity<?> getRaisedRequestStatus(LocalDate date);

    ResponseEntity<?> approveRequest(RaisedRequest raisedRequest, int id);

    // Attendance management

    ResponseEntity<?> createAttendanceSheetByDate(LocalDate date, String status);

    ResponseEntity<?> getRaisedRequestToSupervisor();

    ResponseEntity<?> getRaisedRequestToSupervisorByStatus();

    ResponseEntity<?> getRaisedRequestToSupervisorByStatus(int page, int size, boolean filter, LocalDate date,
            String status, int memberId);

    ResponseEntity<?> approveRequests(RaisedRequest raisedRequest, List<Integer> ids);

    ResponseEntity<?> getTimeSheetByStatus(int month, int year);

    ResponseEntity<?> getAttendanceSheetByUser(LocalDate date, int page, int size);

    ResponseEntity<?> getAttendanceSheetBySupervisor(LocalDate date, int page, int size, Integer userid);

    ResponseEntity<?> deleteAttendance(Integer id);

    ResponseEntity<?> getDataBasedOnID(int id);

    ResponseEntity<?> updateRaisedRequest(int id, LocalDate date, String reason);

    // Self Activity
    ResponseEntity<?> getSelfActivity(String month,String year,int id);

    ResponseEntity<?> getRaisedRequestByUserDate(LocalDate date, int id);

    ResponseEntity<?> getAttendanceRequestByUserDate(LocalDate date, int id);

}
