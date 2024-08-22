package pm.security;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import pm.jwt.AuthEntryPointJwt;
import pm.jwt.AuthTokenFilter;
import pm.service.security.UserDetailsServiceImpl;

@Configuration
@EnableMethodSecurity

public class WebSecurityConfiguration {

	@Autowired
	private UserDetailsServiceImpl userDetailsService;

	@Autowired
	private AuthEntryPointJwt unauthorizedHandler;

	@Bean
	public AuthTokenFilter authenicationJwtTokenFilter() {
		return new AuthTokenFilter();
	}
	 @Autowired
	    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(List.of("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);

		return source;
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(userDetailsService);
		authProvider.setPasswordEncoder(passwordEncoder());

		return authProvider;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
		return authConfig.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	  public SecurityFilterChain filterChain(HttpSecurity http,HandlerMappingIntrospector introspector) throws Exception {
	    
        MvcRequestMatcher swaggerMatcher = new MvcRequestMatcher(introspector, "/swagger-ui/**");
        MvcRequestMatcher swagger2Matcher = new MvcRequestMatcher(introspector, "/v3/api-docs/**");
        MvcRequestMatcher swagger2Matcher1 = new MvcRequestMatcher(introspector, "/ui");
        MvcRequestMatcher swagger6Matcher = new MvcRequestMatcher(introspector, "/swagger-ui.html");
 
        MvcRequestMatcher webMatcher = new MvcRequestMatcher(introspector, "/web/home");
       http.csrf(csrf -> csrf.disable())
             .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
             .oauth2Login(oath2 -> {
                    oath2.loginPage("/login1").permitAll();
                    oath2.successHandler(oAuth2LoginSuccessHandler);
                })
//           .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
             .authorizeHttpRequests(
                   auth -> auth.requestMatchers("/auth/**", "/mail/service/**","/assets/uploads/**", "/uploads/**","/oauth2/**" ,"/**" ).permitAll()
                         .requestMatchers( "/login1",
                               "/oauth2/callback/outlook", "oauth2/v2.0/token").permitAll()
                         
                         .requestMatchers(swaggerMatcher).permitAll()
                         .requestMatchers(swagger2Matcher).permitAll()
                         .requestMatchers(swagger2Matcher1).permitAll()
                         .requestMatchers(swagger6Matcher).permitAll()
                         .anyRequest().authenticated());
       http.authenticationProvider(authenticationProvider());
       http.addFilterBefore(authenicationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
       http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
       return http.build();
    }

}
