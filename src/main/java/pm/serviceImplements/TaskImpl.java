package pm.serviceImplements;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.lang.Arrays;
import jakarta.mail.Multipart;
import pm.controller.product.ProductApprovalHistroyController;
import pm.dto.*;
import pm.model.flow.Flow;
import pm.model.member.Member;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;
import pm.model.product.ProductStatus;
import pm.model.product.SubProduct;
import pm.model.task.ActivityLog;
import pm.model.task.Task;
import pm.model.task.TaskActivity;
import pm.model.task.TaskCategory;
import pm.model.task.TaskHistory;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.MemberRepository;
import pm.repository.ProdApprovalHistoryRepository;
import pm.repository.ProductRepository;
import pm.repository.RolesRepository;
import pm.repository.TaskActivityRepository;
import pm.repository.TaskCategoryRepository;
import pm.repository.TaskHistoryRepository;
import pm.repository.TaskRepository;
import pm.repository.ActivityLogRepository;
import pm.repository.ActivityRequestRepository;
import pm.repository.UsersRepository;
import pm.request.ActivityRequest;
import pm.request.TaskActivityRequest;
import pm.response.ActivityDTO;
import pm.response.ApiResponse;
import pm.response.ProductTaskResponse;
import pm.service.TaskService;
import pm.utils.AuthUserData;

@Service
public class TaskImpl implements TaskService {

	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private TaskHistoryRepository taskHistoryRepository;

	@Value("${fileBasePath}")
	private String fileBasePath;

	@Value("${getPath}")
	private String getPath;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private ProdApprovalHistoryRepository approvalHistoryRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private RolesRepository rolesRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private TaskCategoryRepository taskCategoryRepository;

	@Autowired
	private TaskActivityRepository taskActivityRepository;

	@Autowired
	private ActivityLogRepository activityLogRepository;
	@Autowired
	private ActivityRequestRepository activityrequestrepo;

