package pm.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class JwtResponse {
	private String name;
	private String token;
	private String employee_id;
	private int id;
	private String profile_pic;
	private String designation;
	private String branch;
	private Set<String> role;
	private String roleIntake;
	private LocalDate createdAt;
	private String superviser = "false";
	private String finalApprover = "false";

	public JwtResponse(String name, String jwt, String employee_id, int id, String profile_pic, String designation,
			String branch, List<String> roles, String roleIntake, LocalDate createdAt) {
		this.name = name;
		this.token = jwt;
		this.employee_id = employee_id;
		this.id = id;
		this.profile_pic = profile_pic;
		this.designation = designation;
		this.branch = branch;
		this.role = new HashSet<>(roles);
		this.roleIntake = roleIntake;
		this.createdAt = createdAt;

	}

	public JwtResponse(String name, String token, String employee_id, int id, String profile_pic, String designation,
			String branch, List<String> role, String roleIntake, LocalDate createdAt, String superviser,
			String finalApprover) {
		this.name = name;
		this.token = token;
		this.employee_id = employee_id;
		this.id = id;
		this.profile_pic = profile_pic;
		this.designation = designation;
		this.branch = branch;
		this.role = new HashSet<>(role);
		this.roleIntake = roleIntake;
		this.createdAt = createdAt;
		this.superviser = superviser;
		this.finalApprover = finalApprover;
	}
}
