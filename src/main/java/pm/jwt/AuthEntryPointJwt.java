package pm.jwt;

import java.io.IOException;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pm.response.ApiResponse;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

	private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

	// @Override
	// public void commence(HttpServletRequest request, HttpServletResponse
	// response, AuthenticationException authException)
	// throws IOException, ServletException {
	// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	// response.setContentType("application/json");
	// ApiResponse customErrorResponse = new ApiResponse(false, "Unauthorized: " +
	// authException.getMessage(),null);
	//
	// ObjectMapper objectMapper = new ObjectMapper();
	// objectMapper.writeValue(response.getWriter(), customErrorResponse);
	// }

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException)
			throws IOException, ServletException {
		if (authException instanceof BadCredentialsException) {
			// Handle bad credentials error
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			ApiResponse customErrorResponse = new ApiResponse(false, "Unauthorized: Bad credentials", null);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(response.getWriter(), customErrorResponse);
		}
		// else if (authException instanceof NoHandlerFoundException) {
		// // Handle disabled user error
		// response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		// response.setContentType("application/json");
		// ApiResponse customErrorResponse = new ApiResponse(false, "Unauthorized: User
		// is disabled", null);
		//
		// ObjectMapper objectMapper = new ObjectMapper();
		// objectMapper.writeValue(response.getWriter(), customErrorResponse);
		// }
		else if (authException instanceof InsufficientAuthenticationException) {
			// Handle insufficient authentication error
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			ApiResponse customErrorResponse = new ApiResponse(false, "Unauthorized: Insufficient authentication", null);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(response.getWriter(), customErrorResponse);
		} else {
			// Handle other authentication errors
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType("application/json");
			ApiResponse customErrorResponse = new ApiResponse(false, "Unauthorized: " + authException.getMessage(),
					null);

			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.writeValue(response.getWriter(), customErrorResponse);
		}
	}
}
