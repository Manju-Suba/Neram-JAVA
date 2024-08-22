package pm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SwipeDetailsRequest {
    private boolean status;
    private String message;
    private List<SwipeDetailsResponsDTO> result;
}
