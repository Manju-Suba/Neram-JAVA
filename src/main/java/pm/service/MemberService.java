package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.request.MemberRequest;
import pm.request.UpdateMemberRequest;

@Service
public interface MemberService {

    ResponseEntity<?> getProductList(int page, int size, boolean search, int value);

    ResponseEntity<?> create(MemberRequest memberRequest);

    ResponseEntity<?> view(int id);

    ResponseEntity<?> update(int id, MemberRequest memberRequest);

    ResponseEntity<?> userList();

    ResponseEntity<?> userListid(List<Integer> id);

    ResponseEntity<?> memberdataCount();

    ResponseEntity<?> userListbybranch(String branch, Integer id);

    ResponseEntity<?> allUserListbybranch(String branch);
    ResponseEntity<?> allUserListbybranchandid(String branch,Integer id);


}
