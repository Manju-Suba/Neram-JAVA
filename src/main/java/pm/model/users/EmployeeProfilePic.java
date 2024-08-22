package pm.model.users;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@ToString
@Builder
@Table(name = "profilepic")
public class EmployeeProfilePic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Correct strategy for MySQL
    private Long id;  // Change to Long to match typical usage with auto-increment fields
    @Column(name = "empid", nullable = false)
    private String empid;
    private String profilePic;
}
