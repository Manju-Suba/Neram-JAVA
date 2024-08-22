package pm.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Member {

    private  int id;
    private String  name;
    private String username;
    private String profile_pic;
    private String designation;
}
