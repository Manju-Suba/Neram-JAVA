package pm.controller.masters;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestParam;
import pm.model.task.TaskCategory;
import pm.model.users.Roles;
import pm.service.RoleService;

@RequestMapping("/master/role")
@CrossOrigin("*")
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "Get the list of roles")
    @GetMapping("/list")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search,
            @RequestParam(required = false) String value) {
        return roleService.list(page, size, search, value);
    }

    @Operation(summary = "Create a new role")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PostMapping("/create")
    public ResponseEntity<?> create(String name) {
        return roleService.create(name);
    }

    @Operation(summary = "Get a role by ID")
    @GetMapping("/byid/{id}")
    public ResponseEntity<?> byId(@PathVariable int id) {
        return roleService.byId(id);
    }

    @Operation(summary = "Update a role by ID")
    @PutMapping("/update/{id}")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> update(@PathVariable int id, String name) {
        return roleService.update(name, id);
    }

    @DeleteMapping("/delete/{id}")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> deleteTheRole(@PathVariable List<Integer> id) {
        return roleService.deleteRole(id);
    }

    @Operation(summary = "Get the list of roles for dropdown")
    @GetMapping("/all-list")
    public ResponseEntity<?> allList() {
        return roleService.allList();
    }

}
