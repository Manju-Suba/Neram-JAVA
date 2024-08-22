package pm.controller.mobileApp;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import pm.mobileApp.service.RaisedService;
import pm.response.ApiResponse;

@RequestMapping("/raised")
@CrossOrigin("*")
@RestController
public class RaisedController {

    private final RaisedService raisedService;

    public RaisedController(RaisedService raisedService) {
        this.raisedService = raisedService;
    }

    @GetMapping("/request-list")
    @Operation(summary = "Requested list for supervisor", description = "Requested list for supervisor")
    public ResponseEntity<ApiResponse> getRequestedListForSupervisor(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int memberId, @RequestParam String status) {
        try {
            return raisedService.getRequestedListForSupervisor(page, size, date, memberId, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Data not found", e.getMessage()));
        }
    }

    @GetMapping("/raised-request")
    @Operation(summary = "Requested list for supervisor", description = "Requested list for supervisor")
    public ResponseEntity<ApiResponse> getRaisedRequestedListForSupervisor(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) LocalDate date,
            @RequestParam(defaultValue = "0") int memberId,
            @Parameter(description = "Status", schema = @Schema(type = "string", allowableValues = { "both", "Approved",
                    "Rejected" })) @RequestParam String status) {
        try {
            return raisedService.getRaisedRequestedListForSupervisor(page, size, date, memberId, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Data not found", e.getMessage()));
        }
    }
}
