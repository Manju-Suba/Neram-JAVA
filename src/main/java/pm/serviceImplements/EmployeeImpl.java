package pm.serviceImplements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Getter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pm.dto.UserDTO;
import pm.exception.ProductNameAlreadyExistsException;
import pm.model.users.EmployeeProfilePic;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.EmployeeProfilePicRepo;
import pm.repository.RolesRepository;
import pm.repository.UserWidgetsRepository;
import pm.repository.UsersRepository;
import pm.request.UserCreateRequest;
import pm.response.ApiResponse;
import pm.service.EmployeeService;
import pm.utils.CommonFunct;

@Service
public class EmployeeImpl implements EmployeeService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private CommonFunct commonFunct;

    @Autowired
    private EmployeeProfilePicRepo employeeProfilePicRepo;

    @Autowired
    private DashboardServiceImpl dashboardServiceImpl;

    @Autowired
    private UserWidgetsRepository userWidgetsRepository;

    @Value("${fileBasePath}")
    private String fileBasePath;
    @Getter
    private static final String defaultImagePath = "default-profile.png";

    @Override
    public ResponseEntity<?> create(UserCreateRequest userCreateRequest, MultipartFile file) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(userCreateRequest);
        if (!violations.isEmpty()) {
            List<String> errorMessages = new ArrayList<>();
            for (ConstraintViolation<UserCreateRequest> violation : violations) {
                errorMessages.add(violation.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Validation error", errorMessages));
        }
        try {
            if ((userCreateRequest.getRole_id() <= 0)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "Role not Found Select Correct Role", Collections.emptyList()));
            }

            LocalDateTime currDateTime = LocalDateTime.now();
            if (usersRepository.existsByUsernameAndIs_deletedFalse(userCreateRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                        .body(new ApiResponse(false, "Employee ID already exists", Collections.emptyList()));
            }
            if (usersRepository.existsByEmailAndIs_deletedFalse(userCreateRequest.getEmail())) {
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                        .body(new ApiResponse(false, "Email already exists", Collections.emptyList()));
            }

            Set<Roles> addroles = new HashSet<>();
            Users user = new Users();
            Optional<Roles> roles = rolesRepository.findById(userCreateRequest.getRole_id());
            addroles.add(roles.get());
            BeanUtils.copyProperties(userCreateRequest, user);
            user.setRole_id(addroles);
            user.setPassword(encoder.encode("12345678"));
            user.setCreated_at(currDateTime);
            user.setUpdated_at(currDateTime);
            user.set_deleted(false);
            if (file != null && !file.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String formattedTime = currDateTime.format(formatter);
                String fileName = formattedTime + "_" + file.getOriginalFilename();
                Path path = Paths.get(fileBasePath + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                File fileexist = path.toFile();
                if (fileexist.exists()) {
                    user.setProfile_pic(fileName);
                }
            } else {
                user.setProfile_pic(defaultImagePath);
            }
            user.setStatus(true);

            user = usersRepository.save(user);
            dashboardServiceImpl.widgetSetToEmployee(true, user.getUsername());
            String message = "User Created Successfully.";
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, message, user));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }

    }

    // =================================================================Get All
    // Active
    // Empoloyees====================================================================================================
    @Override
    public ResponseEntity<?> list(int page, int size, boolean search, String value) {

        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        Page<Users> users = null;

        if (search) {
            users = getUsersBySearchValue(value, pageable);

        } else {

            users = usersRepository.getPageActiveEmployees(pageable);
        }
        List<UserDTO> userDTO = new ArrayList<UserDTO>();

        for (Users user : users) {
            UserDTO dto = new UserDTO();
            if (user.getFinalApprove() != null) {
                // Optional<Users> finalApprovername =
                // usersRepository.findByUserNameGetName(user.getFinalApprove());
                dto.setApprovalFinalName(usersRepository.findByUserNameGetName(user.getFinalApprove()));
            }
            if (user.getSupervisor() != null) {
                // Optional<Users> supervisorName =
                // usersRepository.findByUsername(user.getSupervisor());
                dto.setSupervisor(usersRepository.findByUserNameGetName(user.getSupervisor()));
            }
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setEmployeeId(user.getUsername());
            dto.setBranch(user.getBranch());
            dto.setDesignation(user.getDesignation());
            dto.setRoleType(user.getRoleType());
            dto.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            dto.setUserStatus(usersRepository.findByIdandStatus(user.getId()) != 0);
            dto.setProfile_pic(user.getProfile_pic());
            userDTO.add(dto);
        }

        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(userDTO);

        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, modifiedUserDTOs));
    }

    public Page<Users> getUsersBySearchValue(String value, Pageable pageable) {
        List<Integer> userId = usersRepository.searchNameFields(value);
        if (userId.size() <= 0) {
            return usersRepository.searchAllFieldsWithRoleName(value, value, pageable);

        } else {
            return usersRepository.searchAllFields(value, userId, pageable);

        }

    }

    @Override
    public ResponseEntity<?> getListByBranch(String branch) {
        // List<Users> users = usersRepository.findByBranch(branch);

        List<Users> users = usersRepository.findByActiveEmployeeBranch(branch);
        List<UserDTO> userDTO = new ArrayList<UserDTO>();
        for (Users user : users) {
            UserDTO dto = new UserDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setEmployeeId(user.getUsername());
            dto.setProfile_pic(user.getProfile_pic());
            dto.setBranch(user.getBranch());
            dto.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            dto.setUserName(user.getUsername());
            userDTO.add(dto);
        }
        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, userDTO));
    }

    @Override
    public ResponseEntity<?> view(int id) {
        // Users users = usersRepository.findById(id).orElse(null);

        Optional<Users> userOptional = usersRepository.findByActiveEmployeeById(id);

        if (userOptional.isEmpty()) {
            String message = "User not found for ID: " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, message, null));
        }

        Users users = userOptional.get();

        UserDTO dto = new UserDTO();
        dto.setId(users.getId());
        dto.setName(users.getName());
        dto.setEmail(users.getEmail());
        dto.setEmployeeId(users.getUsername());
        dto.setProfile_pic(users.getProfile_pic());
        dto.setBranch(users.getBranch());
        dto.setDesignation(users.getDesignation());
        dto.setDoj(users.getJod());
        // Optional<Users> name = usersRepository.findById(users.getSupervisor());
        // if (name.isPresent()) {
        // dto.setSupervisor(name.get().getName());
        // }
        dto.setSupervisor(usersRepository.findByUserNameGetName(users.getSupervisor()));
        dto.setRoleType(users.getRoleType());
        dto.setRole(users.getRole_id().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(", ")));
        String roleIds = users.getRole_id().stream()
                .map(role -> String.valueOf(role.getId()))
                .collect(Collectors.joining(", "));

        Users supervisor = usersRepository.findByUserNameGetAll(users.getSupervisor()).orElse(null);
        String supId;
        String apprId;
        String appname;
        if (supervisor != null) {
            supId = supervisor.getUsername();

        } else {
            supId = "";
        }
        Users approvefinalid = usersRepository.findByUserNameGetAll(users.getFinalApprove()).orElse(null);
        if (approvefinalid != null) {
            apprId = approvefinalid.getUsername();
            appname = approvefinalid.getName();
        } else {
            apprId = "";
            appname = "";
        }

        dto.setApprovalFinalId(apprId);
        dto.setRolesid(roleIds);
        dto.setSupervisorId(supId);

        dto.setApprovalFinalName(appname);
        String message = "User Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, dto));
    }

    @Override
    public ResponseEntity<?> update(UserCreateRequest userCreateRequest, MultipartFile file, int id) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<UserCreateRequest>> violations = validator.validate(userCreateRequest);
        if (!violations.isEmpty()) {
            List<String> errorMessages = new ArrayList<>();
            for (ConstraintViolation<UserCreateRequest> violation : violations) {
                errorMessages.add(violation.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Validation error", errorMessages));
        }
        try {
            LocalDateTime currDateTime = LocalDateTime.now();
            // Users users = usersRepository.findById(id).orElse(null);

            Users users = usersRepository.findByActiveEmployeeById(id).orElse(null);
            if (users == null) {
                String message = "User not found for ID: " + id;
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, message, null));
            }

            String newEmail = userCreateRequest.getEmail();
            if (!users.getEmail().equals(newEmail)) {

                Optional<Users> userByEmail = usersRepository.findByEmailAndIs_deletedFalse(newEmail);

                if (userByEmail.isPresent() && userByEmail.get().getId() != users.getId()) {

                    String message = "Email already exists";
                    return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                            .body(new ApiResponse(false, message, null));
                }
            }

            String newUserName = userCreateRequest.getUsername();
            if (!users.getUsername().equals(newUserName)) {
                Optional<Users> userByUserName = usersRepository.findByUsernameAndIs_deletedFalse(newUserName);

                if (userByUserName.isPresent() && userByUserName.get().getId() != users.getId()) {
                    String message = "Employee ID already exists";
                    return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                            .body(new ApiResponse(false, message, null));
                }
            }
String oldusername = users.getUsername();


            Set<Roles> rolesSet = new HashSet<Roles>();
            users.setUpdated_at(currDateTime);
            users.setName(userCreateRequest.getName());
            users.setUsername(userCreateRequest.getUsername());
            users.setEmail(userCreateRequest.getEmail());
            users.setBranch(userCreateRequest.getBranch());
            users.setDesignation(userCreateRequest.getDesignation());
            // users.setFinalApprove(userCreateRequest.getFinalApprove());
            users.setFinalApprove(
                    userCreateRequest.getFinalApprove() != null ? userCreateRequest.getFinalApprove() : "");

            users.setRoleType(userCreateRequest.getRoleType());
            Optional<Roles> roles = rolesRepository.findById(userCreateRequest.getRole_id());
            rolesSet.add(roles.get());
            users.setRole_id(rolesSet);
            users.setSupervisor(userCreateRequest.getSupervisor());
            users.setJod(userCreateRequest.getJod());
            if (file != null && !file.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                String formattedTime = currDateTime.format(formatter);
                String fileName = formattedTime + "_" + file.getOriginalFilename();
                Path path = Paths.get(fileBasePath + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                File fileexist = path.toFile();
                if (fileexist.exists()) {
                    users.setProfile_pic(fileName);

                }
            }
            users.setStatus(true);
            users = usersRepository.save(users);
            if(!oldusername.equalsIgnoreCase(userCreateRequest.getUsername())){
                userWidgetsRepository.deleteByEmpId(oldusername);
                dashboardServiceImpl.widgetSetToEmployee(true, userCreateRequest.getUsername());
            }
            String message = "User Updated Successfully.";
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, message, users));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> updateUserStatus(int id, Boolean status) {
        Optional<Users> optionalUser = usersRepository.findByActiveEmployeeById(id);

        if (optionalUser.isEmpty()) {
            String message = "User not found for ID: " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, message, null));
        }

        if (optionalUser.isPresent()) {
            Users user = optionalUser.get();
            user.setStatus(status);

            Users updatedUser = usersRepository.save(user);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "User status updated", updatedUser));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found with id: " + id));
        }
    }

    @Override
    public ResponseEntity<?> deleteaUser(List<Integer> id) {
        usersRepository.updateIsDeleted(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "User Deleted Successfully", Collections.EMPTY_LIST));

    }

    @Override
    public ResponseEntity<?> updateSupervisor() {
        List<Users> users = usersRepository.findAll();
        Set<Integer> processedUserIds = new HashSet<>(); // Set to store processed user IDs

        for (Users user : users) {
            Integer userId = user.getId(); // Use Integer instead of int

            // Check if the user ID is already processed
            if (processedUserIds.contains(userId)) {
                continue; // Skip this iteration if the user ID is already processed
            }

            // Update supervisor if it's not null, not empty, and not "0"
            if (user.getSupervisor() != null && !user.getSupervisor().isEmpty() && !user.getSupervisor().equals("0")) {
                user.setSupervisor(usersRepository.findByIdGetUsernameforcommonUpdate(user.getSupervisor()));

            }

            // Update finalApprove if it's not null, not empty, and not "0"
            if (user.getFinalApprove() != null && !user.getFinalApprove().isEmpty()
                    && !user.getFinalApprove().equals("0")) {
                user.setFinalApprove(usersRepository.findByIdGetUsernameforcommonUpdate(user.getFinalApprove()));

            }

            // Save the updated user entity
            usersRepository.save(user);

            // Add the processed user ID to the set
            processedUserIds.add(userId);
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Updated successfully", Collections.emptyList()));
    }

    @Override
    public ResponseEntity<?> updateJOD() {
        List<Users> users = usersRepository.findAll();
        for (Users user : users) {
            // Convert LocalDateTime to LocalDate
            user.setJod(user.getCreated_at().toLocalDate());
            usersRepository.save(user);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Updated Joining Date successfully", Collections.emptyList()));
    }

@Override
    public EmployeeProfilePic getProfilePic(String userId) {
        try {
            // Attempt to find the profile picture by employee ID
            EmployeeProfilePic profilePic = employeeProfilePicRepo.findByEmpid(userId);

            // If no profile picture is found, throw a custom exception
            if (profilePic == null) {
                throw new ProductNameAlreadyExistsException("Profile picture not found for user ID: " + userId);
            }

            return profilePic; // Return the found profile picture
        } catch (Exception e) {

            throw new ProductNameAlreadyExistsException("An error occurred while retrieving the profile picture "+e.getMessage());
        }
    }

}
