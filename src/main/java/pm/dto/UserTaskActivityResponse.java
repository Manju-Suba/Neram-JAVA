package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserTaskActivityResponse {

	private Integer id;
	private LocalDate activity_date;
	private String hours;
	private String description;
	private String status;
	private boolean draft;
	private LocalDateTime created_at;
	private LocalDateTime updated_at;
	private boolean is_deleted;
	private String userName; // from Users entity
	private String taskName; // from Task entity
	private String productName; // from Product entity
	private int pro_id;
	private String finalApproveStatus; // from Product entity
	private String branch;
	private boolean approved;
	private String approvedStatus;
	private String ownerStatus;
	private Boolean assignedStatus;

	public UserTaskActivityResponse(Integer id, LocalDate activity_date, String hours, String description,
			String status, boolean draft, LocalDateTime created_at, LocalDateTime updated_at, boolean is_deleted,
			String userName, String taskName, String productName, String finalApproveStatus, String branch,
			boolean approved, String approvedStatus) {
		super();
		this.id = id;
		this.activity_date = activity_date;
		this.hours = hours;
		this.description = description;
		this.status = status;
		this.draft = draft;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.is_deleted = is_deleted;
		this.userName = userName;
		this.taskName = taskName;
		this.productName = productName;
		this.finalApproveStatus = finalApproveStatus;
		this.branch = branch;
		this.approved = approved;
		this.approvedStatus = approvedStatus;
	}

	public UserTaskActivityResponse(Integer id, LocalDate activity_date, String hours, String description,
			String status, boolean draft, LocalDateTime created_at, LocalDateTime updated_at, boolean is_deleted,
			String userName, String taskName, String productName, String finalApproveStatus, String branch,
			boolean approved, String approvedStatus, String ownerStatus) {
		this.id = id;
		this.activity_date = activity_date;
		this.hours = hours;
		this.description = description;
		this.status = status;
		this.draft = draft;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.is_deleted = is_deleted;
		this.userName = userName;
		this.taskName = taskName;
		this.productName = productName;
		this.finalApproveStatus = finalApproveStatus;
		this.branch = branch;
		this.approved = approved;
		this.approvedStatus = approvedStatus;
		this.ownerStatus = ownerStatus;
	}

	public UserTaskActivityResponse(Integer id, LocalDate activity_date, String hours, String description,
			String status, boolean draft, LocalDateTime created_at, LocalDateTime updated_at, boolean is_deleted,
			String userName, String taskName, String productName, int pro_id, String finalApproveStatus, String branch,
			boolean approved, String approvedStatus, String ownerStatus, Boolean assignedStatus) {
		this.id = id;
		this.activity_date = activity_date;
		this.hours = hours;
		this.description = description;
		this.status = status;
		this.draft = draft;
		this.created_at = created_at;
		this.updated_at = updated_at;
		this.is_deleted = is_deleted;
		this.userName = userName;
		this.taskName = taskName;
		this.productName = productName;
		this.pro_id = pro_id;
		this.finalApproveStatus = finalApproveStatus;
		this.branch = branch;
		this.approved = approved;
		this.approvedStatus = approvedStatus;
		this.ownerStatus = ownerStatus;
		this.assignedStatus = assignedStatus;
	}

}
