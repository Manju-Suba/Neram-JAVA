package pm.response;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponsePageable {
    private boolean status;
    private String message;

    private Object data;
    private long totalElements;
    private int totalPages;
    private int currentPage;

    public ApiResponsePageable(boolean status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }
}
