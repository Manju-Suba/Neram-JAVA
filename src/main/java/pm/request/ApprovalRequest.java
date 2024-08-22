package pm.request;

import java.time.LocalDateTime;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import pm.model.product.EProductApproStatus;

@Data
public class ApprovalRequest {
    private String remarks;
    @Enumerated(EnumType.STRING)
    private EProductApproStatus status;
    private LocalDateTime updated_at;
}
