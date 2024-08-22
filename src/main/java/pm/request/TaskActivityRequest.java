package pm.request;

import java.util.List;

import lombok.Data;
import pm.model.task.TaskActivity;

@Data
public class TaskActivityRequest {
    private List<TaskActivity> taskActivities;

}
