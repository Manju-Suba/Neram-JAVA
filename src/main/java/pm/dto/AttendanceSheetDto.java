package pm.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@Data

@NoArgsConstructor
public class AttendanceSheetDto {
    private Date appliedDate;
    private String userName;
    private String status;

    public AttendanceSheetDto(Date appliedDate, String userName, String status) {
        this.appliedDate = appliedDate;
        this.userName = userName;
        this.status = status;
    }
}

