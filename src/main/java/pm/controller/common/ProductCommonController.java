package pm.controller.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import pm.service.ProductCommonService;

@RequestMapping("/common")
@CrossOrigin("*")
@RestController
public class ProductCommonController {
	@Autowired
	private ProductCommonService commonService;

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/prod_head")
	public ResponseEntity<?> getProductHead() {
		return commonService.getProductHead();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/tech_head")
	public ResponseEntity<?> getTechHead() {
		return commonService.getTechHead();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/data_head")
	public ResponseEntity<?> getDataHead() {
		return commonService.getDataHead();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/how_head")
	public ResponseEntity<?> getHowHead() {
		return commonService.getHowHead();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/prod_owner")
	public ResponseEntity<?> getProductOwner() {
		return commonService.getProductOwner();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/tech_owner")
	public ResponseEntity<?> getTechOwner() {
		return commonService.getTechOwner();
	}

	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/data_owner")
	public ResponseEntity<?> getDataOwner() {
		return commonService.getDataOwner();
	}


	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/how_owner")
	public ResponseEntity<?> gethowOwner() {
		return commonService.gethowOwner();
	}


	@Operation(summary = "Get the list of users", hidden = true)
	@GetMapping("/approflow/{id}")
	public ResponseEntity<?> approvalFlow(@PathVariable int id) {
		return commonService.approvalFlow(id);
	}
}