	@Override
	public ResponseEntity<?> create(Task task, MultipartFile file) {
		try {
			LocalDateTime currDateTime = LocalDateTime.now();
			int user_id = AuthUserData.getUserId();
			Optional<Users> createdByUser = usersRepository.findById(user_id);
			task.setCreated_at(currDateTime);
			task.setUpdated_at(currDateTime);
			task.set_deleted(false);
			task.setCreatedBy(user_id);
			task.setBranch(createdByUser.get().getBranch());
			if (file != null && !file.isEmpty()) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

				String formattedTime = currDateTime.format(formatter);
				String fileName = formattedTime + "_" + file.getOriginalFilename();
				Path path = Paths.get(fileBasePath + fileName);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				File fileexist = path.toFile();
				if (fileexist.exists()) {
					task.setFile(fileName);
				}
			}

			task.setStatus(ProductStatus.CREATED.toString());

			task = taskRepository.save(task);
			Optional<Task> taskData = taskRepository.findById(task.getId());
			TaskHistory taskHistory = new TaskHistory();
			taskHistory.setTask(taskData.get());
			taskHistory.setStatus(EProductApproStatus.Assigned.toString());
			taskHistory.set_deleted(false);
			taskHistory.setCreated_at(currDateTime);
			taskHistory.setCreatedBy(createdByUser.get());
			taskHistory.setMember(task.getAssignedTo());

			taskHistoryRepository.save(taskHistory);

			String message = "Task Created Successfully.";
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
		}

	}

	@Override
	public ResponseEntity<?> get() {
		List<Task> task = taskRepository.findAll();
		task.sort(Comparator.comparing(Task::getId).reversed());
		List<ProductTaskResponse> productTaskResponses = new ArrayList<>();
		for (Task task_data : task) {
			ProductTaskResponse productTaskResponse = new ProductTaskResponse();
			BeanUtils.copyProperties(task_data, productTaskResponse);

		}
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Success", task));
	}

	public ResponseEntity<?> view(int id) {
		List<TaskDTO> taskdetailsDTOs = new ArrayList<>();
		Optional<Task> taskData = taskRepository.findById(id);
		if (taskData.isEmpty()) {
			String errorMessage = "Task not found.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, errorMessage, null));
		}
		TaskDTO taskDTO = new TaskDTO();
		taskDTO.setId(taskData.get().getId());
		taskDTO.setTask_name(taskData.get().getTask_name());
		taskDTO.setTaskCategory(taskData.get().getTaskCategory());
		taskDTO.setStart_date(taskData.get().getStart_date());
		taskDTO.setEnd_date(taskData.get().getEnd_date());
		taskDTO.setPriority(taskData.get().getPriority());
		taskDTO.setDescription(taskData.get().getDescription());
		Optional<Users> createdBY = usersRepository.findById(taskData.get().getCreatedBy());
		taskDTO.setCreatedBy(createdBY.get().getName());
		List<Integer> assignedToJson = taskData.get().getAssignedTo();

		List<MemberDTO> memberDTOs = new ArrayList<>();
		for (Integer data : assignedToJson) {
			Optional<Member> member = memberRepository.findById(data);
			MemberDTO memberDTO = new MemberDTO();
			memberDTO.setId(member.get().getId());
			memberDTO.setName(member.get().getMember().getName());
			// Optional<Roles> roleNames = rolesRepository.findById(member.get().getRole());
			// memberDTO.setRole(roleNames.get().getName().toString());
			memberDTO.setRole(member.get().getRole());
			memberDTO.setProfile_pic(member.get().getMember().getProfile_pic());
			memberDTOs.add(memberDTO);
		}
		taskDTO.setAssignedTo(memberDTOs);
		taskDTO.setStatus(taskData.get().getStatus());
		taskDTO.setCreated_at(taskData.get().getCreated_at());
		List<Member> members = memberRepository.findByProdId(taskData.get().getProdId());
		List<MemberDTO> createdmember = new ArrayList<>();

		for (Member data : members) {
			MemberDTO memberDTO = new MemberDTO();
			memberDTO.setId(data.getId());
			memberDTO.setName(data.getMember().getName());
			// Optional<Roles> roleNames = rolesRepository.findById(data.getRole());
			// memberDTO.setRole(roleNames.get().getName().toString());
			memberDTO.setRole(data.getRole());
			memberDTO.setProfile_pic(data.getMember().getProfile_pic());
			createdmember.add(memberDTO);
		}
		Optional<Product> product = productRepository.findById(taskData.get().getProdId());
		// Users user =
		// usersRepository.findById(product.get().getProdOwner()).orElse(null);
		// Users users =
		// usersRepository.findById(product.get().getTechOwner()).orElse(null);
		taskDTO.setProdId(product.get().getId());
		taskDTO.setName(product.get().getName());
		taskDTO.setBudget(product.get().getBudget());
		taskDTO.setCategoryID(product.get().getCategory().getId());
		taskDTO.setCategoryName(product.get().getCategory().getName());
		taskDTO.setProd_headId(product.get().getProdHead().getId());
		taskDTO.setProd_name(product.get().getProdHead().getName());
		taskDTO.setTech_headId(product.get().getTechHead().getId());
		taskDTO.setTech_name(product.get().getTechHead().getName());
		taskDTO.setCurrency(product.get().getCurrency());
		taskDTO.setCreatedAt(product.get().getCreatedAt());
		taskDTO.setEndDate(product.get().getEndDate());
		taskDTO.setStartDate(product.get().getStartDate());
		taskDTO.setStatus(product.get().getStatus().toString());
		taskDTO.setSummary(product.get().getSummary());
		taskDTO.setFile(product.get().getFile());
		taskDTO.setFlow(product.get().getFlow().getId());
		taskDTO.setFlowName(product.get().getFlow().getName());
		taskDTO.setIsDeleted(product.get().getIsDeleted());
		taskDTO.setUpdatedAt(product.get().getUpdatedAt());
		taskDTO.setProdOwner(product.get().getProdOwner());
		taskDTO.setTechOwner(product.get().getTechOwner());
		taskDTO.setUpdatedAt(product.get().getUpdatedAt());
		taskDTO.setMember(createdmember);
		// if (users != null) {
		// taskDTO.setTechOwnerName(users.getName());
		// } else {
		// taskDTO.setTechOwnerName("Unknown Tech Owner");
		// }
		// if (user != null) {
		// taskDTO.setProdOwnerName(user.getName());
		// } else {
		// taskDTO.setProdOwnerName("Unknown Prod Owner");
		// }
		taskdetailsDTOs.add(taskDTO);

		String message = "Task Created Successfully.";
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, taskdetailsDTOs));
	}

	@Override
	public ResponseEntity<?> getCategory() {
		List<TaskCategory> taskCategory = taskCategoryRepository.findAll();
		taskCategory.sort(Comparator.comparing(TaskCategory::getId).reversed());
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Success", taskCategory));
	}

	@Override
	public ResponseEntity<?> memberList(int id) {
		List<Member> members = memberRepository.findByProdId(id);
		List<MemberDTO> createdmember = new ArrayList<>();
		for (Member member : members) {
			MemberDTO memberDTO = new MemberDTO();
			memberDTO.setId(member.getId());
			memberDTO.setName(member.getMember().getName());
			// Optional<Roles> roleNames = rolesRepository.findById(member.getRole());
			// memberDTO.setRole(roleNames.get().getName().toString());
			memberDTO.setRole(member.getRole());
			memberDTO.setBranch(member.getMember().getBranch());
			memberDTO.setProfile_pic(member.getMember().getProfile_pic());
			memberDTO.setUserId(member.getMember().getId());
			createdmember.add(memberDTO);
		}
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Success", createdmember));
	}

	@Override
	public ResponseEntity<?> updateActivity(ActivityRequest activityRequest) {
		int user_id = AuthUserData.getUserId();
		LocalDateTime currDateTime = LocalDateTime.now();
		List<String> errors = new ArrayList<>();

		for (Integer taskId : activityRequest.getId()) {
			// Save Activity Log
			ActivityLog activityLog = new ActivityLog();
			activityLog.setTaskActivity(taskId);
			activityLog.setStatus(activityRequest.getStatus());
			activityLog.setRemarks(activityRequest.getRemarks());
			activityLog.setCreatedBy(user_id);
			activityLog.setCreatedat(currDateTime);
			activityLog.setIsDeleted(false);
			activityLogRepository.save(activityLog);

			// Update Task Activity
			TaskActivity taskActivity = taskActivityRepository.findById(taskId).orElse(null);
			if (taskActivity != null) {
				taskActivity.set_approved(true);
				if ("Contract".equals(taskActivity.getUser().getRoleType())) {
					taskActivity.setFinalApprove("TL Approved");
				}
				// taskActivity.setStatus(activityRequest.getStatus()); // You may uncomment
				// this line if needed
				taskActivityRepository.save(taskActivity);
			} else {
				// Handle the case where the TaskActivity with the given ID is not found
				errors.add("TaskActivity with ID " + taskId + " not found.");
			}
		}

		if (!errors.isEmpty()) {
			// Return error response
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "Error", errors));
		} else {
			// Return success response
			return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Success", null));
		}
	}

	@Override
	public ResponseEntity<?> createMemberActivity(TaskActivityRequest taskActivity) {
		List<TaskActivity> createdActivities = new ArrayList<>();
		List<TaskActivity> taskActivitiesList = taskActivity.getTaskActivities();
		for (int i = 0; i < taskActivitiesList.size(); i++) {
			TaskActivity data = taskActivitiesList.get(i);
			LocalDateTime currDateTime = LocalDateTime.now();
			int user_id = AuthUserData.getUserId();
			Optional<Users> createdByUser = usersRepository.findById(user_id);
			data.setCreated_at(currDateTime);
			data.setFinalApprove("Not Yet");
			data.setUpdated_at(currDateTime);
			data.set_deleted(false);
			data.setUser(createdByUser.get());
			data.setDraft(false);
			data.setActivity_date(taskActivitiesList.get(i).getActivity_date());
			data.setDescription(taskActivitiesList.get(i).getDescription());
			data.setTask(taskActivitiesList.get(i).getTask());
			data.setHours(taskActivitiesList.get(i).getHours());
			data.setStatus(taskActivitiesList.get(i).getStatus());
			data.setProduct(taskActivitiesList.get(i).getProduct());
			data.set_approved(false);
			data.setBranch(createdByUser.get().getBranch());
			TaskActivity exActivity = taskActivityRepository.save(data);
			createdActivities.add(exActivity);
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "Activity Created Successfully", createdActivities));

	}

	@Override
	public ResponseEntity<?> saveasdraftActivity(TaskActivityRequest taskActivity) {
		List<TaskActivity> createdActivities = new ArrayList<>();
		List<TaskActivity> taskActivitiesList = taskActivity.getTaskActivities();

		for (int i = 0; i < taskActivitiesList.size(); i++) {
			TaskActivity data = taskActivitiesList.get(i);
			LocalDateTime currDateTime = LocalDateTime.now();
			int user_id = AuthUserData.getUserId();
			Optional<Users> createdByUser = usersRepository.findById(user_id);
			data.setCreated_at(currDateTime);
			data.setUpdated_at(currDateTime);
			data.set_deleted(false);
			data.setUser(createdByUser.get());
			data.setFinalApprove("Not Yet");
			data.setDraft(true);
			data.setActivity_date(taskActivitiesList.get(i).getActivity_date());
			data.setDescription(taskActivitiesList.get(i).getDescription());
			data.setTask(taskActivitiesList.get(i).getTask());
			data.setHours(taskActivitiesList.get(i).getHours());
			data.setStatus(taskActivitiesList.get(i).getStatus());
			data.setProduct(taskActivitiesList.get(i).getProduct());
			TaskActivity exActivity = taskActivityRepository.save(data);
			createdActivities.add(exActivity);
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "Activity Created Successfully", createdActivities));

	}

	@Override
	public ResponseEntity<?> getProductByUser() {
		Integer userId = AuthUserData.getUserId();
		List<ProductNamesDTO> productNamesDTOS = new ArrayList<>();
		Optional<Users> userOptional = usersRepository.findById(userId);
		if (userOptional.isPresent()) {
			Users user = userOptional.get();

			List<Member> members = memberRepository.findByMember(user);
			List<Product> products = new ArrayList<>();

			for (Member member : members) {
				Integer productId = member.getProdId();

				// Check if the product is assigned to any task
				boolean productAssignedToTask = taskRepository.existsByProdId(productId);

				if (productAssignedToTask) {
					Product product = productRepository.findById(productId).orElse(null);
					if (product != null) {
						ProductNamesDTO productNames = new ProductNamesDTO();
						productNames.setId(product.getId());
						productNames.setName(product.getName());

						// products.add(product);
						productNamesDTOS.add(productNames);
					}
				}
				productNamesDTOS.sort(Comparator.comparing(ProductNamesDTO::getId).reversed());

			}

			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Fetched Successfully", productNamesDTOS));
		}

		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(new ApiResponse(false, "User not found", Collections.emptyList()));
	}

	@Override
	public ResponseEntity<?> getTaskbyproduct(int id) {
		Integer userId = AuthUserData.getUserId();

		List<Task> tasks = taskRepository.findByProdId(id);
		tasks.sort(Comparator.comparing(Task::getId).reversed());
		List<ProductNamesDTO> filteredTasks = new ArrayList<>();

		for (Task task : tasks) {
			List<Integer> assignedTo = task.getAssignedTo();
			if (assignedTo != null && assignedTo.contains(userId)) {
				ProductNamesDTO productNames = new ProductNamesDTO();
				productNames.setId(task.getId());
				productNames.setName(task.getTask_name());
				filteredTasks.add(productNames);
			}
		}

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "Fetched Successfully", filteredTasks));

	}

	@Override
	public ResponseEntity<?> getWeeklytaskActivity(LocalDate fromdate, Integer type) {
		try {
			Optional<Users> user = usersRepository.findByIdWithRoles(AuthUserData.getUserId());
			List<Member> member_data = memberRepository.findByMemberAndRole(user.get().getId(), "Team Lead");
			if (member_data.isEmpty()) {
				return ResponseEntity.status(HttpStatusCode.valueOf(403)).body(
						new ApiResponse(false, "No Member Found in Member Data List List ", Collections.emptyList()));

			}

			// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			// LocalDate localDate = LocalDate.parse(fromdate, formatter);
			List<TaskActivityResponse> all_Task_activity = new ArrayList<>();
			for (Member member : member_data) {
				Product product_data = productRepository.findById(member.getProdId()).orElse(null);
				List<TaskActivityResponse> task_activity_data = null;
				if (type == 0) {
					task_activity_data = taskActivityRepository
							.findByProductAndActivityDateBetweenNative(member.getProdId(), fromdate,
									user.get().getBranch())
							.stream()
							.map(row -> new TaskActivityResponse((Integer) row[0],
									convertToDateTime(row[1]).toLocalDate(),
									(String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
									((java.sql.Timestamp) row[6]).toLocalDateTime(),
									((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
									(String) row[10], (String) row[11], (String) row[12], (String) row[13], null))
							.collect(Collectors.toList());
				} else if (type == 1) {
					task_activity_data = taskActivityRepository
							.findByProductAndActivityDateBetweenNativedraft(product_data.getId(), fromdate)
							.stream()
							.map(row -> new TaskActivityResponse((Integer) row[0],
									convertToDateTime(row[1]).toLocalDate(),
									(String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
									((java.sql.Timestamp) row[6]).toLocalDateTime(),
									((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
									(String) row[10], (String) row[11], (String) row[12], (String) row[13], null))
							.collect(Collectors.toList());
				}

				all_Task_activity.addAll(task_activity_data);
				all_Task_activity.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());

			}
			if (all_Task_activity.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT)
						.body(new ApiResponse(false, "No product Found in Product List ", Collections.emptyList()));
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Fetched Successfully", all_Task_activity));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "An error occurred while processing the request.", e.getMessage()));
		}
	}

	@Override
	public ResponseEntity<?> getWeeklytaskActivityall(String fromdate) {
		try {

			Optional<Users> user = usersRepository.findByIdWithRoles(AuthUserData.getUserId());

			List<Member> member_data = memberRepository.findByMemberAndRole(user.get().getId(), "Team Lead");
			if (member_data.isEmpty()) {
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(
						new ApiResponse(false, "No Member Found in Member Data List List ", Collections.emptyList()));

			}

			List<TaskActivityResponse> all_Task_activity = new ArrayList<>();

			for (Member member : member_data) {
				Product product_data = productRepository.findById(member.getProdId()).orElse(null);
				List<TaskActivityResponse> task_activity_data = null;
				if (fromdate.equalsIgnoreCase("nodate")) {
					task_activity_data = taskActivityRepository
							.findByProduc(member.getProdId(), user.get().getBranch())
							.stream()
							.map(row -> new TaskActivityResponse((Integer) row[0],
									convertToDateTime(row[1]).toLocalDate(),
									(String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
									((java.sql.Timestamp) row[6]).toLocalDateTime(),
									((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
									(String) row[10], (String) row[11], (String) row[12], (String) row[13], null))
							.collect(Collectors.toList());

				} else {

					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
					LocalDate localDate = LocalDate.parse(fromdate, formatter);

					task_activity_data = taskActivityRepository
							.findByProductAndActivityDateBetweenNative(member.getProdId(), localDate,
									user.get().getBranch())
							.stream()
							.map(row -> new TaskActivityResponse((Integer) row[0],
									convertToDateTime(row[1]).toLocalDate(),
									(String) row[2], (String) row[3], (String) row[4], (boolean) row[5],
									((java.sql.Timestamp) row[6]).toLocalDateTime(),
									((java.sql.Timestamp) row[7]).toLocalDateTime(), (boolean) row[8], (String) row[9],
									(String) row[10], (String) row[11], (String) row[12], (String) row[13], null))
							.collect(Collectors.toList());

				}

				all_Task_activity.addAll(task_activity_data);
				all_Task_activity.sort(Comparator.comparing(TaskActivityResponse::getId).reversed());

			}
			if (all_Task_activity.isEmpty()) {
				return ResponseEntity.status(HttpStatus.NO_CONTENT)
						.body(new ApiResponse(false, "No product Found in Product List ", Collections.emptyList()));
			}
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Fetched  Successfully", all_Task_activity));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "An error occurred while processing the request.", e.getMessage()));
		}
	}

	private LocalDateTime convertToDateTime(Object dateObject) {
		if (dateObject instanceof java.sql.Timestamp) {
			return ((java.sql.Timestamp) dateObject).toLocalDateTime();
		} else if (dateObject instanceof java.sql.Date) {
			return ((java.sql.Date) dateObject).toLocalDate().atStartOfDay();
		} else {
			throw new IllegalArgumentException("Unsupported date type");
		}
	}

	@Override
	public ResponseEntity<?> getProductByUserAndProductId(int id) {
		Optional<Users> users = usersRepository.findById(AuthUserData.getUserId());
		List<Member> members = memberRepository.findByMemberAndProdId(users.get(), id);
		List<Product> products = new ArrayList<>();
		for (Member member : members) {
			Product product = productRepository.findById(member.getProdId()).get();
			products.add(product);
		}
		products.sort(Comparator.comparing(Product::getId).reversed());
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Fetched Successfully", products));
	}

	@Override
	public ResponseEntity<?> notifyTeamLeaderIfTasksMissing() {
		LocalDate yesterday = LocalDate.now().minusDays(1);
		Optional<Users> user = usersRepository.findByIdWithRoles(AuthUserData.getUserId());

		List<Member> member_data = memberRepository.findByMemberAndRole(user.get().getId(), "Team Lead");

		Set<String> missingUsers = new HashSet<>(); // Use a Set to avoid duplicates

		for (Member member : member_data) {
			// Product product_data =
			// productRepository.findById(member.getProdId()).orElse(null);

			List<Member> members = memberRepository.findByProdIdAndRole(member.getProdId(), "Team Member");

			for (Member teammember : members) {

				boolean hasTaskActivityYesterday = hasTaskActivityForDate(teammember.getMember(), yesterday);

				if (!hasTaskActivityYesterday) {
					String message = "User " + teammember.getMember().getName() +
							" did not submit a task activity for yesterday. Date: " + yesterday;
					missingUsers.add(message);
				}
			}
		}

		if (!missingUsers.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Users with missing task activities", missingUsers));
		} else {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "All users submitted task activities for yesterday", null));
		}
	}

	private boolean hasTaskActivityForDate(Users user, LocalDate date) {
		BigInteger count = taskActivityRepository.countByUserAndActivityDateNative(user.getId(), date);
        return count.intValue() > 0;
	}

	public ResponseEntity<?> getTaskActivityRequestDates() {
		LocalDate today = LocalDate.now();
		// LocalDate startOfMonth = today.withDayOfMonth(1);
		// LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());
		List<LocalDate> dateList = new ArrayList<>();
		int authuser = AuthUserData.getUserId();
		// List<TaskActivity> taskactivity =
		// taskActivityRepository.findByUserIdAndActivityDateBetween(
		// authuser, startOfMonth, endOfMonth
		// );
		List<TaskActivity> taskactivity = taskActivityRepository.findByUserIdAndActivityDateBetween(
				authuser);
		// List<pm.model.activityrequest.ActivityRequest> activityrequest =
		// activityrequestrepo.findByUserIdAndRequestDateBetween(
		// authuser, startOfMonth, endOfMonth
		// );
		List<pm.model.activityrequest.ActivityRequest> activityrequest = activityrequestrepo
				.findByUserIdAndRequestDateBetween(
						authuser);
		for (TaskActivity record : taskactivity) {
			dateList.add(record.getActivity_date());
		}
		for (pm.model.activityrequest.ActivityRequest record : activityrequest) {
			dateList.add(record.getRequestDate());
		}
		List<LocalDate> uniqueDates = dateList.stream().distinct().collect(Collectors.toList());
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Fetched Successfully", uniqueDates));

	}

}
