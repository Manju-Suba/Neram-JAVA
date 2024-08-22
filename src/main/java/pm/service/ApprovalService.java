package pm.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.model.product.Product;
import pm.request.ApprovalRequest;

@Service
public interface ApprovalService {

    ResponseEntity<?> getProductList(int page, int size, boolean search, int vaule, String status);

    ResponseEntity<?> approveProduct(List<Integer> id, ApprovalRequest approvalRequest);

    ResponseEntity<?> getRejectProduct(int id);

    ResponseEntity<?> updateProductAndHistory(int id, Product product, MultipartFile file);

    ResponseEntity<?> getApprovalDate(LocalDate date);

    ResponseEntity<?> getApprovalDateSunday(LocalDate date);

    ResponseEntity<?> getProductListSearch(String status);

}
