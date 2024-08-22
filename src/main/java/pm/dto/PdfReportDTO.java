package pm.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PdfReportDTO {

    private String Name;
    private String email;
    private LocalDate Date_of_join;
    private String Day;
    private String AttendanceStatus;
    private String Total_work_hours;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class ProductDetailsDto {
        private String Product_Name;
        private LocalDate activity_date;
        private String Task;
        private String Description;
        private String Task_Status;
        private String Work_hour;
    }

}
