package pm.model.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Users")
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private int id;
	@NotBlank
	private String username;
	@NotBlank
	private String password;
	private String email;
	@NotBlank
	private String name;
	@NotBlank
	@Pattern(regexp = ".*\\.(jpg|jpeg|png|pdf|docx)$")
	private String profile_pic;
	private String designation;
	private String roleType;

	private String supervisor;
	private String branch;
	private String finalApprove;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "users_roles", joinColumns = @JoinColumn(name = "users_id"), inverseJoinColumns = @JoinColumn(name = "roles_id"))

	private Set<Roles> role_id = new HashSet<>();
	private boolean status;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	private boolean is_deleted;

	private LocalDate jod;

	public void setAuthorities(List<GrantedAuthority> authorities) {
	}

	public boolean get_deleted() {
		return false;
	}

	public boolean getStatus() {
		return false;
	}

}
