package pm.mobileAppDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RaisedRequestResponse {
    private int id;
    private String userName;
    private LocalDate requestDate;
    private String reason;
    private String status;

}
