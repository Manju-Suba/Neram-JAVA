package pm.serviceImplements;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.Map.entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

import pm.dto.UserDTO;
import pm.dto.UserDetailsDTO;
import pm.jwt.JwtUtils;
import pm.model.users.Login_history;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.Login_historyRepository;
import pm.repository.RolesRepository;
import pm.repository.TokenRepository;
import pm.repository.UsersRepository;
import pm.request.LoginRequest;
import pm.response.ApiResponse;
import pm.service.UserService;
import pm.service.security.UserDetailsImpl;
import pm.utils.CommonFunct;

@Service
public class UserImpl implements UserService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private CommonFunct commonFunct;

    @Autowired
    private Login_historyRepository historyRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Value("${getPath}")
    private String getPath;

    @Value("${fileBasePath}")
    private String fileBasePath;
    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public ResponseEntity<?> signIn(LoginRequest loginRequest) {
        try {
            LocalDateTime curreDateTime = LocalDateTime.now();
            Login_history login_history = new Login_history();
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Integer status_present = usersRepository.findByIdandStatus(userDetails.getId());
            Long count = usersRepository.countNonDeletedUsers(userDetails.getId());

            // Use statusPresent and nonDeletedCount as needed

            if (count == 0) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ApiResponse(false, "User is Deleted", Collections.emptyList()));
            }
            if (status_present != 0) {

                List<String> roles = userDetails.getRole_id().stream().map(role -> role.getName())
                        .collect(Collectors.toList());
                // String supervisor =
                // usersRepository.getsupervisorcount(userDetails.getUsername());
                // String final_approver =
                // usersRepository.getfinalApproverCount(userDetails.getUsername());

                Map<String, String> counts = usersRepository
                        .getSupervisorAndFinalApproverCounts(userDetails.getUsername());

                String supervisor = counts.get("supervisorCount");
                String final_approver = counts.get("finalApproverCount");
                String profilePicPath = fileBasePath + userDetails.getProfile_pic(); // Assuming profilePic is the file
                                                                                     // name

                Path filePath = Paths.get(profilePicPath);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("id", userDetails.getId());
                responseData.put("email", userDetails.getEmail());
                responseData.put("jod", userDetails.getJod().toString());
                responseData.put("name", userDetails.getName());
                responseData.put("branch", userDetails.getBranch());
                if (Files.exists(filePath)) {
                    responseData.put("profile_pic", userDetails.getProfile_pic());
                } else {
                    responseData.put("profile_pic", EmployeeImpl.getDefaultImagePath());
                }
                responseData.put("designation", userDetails.getBranch() + " " + userDetails.getDesignation());
                responseData.put("roleIntake", userDetails.getRoleIntake());
                responseData.put("role", roles);
                responseData.put("employee_id", userDetails.getUsername());
                responseData.put("superviser", supervisor);
                responseData.put("finalApprover", final_approver);

                String acccesToken = jwtUtils.generateTokenFromUsernameintoClaims(userDetails.getUsername(),
                        responseData);
                String refreshToken = jwtUtils.generateRefreshTokenFromUsernameintoClaims(userDetails.getUsername());

                Map<String, Object> userToken = Map.ofEntries(
                        entry("Token", acccesToken), entry("RefreshToken", refreshToken));

                ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userToken);

                ApiResponse response = new ApiResponse(true, "Login Successfully", userToken);
                if (response.isStatus()) {
                    login_history.setLogged_in(curreDateTime);
                    login_history.setUser_id(userDetails.getId());
                    historyRepository.save(login_history);
                }
                return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString()).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "User is not active", ""));

            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Login Failed", e.getMessage()));
        }
    }

    public ResponseEntity<?> logout() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        ApiResponse response = new ApiResponse(true, "User has been logged out!", "");
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @Override
    public ResponseEntity<?> userList() {
        // List<Users> users = usersRepository.findAll();

        List<Users> users = usersRepository.getActiveEmployees();
        List<UserDetailsDTO> dataDTOList = new ArrayList<>();
        // Transform data and populate the DTO list
        for (Users user : users) {
            UserDetailsDTO dataDTO = new UserDetailsDTO();
            dataDTO.setId(user.getId());
            dataDTO.setName(user.getName());
            dataDTO.setUsername(user.getUsername());
            dataDTO.setProfile_pic(user.getProfile_pic());
            dataDTO.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            dataDTO.getStatus(user.getStatus());
            dataDTOList.add(dataDTO);
        }

        // Convert the list to an array if needed
        UserDetailsDTO[] dataArray = dataDTOList.toArray(new UserDetailsDTO[0]);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "data fetched", dataArray));
    }

    @Override
    public ResponseEntity<?> approvalUserList() {
        String desg = "Approver";
        List<Users> users = usersRepository.findByDesignation(desg);
        List<UserDTO> dataDTOList = new ArrayList<>();
        for (Users user : users) {
            UserDTO dataDTO = new UserDTO();
            dataDTO.setId(user.getId());
            dataDTO.setName(user.getName());
            dataDTO.setUserName(user.getUsername());
            dataDTO.setProfile_pic(user.getProfile_pic());
            dataDTO.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            dataDTO.setUserStatus(user.getStatus());
            dataDTOList.add(dataDTO);
        }
        List<UserDTO> modifiedUserDTOs = commonFunct.commonFunction1(dataDTOList);

        UserDTO[] dataArray = dataDTOList.toArray(new UserDTO[modifiedUserDTOs.size()]);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "data fetched", dataArray));
    }

    @Override
    public ResponseEntity<?> approvalUserList(List<Integer> ids) {
        String desg = "Head";
        List<Users> users = usersRepository.findByDesignation(desg);
        users = users.stream()
                .filter(user -> !ids.contains(user.getId()))
                .collect(Collectors.toList());
        List<UserDetailsDTO> dataDTOList = new ArrayList<>();
        for (Users user : users) {
            UserDetailsDTO dataDTO = new UserDetailsDTO();
            dataDTO.setId(user.getId());
            dataDTO.setName(user.getName());
            dataDTO.setUsername(user.getUsername());
            dataDTO.setProfile_pic(user.getProfile_pic());
            dataDTO.setRole(user.getRole_id().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.joining(", ")));
            dataDTO.getStatus(user.getStatus());
            dataDTOList.add(dataDTO);
        }
        UserDetailsDTO[] dataArray = dataDTOList.toArray(new UserDetailsDTO[0]);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "data fetched", dataArray));
    }

    @Override
    public ResponseEntity<?> getRoles() {
        List<Roles> roles = rolesRepository.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "data fetched", roles));

    }

    @Override
    public ResponseEntity<?> getUser() {
        List<Users> users = usersRepository.getActiveEmployees();
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "data fetched", users));

    }

    @Override
    public ResponseEntity<?> refreshToken(String refreshToken, String accessToken) {
        try {
            String validationResult = jwtUtils.validateRefresh(refreshToken);

            if (validationResult.equals("Token is valid") && !tokenRepository.existsByRefreshToken(refreshToken)) {
                Claims refreshClaims = jwtUtils.decodeJwt(refreshToken);
                Claims accessClaims = jwtUtils.decodeJwt(accessToken);
                String refreshSubject = refreshClaims.getSubject();
                String accessSubject = accessClaims.getSubject();
                if (refreshSubject.equals(accessSubject)) {
                    Map<String, Object> userDataMap = new HashMap<>();

                    String[] fieldsToInclude = { "role", "createdAt", "name", "branch", "id", "designation",
                            "roleIntake", "username", "profile_pic", "superviser", "finalApprover" };

                    for (String field : fieldsToInclude) {
                        Object value = accessClaims.get(field);
                        if (value != null) {
                            userDataMap.put(field, value);
                        }
                    }

                    // Generate new access token
                    String newAccessToken = jwtUtils.generateTokenFromUsernameintoClaims(refreshSubject, userDataMap);
                    Map<String, Object> userToken = Map.of("Token", newAccessToken);
                    ApiResponse response = new ApiResponse(true, "Access token generated successfully.", userToken);
                    return ResponseEntity.ok(response);
                } else {
                    ApiResponse response = new ApiResponse(false,
                            "Refresh token subject does not match access token subject.", Collections.emptyList());
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                ApiResponse response = new ApiResponse(false, "Invalid or expired refresh token.",
                        Collections.emptyList());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse(false, e.getMessage(), Collections.emptyList());
            return ResponseEntity.badRequest().body(errorResponse);
        }

    }

    // token validation check
    public ResponseEntity<ApiResponse> tokenCheck(String accessToken) {
        String validationResult = jwtUtils.validateJwtToken1(accessToken); // Call validateJwtToken method

        boolean isValid = validationResult.equals("Token is valid");
        ApiResponse response = new ApiResponse();

        if (isValid) {
            response.setMessage("Token is valid");
            response.setStatus(true);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.setMessage("Invalid token");
            response.setStatus(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);

        }

    }

    @Override
    public ResponseEntity<ApiResponse> microoftsignIn(String email) {

        Optional<Users> document = usersRepository.findByEmail(email);

        if (document.isPresent()) {
            Users userdata = document.get();
            Integer statusPresent = usersRepository.findByIdandStatus(userdata.getId());
            Long count = usersRepository.countNonDeletedUsers(userdata.getId());

            if (statusPresent != 0 && count != 0) {
                String username = userdata.getUsername();
                Users userWithRoles = usersRepository.findByEmailWithRoles(email).orElseThrow();

                String profilePicPath = fileBasePath + userWithRoles.getProfile_pic();
                Path filePath = Paths.get(profilePicPath);
                List<GrantedAuthority> authorities = userWithRoles.getRole_id().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList());
                Set<String> roles = authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                String rolesString = String.join(", ", roles);
                String designationData = userWithRoles.getBranch() + " " + userWithRoles.getDesignation();
                String supervisor = usersRepository.getsupervisorcount(userWithRoles.getUsername());
                String finalApprover = usersRepository.getfinalApproverCount(userWithRoles.getUsername());

                Map<String, Object> userDataMap = new HashMap<>();
                userDataMap.put("email", email);
                userDataMap.put("name", userWithRoles.getName());
                userDataMap.put("id", Math.toIntExact(userWithRoles.getId()));
                if (Files.exists(filePath)) {
                    userDataMap.put("profile_pic", userWithRoles.getProfile_pic());
                } else {
                    userDataMap.put("profile_pic", EmployeeImpl.getDefaultImagePath());
                }
                userDataMap.put("designation", designationData);
                userDataMap.put("branch", userWithRoles.getBranch());
                userDataMap.put("jod", userWithRoles.getJod().toString());
                userDataMap.put("employee_id", userWithRoles.getUsername());
                userDataMap.put("role", rolesString);
                userDataMap.put("superviser", supervisor);
                userDataMap.put("finalApprover", finalApprover);

                String accessToken = jwtUtils.generateTokenFromUsernameintoClaims(username, userDataMap);
                String refreshToken = jwtUtils.generateRefreshTokenFromUsernameintoClaims(username);

                Map<String, Object> userToken = Map.of(
                        "Token", accessToken,
                        "RefreshToken", refreshToken);

                ApiResponse apiResponse = new ApiResponse(true, "User signed in successfully", userToken);
                return ResponseEntity.ok(apiResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "User not authorized"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, "User not found"));
        }
    }
}
