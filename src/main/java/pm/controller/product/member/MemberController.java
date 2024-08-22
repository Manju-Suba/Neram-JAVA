package pm.controller.product.member;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestParam;
import pm.request.MemberRequest;
import pm.request.UpdateMemberRequest;
import pm.service.MemberService;

@RequestMapping("/member")
@CrossOrigin("*")
@RestController
public class MemberController {

	@Autowired
	private MemberService memberService;

	@Operation(summary = "Get product list for assignment")
	@PreAuthorize("hasAnyAuthority('Owner')")
	@GetMapping("/product-list")
	public ResponseEntity<?> productList(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search, @RequestParam(required = false) int value) {
		return memberService.getProductList(page, size, search, value);
	}

	@Operation(summary = "Add Team Members for Product  ")
	@PreAuthorize("hasAnyAuthority('Owner')")
	@PostMapping("/create")
	public ResponseEntity<?> create(MemberRequest memberRequest) {
		return memberService.create(memberRequest);
	}

	@Operation(summary = "View a member by  Product ID")
	@GetMapping("/view/{id}")
	public ResponseEntity<?> view(@PathVariable int id) {
		return memberService.view(id);
	}

	@Operation(summary = "Update a member by Product ID")
	@PreAuthorize("hasAnyAuthority('Owner')")
	@PutMapping("/update/{id}")
	public ResponseEntity<?> update(@PathVariable int id, MemberRequest memberRequest) {
		return memberService.update(id, memberRequest);
	}

	@Operation(summary = "Get the list of users")
	@PreAuthorize("hasAnyAuthority('Owner')")
	@GetMapping("/user/list")
	public ResponseEntity<?> userList() {
		return memberService.userList();
	}

	@Operation(summary = "Get the list of users by list of ids  ")
	@GetMapping("/user/listbyid/{ids}")
	public ResponseEntity<?> userList(@PathVariable(required = false) List<Integer> ids) {
		return memberService.userListid(ids);
	}

	@Operation(summary = "Get the list of users count of team ", hidden = true)
	@GetMapping("/count")
	public ResponseEntity<?> memberdataCount() {
		return memberService.memberdataCount(); 
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/user/list/{branch}/{id}")
	public ResponseEntity<?> userListbybranch(@PathVariable String branch, @PathVariable Integer id) {
		return memberService.userListbybranch(branch, id);
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/user/list/{branch}")
	public ResponseEntity<?> allUserListbybranch(@PathVariable String branch) {
		return memberService.allUserListbybranch(branch);
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/user/list/show/{branch}/{id}")
	public ResponseEntity<?> allUserListbybranchandproduct(@PathVariable String branch, @PathVariable Integer id) {
		return memberService.allUserListbybranchandid(branch, id);
	}

}