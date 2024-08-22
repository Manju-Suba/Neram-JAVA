package pm.mobileAppDto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class MembersActivityResponse {
    private String username;
    private String prodname;
    private int id;
    private LocalDate activeDate;
    private LocalDateTime createdOn;
    private String description;
    private String hours;
    private String task;
    private String supervisorStatus;
    private String finalApproveStatus;
    // private String status;

    public MembersActivityResponse(String username, int id) {
        this.username = username;
        this.id = id;
    }

    public MembersActivityResponse(String username, int id, String prodname, LocalDate activeDate,
            LocalDateTime createdOn, String description, String hours, String task, String supervisorStatus) {
        this.username = username;
        this.id = id;
        this.prodname = prodname;
        this.activeDate = activeDate;
        this.createdOn = createdOn;
        this.description = description;
        this.hours = hours;
        this.task = task;
        this.supervisorStatus = supervisorStatus;
    }

    public MembersActivityResponse(String username, int id, String prodname, LocalDate activeDate,
            LocalDateTime createdOn, String description, String hours, String task, String supervisorStatus,
            String finalApproveStatus) {
        this.username = username;
        this.id = id;
        this.prodname = prodname;
        this.activeDate = activeDate;
        this.createdOn = createdOn;
        this.description = description;
        this.hours = hours;
        this.task = task;
        this.supervisorStatus = supervisorStatus;
        this.finalApproveStatus = finalApproveStatus;
    }
}
