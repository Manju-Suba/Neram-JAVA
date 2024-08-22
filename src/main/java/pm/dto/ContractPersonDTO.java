package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractPersonDTO {

	// Contract person Constructor
	public ContractPersonDTO(int id, String name, LocalDateTime created_at, String branch, String supervisor_name,
			String commonTimeSheet) {
		this.id = id;
		this.name = name;
		this.date_of_joining = created_at;
		this.branch = branch;
		this.supervisor_name = supervisor_name;
		this.hours = commonTimeSheet;
	}

	// Supervisor constructor
	public ContractPersonDTO(int id, String name, String supervisorname, LocalDateTime created_at, String branch,
			String commonTimeSheet) {
		this.id = id;
		this.name = name;
		this.supervisor_name = supervisorname;
		this.date_of_joining = created_at;
		this.branch = branch;
		this.hours = commonTimeSheet;
	}

	private int id;
	private String name;
	private String supervisor_name;
	private String branch;
	private String final_Approve = "Not yet";
	private LocalDateTime date_of_joining;
	private String hours;
	private LocalDate activityDate;
	private String supervisorStatus;

	public ContractPersonDTO(int id, String name, String supervisorname, LocalDateTime created_at, String branch,
			String commonTimeSheet, String finalApproved) {
		this.id = id;
		this.name = name;
		this.supervisor_name = supervisorname;
		this.date_of_joining = created_at;
		this.branch = branch;
		this.hours = commonTimeSheet;
		this.final_Approve = finalApproved;
	}

	public ContractPersonDTO(int id, String name, String supervisor, LocalDateTime created_at, String branch,
			String commonTimeSheet, String finalApproved, LocalDate activityDate,String supervisorStatus) {
		this.id = id;
		this.name = name;
		this.supervisor_name = supervisor;
		this.date_of_joining = created_at;
		this.branch = branch;
		this.hours = commonTimeSheet;
		this.final_Approve = finalApproved;
		this.activityDate = activityDate;
		this.supervisorStatus = supervisorStatus;
	}
}
