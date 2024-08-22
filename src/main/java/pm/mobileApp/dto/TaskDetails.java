package pm.mobileApp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
// @AllArgsConstructor
@NoArgsConstructor
public class TaskDetails {

    private String taskName;
    private String hours;
    private String description;
    private String approverRemarks;

    public TaskDetails(String taskName, String hours, String description, String approverRemarks) {
        this.taskName = taskName;
        this.hours = hours;
        this.description = description;
        this.approverRemarks = approverRemarks;
    }

}
