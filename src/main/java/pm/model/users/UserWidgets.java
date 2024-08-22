package pm.model.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_widgets")
public class UserWidgets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String emp_id;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Role must contain only alphabetic letters")
    private String role;
    private String widget_count;
    private String widget_table;
    private String remaining_widget;
    private String remaining_widget_count;
    private String remaining_widget_table;
    private String total_widget;
    private String updated_by;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @PrePersist
    protected void onCreate() {
        created_at = LocalDateTime.now();
        updated_at = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updated_at = LocalDateTime.now();
    }

}
