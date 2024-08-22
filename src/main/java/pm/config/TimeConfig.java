package pm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
@Configuration
public class TimeConfig {
    @Value("${comparison.time}")
    private String comparisonTimeString;

    public LocalTime getComparisonTime() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return LocalTime.parse(comparisonTimeString, formatter);
    }
}
