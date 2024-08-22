package pm.controller.common;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import pm.service.UserService;

@RequestMapping("/user")
@CrossOrigin("*")
@RestController
public class UserCommonController {
	@Autowired
	private UserService userService;

	@Operation(summary = "Get the list of users",hidden = false)
	@GetMapping("/list")
	public ResponseEntity<?> getAllUsersDetails() {
		return userService.userList();
	}
	@Operation(summary = "Get the list of users who have Designiation Approver",hidden = true)
	@GetMapping("/approval-user/list")
	public ResponseEntity<?> getAllApprovalUsers() {
		return userService.approvalUserList();
	}
	
	@Operation(summary = "Get the list of users who have Designiation Head",hidden = true)
	 @GetMapping("/approval-user/listid/{ids}")
	    public ResponseEntity<?> getApprovalUsersByIds(@PathVariable(required = false) List<Integer> ids) {
	    	return userService.approvalUserList(ids);
		}

	@Operation(summary = "Get the list of Roles",hidden = true)
	@GetMapping("/roles")
	public ResponseEntity<?> getRoles() {
		return userService.getRoles();
	}

	@Operation(summary = "Get the list of users",hidden = true)
	@GetMapping("/list/user")
	public ResponseEntity<?> getUser() {
		return userService.getUser();
	}
}
