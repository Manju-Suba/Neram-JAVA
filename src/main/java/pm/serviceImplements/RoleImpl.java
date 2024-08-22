package pm.serviceImplements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.RolesRepository;
import pm.response.ApiResponse;
import pm.service.RoleService;

@Service
public class RoleImpl implements RoleService {

    @Autowired
    private RolesRepository rolesRepository;

    @Override
    public ResponseEntity<?> list(int page, int size, boolean search, String value) {

        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        Page<Roles> roles = null;
        if (search) {
            roles = rolesRepository.findRolesByNameRegex(value, pageable);

        } else {

            roles = rolesRepository.getActiveRolesWithPage(pageable);
        }

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "Designation fetched successfully", roles));
    }

    @Override
    public ResponseEntity<?> create(String name) {
        LocalDateTime currDateTime = LocalDateTime.now();
        // Validate the name field using the regular expression
        // if (!name.matches("^[a-zA-Z ]+$")) {
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        // .body(new ApiResponse(false, "Name must contain only alphabetic letters",
        // null));
        // }
        // if (name == null) {
        // return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        // .body(new ApiResponse(false, "Name cannot be null", null));
        // }
        // Check for blank name
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name cannot be blank", null));
        }
        if (name.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name exceeds maximum length of 255 characters", null));
        }
        name = name.trim();
        String trimmedName = name.replaceAll("\\s+", " ");
        Integer result = rolesRepository.existsByRoleNameAndIs_deletedFalse(trimmedName);
        boolean exists = result != null && result == 1;
        if (exists) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body(new ApiResponse(false, "Designation is already exist ", Collections.emptyList()));
        }
        Roles roles = new Roles();
        roles.setName(name);
        roles.setCreated_at(currDateTime);
        roles.setUpdated_at(currDateTime);
        roles.set_deleted(false);

        roles.setStatus(true);
        roles = rolesRepository.save(roles);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "Designation create successfully", roles));
    }

    @Override
    public ResponseEntity<?> byId(int id) {
        // Roles roles = rolesRepository.findById(id).orElse(null);
        Optional<Roles> roles = rolesRepository.FindByActiveRoleById(id);
        if (roles.isEmpty()) {
            String message = "The Designation is Not Found For the Given Id : " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, message, null));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "Designation fetched successfully", roles));
    }

    @Override
    public ResponseEntity<?> update(String roles, int id) {
        Roles role = rolesRepository.FindByActiveRoleById(id).orElse(null);
        if (role == null) {
            String message = "The Designation is Not Found For the Given Id : " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, message, null));
        }

        String trimmedName = roles.replaceAll("\\s+", " ");
        if (!role.getName().trim().equals(roles.trim())) { // Trim both strings before comparison
            Optional<Roles> roleByRoleName = rolesRepository.findByRoleNameAndIs_deletedFalse(trimmedName); // Trim
                                                                                                            // newrole
                                                                                                            // before
                                                                                                            // querying
                                                                                                            // the
                                                                                                            // database
            if (roleByRoleName.isPresent() && roleByRoleName.get().getId() != id) {
                String message = "Designation is already exists.";
                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new ApiResponse(false, message, null));
            }
        }
        if (roles.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name exceeds maximum length of 255 characters", null));
        }

        LocalDateTime currDateTime = LocalDateTime.now();
        role.setName(roles);
        role.setCreated_at(currDateTime);
        role.setUpdated_at(currDateTime);
        role.set_deleted(false);
        role.setStatus(true);
        role = rolesRepository.save(role);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "Designation Updated successfully", role));
    }

    @Override
    public ResponseEntity<?> deleteRole(List<Integer> id) {
        rolesRepository.updateIsDelete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Designation Deleted Succesfully", Collections.emptyList()));
    }

    @Override
    public ResponseEntity<?> allList() {
        List<Roles> roles = rolesRepository.getActiveRoles();
        roles.sort(Comparator.comparingInt(Roles::getId).reversed());
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "Designation fetched successfully", roles));
    }

}
