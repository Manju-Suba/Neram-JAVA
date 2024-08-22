package pm.model.task;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "task_categories")
public class TaskCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
	//@Pattern(regexp = "^[a-zA-Z ]+$", message = "GroupName must contain only alphabetic letters")
    @Column(name = "group_name")
    private String groupName;
    
    @Column(name = "category")
    private String category;

    @Column(name = "status")
    private boolean status;
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted;
    
    
//    public void setCategories(String[] categories) {
//        this.category = categories;
//    }
//
//    public String[] getCategories() {
//        return this.category.split(",");
//    }
}
