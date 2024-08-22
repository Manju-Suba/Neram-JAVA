package pm.mobileApp.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import pm.mobileApp.dto.InputDto;
import pm.mobileApp.dto.MyteamFilterDto;
import pm.response.ApiResponse;

public interface TimesheetService {

        ResponseEntity<ApiResponse> getDraftProductListWithFilters(int page, int size, LocalDate date, String filter,
                        String status);

        ResponseEntity<ApiResponse> getDraftTaskListWithFilters(int page, int size, LocalDate date, int prodId,
                        String status);

        ResponseEntity<?> getMembersActivityListWithFilters(int page, int size, LocalDate fromdate,LocalDate todate, int userId,String status,String filter,int prodId);

        ResponseEntity<?> getContractMembersActivityListWithFilters(int page, int size, LocalDate date, int userId,
                        String status,boolean filter,int prodId);

        ResponseEntity<?> getProductsMembersActivityListWithFilters(int page, int size, LocalDate date, int userId, String status,String filter,int prodId);

        ResponseEntity<?> getMyReportDetails(int id, LocalDate date);

        ResponseEntity<?> getMyTeamReportList(InputDto inputDto);

        ResponseEntity<?> getMyteamSingleMemberReport(MyteamFilterDto inputDto);
}
