package pm.request;

import java.util.List;

import lombok.Data;
import pm.model.member.Member;

@Data
public class MemberRequest {
    private List<AddMemberRequest> members;

}
