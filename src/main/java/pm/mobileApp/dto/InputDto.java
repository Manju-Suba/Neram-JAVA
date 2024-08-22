package pm.mobileApp.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InputDto {

    private LocalDate fromDate;
    private LocalDate toDate;
    private String userName;

}
