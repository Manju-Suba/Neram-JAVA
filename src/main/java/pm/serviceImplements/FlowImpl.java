package pm.serviceImplements;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.FlowCreateDto;
import pm.dto.FlowDTO;
import pm.dto.UserDTO;
import pm.model.flow.EStatus;
import pm.model.flow.Flow;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.FlowsRepository;
import pm.repository.UsersRepository;
import pm.request.FlowRequest;
import pm.response.ApiResponse;
import pm.service.EmailService;
import pm.service.FlowService;
import pm.utils.AuthUserData;
import pm.utils.CommonFunct;

@Service
public class FlowImpl implements FlowService {

	private final FlowsRepository flowsRepository;
	private final UsersRepository usersRepository;
	private final EmailService emailservice;
	private final CommonFunct commonFunct;

	public FlowImpl(FlowsRepository flowsRepository,
			UsersRepository usersRepository, EmailService emailservice, CommonFunct commonFunct) {
		this.flowsRepository = flowsRepository;
		this.usersRepository = usersRepository;
		this.emailservice = emailservice;
		this.commonFunct = commonFunct;
	}

	@Value("${myapp.customProperty}")
	private String portalUrl;

	private final ExecutorService executorService = Executors.newCachedThreadPool();

	public ResponseEntity<?> createFlow(String name, List<FlowCreateDto> accessBy,
			List<FlowCreateDto> selectedApprovals) {
		try {
			List<Integer> accessData = extractUserIds(accessBy);
			List<Integer> approvalData = extractUserIds(selectedApprovals);

			Set<Integer> accesspersons;
			Set<Integer> approvalpersons;
//			accesspersons.addAll(validateUserIds(accessData, "Head"));
			accesspersons = new HashSet<>(validateUserIds(accessData, "Head"));
			approvalpersons = new HashSet<>(validateUserIds(approvalData,"Approver"));
//			approvalpersons.addAll(validateUserIds(approvalData, "Approver"));

			if (!accesspersons.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Invalid Access Persons user IDs: " + accesspersons, null));
			}

			if (!approvalpersons.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Invalid approval Persons user IDs: " + approvalpersons, null));
			}

			if (!name.matches("^[a-zA-Z0-9\\s-]*$")) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Flow Name must contain only letters", null));
			}

			if (accessData.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Access person list cannot be empty", Collections.emptyList()));
			}

			if (approvalData.isEmpty()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Approval person list cannot be empty", Collections.emptyList()));
			}

			if (accessData.size() != new HashSet<>(accessData).size()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Duplicate IDs found in access_to list", null));
			}

			if (approvalData.size() != new HashSet<>(approvalData).size()) {
				return ResponseEntity.badRequest()
						.body(new ApiResponse(false, "Duplicate IDs found in approval_by list", null));
			}
			Flow flows = new Flow();
			Integer result = flowsRepository.existsByNameAndIsDeletedFalse(name);
			boolean exists = result != null && result == 1;
			if (exists) {
				return ResponseEntity.status(HttpStatus.CONFLICT)
						.body(new ApiResponse(false, "Flow Name Already Exists", null));
			}

			flows.setName(name);
			flows.setAccess_to(accessData);
			flows.setApproval_by(approvalData);
			flows.setCreated_at(LocalDateTime.now());
			flows.setStatus(EStatus.Pending);
			flows.setCreated_by(AuthUserData.getUserId());
			flows = flowsRepository.save(flows);

			Flow finalFlows = flows;
			executorService.execute(() -> sendAccessNotifications(accessData,
					finalFlows.getName()));

			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Flow created successfully", flows));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to create Flow", e.getMessage()));
		}
	}

	private Set<Integer> validateUserIds(List<Integer> userIds, String designation) {
		Set<Integer> invalidUserIds = new HashSet<>();

		List<Users> userList = usersRepository.findByActiveEmployeeByIdAndDesignation(userIds, designation);

		for (Integer userId : userIds) {
			boolean found = false;
			for (Users user : userList) {
				if (user.getId() == userId) {
					found = true;
					break;
				}
			}
			if (!found) {
				invalidUserIds.add(userId);
			}
		}

		return invalidUserIds;
	}

	private void sendAccessNotifications(List<Integer> accessData, String flowName) {
		for (Integer userId : accessData) {
			Optional<Users> users = usersRepository.findById(userId);
			String body = "<html><body>" + "<p> <b>" + users.get().getName() + "</b>,</p>"
					+ "<p>You have been granted access to the New Flow <b>" + flowName + "</b>,</p>"
					+ " We kindly request you to review the details by clicking on the \"View Details\" link provided below: <p style='text-align: center;'><a href='"
					+ portalUrl +
					"#/product/list' style='color: #007bff; text-decoration: none;font-weight: bold'>"
					+ "View Details"
					+ "</a></p></body></html>";

			emailservice.sendEmail(users.get().getEmail(), "Access Notification", body);
		}
	}

	// ==========================================================================================get
	// all Active Flow
	// List===============================================================
	public ResponseEntity<?> getFlowList() {
		List<Flow> flowsList = flowsRepository.getAllActiveFlow();
		flowsList = flowsList.stream().sorted(Comparator.comparingInt(Flow::getId).reversed())
				.collect(Collectors.toList());
		if (flowsList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "No data found", Collections.emptyList()));
		}
		List<FlowDTO> flowData = flowsList.stream().map(flow -> {
			FlowDTO flowDTO = new FlowDTO();
			flowDTO.setId(flow.getId());
			flowDTO.setName(flow.getName());
			List<UserDTO> accessToUsers = flow.getAccess_to().stream()
					.map(accessId -> usersRepository.findById(accessId)).filter(Optional::isPresent).map(Optional::get)
					.map(user -> {
						String roleNames = user.getRole_id().stream().map(Roles::getName)
								.collect(Collectors.joining(", ")); // Join role names into a single string
						return new UserDTO(user.getId(), user.getName(), roleNames, user.getProfile_pic(),
								user.getBranch());
					}).collect(Collectors.toList());

			accessToUsers = commonFunct.commonFunction1(accessToUsers);
			List<UserDTO> approvalByUsers = flow.getApproval_by().stream()
					.map(accessId -> usersRepository.findById(accessId)).filter(Optional::isPresent).map(Optional::get)
					.map(user -> {
						String roleNames = user.getRole_id().stream().map(Roles::getName)
								.collect(Collectors.joining(", ")); // Join role names into a single string
						return new UserDTO(user.getId(), user.getName(), roleNames, user.getProfile_pic(),
								user.getBranch());
					}).collect(Collectors.toList());
			approvalByUsers = commonFunct.commonFunction1(approvalByUsers);
			flowDTO.setAccess(accessToUsers);
			flowDTO.setApprovals(approvalByUsers);
			return flowDTO;
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "Flow List Fetched successfully", flowData));
	}

	@Override
	public ResponseEntity<?> getFlowListByProducthead(String status) {
		List<Flow> flowsList = flowsRepository.getAllActiveFlow();
		List<Flow> flowIdList = new ArrayList<>();

		if (status!=null && status.equalsIgnoreCase("admin")) {
			flowIdList.addAll(flowsList);
		}else {
			for (Flow accessId : flowsList) {
				if (accessId.getAccess_to().contains(AuthUserData.getUserId())) {
					Optional<Flow> flow = flowsRepository.findById(accessId.getId());
					if (flow.isPresent()) {
						flowIdList.add(flow.get());
					}
				}
			}
		}

		if (flowIdList.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "No data found", Collections.emptyList()));
		}

		flowIdList = flowIdList.stream().sorted(Comparator.comparingInt(Flow::getId).reversed())
				.collect(Collectors.toList());
		List<FlowDTO> flowData = flowIdList.stream().map(flow -> {
			FlowDTO flowDTO = new FlowDTO();
			flowDTO.setId(flow.getId());
			flowDTO.setName(flow.getName());
			List<UserDTO> accessToUsers = flow.getAccess_to().stream()
					.map(accessId -> usersRepository.findById(accessId)).filter(Optional::isPresent).map(Optional::get)
					.map(user -> {
						String roleNames = user.getRole_id().stream().map(role -> role.getName())
								.collect(Collectors.joining(", "));
						return new UserDTO(user.getId(), user.getName(), roleNames, user.getProfile_pic(),
								user.getBranch());
					}).collect(Collectors.toList());
			accessToUsers = commonFunct.commonFunction1(accessToUsers);
			List<UserDTO> approvalByUsers = flow.getApproval_by().stream()
					.map(approvalId -> usersRepository.findById(approvalId)).filter(Optional::isPresent)
					.map(Optional::get).map(user -> {
						String roleNames = user.getRole_id().stream().map(role -> role.getName())
								.collect(Collectors.joining(", "));
						return new UserDTO(user.getId(), user.getName(), roleNames, user.getProfile_pic(),
								user.getBranch());
					}).collect(Collectors.toList());
			approvalByUsers = commonFunct.commonFunction1(approvalByUsers);
			flowDTO.setAccess(accessToUsers);
			flowDTO.setApprovals(approvalByUsers);
			return flowDTO;
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "Flow list fetched successfully", flowData));
	}
	// flow with infinite scroll function

	// @Override
	// public ResponseEntity<?> getFlowListByProducthead(int page, int size, String
	// search, boolean filter) {
	// Pageable pageable = PageRequest.of(page, size);
	//
	// Page<Flow> flowList = null;
	//
	// if (filter) {
	// flowList = flowsRepository.findByTechOwnerSearch(AuthUserData.getUserId(),
	// search, pageable);
	// } else {
	// flowList = flowsRepository.getByTechOwnerSearch(AuthUserData.getUserId(),
	// pageable);
	// }
	//
	// if (flowList.isEmpty()) {
	// return ResponseEntity.status(HttpStatus.OK)
	// .body(new ApiResponse(false, "No records", Collections.emptyList()));
	// }
	//
	// List<FlowDTO> flowData = flowList.stream().map(flow -> {
	// FlowDTO flowDTO = new FlowDTO();
	// flowDTO.setId(flow.getId());
	// flowDTO.setName(flow.getName());
	// List<UserDTO> accessToUsers = convertStringToList(flow.getAccess()).stream()
	// .map(accessId ->
	// usersRepository.findById(accessId)).filter(Optional::isPresent).map(Optional::get)
	// .map(user -> {
	// String roleNames = user.getRole_id().stream().map(role -> role.getName())
	// .collect(Collectors.joining(", "));
	// return new UserDTO(user.getId(), user.getName(), roleNames,
	// user.getProfile_pic(),
	// user.getBranch());
	// }).collect(Collectors.toList());
	//
	// List<UserDTO> approvalByUsers = flow.getApproval_by().stream()
	// .map(approvalId ->
	// usersRepository.findById(approvalId)).filter(Optional::isPresent)
	// .map(Optional::get).map(user -> {
	// String roleNames = user.getRole_id().stream().map(role -> role.getName())
	// .collect(Collectors.joining(", "));
	// return new UserDTO(user.getId(), user.getName(), roleNames,
	// user.getProfile_pic(),
	// user.getBranch());
	// }).collect(Collectors.toList());
	//
	// flowDTO.setAccess(accessToUsers);
	// flowDTO.setApprovals(approvalByUsers);
	// return flowDTO;
	// }).collect(Collectors.toList());
	//
	// return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
	// "fetched successfully", flowData));
	// }

	public List<Integer> convertStringToList(String numbersString) {
		return Arrays.stream(numbersString.split(","))
				.map(Integer::parseInt)
				.collect(Collectors.toList());
	}

	@Override
	public ResponseEntity<?> viewFlow(int id) {
		Optional<Flow> flow = flowsRepository.getActiveFlowById(id);

		if (flow.isPresent()) {
			FlowDTO flowDTO = new FlowDTO(flow.get().getId(), flow.get().getName());
			flowDTO.setId(id);
			flowDTO.setName(flow.get().getName());
			List<UserDTO> accessToUsers = flow.get().getAccess_to().stream()
					.map(accessId -> usersRepository.findById(accessId))
					.filter(Optional::isPresent).map(user -> {
						String roleNames = user.get().getRole_id().stream().map(role -> role.getName())
								.collect(Collectors.joining(", "));
						return new UserDTO(user.get().getId(), user.get().getName(), roleNames,
								user.get().getProfile_pic(), user.get().getBranch());
					}).collect(Collectors.toList());
			accessToUsers = commonFunct.commonFunction1(accessToUsers);
			flowDTO.setAccess(accessToUsers);
			List<UserDTO> approvalToUsers = flow.get().getApproval_by().stream()
					.map(approvalId -> usersRepository.findById(approvalId)).filter(Optional::isPresent).map(user -> {
						String roleNames = user.get().getRole_id().stream().map(role -> role.getName())
								.collect(Collectors.joining(", "));
						return new UserDTO(user.get().getId(), user.get().getName(), roleNames,
								user.get().getProfile_pic(), user.get().getBranch());
					}).collect(Collectors.toList());
			approvalToUsers = commonFunct.commonFunction1(approvalToUsers);
			flowDTO.setApprovals(approvalToUsers);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Flow Fetched Successfully", flowDTO));
		} else {
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "Flow not found", null));
		}
	}

	@Override
	public ResponseEntity<?> updateFlow(int id, FlowRequest flowRequest) {
		Optional<Flow> optionalFlow = flowsRepository.getActiveFlowById(id);

		// Validate the name field using the regular expression
		String name = flowRequest.getName();
		if (!name.matches("^[a-zA-Z0-9\\s-]*$")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.body(new ApiResponse(false, "Name must contain only alphabetic letters", null));
		}

		if (!optionalFlow.isPresent()) {
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "Flow not found", null));
		}

		Flow flow = optionalFlow.get();
		if (flowsRepository.existsByNameAndNotId(flowRequest.getName(), id)) {

			String errorMessage = String.format("Flow with name '%s' already exists.", flowRequest.getName());
			return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(false, errorMessage, null));
		}

		flow.setName(flowRequest.getName());

		List<Integer> accessData = extractUserIds(flowRequest.getAccess_to());
		List<Integer> approvalData = extractUserIds(flowRequest.getApproval_by());
		Set<Integer> accesspersons = new HashSet<>();
		Set<Integer> approvalpersons = new HashSet<>();
