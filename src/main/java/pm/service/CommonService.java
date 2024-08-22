package pm.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pm.model.product.Product;
import pm.response.OwnerDetails;

public interface CommonService {

    Page<Product> getProductListByOwner(Pageable pageable, boolean search, int value);

    Page<Product> getProductListByHead(Pageable pageable, boolean search, int value, String status,String key);

    OwnerDetails getUserByIdWithProduct(int userId, int productId);

    OwnerDetails getUserById(int userId);

    List<Object[]> getAllProductListByHead(String status);

    public void updateCommonTaskActivity();
}
