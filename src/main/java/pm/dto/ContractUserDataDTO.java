package pm.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractUserDataDTO {

    private String name;

    private String email;

    private LocalDate doj;

    private String day;

    private String attendanceStatus;
    private LocalDate date;

    private String Total_work_hours;

    private String finalApprove;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class commonTimeSheetRecord {

        private String username;
        private String productName;

        private LocalDate activity_date;

        private String task;

        private String hours;

        private String userstatus;

        private String description;

        private String approveStatus;

        private String remarks;
        private String finalApprove;
        private String approverName;
        private String finalName;

    }

}
