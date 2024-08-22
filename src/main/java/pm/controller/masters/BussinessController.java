package pm.controller.masters;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import pm.model.product.BussinessCategory;
import pm.service.BussinessService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/master/bussiness")
@CrossOrigin("*")
@RestController
// @PreAuthorize("hasAnyAuthority('Admin')")
public class BussinessController {

    @Autowired
    private BussinessService bussinessService;

    @Operation(summary = "Create a new business category")
    // @PreAuthorize("hasAnyAuthority('Admin','Internal Admin')")
    @PostMapping("/create")
    public ResponseEntity<?> create(String name) {
        return bussinessService.create(name);
    }

    @Operation(summary = "Get the list of business categories")
    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search,
            @RequestParam(required = false) String value) {
        return bussinessService.list(page, size, search, value);
    }

    @Operation(summary = "Get a business category by ID")
    @GetMapping("/byid/{id}")
    public ResponseEntity<?> byId(@PathVariable int id) {
        return bussinessService.byId(id);
    }

    @Operation(summary = "Update a business category by ID")
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable int id, String name) {
        return bussinessService.update(name, id);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTheBussinessCategory(@PathVariable List<Integer> id) {
        return bussinessService.deleteTheBussinessCategory(id);
    }

}
