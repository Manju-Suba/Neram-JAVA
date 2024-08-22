package pm.pdfGenerator;

import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.springframework.core.io.ClassPathResource;
import pm.dto.PdfReportDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class NewReport {
    public byte[] generatePdfDateRange(Map<String, Object> userdata, Map<LocalDate, List<Map<String, Object>>> data, LocalDate start, LocalDate end) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Prepare the data for the HTML template
            String htmlTemplate = loadHtmlTemplate("templates/pdfReport.html");
            String heading = getHeading(start, end);
            String dateRange = " (" + start + " - " + end + ")";
            // Add your logic to populate the HTML with data here

            // Replace placeholders in the HTML with actual data
            String populatedHtml = populateHtmlTemplate(htmlTemplate, heading, dateRange, userdata, data);

            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(populatedHtml);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String loadHtmlTemplate(String templatePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(templatePath);

        if (inputStream == null) {
            throw new FileNotFoundException("Template file not found in classpath: " + templatePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        }
    }

    private String getHeading(LocalDate start, LocalDate end) {
        String startMonth = start.format(DateTimeFormatter.ofPattern("MMMM"));
        String endMonth = end.format(DateTimeFormatter.ofPattern("MMMM"));
        if (startMonth.equals(endMonth)) {
            return "Timesheet details for " + startMonth;
        } else {
            return "Timesheet details for " + startMonth + " - " + endMonth;
        }
    }

    public String populateHtmlTemplate(String htmlTemplate, String heading, String dateRange, Map<String, Object> userdata, Map<LocalDate, List<Map<String, Object>>> data) {
        // Initialize a StringBuilder to build the HTML content
        String tickImageBase64 = "";
        String logoImageBase64 = "";
        try {
            tickImageBase64 = getImageAsBase64("static/assets/uploads/tick.png");
            logoImageBase64 = getImageAsBase64("static/assets/uploads/logo.png");
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder htmlContent = new StringBuilder(htmlTemplate);

        // Replace simple placeholders
        String populatedHtml = htmlContent.toString()
                .replace("${heading}", escapeHtml(heading))
                .replace("${dateRange}", escapeHtml(dateRange))
                .replace("${tickImage}", "data:image/png;base64," + escapeHtml(tickImageBase64))
                .replace("${logoImage}", "data:image/png;base64," + escapeHtml(logoImageBase64));

        // Replace employee details placeholders
        PdfReportDTO summaryDTO = (PdfReportDTO) userdata.get("UserDetail");
        if (summaryDTO != null) {
            populatedHtml = populatedHtml
                    .replace("${name}", escapeHtml(summaryDTO.getName()))
                    .replace("${email}", escapeHtml(summaryDTO.getEmail()))
                    .replace("${designation}", escapeHtml(summaryDTO.getDay()))
                    .replace("${attendanceStatus}", escapeHtml(summaryDTO.getAttendanceStatus()));
        }

        // Generate table rows for the data map
        StringBuilder dataRows = new StringBuilder();
        long index = 1;
        String totalHoursValue = "";
        Set<String> supervisorNames = new HashSet<>();
        Set<String> finalApprovalNames = new HashSet<>();
        for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : data.entrySet()) {
            List<Map<String, Object>> reportList = entry.getValue();
            boolean hasReport = false; // Flag to check if there are any report values

            for (Map<String, Object> report : reportList) {
                String productName = escapeHtml((String) report.get("name"));
                String hoursString = escapeHtml(report.get("hours").toString());
                String username = escapeHtml((String) report.get("username"));
                String taskName = escapeHtml((String) report.get("task"));
                Object taskHoursObj = report.get("taskHours");
                String supervisorName = escapeHtml((String) report.get("supervisorApproval"));
                String finalApprovalName = escapeHtml((String) report.get("finalapprovalname"));
                supervisorNames.add(supervisorName);
                finalApprovalNames.add(finalApprovalName);
                String totalHours;

                if (taskHoursObj instanceof java.sql.Time) {
                    java.sql.Time taskHoursTime = (java.sql.Time) taskHoursObj;
                    totalHours = taskHoursTime.toString(); // Converts to "HH:MM:SS" format
                } else {
                    totalHours = taskHoursObj.toString(); // Handle other types if necessary
                }

                totalHoursValue = totalHours;
                Date sqlDate = (Date) report.get("activityDate");
                LocalDate activityDate = sqlDate.toLocalDate();

                dataRows.append("<tr>")
                        .append("<td class='td'>").append(index).append("</td>")
                        .append("<td class='td'>").append(username).append("</td>")
                        .append("<td class='td'>").append(activityDate).append("</td>")
                        .append("<td class='td'>").append(productName).append("</td>")
                        .append("<td class='td'>").append(hoursString).append("</td>")
                        .append("<td class='td'>").append(taskName).append("</td>")
                        .append("</tr>");

                index++;
                hasReport = true; // Set the flag to true when there is at least one report
            }

            // Add the total hours row only if there are report values
            if (hasReport) {
                dataRows.append("<tr>")
                        .append("<td class='sub-total' colspan='5'>Sub Total Hours (per day)</td>")
                        .append("<td class='sub-total'>").append(totalHoursValue).append("</td>")
                        .append("</tr>");
            }
        }

        StringJoiner joiner = new StringJoiner(", ");
        for (String name : finalApprovalNames) {
            joiner.add(name);
        }
        String finalApprovalString = escapeHtml(joiner.toString());
        String supervisorNamesString = escapeHtml(String.join(", ", supervisorNames));

        populatedHtml = populatedHtml
                .replace("${supervisorNames}", supervisorNamesString)
                .replace("${finalApprovalNames}", finalApprovalString);

        // Replace the placeholder with actual data rows
        populatedHtml = populatedHtml.replace("${dataRows}", dataRows.toString());

        return populatedHtml;
    }


    public static String getImageAsBase64(String imagePath) throws IOException {
        ClassPathResource resource = new ClassPathResource(imagePath);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] imageBytes = inputStream.readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        }
    }

    private String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

}
