package pm.response;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.dto.UserDTO;
import pm.model.task.Task;
import pm.model.task.TaskCategory;
import pm.model.users.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
	 private Integer id;
	    private String task_name;
	    private LocalDate start_date;
	    private LocalDate end_date;
	    private String description;
	    private String priority;

	    private String file;
	    private String status;
	    private LocalDateTime created_at;
	    private LocalDateTime updated_at;
	    private boolean is_deleted;

	 
	    private Integer prodId;

	    
	    private List<Integer> assignedTo;


	    private Integer createdBy;

	  
	    private TaskCategory taskCategory;
	    private List<UserDTO> users;
}
