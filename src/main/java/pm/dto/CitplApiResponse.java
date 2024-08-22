package pm.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CitplApiResponse {
	private String employeeID;
    private Date date;
    private String signIn;
    private String signOut;
}
