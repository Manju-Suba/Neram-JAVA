package pm.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TaskApprovalResonse {
    private int id;
    private String supervisor_approved;
    private LocalDate activity_date;
    private String hours;
    private String description;
    private String status;
    private boolean draft;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    private String approval_user_name;
    private String task_user_name;
    private int user_id;
    private String taskName;
    private String productName;
    private int pro_id;
    private String final_approve;
    private Boolean assignedStatus;
}
