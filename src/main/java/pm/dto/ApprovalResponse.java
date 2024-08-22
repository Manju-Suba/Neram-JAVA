package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.product.EProductApproStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApprovalResponse {
    private Long count;
    private EProductApproStatus status;
}
