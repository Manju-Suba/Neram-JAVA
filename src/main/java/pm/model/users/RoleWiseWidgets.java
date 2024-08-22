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
@Table(name = "role_wise_widgets")
public class RoleWiseWidgets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Role must contain only alphabetic letters")
    private String role;
    private String widget_count;
    private String widget_table;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
