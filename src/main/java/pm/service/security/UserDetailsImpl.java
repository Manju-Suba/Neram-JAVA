package pm.service.security;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import pm.model.users.Roles;
import pm.model.users.Users;

public class UserDetailsImpl implements UserDetails {
	private static final long serialVersionUID = 1L;

	private final int id;
	private final String username;
	@JsonIgnore
	private final String password;
	private final String name;
	private final String profile_pic;
	private final String designation;
	private final Set<Roles> role;
	private final String roleIntake;
	private final String email;
	private final String branch;
	private final LocalDate jod;
	private final Collection<? extends GrantedAuthority> authorities;

	public UserDetailsImpl(int id, String username, String password, String name, String profile_pic,
			String designation, Set<Roles> role, String roleIntake, String email, String branch,
			LocalDate jod,
			Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.name = name;
		this.profile_pic = profile_pic;
		this.designation = designation;
		this.role = role;
		this.roleIntake = roleIntake;
		this.email = email;
		this.branch = branch;
		this.jod = jod;
		this.authorities = authorities;
	}

	public static UserDetailsImpl build(Users user) {
		String designation = user.getDesignation();
		String roleid = mapDesignationToRole(designation);

		List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(roleid));
		// System.out.println(authorities);
		return new UserDetailsImpl(user.getId(), user.getUsername(), user.getPassword(), user.getName(),
				user.getProfile_pic(), user.getDesignation(), user.getRole_id(), user.getRoleType(), user.getEmail(),
				user.getBranch(),
				user.getJod(),
				authorities);
	}

	private static String mapDesignationToRole(String designation) {
		// Your logic to map designation to a role (authority)
		// For example, you can have a simple mapping or more complex logic here
		// This is just a placeholder, replace it with your actual logic
		if ("Admin".equalsIgnoreCase(designation)) {
			return "Admin";
		} else if ("Head".equalsIgnoreCase(designation)) {
			return "Head";
		} else if ("Owner".equalsIgnoreCase(designation)) {
			return "Owner";
		} else if ("Approver".equalsIgnoreCase(designation)) {
			return "Approver";
		} else if ("Internal Admin".equalsIgnoreCase(designation)) {
			return "Internal Admin";
		} else {
			return "Employee";
		}
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getProfile_pic() {
		return profile_pic;
	}

	public LocalDate getJod() {
		return jod;
	}

	public String getRoleIntake() {
		return roleIntake;
	}

	public String getDesignation() {
		return designation;
	}

	public String getBranch() {
		return branch;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UserDetailsImpl user = (UserDetailsImpl) o;
		return Objects.equals(id, user.id);
	}

	public String getEmail() {
		// TODO Auto-generated method stub
		return email;
	}

	public Set<Roles> getRole_id() {
		return role;
	}
}
