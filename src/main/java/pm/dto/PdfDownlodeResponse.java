package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PdfDownlodeResponse {
    private Object  name;
    private Object  hours;
    private Object  username;
    private Object  activitydate;
    private  Object taskname;
    private Object totalHours;
private Object supervisorname;
private String finalapprovaname;
    public PdfDownlodeResponse(Object hours) {
        this.hours = hours;
    }
}
