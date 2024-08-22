package pm.controller.product;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import pm.request.ProductAssignRequest;
import pm.request.ProductCreateRequest;
import pm.service.ProductService;

@RequestMapping("/product")
@CrossOrigin("*")
@RestController
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Get all Bussiness categories", hidden = true)
    // @PreAuthorize("hasAnyAuthority('Head', 'Owner')")
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategory() {
        return productService.getAllCategory();
    }

    @Operation(summary = "Create a new product")
    // @PreAuthorize("hasAnyAuthority('Head')")
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(ProductCreateRequest productCreateRequest,
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String option,
            @RequestPart(value = "files", required = false) MultipartFile file) {
        return productService.createProduct(productCreateRequest, option, file, key);
    }

    @Operation(summary = "Get  All created product list with page")
    @GetMapping("/list")
    // @PreAuthorize("hasAnyAuthority('Head')")
    public ResponseEntity<?> getCreatedProductList(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam boolean search,
            @RequestParam(required = false) int value, @RequestParam String status,
            @RequestParam(required = false) String key) {
        return productService.getCreatedProductList(page, size, search, value, status, key);
    }

    @Operation(summary = "Get  All created product list by status")
    @GetMapping("/head/search")
    public ResponseEntity<?> getProductList(@RequestParam String status) {
        return productService.getProductList(status);
    }

    @Operation(summary = "Get approved product list !================================For Mobile========================")
    @GetMapping("/approvedlist")
    public ResponseEntity<?> getApprovedList(@RequestParam(required = false) String status,
            @RequestParam(required = false) String filter) {
        return productService.getApprovedList(status, filter);
    }

    @Operation(summary = "Get draft product list")
    @GetMapping("/draft")
    public ResponseEntity<?> getDraftList() {
        return productService.getDraftList();
    }

    @Operation(summary = "Get product by ID")
    @GetMapping("/view/{id}")
    public ResponseEntity<?> getProduct(@PathVariable int id) {
        return productService.getProduct(id);
    }

    @Operation(summary = "Get product by Name")
    @GetMapping("/filter/{name}")
    public ResponseEntity<?> filterProductList(@PathVariable String name) {
        return productService.filterProductList(name);
    }

    @Operation(summary = "Get product by owners drop down")
    @GetMapping("owner/serach")
    public ResponseEntity<?> getProductNameList() {
        return productService.getProductNameList();
    }

    @Operation(summary = "Assign A Product to Owner")
    @PreAuthorize("hasAnyAuthority('Head')")
    @PutMapping("/assign/{id}")
    public ResponseEntity<?> assignProduct(ProductAssignRequest product, @RequestParam(required = true) String owner,
            @PathVariable int id) {
        return productService.assignProduct(id, product, owner);
    }

    @Operation(summary = "Update The Product by Id")
//    @PreAuthorize("hasAnyAuthority('Head','Internal Admin')")
    @PutMapping(value = "/update/{id}/{option}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(@PathVariable int id, ProductCreateRequest productCreateRequest,
            @RequestPart(value = "files", required = false) MultipartFile file,
            @PathVariable(required = false) String option,@RequestParam(required = false) String key) {
        return productService.updateProduct(id, productCreateRequest, file, option,key);
    }

    // @GetMapping("/getallProducts")
    // public ResponseEntity<?> getAllProducts() {
    // try {
    // return productService.getAllProducts();
    // } catch (Exception e) {
    // return
    // ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    // }
    // }
    @Operation(summary = "Get product by ID", hidden = true)
    @GetMapping("/byid/{id}")
    public ResponseEntity<?> getAllProductsById(@PathVariable int id) {
        try {
            return productService.getAllProductsById(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Get product List By Status For Approver Login (Approved,Pending,Rejected) ")
    @GetMapping("/list/{status}")
    public ResponseEntity<?> listProductsByStatus(@PathVariable String status) {
        return productService.listProductsByStatus(status);
    }

    @Operation(summary = "Get product by ID", hidden = true)
    @GetMapping("/list1/{status}")
    public ResponseEntity<?> listProductsByStatusa(@PathVariable String status) {
        return productService.listProductsByStatusOriginal(status);
    }

    @Operation(summary = "Get product by ID", hidden = true)
    @GetMapping("/getproductbytask/{id}")
    public ResponseEntity<?> getProductwithtask(@PathVariable int id) {
        try {
            return productService.getProductwithtask(id);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //
    @Operation(summary = "Get product by ID", hidden = true)
    @GetMapping("/checkProductUser/{id}")
    public ResponseEntity<?> checkProductUserExists(@PathVariable int id) {
        return productService.checkProductUserExists(id);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('Head')")
    public ResponseEntity<?> deleteTheProduct(@PathVariable List<Integer> id) {
        return productService.deleteaProduct(id);
    }

}
