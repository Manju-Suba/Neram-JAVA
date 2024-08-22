package pm.mobileAppDto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MembersActivityUserDto {
    private String username;
    private int userid;

    public MembersActivityUserDto(String username, int userid) {
        this.username = username;
        this.userid = userid;
    }

    public MembersActivityUserDto(String username) {
        this.username = username;
    }
}
