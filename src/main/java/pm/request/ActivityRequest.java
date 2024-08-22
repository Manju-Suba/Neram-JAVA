package pm.request;

import java.util.List;

import lombok.Data;

@Data
public class ActivityRequest {
    private String status;
    private List<Integer> id;
    private String remarks;
    private String reason;

}
