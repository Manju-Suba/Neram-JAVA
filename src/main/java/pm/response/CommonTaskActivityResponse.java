package pm.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonTaskActivityResponse {
//    public CommonTaskActivityResponse(Integer integer, LocalDate localDate, String string, String string2,
//            String string3, boolean b, LocalDateTime localDateTime, LocalDateTime localDateTime2, boolean c,
//            String string4, String string5, String string6, String string7, String string8, boolean d,
//            String supervisorStatus) {
//    }
//
//    public CommonTaskActivityResponse(Integer integer, LocalDate localDate, String string, String string2,
//            String string3, boolean b, LocalDateTime localDateTime, LocalDateTime localDateTime2, boolean c,
//            String string4, String string5, String string6, String string7, String string8, boolean d) {
//    }

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
    private int productId;
    private String finalApproveStatus; // from Product entity
    private String branch;
    private boolean approved;
    private String supervisorStatus;

    public CommonTaskActivityResponse(Integer id, LocalDate activity_date, String hours, String description, String status, boolean draft, LocalDateTime created_at, LocalDateTime updated_at, boolean is_deleted, String userName, String taskName, String productName, String finalApproveStatus, String branch, boolean approved, String supervisorStatus) {
        this.id = id;
        this.activity_date = activity_date;
        this.hours = hours;
        this.description = description;
        this.status = status;
        this.draft = draft;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.is_deleted = is_deleted;
        this.userName = userName;
        this.taskName = taskName;
        this.productName = productName;
        this.finalApproveStatus = finalApproveStatus;
        this.branch = branch;
        this.approved = approved;
        this.supervisorStatus = supervisorStatus;
    }
}
