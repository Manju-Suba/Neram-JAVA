package pm.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pm.model.product.Product;
import pm.model.task.Task;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductTaskDTO {
    private Product product;
    private List<Task> task;
}
