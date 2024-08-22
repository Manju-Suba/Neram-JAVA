package pm.mobileApp.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AllReportDto {

    private LocalDate activityDate;

    List<MyTeamTimeSheetDto> recordForTheDate;



}
