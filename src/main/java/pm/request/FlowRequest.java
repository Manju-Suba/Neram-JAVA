package pm.request;


import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.dto.FlowCreateDto;
import pm.model.users.Users;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FlowRequest {
	
	@NotBlank
	@Pattern(regexp = "^[a-zA-Z]+$", message = "Name must contain only alphabetic letters")
	private String name;
	
	private List<FlowCreateDto> access_to;
	
	private List<FlowCreateDto>  approval_by;
//    private int created_by;
	
	
	
	

}
