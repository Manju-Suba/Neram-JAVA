package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskActivityResponseStatus {
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
	private String finalApproveStatus; // from Product entity
	private String ownerStatus; // from Product entity
	private String branch;
	private boolean approved;
	private String approvedStatus;
	private String supervisorName;
	private String finalName;
	private String SupervisorStatus;
	private String roleType;

	public UserTaskActivityResponseStatus(Integer id, LocalDate activity_date, String status, String userName,
			String taskName, String productName, String branch, String hours) {
		super();
		this.id = id;
		this.activity_date = activity_date;
		this.status = status;
		this.userName = userName;
		this.taskName = taskName;
		this.productName = productName;
		this.branch = branch;
		this.hours = hours;
	}

	public UserTaskActivityResponseStatus(LocalDate activity_date, String userName, String branch, String status) {
		super();
		this.activity_date = activity_date;
		this.userName = userName;
		this.branch = branch;
		this.status = status;
	}

	public UserTaskActivityResponseStatus(Integer id, LocalDate activity_date, String username, String supervisorStatus,
			String branch, String status) {
		super();
		this.id = id;
		this.activity_date = activity_date;
		this.userName = username;
		this.SupervisorStatus = supervisorStatus;
		this.branch = branch;
		this.status = status;

	}

	public void setSupervisorName(String name) {
		this.supervisorName = name;

	}

	public UserTaskActivityResponseStatus(Integer id2, String username2, String branch2, String status2) {

		this.id = id2;
		this.userName = username2;
		this.branch = branch2;
		this.status = status2;

	}

	public void setHours(String hours) {
		this.hours = hours;
	}
}
