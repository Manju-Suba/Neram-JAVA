package pm.model.task;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

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

import pm.model.product.Product;
import pm.model.users.Users;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "task_activities")
public class TaskActivity {
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
    private String finalApprove;
    private String branch;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "task_id", referencedColumnName = "id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "prod_id", referencedColumnName = "id")
    private Product product;

    private boolean is_approved = false;
}
