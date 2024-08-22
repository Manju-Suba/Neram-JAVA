package pm.model.activityrequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.EProductApproStatus;

@Entity
@Getter
@Data
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "activity_request")
public class ActivityRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "user_id")
    private Integer userId;
    @Column(name = "sended_to")
    private int sendedTo;
    @Column(name = "request_date")
    private LocalDate requestDate;
    @Enumerated(EnumType.STRING)
    private EProductApproStatus status;
    @Column(name = "created_at")
    private LocalDateTime createdat;
    @Column(name = "updated_at")
    private LocalDateTime updatedat;
    @Column(name = "is_deleted")
    private boolean isdeleted;
    private String remarks;
    private String reason;
}
