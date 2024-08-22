package pm.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskListDto {
    private int id;
    private String taskName;
    private String hours;
    private String description;
    private String status;
    private LocalDate activityDate;
}
