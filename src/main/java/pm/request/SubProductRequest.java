package pm.request;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import pm.model.product.SubProduct;

@Data
public class SubProductRequest {
    // private int prodId;
    // private String subName;
    // private int categoryId;
    // private LocalDate startDate;
    // private LocalDate endDate;
    // private MultipartFile file;
    private List<SubProduct> subProducts;
}
