package pm.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class OwnerDetails {
	private int id;
	private String name;
	private String designation;
	private String profilePic;
	private String approvalStatus;
}
