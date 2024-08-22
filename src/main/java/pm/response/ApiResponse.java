package pm.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse {
	public ApiResponse(boolean b, String string) {
	}

	private boolean status;
	private String message;
	private Object data;

}
