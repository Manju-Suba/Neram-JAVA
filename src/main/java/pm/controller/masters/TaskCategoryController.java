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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pm.model.task.TaskCategory;
import pm.service.TaskCategoryService;
import io.swagger.v3.oas.annotations.Operation;

@RequestMapping("/master/taskcategory")
@CrossOrigin("*")
@RestController
public class TaskCategoryController {
    @Autowired
    private TaskCategoryService taskCategoryService;

    @Operation(summary = "Create a new task category")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PostMapping("/create")
    public ResponseEntity<?> create(String groupName, String category) {
        return taskCategoryService.create(groupName, category);
    }

    @Operation(summary = "Get the list of task categories")
    @GetMapping("/list")
    // @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search,
            @RequestParam(required = false) String value) {
        return taskCategoryService.list(page, size, search, value);
    }

    @Operation(summary = "Get a task category by ID")
    @GetMapping("/byid/{id}")
    public ResponseEntity<?> byId(@PathVariable int id) {
        return taskCategoryService.byId(id);
    }

    @Operation(summary = "Update a task category by ID")
    @PutMapping("/update/{id}")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> update(@PathVariable int id, TaskCategory taskCategory) {
        return taskCategoryService.update(taskCategory, id);
    }

    @Operation(summary = "Map designation to a task category")
    @PostMapping("/map/designation")
    public ResponseEntity<?> mapDesignation(@RequestParam int groupId, @RequestParam String roleId) {
        return taskCategoryService.groupMappingDesignation(groupId, roleId);
    }

    @Operation(summary = "Get roles associated with a task category", hidden = true)
    @GetMapping("/roles/{id}")
    public ResponseEntity<?> roles(@PathVariable int id) {
        return taskCategoryService.getCategoriesroles(id);
    }

    @Operation(summary = "Get all task categories based on Designiation map")
    @GetMapping("/get/taskcategory")
    public ResponseEntity<?> getTaskCategory() {
        return taskCategoryService.getTaskCategory();

    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('Admin')")
    public ResponseEntity<?> deleteTheTaskCategory(@PathVariable List<Integer> id) {
        return taskCategoryService.DeleteTheTaskCategory(id);
    }

}
