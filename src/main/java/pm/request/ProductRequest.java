package pm.request;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import pm.model.product.ProductStatus;
import pm.model.users.Users;

@Data
public class ProductRequest {
    private String files;
    private int flow;
    private Users prodHead;
    private Users techHead;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String summary;
    private String budget;
    private String currency;
    private int prodOwner;
    private int createdBy;
    private int techOwner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;
    private ProductStatus status;
    private MultipartFile file;
}
