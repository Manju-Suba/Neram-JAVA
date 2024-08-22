package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SupervisorResponse {
    private LocalDate activitydate;
    private String name;
    private String hours;
}
