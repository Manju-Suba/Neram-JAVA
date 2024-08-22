package pm.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pm.model.flow.Flow;
import pm.model.product.Product;
import pm.model.product.ProductStatus;
import pm.model.product.BussinessCategory;
import pm.model.users.Users;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductResponse {
    private Product product;
    private Integer taskCount;
    private String role;

}
