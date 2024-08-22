package pm.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.task.CommonTimeSheetActivity;
import pm.payload.TimeSheetPayload;

import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class CommonTaskActivityRequest {
    private List<TimeSheetPayload> commonTimeSheetActivities;
    private String status;

}
