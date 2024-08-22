package pm.response;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskCategoryResponse {

    private Integer id;
    private String groupName;
    private String[] categories;

    private boolean status;
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
    private Integer[] designations;
    private String roleIds;

    private boolean isDeleted;

}
