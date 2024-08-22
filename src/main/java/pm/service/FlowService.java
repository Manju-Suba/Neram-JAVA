package pm.service;

import java.util.List;
import org.springframework.http.ResponseEntity;

import pm.dto.FlowCreateDto;
import pm.request.FlowRequest;
import pm.response.ApiResponse;

public interface FlowService {
	ResponseEntity<?> createFlow(String createFlowRequest, List<FlowCreateDto> accessBy,
			List<FlowCreateDto> selectedApprovals);

	ResponseEntity<?> getFlowList();

	ResponseEntity<?> viewFlow(int id);

	ResponseEntity<?> updateFlow(int id, FlowRequest flowRequest);

	ResponseEntity<?> getFlowListByProducthead(String status);

	ResponseEntity<ApiResponse> softDeleteFlow(List<Integer> id);
}