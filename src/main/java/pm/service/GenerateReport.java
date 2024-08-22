package pm.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface GenerateReport {
    ResponseEntity<?> generateReport(LocalDate date);
    ResponseEntity<?> generateReportDateRange(LocalDate fromdate, LocalDate todate, int userId, int page, int size, String roleType);
    ResponseEntity<?> getContractPerson(LocalDate fromdate,LocalDate todate,int page, int size);
    List<Map<String, Object>> pdfExcelReport(int id,LocalDate date);
    ResponseEntity<?> getUsersDataList(int id, LocalDate date);
    ResponseEntity<?> getUsersDataListdaterange(int id, LocalDate fromdate,LocalDate todate);
    ResponseEntity<?> getContractPersonList(String roletype);
    ResponseEntity<?> generateReportDateRange( LocalDate fromdate, LocalDate todate,List<Integer>users_id);
    // byte[] generateExcelFile(List<Map<String, Object>> reports);
}
