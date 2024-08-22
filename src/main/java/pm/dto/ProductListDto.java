package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProductStatus;
import pm.response.OwnerDetails;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductListDto {
private int id;
private String productName;
private String flowName;
private OwnerDetails prodHead;
private OwnerDetails techHead;
private LocalDate startDate;
private LocalDate endDate;
private List<OwnerDetails> approvalFlow;
private String budgetDetails;
private String currency;
private List<OwnerDetails> technicalOwners;
private List<OwnerDetails> productOwners;
private String fileName;
private String bussinessCategory;
private EProductApproStatus approvalStatus;
@Enumerated(EnumType.STRING)
private ProductStatus status;
private String summary;
private List<MemberDTO> flowstatus;
}
