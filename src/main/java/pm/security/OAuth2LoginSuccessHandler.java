package pm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import io.jsonwebtoken.io.IOException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

import pm.model.users.EmployeeProfilePic;
import pm.model.users.RefreshToken;
import pm.model.users.Roles;
import pm.model.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static java.util.Map.entry;

import pm.jwt.JwtUtils;
import pm.repository.EmployeeProfilePicRepo;
import pm.repository.UsersRepository;
import pm.request.UserCreateRequest;
import pm.response.ApiResponse;
import pm.response.JwtResponse;
import pm.service.RefreshTokenService;
import pm.service.security.UserDetailsImpl;
import pm.serviceImplements.EmployeeImpl;

import javax.imageio.ImageIO;
import javax.swing.*;

@Configuration
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    public UsersRepository userRepo;
    @Value("${myapp.customProperty}")
    private String frontendUrl;

    @Autowired
    private JwtUtils jwtUtils;
    @Value("${spring.security.oauth2.client.registration.azure-dev.client-id}")
    private String clienId;
    @Value("${myapp.tenentId}")
    private String tenentId;
    @Value("${fileBasePath}")
    private String fileBasePath;

    @Autowired
    private EmployeeProfilePicRepo employeeProfilePicRepo;
    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException, java.io.IOException {
        OAuth2AuthenticationToken oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> userDataMap = new HashMap<>(); // Move the declaration here
        Map<String, Object> tokendata = new HashMap<>(); // Move the declaration here
        LocalDateTime currentTime = LocalDateTime.now();
        if ("azure-dev".equalsIgnoreCase(oAuth2AuthenticationToken.getAuthorizedClientRegistrationId())) {
            DefaultOAuth2User principal = (DefaultOAuth2User) authentication.getPrincipal();
            String pictureUrl = (String) principal.getAttributes().get("picture");
            System.out.println(pictureUrl);

            Map<String, Object> attributes = principal.getAttributes();
            String email = attributes.getOrDefault("email", "").toString();

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());

            String accessToken = authorizedClient.getAccessToken().getTokenValue();
            String pictureUrldata = getProfilePictureUrlFromGraphAPI(accessToken);

            if (authentication.isAuthenticated() && !userRepo.existsByEmail(email)) {
                redirectLogout(request, response, userDataMap);
                return;
            }

            Optional<Users> document = userRepo.findByEmail(email);
            Integer status_present = userRepo.findByIdandStatus(document.get().getId());
            Long count = userRepo.countNonDeletedUsers(document.get().getId());

            if (document.isPresent() && status_present != 0 && count != 0) {
                String username = document.get().getUsername();
                Long imagePresent = employeeProfilePicRepo.countByEmployeeId(username);
                if (imagePresent > 0) {
                    employeeProfilePicRepo.updateProfilePicUrl(pictureUrldata, username);
                } else {
                    EmployeeProfilePic employeeProfilePic = EmployeeProfilePic.builder()
                            .empid(username) // Assuming `username` is the employee ID
                            .profilePic(pictureUrldata)
                            .build();

                    employeeProfilePicRepo.save(employeeProfilePic);
                }
                Users userdata = userRepo.findByEmailWithRoles(email)
                        .orElseThrow();

                String profilePicPath = fileBasePath + userdata.getProfile_pic(); // Assuming profilePic is the file
                // name
                Path filePath = Paths.get(profilePicPath);
                List<GrantedAuthority> authorities = userdata.getRole_id().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .collect(Collectors.toList());
                Set<String> roles = authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                String rolesString = roles.stream()
                        .collect(Collectors.joining(", "));
                String designationdata = userdata.getBranch() + " " + userdata.getDesignation();
                String supervisor = userRepo.getsupervisorcount(userdata.getUsername());
                String final_approver = userRepo.getfinalApproverCount(userdata.getUsername());
                userDataMap.put("email", email);
                userDataMap.put("name", userdata.getName());
                userDataMap.put("id", Math.toIntExact(userdata.getId()));
                if (Files.exists(filePath)) {
                    userDataMap.put("profile_pic", userdata.getProfile_pic());
                } else {
                    userDataMap.put("profile_pic", EmployeeImpl.getDefaultImagePath());
                }
                userDataMap.put("designation", designationdata);
                userDataMap.put("branch", userdata.getBranch());
                userDataMap.put("jod", userdata.getJod().toString());
                userDataMap.put("employee_id", userdata.getUsername());
                userDataMap.put("role", rolesString);
                userDataMap.put("superviser", supervisor);
                userDataMap.put("finalApprover", final_approver);

                String acccesToken = jwtUtils.generateTokenFromUsernameintoClaims(username, userDataMap);
                String refreshToken = jwtUtils.generateRefreshTokenFromUsernameintoClaims(username);

                Map<String, Object> userToken = Map.ofEntries(
                        entry("Token", acccesToken), entry("RefreshToken", refreshToken));

                String redirectUrl = buildRedirectUrlWithUserData(frontendUrl, userToken);
                System.out.println(redirectUrl);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                this.setAlwaysUseDefaultTargetUrl(true);
                this.setDefaultTargetUrl(redirectUrl);

            } else {
                // String logoutUrl = "https://login.microsoftonline.com/" + tenentId +
                // "/oauth2/v2.0/logout"
                // + "?post_logout_redirect_uri=" + frontendUrl
                // + "&client_id=" + clienId;
                // // User does not exist in the database
                // String redirectUrl = buildRedirectUrlWithUserData(frontendUrl, userDataMap);
                // getRedirectStrategy().sendRedirect(request, response, logoutUrl);
                // this.setAlwaysUseDefaultTargetUrl(true);
                // this.setDefaultTargetUrl(redirectUrl);

                redirectLogout(request, response, userDataMap);

            }
        }

        super.onAuthenticationSuccess(request, response, authentication);
    }

    public static String convertByteArrayToString(String imageData) {
        return Base64.getEncoder().encodeToString(imageData.getBytes());
    }

    private String buildRedirectUrlWithUserData(String baseUrl, Map<String, Object> userDataMap)
            throws UnsupportedEncodingException {
        // Append query parameters to the base URL
        StringBuilder redirectUrlBuilder = new StringBuilder(baseUrl);
        if (!userDataMap.isEmpty()) {
            redirectUrlBuilder.append("?");

            for (Map.Entry<String, Object> entry : userDataMap.entrySet()) {
                if (entry.getValue() != null) { // Check if entry value is not null

                    redirectUrlBuilder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
                    redirectUrlBuilder.append("=");
                    redirectUrlBuilder
                            .append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
                    redirectUrlBuilder.append("&");
                }
            }
            // Remove the trailing "&"
            redirectUrlBuilder.deleteCharAt(redirectUrlBuilder.length() - 1);
        }

        return redirectUrlBuilder.toString();
    }

    private void redirectLogout(HttpServletRequest request, HttpServletResponse response,
            Map<String, Object> userDataMap) throws IOException, java.io.IOException {
        String logoutUrl = "https://login.microsoftonline.com/" + tenentId + "/oauth2/v2.0/logout"
                + "?post_logout_redirect_uri=" + frontendUrl
                + "&client_id=" + clienId;
        String redirectUrl = buildRedirectUrlWithUserData(frontendUrl, userDataMap);
        getRedirectStrategy().sendRedirect(request, response, logoutUrl);
        this.setAlwaysUseDefaultTargetUrl(true);
        this.setDefaultTargetUrl(redirectUrl);
    }

    public static String getProfilePictureUrlFromGraphAPI(String accessToken) {
        String graphUrl = "https://graph.microsoft.com/v1.0/me/photo/$value";
        HttpURLConnection connection = null;

        try {
            URL url = new URL(graphUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, bytesRead);
                }
                buffer.flush();

                String pictureUrl = Base64.getEncoder().encodeToString(buffer.toByteArray());
                inputStream.close();
                return pictureUrl;
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                return EmployeeImpl.getDefaultImagePath(); // or return a URL to a default image
            } else {
                InputStream errorStream = connection.getErrorStream();
                if (errorStream != null) {
                    ByteArrayOutputStream errorBuffer = new ByteArrayOutputStream();
                    byte[] errorData = new byte[1024];
                    int errorBytesRead;
                    while ((errorBytesRead = errorStream.read(errorData, 0, errorData.length)) != -1) {
                        errorBuffer.write(errorData, 0, errorBytesRead);
                    }
                    errorBuffer.flush();
                    System.err.println("Error response: " + new String(errorBuffer.toByteArray()));
                }
                System.err.println("Failed to fetch profile picture. Response code: " + responseCode);
                return null;
            }
        } catch (IOException | MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}
