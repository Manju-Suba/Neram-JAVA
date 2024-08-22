package pm.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Data;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProductStatus;
import pm.model.task.TaskCategory;
import pm.model.users.Roles;

@Data
public class TaskDTO {

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

    private int prodId;

    private List<MemberDTO> assignedTo;

    private String createdBy;
    private TaskCategory taskCategory;

    private int flow;

    private String flowName;

    private String name;

    private int categoryID;

    private String categoryName;

    private LocalDate startDate;

    private int Prod_headId;
    private int tech_headId;
    private String prod_name;
    private String tech_name;
    private String prodOwner;
    private String techOwner;
    private String prodOwnerName;
    private String techOwnerName;
    private LocalDate endDate;

    private String summary;

    private String projectfile;

    private String budget;

    private String currency;

    private ProductStatus projectstatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isDeleted;

    private EProductApproStatus approvalStatus;

    private String approvalremarks;

    private String approvalby;

    private Roles approvalRole;

    private List<UserDTO> approvalFlow;

    private List<MemberDTO> member;
}