//		accesspersons.addAll(validateUserIds(accessData, "Head"));

		accesspersons = new HashSet<>(validateUserIds(accessData, "Head"));
		approvalpersons = new HashSet<>(validateUserIds(approvalData,"Approver"));
//		approvalpersons.addAll(validateUserIds(approvalData, "Approver"));

		if (!accesspersons.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Invalid Access Persons user IDs: " + accesspersons, null));
		}

		if (!approvalpersons.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Invalid approval Persons user IDs: " + approvalpersons, null));
		}

		if (accessData.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Access person list cannot be empty", Collections.emptyList()));
		}

		if (approvalData.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Approval person list cannot be empty", Collections.emptyList()));
		}

		if (accessData.size() != new HashSet<>(accessData).size()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Duplicate IDs found in Access Person list", null));
		}

		if (approvalData.size() != new HashSet<>(approvalData).size()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse(false, "Duplicate IDs found in approval Person list", null));
		}
		flow.setAccess_to(accessData);
		flow.setApproval_by(approvalData);
		flow.setCreated_at(LocalDateTime.now());
		flow.setStatus(EStatus.Pending);
		flow.setCreated_by(AuthUserData.getUserId());

		try {
			flow = flowsRepository.save(flow);
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Flow updated successfully", flow));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "Failed to update flow", e.getMessage()));
		}
	}

	private List<Integer> extractUserIds(List<FlowCreateDto> usersList) {
		return usersList.stream().map(FlowCreateDto::getId).collect(Collectors.toList());
	}

	@Override
	public ResponseEntity<ApiResponse> softDeleteFlow(List<Integer> id) {
		try {
			flowsRepository.softDeleteById(id);
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Succesfully Deleted the Flow", Collections.emptyList()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Not Deleted ", e.getMessage()));
		}
	}
}