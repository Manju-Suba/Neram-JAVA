package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardResponse {
    private Long contractMemberCount;
    private Long supervisorCount;
    private List<DashBoardName> usernames;

}
