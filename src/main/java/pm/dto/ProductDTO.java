package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProductStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductDTO {
	private Integer id;

	private int flow;

	private String flowName;

	private String name;

	private int categoryID;

	private String categoryName;

	private LocalDate startDate;

	private int Prod_headId;
	private int tech_headId;
	private int data_headId;
	private int how_headId;
	private String prod_name;
	private String tech_name;
	private String data_name;
	private String how_name;
	private String prodOwner;
	private String techOwner;
	private String dataOwner;
	private String howOwner;
	private List<String> prodOwnerName;
	private List<String> techOwnerName;
	private List<String> dataOwnerName;
	private List<String> howOwnerName;
	private LocalDate endDate;

	private String summary;

	private String file;

	private String budget;

	private String currency;

	private ProductStatus status;

	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private Boolean isDeleted;

	private EProductApproStatus approvalStatus;

	private String approvalremarks;

	private String approvalby;

	private String approvalRole;

	private List<UserDTO> approvalFlow;

	private List<MemberDTO> member;
	private int createdBy;

}
