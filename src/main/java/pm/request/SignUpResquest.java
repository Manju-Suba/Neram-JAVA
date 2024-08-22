package pm.request;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignUpResquest {

	private String username;
	private String password;
	private Set<String> roles_id;
	private boolean status;

}
