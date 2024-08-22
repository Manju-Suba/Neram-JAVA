package pm.model.taskapproval;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.task.TaskActivity;
import pm.model.users.Users;

@Entity
@Getter
@Setter

@AllArgsConstructor
@NoArgsConstructor
@Table(name = "team_leader_approval")
public class TeamLeaderApproval {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long approvalId;

//	@ManyToOne
//	@JoinColumn(name = "activity_id")
//	private TaskActivity taskActivity;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private Users users;
	@ManyToOne
	@JoinColumn(name = "team_leader_id")
	private Users teamLeader;

	private String status; // 'Approved', 'Rejected', etc.
	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "approved_at")
	private LocalDate apprrovedAt;
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@Column(name = "is_deleted")
	private Boolean isDeleted = false;
}
