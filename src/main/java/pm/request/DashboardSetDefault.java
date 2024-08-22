package pm.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardSetDefault {
    private List<String> remainingWidget;
    private List<String> remainingWidgetCount;
    private List<String> remainingWidgetTable;
    private List<String> totalWidget;
    private List<String> widgetCount;
    private List<String> widgetTable;


    public void addDoubleQuotes() {
        this.remainingWidget = addQuotesToList(remainingWidget);
        this.remainingWidgetCount = addQuotesToList(remainingWidgetCount);
        this.remainingWidgetTable = addQuotesToList(remainingWidgetTable);
        this.totalWidget = addQuotesToList(totalWidget);
        this.widgetCount = addQuotesToList(widgetCount);
        this.widgetTable = addQuotesToList(widgetTable);
    }

    private List<String> addQuotesToList(List<String> list) {
        return list.stream()
                .map(s -> "\"" + s + "\"")
                .collect(Collectors.toList());
    }

}
