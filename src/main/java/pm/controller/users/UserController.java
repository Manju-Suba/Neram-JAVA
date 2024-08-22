package pm.controller.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import pm.jwt.AuthTokenFilter;
import pm.model.users.Token;
import pm.repository.TokenRepository;
import pm.request.LoginRequest;
import pm.response.ApiResponse;
import pm.service.CommonService;
import pm.service.RefreshTokenService;
import pm.service.UserService;

@RequestMapping("/auth")
@CrossOrigin("*")
@RestController
public class UserController {

    @Value("${spring.security.oauth2.client.registration.azure-dev.client-id}")
    private String clienId;
    @Value("${myapp.tenentId}")
    private String tenentId;
    @Value("${myapp.customProperty}")
    private String frontendUrl;
    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthTokenFilter authTokenFilter;
    @Autowired
    private CommonService commonService;

    @Autowired
    private TokenRepository tokenRepository;

    @Operation(summary = "User sign-in")
    @PostMapping("/signin")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        return userService.signIn(loginRequest);
    }

    @Operation(summary = "Not in Use this   endpoint  !!! Ignore this ", hidden = true)
    @GetMapping("/your-endpoint")
    public ResponseEntity<String> yourEndpoint(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        // Now you can use the 'baseUrl' variable for further processing
        return ResponseEntity.ok("Response from your endpoint" + baseUrl);
    }

    private String getBaseUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String uri = request.getRequestURI();
        String baseUrl = url.substring(0, url.length() - uri.length()) + request.getContextPath();
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    @Operation(summary = "User sign-out")
    @PostMapping("/signout")
    public String logoutUser(@RequestParam String logout) {
        try {
            SecurityContextHolder.clearContext();

            if ("outlook".equals(logout)) {
                return "https://login.microsoftonline.com/" + tenentId +
                        "/oauth2/v2.0/logout"
                        + "?post_logout_redirect_uri=" + frontendUrl
                        + "&client_id=" + clienId;
            } else {
                return frontendUrl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during logout";
        }
    }

    // @Operation(summary = "User sign-out")
    // @PostMapping("/signout")
    // public String logoutUser(@RequestParam String logout,
    // @RequestParam String refreshToken, HttpServletRequest request) {
    // try {
    // String token = authTokenFilter.parseJwt(request);
    // LocalDateTime currentTime = LocalDateTime.now();
    // Token tokens = new Token();
    // tokens.setAccessToken(token);
    // tokens.setRefreshToken(refreshToken);
    // tokens.setCreatedAt(currentTime);
    // tokenRepository.save(tokens);
    //
    // SecurityContextHolder.clearContext();
    //
    // if ("outlook".equals(logout)) {
    // return "https://login.microsoftonline.com/" + tenentId +
    // "/oauth2/v2.0/logout"
    // + "?post_logout_redirect_uri=" + frontendUrl
    // + "&client_id=" + clienId;
    // } else {
    // return frontendUrl;
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // return "Error during logout";
    // }
    // }

    // @Operation(summary = "User sign-out")
    // @PostMapping("/signout")
    // public String logoutUser(@RequestBody LogoutRequest logoutRequest) {
    // try {
    // String token = logoutRequest.getToken();
    // String refreshToken = logoutRequest.getRefreshToken();
    // LocalDateTime currentTime = LocalDateTime.now();
    // Token tokens = new Token();
    // tokens.setAccessToken(token);
    // tokens.setRefreshToken(refreshToken);
    // tokens.setCreatedAt(currentTime);
    // tokenRepository.save(tokens);
    // SecurityContextHolder.clearContext();

    // if ("outlook".equals(logoutRequest.getLogout())) {
    // return "https://login.microsoftonline.com/" + tenentId +
    // "/oauth2/v2.0/logout"
    // + "?post_logout_redirect_uri=" + frontendUrl
    // + "&client_id=" + clienId;
    // } else {
    // return frontendUrl;
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // return "Error during logout";
    // }
    // }

    // @PostMapping("/signout/{id}")
    // public String logoutUser(@PathVariable Integer id,@RequestParam String
    // logout, HttpServletRequest request) {
    // try {
    // // UserDetailsImpl userDetails = (UserDetailsImpl)
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // // int userId = userDetails.getId();
    // refreshTokenService.deleteByUserId(id);
    // SecurityContextHolder.clearContext();
    //
    // String token = authTokenFilter.parseJwt(request);
    // tokenBlacklist.blacklistToken(token);
    //
    // if (logout.equals("outlook")) {
    // return "https://login.microsoftonline.com/" + tenentId +
    // "/oauth2/v2.0/logout" +
    // "?post_logout_redirect_uri=" + frontendUrl +
    // "&client_id=" + clienId;
    // } else {
    // return frontendUrl;
    // }
    // } catch (EmptyResultDataAccessException e) {
    // // Handle the case where the refresh token is not found (expired)
    // // You may want to log the exception or take other appropriate actions
    // return "Refresh token not found. User already logged out or session
    // expired.";
    // }
    // }
    @Operation(summary = "Not in Use this   endpoint  !!! Ignore this ", hidden = true)
    @GetMapping("/login")
    public String getUser() {
        return "user admin login";
    }

    // ==================================refresh
    @Operation(summary = "Refresh token Endpoint")
    @PostMapping("/refreshtoken")
    public ResponseEntity<?> RefreshTokenSignin(@RequestParam String refreshToken, @RequestParam String accessToken) {
        return userService.refreshToken(refreshToken, accessToken);

    }

    // @PostMapping("/signout")
    // public ResponseEntity<?> logoutUser() {
    // UserDetailsImpl userDetails = (UserDetailsImpl)
    // SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // int userId = userDetails.getId();
    // refreshTokenService.deleteByUserId(userId);
    // return ResponseEntity.ok(new ApiResponse(true,"Log out successful!"));
    // }
    //

    @Operation(summary = "update  a table", hidden = true)
    @PostMapping("/updateSupervisor")
    public void RefreshTokenSignin() {
        commonService.updateCommonTaskActivity();

    }

    @GetMapping("/tokencheck")
    public ResponseEntity<ApiResponse> tokenCheck(@RequestParam String token) {
        return userService.tokenCheck(token);
    }

    @PostMapping("/microsoft")
    public ResponseEntity<ApiResponse> microsoftLogin(@RequestParam String token) {
        return userService.microoftsignIn(token);
    }
}
