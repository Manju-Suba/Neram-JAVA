package pm.request;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.users.Roles;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateRequest {

	@NotBlank(message = "Username is required")
	@NotNull(message = "Username must not be null")
	private String username;
	@Email(message = "Email should be valid")
	@NotBlank(message = "Email is required")
	@NotNull(message = "Email must not be null")
	private String email;
	@NotBlank(message = "Name is required")
	@NotNull(message = "Name must not be null")
	private String name;
	// private String profile_pic;
	@NotNull(message = "Desgination must not be null")
	private String designation;
	@NotNull(message = "roleType must not be null")
	private String roleType;

	private String supervisor;
	@NotNull(message = "Branch must not be null")
	private String branch;
	private String finalApprove;
	@NotNull(message = "role must not be null")
	private Integer role_id;
	// @NotNull(message = "Joining date is required")
	private LocalDate jod;

}
