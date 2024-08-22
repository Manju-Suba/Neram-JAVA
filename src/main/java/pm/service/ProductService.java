package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.model.product.Product;
import pm.request.ProductAssignRequest;
import pm.request.ProductCreateRequest;
import pm.response.ApiResponse;

@Service
public interface ProductService {

    ResponseEntity<?> getAllCategory();

    ResponseEntity<?> getCreatedProductList(int page, int size, boolean search, int vaule, String status,String key);

    ResponseEntity<?> getDraftList();

    ResponseEntity<?> createProduct(ProductCreateRequest productCreateRequest, String option, MultipartFile file,String key);

    ResponseEntity<?> getProduct(int id);

    ResponseEntity<?> filterProductList(String name);

    ResponseEntity<?> getProductNameList();

    ResponseEntity<?> assignProduct(int id, ProductAssignRequest product, String owner);

    ResponseEntity<ApiResponse> getProduct();

    ResponseEntity<?> updateProduct(int id, ProductCreateRequest productCreateRequest, MultipartFile file, String option,String key);

    ResponseEntity<?> getAllProducts();

    ResponseEntity<?> getAllProductsById(int id);

    ResponseEntity<?> listProductsByStatus(String status);

    ResponseEntity<?> getProductwithtask(int id);

    ResponseEntity<?> getAllProductsByAssigned();

    ResponseEntity<?> getApprovedList(String status,String filter);

    ResponseEntity<?> listProductsByStatusOriginal(String status);

    ResponseEntity<?> checkProductUserExists(int id);

    ResponseEntity<?> deleteaProduct(List<Integer> id);

    ResponseEntity<?> getProductList(String status);
}
