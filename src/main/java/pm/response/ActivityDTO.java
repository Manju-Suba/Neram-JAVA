package pm.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class ActivityDTO {
    private Integer id;
    private LocalDateTime activity_date;
    private String hours;
    private String description;
    private String status;
    private boolean draft;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private boolean is_deleted;

    private Integer userId;
    private String userName; // Add other user-related fields as needed

    private Integer taskId;
    private String taskName; // Add other task-related fields as needed

    private Integer productId;
    private String productName; // Add other product-related fields as needed

    // Constructors, getters, setters
}
