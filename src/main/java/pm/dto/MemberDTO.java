package pm.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberDTO {

    private Integer id;

    private String name;

    private Integer userId;

    private String role;

    private String profile_pic;

    private String branch;
    private String status;
}
