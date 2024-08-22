package pm.jwt;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pm.model.users.Token;
import pm.repository.TokenRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.service.security.UserDetailsServiceImpl;
import pm.utils.AuthUserData;

public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {

            String jwt = parseJwt(request);
            boolean existsByUserId = tokenRepository.existsByAccessToken(jwt);



//            if (!existsByUserId) {
                String validationResult = jwtUtils.validateJwtToken1(jwt); // Call validateJwtToken method

                if (validationResult.equalsIgnoreCase("Token is valid")) {




                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    Integer id = jwtUtils.getUserIdFromJwtToken(jwt);
                    Long count = usersRepository.countNonDeletedUsers(id);
                    Integer status_present = usersRepository.findByIdandStatus(id);
                    if (count == 0 || status_present == 0) {
                        String errorMessage;
                        if (count == 0) {
                            errorMessage = "User is Deleted";
                        } else {
                            errorMessage = "User is Blocked";
                        }
                        ApiResponse customErrorResponse = new ApiResponse(false, errorMessage, Collections.emptyList());
                        String errorResponseJson = new ObjectMapper().writeValueAsString(customErrorResponse);
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType("application/json");
                        response.getWriter().write(errorResponseJson);
                        return;
                    }

                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (jwt != null) {

                    ApiResponse customErrorResponse = new ApiResponse(false, "You have an Invalid token ",
                            validationResult);
                    String errorResponseJson = new ObjectMapper().writeValueAsString(customErrorResponse);

                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.getWriter().write(errorResponseJson);
                    return; // Exit the method to prevent further processing
                }
//            } else {
//
//                ApiResponse customErrorResponse = new ApiResponse(false, "You have an Invalid token or Logout token", Collections.emptyList());
//                String errorResponseJson = new ObjectMapper().writeValueAsString(customErrorResponse);
//
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                response.setContentType("application/json");
//                response.getWriter().write(errorResponseJson);
//                return;
//            }
        } catch (Exception e) {
            ApiResponse customErrorResponse = new ApiResponse(false, "You have an Invalid token: ", e.getMessage());
            String errorResponseJson = new ObjectMapper().writeValueAsString(customErrorResponse);

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(errorResponseJson);
            return;
        }
        filterChain.doFilter(request, response);
    }

    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;

    }

}
