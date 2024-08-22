package pm.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserDetailsDTO {

    public UserDetailsDTO(int id, String username, String name, String profile_pic,
            String role, LocalDateTime created_at, LocalDateTime updated_at,
            Boolean status, Boolean is_deleted) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.profile_pic = profile_pic;
        this.role = role;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.status = status;
        this.is_deleted = is_deleted;
    }

    private int id;
    private String username;
    private String name;
    private String profile_pic;
    private Boolean status;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    private Boolean is_deleted;
    private String role;

    public void getStatus(boolean status2) {
    }

}
