package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RaisedRequestApproval {
    private int id;
    private String userName;
    private String status;
    private LocalDate activity_date;
    private String description;
}
