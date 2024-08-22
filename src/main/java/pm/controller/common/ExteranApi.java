package pm.controller.common;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pm.serviceImplements.SwipeInSwipeOut;

import java.time.LocalDate;
import java.util.List;
@CrossOrigin("*")
@RestController
public class ExteranApi {

    private final SwipeInSwipeOut swipeInSwipeOut;


    @Autowired
    public ExteranApi(SwipeInSwipeOut swipeInSwipeOut) {
        this.swipeInSwipeOut = swipeInSwipeOut;
    }

    // @Operation(summary = "Budgie API List Configuration")
    // @GetMapping("/fetchData")
    // public ResponseEntity<ResponseWrapper> fetchDataFromExternalApi(
    //         @RequestParam String paramName,
    //         @RequestParam String paramValue) {

    //     ResponseEntity<ResponseWrapper> response = swipeInSwipeOut.callExternalApi(paramName, paramValue);
    //     return response;
    // }

    @Operation(summary = "Supervisor Based Members List")
    @GetMapping("/fetchDataList")
    public ResponseEntity<?> fetchDataListFromExternalApi(
            @RequestParam @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate date, @RequestParam(required = false) List<String> empIds, @RequestParam(value = "checkShortfallHours", defaultValue = "false") boolean checkShortfallHours, @RequestParam(required = false, defaultValue = "false") boolean excessHours, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return swipeInSwipeOut.membersBasedSwipeInOut(date, empIds, checkShortfallHours, excessHours, page, size); //memebersBasedSwipeInOut

    }

    @Operation(summary = "Paricular Members List")
    @GetMapping("/userbased")
    public ResponseEntity<?> fetchMonthBasedActivity(@RequestParam(required=false) @DateTimeFormat(pattern = "MM/dd/yyyy") LocalDate date,@RequestParam(required = false, value = "month") String month, @RequestParam(required = false, value = "year") String year, @RequestParam(value = "checkShortfallHours", defaultValue = "false") boolean checkShortfallHours, @RequestParam(value = "excesshours", defaultValue = "false") boolean excesshours, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return swipeInSwipeOut.monthBasedSwipeInOut(date,month, year, checkShortfallHours, excesshours, page, size);
    }

//    @Scheduled(cron = "0 30 8 * * *")
////    @GetMapping("/cron")
//    public ResponseEntity<ResponseWrapper> cronApi(
//    ) {
//        ResponseEntity<ResponseWrapper> response = swipeInSwipeOut.cronApi();
//        return response;
//    }


}
