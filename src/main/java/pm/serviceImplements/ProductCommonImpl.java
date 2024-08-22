package pm.serviceImplements;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.*;

import jakarta.websocket.server.ServerEndpoint;
import pm.dto.UserDTO;
import pm.model.flow.Flow;
import pm.model.product.ProdApprovalHistory;
import pm.model.users.Users;
import pm.repository.ProdApprovalHistoryRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.service.ProductCommonService;
import pm.utils.CommonFunct;

@Service
public class ProductCommonImpl implements ProductCommonService {
	@Autowired
	private ProdApprovalHistoryRepository approvalHistoryRepository;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private CommonFunct commonFunct;

	@Value("${getPath}")
	private String getPath;

	@Override
	public ResponseEntity<?> getProductHead() {
		List<Users> prod_headData = usersRepository.getProd_headdetails();

		prod_headData = prod_headData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());

		if (prod_headData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : prod_headData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));
	}

	@Override
	public ResponseEntity<?> getTechHead() {
		List<Users> tech_headData = usersRepository.gettech_headdetails();
		tech_headData = tech_headData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());
		if (tech_headData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : tech_headData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));
	}

	@Override
	public ResponseEntity<?> getDataHead() {
		List<Users> data_headData = usersRepository.getdata_headdetails();
		data_headData = data_headData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());
		if (data_headData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : data_headData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));
	}


	// how head
	public ResponseEntity<?> getHowHead() {
		try {
			List<Users> data_headData = usersRepository.gethowheaddetails();
			data_headData = data_headData.stream()
					.sorted(Comparator.comparingInt(Users::getId).reversed())
					.toList();

			if (data_headData.isEmpty()) {
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ApiResponse(false, "No record", Collections.emptyList()));
			}

			List<UserDTO> list = new ArrayList<>();

			for (Users users : data_headData) {
				UserDTO userDTO = new UserDTO();
				userDTO.setId(users.getId());
				userDTO.setName(users.getName());
				userDTO.setProfile_pic(users.getProfile_pic());
				userDTO.setRole(users.getRole_id().stream()
						.map(role -> role.getName())
						.collect(Collectors.joining(", ")));
				userDTO.setUserName(users.getUsername());
				list.add(userDTO);
			}

			List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);

			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(true, "Fetched successfully", modifiedUserDTOs));

		} catch (Exception e) {
			// Log the exception if necessary
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ApiResponse(false, "An error occurred while fetching the data", null));
		}
	}


	@Override
	public ResponseEntity<?> getProductOwner() {
		List<Users> prod_ownerData = usersRepository.getProd_ownerdetails();

		prod_ownerData = prod_ownerData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());
		if (prod_ownerData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : prod_ownerData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);

		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));

	}

	@Override
	public ResponseEntity<?> getTechOwner() {
		List<Users> tech_ownerData = usersRepository.gettech_ownerdetails();
		tech_ownerData = tech_ownerData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());
		if (tech_ownerData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : tech_ownerData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));

	}

	@Override
	public ResponseEntity<?> getDataOwner() {
		List<Users> data_ownerData = usersRepository.getdata_ownerdetails();
		data_ownerData = data_ownerData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.collect(Collectors.toList());
		if (data_ownerData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : data_ownerData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));

	}

	// how owner
	@Override
	public ResponseEntity<?> gethowOwner() {
		List<Users> data_ownerData = usersRepository.gethow_ownerdetails();
		data_ownerData = data_ownerData.stream()
				.sorted(Comparator.comparingInt(Users::getId).reversed())
				.toList();
		if (data_ownerData.isEmpty()) {
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ApiResponse(false, "no record", Collections.emptyList()));
		}
		List<UserDTO> list = new ArrayList<>();

		for (Users users : data_ownerData) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(users.getId());
			userDTO.setName(users.getName());
			userDTO.setProfile_pic(users.getProfile_pic());
			userDTO.setRole(users.getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setUserName(users.getUsername());
			list.add(userDTO);
		}
		List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(list);
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponse(true, "fetched successfully", modifiedUserDTOs));

	}


	public ResponseEntity<?> approvalFlow(int id) {
		List<ProdApprovalHistory> approvalHistories = approvalHistoryRepository.findByProdId(id);
		List<UserDTO> dtos = new ArrayList<>();

		for (ProdApprovalHistory approvalHistory : approvalHistories) {
			UserDTO userDTO = new UserDTO();
			userDTO.setId(approvalHistory.getId());
			userDTO.setApproStatus(approvalHistory.getStatus());
			userDTO.setUpdated_At(approvalHistory.getUpdated_at());
			Optional<Users> users = usersRepository.findById(approvalHistory.getCreated_By());
			userDTO.setRole(users.get().getRole_id().stream()
					.map(role -> role.getName())
					.collect(Collectors.joining(", ")));
			userDTO.setName(users.get().getName());
			userDTO.setUserName(users.get().getUsername());
			dtos.add(userDTO);

		}
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "fetch successfully", dtos));
	}

}
