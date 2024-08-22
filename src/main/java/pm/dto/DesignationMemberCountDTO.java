package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DesignationMemberCountDTO {
    private Long totalCount;
    private Long employeeCount;
    private Long approverCount;
    private Long ownerCount;
    private Long headCount;
}
