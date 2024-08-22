package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.request.LoginRequest;
import pm.response.ApiResponse;

@Service
public interface UserService {

    ResponseEntity<?> signIn(LoginRequest loginRequest);

    ResponseEntity<?> logout();

    ResponseEntity<?> userList();

    ResponseEntity<?> approvalUserList();

    ResponseEntity<?> approvalUserList(List<Integer> ids);

    ResponseEntity<?> getRoles();

    ResponseEntity<?> getUser();

    ResponseEntity<?> refreshToken(String refreshToken, String accessToken);

    ResponseEntity<ApiResponse> tokenCheck(String token);

    ResponseEntity<ApiResponse> microoftsignIn(String token);
}
