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
public class MyteamFilterDto {
    private LocalDate fromDate;
    private LocalDate toDate;
    private int userId;

}
