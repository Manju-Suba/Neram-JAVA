package pm.request;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class RaisedRequest {
    private String requestDate;
    private int userId;
    private String remarks;
    private String reason;
    private String status;

}
