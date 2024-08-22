package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.model.users.EmployeeProfilePic;
import pm.model.users.Users;
import pm.request.UserCreateRequest;

@Service
public interface EmployeeService {

    ResponseEntity<?> create(UserCreateRequest user, MultipartFile file);

    ResponseEntity<?> list(int page, int size, boolean search, String value);

    ResponseEntity<?> getListByBranch(String branch);

    ResponseEntity<?> view(int id);

    ResponseEntity<?> update(UserCreateRequest user, MultipartFile file, int id);

    ResponseEntity<?> updateUserStatus(int id, Boolean status);

    ResponseEntity<?> deleteaUser(List<Integer> id);

    ResponseEntity<?> updateSupervisor();

    ResponseEntity<?> updateJOD();

    EmployeeProfilePic getProfilePic(String userId);
}
