package pm.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSheetPayload {
    private LocalDate activity_date;
    private Integer product;
    private String hours;
    private String task;
    private String description;
    private String status;

}
