package pm.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardUpdateRequest {
    private String empId;
    private String replacementType;
    private String replaceFrom;
    private String replaceTo;
}
