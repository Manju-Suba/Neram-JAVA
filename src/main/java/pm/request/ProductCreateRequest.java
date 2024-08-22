package pm.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProductCreateRequest {
    @NotNull(message = "Id must be not null")
    private Integer flow;
    @NotNull(message = "name must be not null")
    @NotBlank(message = "name must be not blank")
    private String name;
    private Integer prodHead;
    private Integer howHead;
    private String prodHeadValue;
    private Integer techHead;
    private String techHeadValue;
    private Integer dataHead;
    private String dataHeadValue;
    @NotNull(message = "category must be not null")
    private Integer category;
    private LocalDate startDate;
    // private LocalDate startDateValue;
    private LocalDate endDate;
    // private LocalDate endDateValue;
    // @NotNull(message = "summary must be not null")
    // @NotBlank(message = "summary must be not blank")
    private String summary;
    private String budget;
    private String currency;
    private List<Integer> prodOwner;
    private List<Integer> techOwner;
    private List<Integer> dataOwner;
    private List<Integer> howOwner;
}
