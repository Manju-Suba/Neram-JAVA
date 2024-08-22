package pm.model.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.users.Users;

@Entity
@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String task_name;
    private LocalDate start_date;
    private LocalDate end_date;
    private String description;
    private String priority;
    @Pattern(regexp = ".*\\.(jpg|jpeg|png|pdf|docx)$")
    private String file;
    private String status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private boolean is_deleted;

    @Column(name = "prod_id")
    private Integer prodId;

    // @Column(columnDefinition = "JSON")
    // private List<String> assignedTo;
    private List<Integer> assignedTo;

    @JoinColumn(name = "created_by")
    private Integer createdBy;

    @ManyToOne
    @JoinColumn(name = "task_category")
    private TaskCategory taskCategory;
    private String branch;

}
