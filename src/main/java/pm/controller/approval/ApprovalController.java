package pm.controller.approval;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pm.model.product.Product;
import pm.request.ApprovalRequest;
import pm.service.ApprovalService;

@RequestMapping("/approval")
@CrossOrigin("*")
@RestController
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    @Operation(summary = "yesterday count", hidden = true)
    @GetMapping("/product/list")
    public ResponseEntity<?> getProductList(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size, @RequestParam(required = false) boolean search,
            @RequestParam(required = false) int value, @RequestParam String status) {
        return approvalService.getProductList(page, size, search, value, status);
    }

    @Operation(summary = "yesterday count", hidden = true)
    @GetMapping("/product/search")
    public ResponseEntity<?> getProductListSearch(@RequestParam String status) {
        return approvalService.getProductListSearch(status);
    }

    @Operation(summary = "Approve Product", hidden = true)
    @PutMapping("/product/approve/{id}")
    public ResponseEntity<?> approveProduct(@PathVariable List<Integer> id,
            @RequestBody ApprovalRequest approvalRequest) {
        return approvalService.approveProduct(id, approvalRequest);
    }

    @Operation(summary = "yesterday count", hidden = true)
    @GetMapping("/product/reject/{id}")
    public ResponseEntity<?> getRejectProduct(@PathVariable int id) {
        return approvalService.getRejectProduct(id);
    }

    @Operation(summary = "yesterday count", hidden = true)
    @PutMapping("/product/update/{id}")
    public ResponseEntity<?> updateProductAndHistory(@PathVariable int id, Product product,
            @RequestParam(value = "files", required = false) MultipartFile file) {
        return approvalService.updateProductAndHistory(id, product, file);
    }

    // yesterday
    @Operation(summary = "yesterday count", hidden = false)
    @GetMapping("/product/approvaldate/{date}")
    public ResponseEntity<?> getApprovalDate(@PathVariable LocalDate date) {
        // return approvalService.getApprovalDate(date);
        return approvalService.getApprovalDateSunday(date);
    }

}
