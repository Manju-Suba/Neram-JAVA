package pm.controller.flow;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import pm.request.FlowRequest;
import pm.service.FlowService;

@RequestMapping("/flow")
@CrossOrigin("*")
@RestController
public class FlowController {

	private final FlowService flowService;

	public FlowController(FlowService flowService) {
		this.flowService = flowService;
	}

	@Operation(summary = "Create a flow")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Flow created successfully"),
			@ApiResponse(responseCode = "401", description = "Access denied for this user"),
			@ApiResponse(responseCode = "500", description = "Internal server error")
	})
	@PreAuthorize("hasAnyAuthority('Admin')")
	@PostMapping("/create")
	public ResponseEntity<?> createFlow(@RequestBody FlowRequest createFlowRequest) {
		try {
			return flowService.createFlow(createFlowRequest.getName(), createFlowRequest.getAccess_to(),
					createFlowRequest.getApproval_by());
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body("You don't have the required authority to perform this action.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("An internal server error occurred.");
		}
	}

	@Operation(summary = "Get list of flows")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successful retrieval of flow list"),
			@ApiResponse(responseCode = "404", description = "Flow list not found")
	})
	@PreAuthorize("hasAnyAuthority('Admin')")
	@GetMapping("/list")
	public ResponseEntity<?> getFlowList() {
		return flowService.getFlowList();
	}

	@Operation(summary = "Get flow list by product head Access")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved flow list"),
			@ApiResponse(responseCode = "404", description = "Flow list not found")
	})
	@GetMapping("/list/producthead")
	public ResponseEntity<?> getFlowListProductHead(@RequestParam(required = false) String status) {
		return flowService.getFlowListByProducthead(status);
	}

	@Operation(summary = "View a flow by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully retrieved the flow"),
			@ApiResponse(responseCode = "404", description = "Flow not found")
	})
	@PreAuthorize("hasAnyAuthority('Admin')")
	@GetMapping("view/{id}")
	public ResponseEntity<?> viewFlow(@PathVariable int id) {
		return flowService.viewFlow(id);
	}

	@Operation(summary = "Update a flow by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully updated the flow"),
			@ApiResponse(responseCode = "404", description = "Flow not found"),
			@ApiResponse(responseCode = "400", description = "Invalid request payload")
	})
	@PreAuthorize("hasAnyAuthority('Admin')")
	@PutMapping("update/{id}")
	public ResponseEntity<?> updateFlow(@PathVariable int id, @RequestBody FlowRequest flowRequest) {
		return flowService.updateFlow(id, flowRequest);
	}

	@Operation(summary = "Delete a flow by ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully Deleted the flow"),
			@ApiResponse(responseCode = "404", description = "Flow not found"),
			@ApiResponse(responseCode = "400", description = "Invalid request payload")
	})
	@PreAuthorize("hasAnyAuthority('Admin')")
	@DeleteMapping("delete/{id}")
	public ResponseEntity<?> deleteFlow(@PathVariable List<Integer> id) {
		return flowService.softDeleteFlow(id);
	}

}
