package pm.mobileApp.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
// @AllArgsConstructor
@NoArgsConstructor
public class productActivityDto {

    private String productName;

    private List<TaskDetails> taskDetails;

    public productActivityDto(String productName, List<TaskDetails> taskDetails) {
        this.productName = productName;
        this.taskDetails = taskDetails;
    }


}