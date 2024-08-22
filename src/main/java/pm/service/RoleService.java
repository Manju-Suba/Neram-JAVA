package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.model.users.Roles;

@Service
public interface RoleService {

    ResponseEntity<?> list(int page, int size, boolean search, String value);

    ResponseEntity<?> create(String name);

    ResponseEntity<?> byId(int id);

    ResponseEntity<?> update(String roles, int id);

    ResponseEntity<?> deleteRole(List<Integer> id);

    ResponseEntity<?> allList();

}
