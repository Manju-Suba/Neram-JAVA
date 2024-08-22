package pm.controller.common;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import pm.service.MemberSupervisorService;

@RequestMapping("/user")
@CrossOrigin("*")
@RestController
public class MembersupervisorController {

	@Autowired
	private MemberSupervisorService memberSupervisorService;

	@Operation(summary = "Get the list of Supervisor", hidden = false)
	@GetMapping("/supervisorlist")
	public ResponseEntity<?> supervisor(@RequestParam(required = false) String company) {
		return memberSupervisorService.findAllSupervisorIds(company);
		// return supervisor.findSupervisor();
	}

	@Operation(summary = "Get the list of Team Members Based on  Supervisor ID", hidden = false)
	@GetMapping("supervisor/{supervisorId}")
	public ResponseEntity<?> getMembersUnderSupervisor(@PathVariable int supervisorId) {
		return memberSupervisorService.getMembersUnderSupervisor(supervisorId);
	}

	@Operation(summary = "Get the list of Team Members Based on  Supervisor ID", hidden = false)
	@GetMapping("supervisor-cmpy/{supervisorId}")
	public ResponseEntity<?> getMembersUnderSupervisorBasedCompany(@PathVariable int supervisorId) {
		return memberSupervisorService.getMembersUnderSupervisorBasedCompany(supervisorId);
	}

	@Operation(summary = "Get the list of Supervisor", hidden = false)
	@GetMapping("finalApprove/{finalApproveId}")
	public ResponseEntity<?> getMembersUnderFinalApprove(@PathVariable Integer finalApproveId) {
		return memberSupervisorService.getMembersUnderFinalApprove(finalApproveId);
	}

	@Operation(summary = "Get All User  Based On Date", hidden = true)
	@GetMapping("/member/{id}/{date}/{status}")
	public ResponseEntity<?> Activity(@PathVariable Integer id, @PathVariable LocalDate date, String status) {
		return memberSupervisorService.getReport(id, date, status);
	}

	@Operation(summary = "Get the list of Team Members Based on  Supervisor ID and Date")
	@GetMapping("supervisorid/{supervisorId}/{date}/{status}/{pageNumber}/{pageSize}")
	public ResponseEntity<?> getMembersUnderSupervisor(@PathVariable Integer supervisorId, @PathVariable LocalDate date,
			@PathVariable String status, @PathVariable int pageNumber, @PathVariable int pageSize) {
		return memberSupervisorService.getMembersUnderSupervisor(supervisorId, date, status, pageNumber, pageSize);
	}

	@Operation(summary = "Get All User  Based On Date")
	@GetMapping("supervisorid/all/{date}/{status}/{pageNumber}/{pageSize}")
	public ResponseEntity<?> getMembersUnderSupervisoralldata(@PathVariable LocalDate date, @PathVariable String status,
			@PathVariable int pageNumber, @PathVariable int pageSize) {
		return memberSupervisorService.getMembersUnderSupervisorbyall(date, status, pageNumber, pageSize);
	}

	@Operation(summary = "Get All User  Based On Date Range")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful operation"),
			@ApiResponse(responseCode = "400", description = "Bad request"),
			@ApiResponse(responseCode = "404", description = "Not found"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@GetMapping("supervisorid/all/{fromDate}/{toDate}/{status}/{pageNumber}/{pageSize}")
	public ResponseEntity<?> getMembersUndersupervisorallDataDetail(@PathVariable LocalDate fromDate,
			@PathVariable LocalDate toDate,
			@PathVariable String status,
			@PathVariable int pageNumber,
			@PathVariable int pageSize,
			@RequestParam(required = false) Integer supervisorId, @RequestParam(required = false) Integer memberId,
			@RequestParam(required = false) String company,
			@RequestParam(required = false) String roletype) {
		return memberSupervisorService.getMembersUnderSupervisorbyallDetail(fromDate, toDate, status, pageNumber,
				pageSize, supervisorId, memberId, company, roletype);
	}
	//
	// public enum StatusEnum {
	// All,
	// Entered,
	// NotEntered
	// }
}
