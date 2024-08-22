package pm.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAssignRequest {
    private String prodOwner;
    private String techOwner;
    private String dataOwner;
}
