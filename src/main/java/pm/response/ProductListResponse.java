package pm.response;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.ProductStatus;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
@Builder
public class ProductListResponse {

    private int id;
    private String productName;
    private String flowName;
    private List<OwnerDetails> prodHead;
    private List<OwnerDetails> techHead;
    private List<OwnerDetails> dataHead;
    private List<OwnerDetails> howHead;

    private LocalDate startDate;
    private LocalDate endDate;
    private String budgetDetails;
    private String currency;
    private List<OwnerDetails> technicalOwners;
    private List<OwnerDetails> productOwners;
    private List<OwnerDetails> dataOwners;
    private List<OwnerDetails> howOwners;

    private String fileName;
    private String bussinessCategory;
    private ProductStatus status;
    private String summary;
    private String approvalStatus;
    private List<OwnerDetails> approvalFlow;

}
