package pm.dto;

import java.time.LocalDate;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import pm.response.OwnerDetails;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductListDtoOwner {
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
	private String bussinessCategory;
	private List<MemberDTO> member;
}
