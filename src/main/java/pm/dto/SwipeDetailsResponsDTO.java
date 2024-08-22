package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SwipeDetailsResponsDTO {
    private String employeeID;
    private String username;
    private String email;
    private String date;
    private String signIn;
    private String signOut;
    private String totalWorkHours;
    private String actualWorkHours;
    private String shortFallHours;
    private String excessHours;
    private LocalTime timesheetHours;


    @Override
    public String toString() {
        return "SwipeDetailsResponsDTO{" +
                "employeeID='" + employeeID + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", date='" + date + '\'' +
                ", signIn='" + signIn + '\'' +
                ", signOut='" + signOut + '\'' +
                ", totalWorkHours='" + totalWorkHours + '\'' +
                ", actualWorkHours='" + actualWorkHours + '\'' +
                ", shortFallHours='" + shortFallHours + '\'' +
                ", excessHours='" + excessHours + '\'' +
                ", timesheetHours=" + timesheetHours +
                '}';
    }
}
