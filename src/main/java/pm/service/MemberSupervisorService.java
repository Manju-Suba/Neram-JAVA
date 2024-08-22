package pm.service;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

public interface MemberSupervisorService {

	ResponseEntity<?> findAllSupervisorIds(String company);

	ResponseEntity<?> getMembersUnderSupervisor(int supervisorId);

	ResponseEntity<?> getMembersUnderSupervisorBasedCompany(int supervisorId);

	ResponseEntity<?> getMembersUnderFinalApprove(Integer supervisorId);

	ResponseEntity<?> getReport(Integer id, LocalDate date, String status);

	ResponseEntity<?> getMembersUnderSupervisor(Integer supervisorId, LocalDate date, String status, int pageNumber,
			int pageSize);

	// ResponseEntity<?> getMembersUnderSupervisorDetail(Integer supervisorId,
	// LocalDate fromDate, LocalDate toDate,
	// String status,
	// int pageNumber,
	// int pageSize);

	ResponseEntity<?> getMembersUnderSupervisorbyall(LocalDate date, String status, int pageNumber, int pageSize);

	ResponseEntity<?> getMembersUnderSupervisorbyallDetail(LocalDate fromDate, LocalDate toDate, String status,
			int pageNumber, int pageSize,
			Integer supervisorId, Integer memberId, String company, String roletype);

	ResponseEntity<?> getMembersUnderSupervisorbyalldownlode(LocalDate date, String status);

	ResponseEntity<?> getMembersalldownlode(LocalDate date, LocalDate todate, String status);

	ResponseEntity<?> getMembersUnderSupervisordownload(Integer supervisorId, LocalDate date, String status);

}
