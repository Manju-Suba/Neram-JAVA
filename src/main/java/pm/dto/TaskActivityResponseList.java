package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskActivityResponseList {
     private Integer id;
    private LocalDate activity_date;
    private String hours;
    private String description;
    private String status;
    private boolean draft;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private boolean is_deleted;
    private String userName; // from Users entity
    private String taskName; // from Task entity
    private String productName; // from Product entity
    private int pro_id;// from Product entity
    private String finalApproveStatus; // from Product entity
    private String branch;
    private String finalApproverRemarks;
    private Boolean assignedStatus;
}
