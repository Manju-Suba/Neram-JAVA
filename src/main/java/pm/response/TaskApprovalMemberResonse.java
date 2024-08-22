package pm.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class TaskApprovalMemberResonse {
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
    private String taskName;
    private String productName;
    private String final_approve;
    private Boolean assignedStatus;


    public TaskApprovalMemberResonse(int id, String supervisor_approved, LocalDate activity_date, String hours, String description, String status, boolean draft, LocalDateTime created_at, LocalDateTime updated_at, String approval_user_name, String task_user_name, String taskName, String productName, String final_approve, Boolean assignedStatus) {
        this.id = id;
        this.supervisor_approved = supervisor_approved;
        this.activity_date = activity_date;
        this.hours = hours;
        this.description = description;
        this.status = status;
        this.draft = draft;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.approval_user_name = approval_user_name;
        this.task_user_name = task_user_name;
        this.taskName = taskName;
        this.productName = productName;
        this.final_approve = final_approve;
        this.assignedStatus = assignedStatus;
    }
}
