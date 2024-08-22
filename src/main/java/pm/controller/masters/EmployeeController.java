package pm.controller.masters;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import pm.model.task.TimeLimit;
import pm.model.users.EmployeeProfilePic;
import pm.model.users.Users;
import pm.repository.ActivityRequestRepository;
import pm.repository.TimeSheetTimeControl;
import pm.request.UserCreateRequest;
import pm.response.ApiResponse;
import pm.service.EmployeeService;

@RequestMapping("/master/employee")
@CrossOrigin("*")
@RestController
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private TimeSheetTimeControl timeSheetTimeControl;
    @Autowired
    private ActivityRequestRepository activityRequestRepository;

    @Operation(summary = "Create a new user")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            UserCreateRequest user,
            @RequestPart(value = "files", required = false) MultipartFile files) {
        return employeeService.create(user, files);
    }

    @Operation(summary = "Get the list of users")
    @GetMapping("/list")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search,
            @RequestParam(required = false) String value) {
        return employeeService.list(page, size, search, value);
    }

    @Operation(summary = "Get the list of users by branch")
    @GetMapping("list/{branch}")
    public ResponseEntity<?> getListByBranch(@PathVariable String branch) {
        return employeeService.getListByBranch(branch);
    }

    @Operation(summary = "View a user by ID")
    @GetMapping("view/{id}")
    public ResponseEntity<?> view(@PathVariable int id) {
        return employeeService.view(id);
    }

    @Operation(summary = "Update a user by ID")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(UserCreateRequest user,
            @RequestPart(value = "files", required = false) MultipartFile file,
            @PathVariable int id) {
        return employeeService.update(user, file, id);
    }

    @Operation(summary = "Update user status by ID")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PutMapping("/update/{id}/status/{status}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @PathVariable Boolean status) {
        return employeeService.updateUserStatus(id, status);
    }

    @Operation(summary = "Delete a User ")
    @DeleteMapping("/delete/user/{id}")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    public ResponseEntity<?> deleteaUser(@PathVariable List<Integer> id) {
        return employeeService.deleteaUser(id);
    }

    @Operation(summary = "Update user status by ID", hidden = true)
    @PostMapping("/timesheet/timecontrol")
    public ResponseEntity<?> insert(@RequestParam LocalTime hours) {
        TimeLimit limit = new TimeLimit();
        limit.setHour(hours);
        limit.setCreatedat(LocalDateTime.now());
        limit.setStatus("Active");
        limit = timeSheetTimeControl.save(limit);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Time Sheet Time Changed", limit));
    }

//    @Operation(summary = "!!!!!!!!! Rstricted API !!!!!!!  Update user ", hidden = false)
//    @PutMapping("/timesheet/sipervisor/update")
//    public ResponseEntity<?> update() {
//        return employeeService.updateSupervisor();
//    }

    @Operation(summary = "Get Profile pic", hidden = false)
    @GetMapping("/profilepic")
    public ResponseEntity<?> getRaisedRequestCount(@RequestParam String empid) {

        EmployeeProfilePic employeeProfilePic= employeeService.getProfilePic(empid);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Profile pic", employeeProfilePic));
    }

}
