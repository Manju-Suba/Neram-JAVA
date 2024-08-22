package pm.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.dto.EmployeeAttendance;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
@Data
public class ResponseWrapper {
    private String keyLabel;
    private String valueLabel;
    private Map<LocalDate, List<EmployeeAttendance>> data;


    public ResponseWrapper() {
    }

    public ResponseWrapper(String keyLabel, String valueLabel, Map<LocalDate, List<EmployeeAttendance>> data) {
        this.keyLabel = keyLabel;
        this.valueLabel = valueLabel;
        this.data = data;
    }

    public String getKeyLabel() {
        return keyLabel;
    }

    public void setKeyLabel(String keyLabel) {
        this.keyLabel = keyLabel;
    }

    public String getValueLabel() {
        return valueLabel;
    }

    public void setValueLabel(String valueLabel) {
        this.valueLabel = valueLabel;
    }

    public Map<LocalDate, List<EmployeeAttendance>> getData() {
        return data;
    }

    public void setData(Map<LocalDate, List<EmployeeAttendance>> data) {
        this.data = data;
    }
}
