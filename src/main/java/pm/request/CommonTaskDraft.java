package pm.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.product.Product;
import pm.model.users.Users;

@AllArgsConstructor
@NoArgsConstructor
@Data

public class CommonTaskDraft {
    private Integer id;
    private LocalDate activity_date;
    private String hours;
    private String description;
    private String status;
    private String task;

    private int product;
}
