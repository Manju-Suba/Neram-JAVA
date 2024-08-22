package pm.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.users.Users;
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AddMemberRequest {
    private int member;
    private String role;
    private int prodId;
}
