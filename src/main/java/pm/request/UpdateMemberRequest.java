package pm.request;

import lombok.Data;
import pm.model.member.Member;

import java.util.List;
@Data
public class UpdateMemberRequest {
    private List<Member> members;

}
