package pm.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeSheetData {

    private int totalCount;
    private int activeCount;
    private int inactiveCount;
    private List<String> activeMembers;
    private List<String> inactiveMembers;

}
