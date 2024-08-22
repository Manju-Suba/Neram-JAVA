package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmployeeAttendance {
    private String employeeID;
    private String username;
    private String email;
    private String date;
    private String signIn;
    private String signOut;
    private String totalWorkHours; // New field 1
    private String acutalWorkHours; // New field 2
    private String shortfallHours; // New field 1
    private String excessHours; // New field 2
    private String timesheetHours; // New field
    private boolean regularizationStatus;
    private boolean excess;
    private boolean shortfall;

//    public EmployeeAttendance(String employeeID, String username, String email, String date, String signIn, String signOut) {
//        this.employeeID = employeeID;
//        this.username = username;
//        this.email = email;
//        this.date = date;
//        this.signIn = signIn;
//        this.signOut = signOut;
//    }
}
