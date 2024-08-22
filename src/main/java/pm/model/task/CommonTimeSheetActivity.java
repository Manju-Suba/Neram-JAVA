package pm.model.task;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.Product;
import pm.model.users.Users;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "common_task_activities")
public class CommonTimeSheetActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private LocalDate activity_date;
    private String hours;
    private String description;
    private String status;
    private boolean draft;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private boolean is_deleted;
    private String task;
    private String finalApprove;
    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "prod_id", referencedColumnName = "id")
    private Product product;

    private boolean is_approved = false;
    private String branch;
    private String supervisorStatus;
    private boolean supervisorApproved;
    private boolean ownerApproved;
    private String ownerStatus;
    private String supervisor;
    private String contractStatus = null;

}
