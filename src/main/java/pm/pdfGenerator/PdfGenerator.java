package pm.pdfGenerator;
//

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Component;
// import org.springframework.stereotype.Service;
//
// import com.itextpdf.kernel.pdf.PdfWriter;
//
// import jakarta.servlet.http.HttpServletResponse;
// import pm.dto.UserTaskActivityResponseStatus;
// import pm.model.users.Users;
// import pm.pdfInterface.PdfInterface;
// import pm.response.ApiResponse;
// import pm.service.MemberSupervisorService;
//
// import java.awt.Color;
// import java.io.FileNotFoundException;
// import java.io.IOException;
// import java.net.MalformedURLException;
// import java.text.SimpleDateFormat;
// import java.time.LocalDate;
// import java.util.Date;
// import com.itextpdf.kernel.pdf.PdfDocument;
// import com.itextpdf.kernel.pdf.PdfWriter;
// import com.itextpdf.layout.Document;
// import com.itextpdf.layout.element.Cell;
// import com.itextpdf.layout.element.Paragraph;
// import com.itextpdf.layout.element.Table;
// import com.itextpdf.layout.property.TextAlignment;
//
// import java.io.IOException;
// import java.util.List;
//

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import pm.dto.CommonResponse;
import pm.dto.PdfDownlodeResponse;
import pm.dto.PdfReportDTO;
import pm.dto.PdfReportDTO.ProductDetailsDto;
import pm.dto.UserTaskActivityResponseStatus;
import pm.model.users.Users;
import pm.pdfInterface.ContracterBasedPdf;
import pm.repository.AttendanceSheetRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.ProductRepository;
import pm.repository.TaskRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.service.MemberSupervisorService;

@Service
public class PdfGenerator implements ContracterBasedPdf {

    // @Autowired
    // private TaskRepository taskRepository;
    //
    @Autowired
    private MemberSupervisorService memberSupervisorService;

    @Autowired
    private CommonTimeSheetActivityRepository commontimeRepo;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;

    // =============================================pdf
    // -=============================================================
    @Override
    public ResponseEntity<?> gettheUserData(int id, LocalDate date) {
        try {
            Map<String, Object> userReportMap = new HashMap<>();
            Optional<Users> userdata = usersRepository.findById(id);
            String preset = attendanceSheetRepository.findStatusByUserId(id, date);

            DayOfWeek dayOfWeek = date.getDayOfWeek();
            PdfReportDTO basicDetail = new PdfReportDTO(userdata.get().getName(), userdata.get().getEmail(),
                    userdata.get().getCreated_at().toLocalDate(), dayOfWeek.toString(), preset, "");
            userReportMap.put("UserDetail", basicDetail);

            List<CommonResponse> resultList = new ArrayList<>();

            Set<Integer> productIds = new HashSet<>();
            List<Object[]> timesheetData = commontimeRepo.getUsersDataWithIdAndDatewithProd(id, date);
            for (Object[] commonData : timesheetData) {
                Integer ids = (Integer) commonData[2];
                productIds.add(ids);
            }

            for (Integer prodId : productIds) {
                List<Object[]> hours = commontimeRepo.getHoursbyUsersandProductidandDate(id, date, prodId);
                for (Object[] hourData : hours) {
                    CommonResponse commonResponse = new CommonResponse(hourData[1], hourData[2]);
                    resultList.add(commonResponse);
                }
            }

            String hours = commontimeRepo.findHoursByPersonIdAndActivityDate(id, date);
            String message = "Data Fetched Successfully.";

            // Create a list to hold the data of CommonResponse
            List<Map<String, Object>> commonResponseList = new ArrayList<>();
            for (CommonResponse response : resultList) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("name", response.getName());
                resultMap.put("hours", response.getHours());
                commonResponseList.add(resultMap);
            }

            byte[] pdfBytes = generatePdf(userReportMap, commonResponseList, date, hours);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, pdfBytes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error generating or processing PDF", null));
        }

    }

    // date range based list in user pdf file
    public ResponseEntity<?> userDataDateRange(Integer id, LocalDate fromdate, LocalDate todate) {
        Map<String, Object> userReportMap = new HashMap<>();
        Optional<Users> userdata = usersRepository.findById(id);
        List<String> roles = userdata.get().getRole_id().stream().map(role -> role.getName())
                .collect(Collectors.toList());
        PdfReportDTO basicDetail = new PdfReportDTO(userdata.get().getName(), userdata.get().getEmail(),
                userdata.get().getCreated_at().toLocalDate(),
                roles.get(0),
                userdata.get().getUsername(), "");
        userReportMap.put("UserDetail", basicDetail);
        List<LocalDate> dateRange = getDatesInRange(fromdate, todate);
        Map<LocalDate, List<Map<String, Object>>> allcommonResponseMap = new HashMap<>();

        // String hours = commontimeRepo.findHoursByPersonIdAndActivityDateRange(id,
        // fromdate, todate);
        for (LocalDate date : dateRange) {
            List<PdfDownlodeResponse> resultList = new ArrayList<>();
            Set<Integer> productIds = new HashSet<>();
            List<Object[]> timesheetData = commontimeRepo.getUsersDataWithIdAndDatewithProd(id, date);
            for (Object[] commonData : timesheetData) {
                Integer ids = (Integer) commonData[2];
                productIds.add(ids);
            }
            Object desiredValue = null;
            List<Object[]> timevalue = commontimeRepo.getHoursbyUsersandProductidandDatetotalHours(id, date);
            if (timevalue != null && !timevalue.isEmpty()) {
                Object[] timevalues = timevalue.get(0);
                if (timevalues != null && timevalues.length > 0) {
                    desiredValue = timevalues[0];
                }
            }
            for (Integer prodId : productIds) {

                List<Object[]> data = commontimeRepo.getHoursbyUsersandProductidandDaterangepdf(id, date, prodId);

                for (Object[] hourData : data) {
                    String finalapprovalname = "";
                    if (userdata.get().getRoleType().equalsIgnoreCase("On Role")) {
                        finalapprovalname = commontimeRepo.findUsernameByTaskActivityId((Integer) hourData[7]);

                    } else {
                        finalapprovalname = commontimeRepo.findFinalApproverByTaskActivityId((Integer) hourData[7]);

                    }
                    PdfDownlodeResponse commonResponse = new PdfDownlodeResponse(hourData[1], hourData[4], hourData[2],
                            hourData[3], hourData[5], desiredValue, hourData[6], finalapprovalname);
                    resultList.add(commonResponse);
                }
            }

            List<Map<String, Object>> commonResponseList = new ArrayList<>();
            for (PdfDownlodeResponse response : resultList) {
                System.out.println(response);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("name", response.getName());
                resultMap.put("hours", response.getHours());// taskname
                resultMap.put("username", response.getUsername());
                resultMap.put("activityDate", response.getActivitydate());
                resultMap.put("task", response.getTaskname());
                resultMap.put("taskHours", response.getTotalHours());
                resultMap.put("supervisorApproval", response.getSupervisorname());
                resultMap.put("finalapprovalname", response.getFinalapprovaname());
                commonResponseList.add(resultMap);
            }
            Collections.reverse(commonResponseList);

            allcommonResponseMap.put(date, commonResponseList);
        }
        // Convert the map to LinkedHashMap to preserve insertion order
        LinkedHashMap<LocalDate, List<Map<String, Object>>> reversedMap = new LinkedHashMap<>(allcommonResponseMap);

        // Create a new map to store the reversed data
        Map<LocalDate, List<Map<String, Object>>> reversedData = new LinkedHashMap<>();

        // Iterate over the entry set of the reversed map in reverse order
        List<Map.Entry<LocalDate, List<Map<String, Object>>>> entries = new ArrayList<>(reversedMap.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<LocalDate, List<Map<String, Object>>> entry = entries.get(i);
            reversedData.put(entry.getKey(), entry.getValue());
        }

        // Now reversedData contains the map with dates in reverse order
        // byte[] pdfBytes = generatePdfDAteRange(userReportMap, reversedData, fromdate,
        // todate);
        NewReport newReport = new NewReport();
        byte[] pdfBytes = newReport.generatePdfDateRange(userReportMap, reversedData, fromdate, todate);

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Data Fetched Successfully.", pdfBytes));

    }

    public ResponseEntity<?> userDataDateRangeUsers(List<Integer> ids, LocalDate fromdate, LocalDate todate) {
        Map<String, Object> userReportMap = new HashMap<>();
        Map<LocalDate, List<Map<String, Object>>> allCommonResponseMap = new HashMap<>();

        List<LocalDate> dateRange = getDatesInRange(fromdate, todate);

        // Iterate over each date in the date range
        for (LocalDate date : dateRange) {
            List<Map<String, Object>> allUserTasks = new ArrayList<>();

            // Iterate over each user ID
            for (Integer id : ids) {
                Users userData = usersRepository.findById(id).orElse(null);

                // Retrieve task details for the current user and date
                List<PdfDownlodeResponse> resultList = new ArrayList<>();
                Set<Integer> productIds = new HashSet<>();
                List<Object[]> timesheetData = commontimeRepo.getUsersDataWithIdAndDatewithProd(id, date);
                for (Object[] commonData : timesheetData) {
                    Integer prodId = (Integer) commonData[2];
                    productIds.add(prodId);
                }

                for (Integer prodId : productIds) {
                    List<Object[]> data = commontimeRepo.getHoursbyUsersandProductidandDaterangepdf(id, date, prodId);
                    for (Object[] hourData : data) {
                        PdfDownlodeResponse commonResponse = new PdfDownlodeResponse(hourData[1], hourData[4],
                                hourData[2], hourData[3], hourData[5], "", "", "");
                        resultList.add(commonResponse);
                    }
                }

                // Convert task details to the desired format
                List<Map<String, Object>> commonResponseList = new ArrayList<>();
                for (PdfDownlodeResponse response : resultList) {
                    System.out.println(response);
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("name", response.getName());
                    resultMap.put("task", response.getHours());
                    resultMap.put("username", response.getUsername());
                    resultMap.put("activityDate", response.getActivitydate());
                    resultMap.put("hours", response.getTaskname());

                    commonResponseList.add(resultMap);
                }
                Collections.reverse(commonResponseList);

                // Add task details of the current user to the list
                allUserTasks.addAll(commonResponseList);
            }

            // Add task details of all users for the current date to the map
            allCommonResponseMap.put(date, allUserTasks);
        }

        // Generate PDF with the accumulated data
        byte[] pdfBytes = generatePdfDAteRangewithusers(userReportMap, allCommonResponseMap, "");
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Data Fetched Successfully.", pdfBytes));
    }

    private List<LocalDate> getDatesInRange(LocalDate fromdate, LocalDate todate) {
        List<LocalDate> datesInRange = new ArrayList<>();
        LocalDate currentDate = fromdate;
        while (!currentDate.isAfter(todate)) {
            datesInRange.add(currentDate);
            if (currentDate.isEqual(todate)) {
                break; // Break the loop if the current date is equal to the end date
            }
            currentDate = currentDate.plusDays(1);
        }
        return datesInRange;
    }

    private class FooterPageEvent extends PdfPageEventHelper {
        private Image logo;

        public FooterPageEvent(Image logo) {
            this.logo = logo;
        }

        @Override
        // public void onEndPage(PdfWriter writer, Document document) {
        // PdfPTable footer = new PdfPTable(2);
        // try {
        // // Define the widths of the columns
        // float[] widths = new float[] { 1f, 4f };
        // footer.setWidths(widths);
        // footer.setTotalWidth(PageSize.A4.getWidth() - document.leftMargin() -
        // document.rightMargin());
        // footer.setLockedWidth(true);
        // footer.setHorizontalAlignment(Element.ALIGN_CENTER);

        // // Add logo to the first cell
        // PdfPCell logoCell = new PdfPCell(logo, false);
        // logoCell.setBorder(PdfPCell.NO_BORDER);
        // logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        // footer.addCell(logoCell);

        // // Add text to the second cell
        // PdfPCell textCell = new PdfPCell(new Phrase("Generated by Neram Team",
        // FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL)));
        // textCell.setBorder(PdfPCell.NO_BORDER);
        // textCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        // footer.addCell(textCell);

        // // Write the footer
        // footer.writeSelectedRows(0, -1, document.leftMargin(),
        // document.bottomMargin(),
        // writer.getDirectContent());
        // } catch (DocumentException e) {
        // e.printStackTrace();
        // }
        // }
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContent();
                // Load and scale the logo
                // Image logo = loadImageFromResources(
                // "static/assets/uploads/logo.png");
                // logo.scaleAbsolute(40, 30);
                // // Set the desired width and height
                // logo.setAbsolutePosition(document.leftMargin(), document.bottomMargin() +
                // 20);
                // // Position logo in the top-left corner
                // canvas.addImage(logo);
                // Add a centered text
                Font lightFont = FontFactory.getFont(FontFactory.HELVETICA, 10,
                        new BaseColor(150, 150, 150));
                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER,
                        new Phrase(
                                "Report Generated by Neram", lightFont),
                        (document.right() - document.left()) / 2 + document.leftMargin(), document.bottomMargin() - 15,
                        0);
                // Add a center line before the text
                canvas.setColorStroke(
                        new BaseColor(150, 150, 150));
                // Light color for the line
                canvas.moveTo(document.leftMargin(), document.bottomMargin() - 25);
                canvas.lineTo(PageSize.A4.getWidth() - document.rightMargin(), document.bottomMargin() - 25);
                canvas.stroke();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private Image loadImageFromResources(String path) throws IOException, BadElementException {
        // Load image from the resources directory
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(path);
        if (imageStream == null) {
            throw new IOException("Image not found: " + path);
        }
        return Image.getInstance(imageStream.readAllBytes());
    }

    private byte[] generatePdfDAteRange(Map<String, Object> userdata, Map<LocalDate, List<Map<String, Object>>> data,
            LocalDate start, LocalDate end) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            Image logo = loadImageFromResources("static/assets/uploads/logo.png");
            logo.scaleAbsolute(50, 40); // Set the desired width and height (e.g., 50x50)

            // Create the footer page event
            FooterPageEvent footerEvent = new FooterPageEvent(logo);
            writer.setPageEvent(footerEvent);
            document.open();

            // get month (April) from start date and end date
            String startMonth = start.format(DateTimeFormatter.ofPattern("MMMM"));
            String endMonth = end.format(DateTimeFormatter.ofPattern("MMMM"));
            String heading;
            if (startMonth.equals(endMonth)) {
                // set that in heading like Timesheet details for that month
                heading = "Timesheet details for " + startMonth;
            } else {
                heading = "Timesheet details for " + startMonth + " - " + endMonth;
            }

            String dateRange = " (" + start + " - " + end + ")";

            if (!userdata.isEmpty()) {
                PdfReportDTO summaryDTO = (PdfReportDTO) userdata.get("UserDetail");
                if (summaryDTO != null) {
                    // Add "Personal Details" title
                    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 15, Font.BOLD);
                    Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
                    Paragraph title = new Paragraph("Employee Details", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
                    document.add(title);

                    // Add a horizontal line
                    PdfPCell lineCell = new PdfPCell(new Phrase(" "));
                    lineCell.setColspan(3); // Set the cell to span across all columns
                    lineCell.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
                    lineCell.setBorderWidthBottom(1); // Set the bottom border width
                    PdfPTable lineTable = new PdfPTable(1);
                    lineTable.setWidthPercentage(100);
                    lineTable.addCell(lineCell);
                    document.add(lineTable);

                    // Add a table for "Personal Details"
                    Font tableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);
                    PdfPTable personalTable = new PdfPTable(3); // 3 columns
                    personalTable.setWidthPercentage(100); // Set the width of the table to 100%
                    personalTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER); // Set the border to NO_BORDER

                    // Add data to the personal table
                    personalTable.addCell(createTableCell("Name", summaryDTO.getName(), tableFont));
                    personalTable.addCell(createTableCell("Email", summaryDTO.getEmail(), tableFont));
                    personalTable.addCell(createTableCell("Designation", summaryDTO.getDay(), tableFont));
                    personalTable
                            .addCell(createTableCell("Attendance Status", summaryDTO.getAttendanceStatus(), tableFont));

                    personalTable.addCell(createEmptyCell());

                    // Add the personal table to the document
                    document.add(personalTable);

                    // Add a new line
                    document.add(new Paragraph("\n"));

                    // Add "Employee Details" title
                    Paragraph titleParagraph = new Paragraph();
                    Chunk titleChunk = new Chunk(heading, titleFont);
                    titleParagraph.add(titleChunk);

                    // Add the date range with smaller font
                    Chunk dateChunk = new Chunk(dateRange, dateFont);
                    titleParagraph.add(dateChunk);

                    titleParagraph.setAlignment(Element.ALIGN_CENTER);
                    document.add(titleParagraph);

                    PdfPCell lineCell1 = new PdfPCell(new Phrase(" "));
                    lineCell1.setColspan(3); // Set the cell to span across all columns
                    lineCell1.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
                    lineCell1.setBorderWidthBottom(1); // Set the bottom border width
                    PdfPTable lineTable1 = new PdfPTable(1);
                    lineTable1.setWidthPercentage(100);
                    lineTable1.addCell(lineCell);
                    document.add(lineTable1);

                    // Add a table for "Employee Details"
                    PdfPTable empTable = new PdfPTable(new float[] { 1.3f, 4f, 2.4f, 4f, 3f, 2f });
                    empTable.setWidthPercentage(100); // Set the width of the table to 100%

                    // Add headers to the employee table
                    empTable.addCell(createHeaderCell("Sl.No"));
                    empTable.addCell(createHeaderCell("User Name"));
                    empTable.addCell(createHeaderCell("Activity Date"));
                    empTable.addCell(createHeaderCell("Product Name"));
                    empTable.addCell(createHeaderCell("Task"));
                    empTable.addCell(createHeaderCell("Hours"));

                    document.add(new Paragraph("\n"));

                    // Fetch data from your service and populate the employee table
                    long index = 1;
                    Set<String> supervisorNames = new HashSet<>();
                    Set<String> finalApprovalNames = new HashSet<>();
                    // Iterate over the entry set of the data map
                    String totalHoursValue = "";
                    for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : data.entrySet()) {
                        List<Map<String, Object>> reportList = entry.getValue();

                        // Skip processing if reportList is empty
                        if (reportList.isEmpty()) {
                            continue;
                        }

                        for (Map<String, Object> report : reportList) {
                            String productName = (String) report.get("name");
                            String hoursString = report.get("hours").toString();
                            String username = (String) report.get("username");
                            String taskName = (String) report.get("task");
                            Object taskHoursObj = report.get("taskHours");
                            String supervisorName = (String) report.get("supervisorApproval");
                            String finalApprovalName = (String) report.get("finalapprovalname");
                            String totalHours;

                            supervisorNames.add(supervisorName);
                            finalApprovalNames.add(finalApprovalName);
                            System.out.println(finalApprovalName);
                            if (taskHoursObj instanceof java.sql.Time) {
                                java.sql.Time taskHoursTime = (java.sql.Time) taskHoursObj;
                                totalHours = taskHoursTime.toString(); // Converts to "HH:MM:SS" format
                            } else {
                                totalHours = taskHoursObj.toString(); // Handle other types if necessary
                            }

                            totalHoursValue = totalHours;

                            Date sqlDate = (Date) report.get("activityDate");
                            LocalDate activityDate = sqlDate.toLocalDate();

                            if (index % 2 == 0) {
                                empTable.addCell(createGrayCell(String.valueOf(index)));
                                empTable.addCell(createGrayCell(username));
                                empTable.addCell(createGrayCell(String.valueOf(activityDate)));
                                empTable.addCell(createGrayCell(productName));
                                empTable.addCell(createGrayCell(hoursString));
                                empTable.addCell(createGrayCell(taskName));
                            } else {
                                empTable.addCell(createCell(String.valueOf(index)));
                                empTable.addCell(createCell(username));
                                empTable.addCell(createCell(String.valueOf(activityDate)));
                                empTable.addCell(createCell(productName));
                                empTable.addCell(createCell(hoursString));
                                empTable.addCell(createCell(taskName));
                            }
                            index++;
                        }

                        // Add total hours row at the end of entries for the current date
                        // Create the "Sub Total Hours" cell
                        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Sub Total Hours (per day)"));
                        totalLabelCell.setColspan(5); // Span across multiple columns for the label
                        totalLabelCell.setHorizontalAlignment(Element.ALIGN_CENTER); // Center horizontally
                        totalLabelCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Center vertically
                        totalLabelCell.setBackgroundColor(new BaseColor(255, 255, 204)); // Light yellow color
                        totalLabelCell.setFixedHeight(20f); // Set fixed height (adjust the value as needed)
                        empTable.addCell(totalLabelCell);

                        // Create the total hours cell
                        PdfPCell totalHoursCell = new PdfPCell(new Phrase(totalHoursValue));
                        totalHoursCell.setBackgroundColor(new BaseColor(255, 255, 204)); // Light yellow color
                        totalHoursCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        totalHoursCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        empTable.addCell(totalHoursCell);

                    }
                    StringJoiner joiner = new StringJoiner(", ");
                    for (String name : finalApprovalNames) {
                        joiner.add(name);
                    }
                    String finamapprovalstring = joiner.toString();
                    String supervisornamesString = String.join(", ", supervisorNames);

                    // ClassLoader classLoader = Example.class.getClassLoader();
                    // URL resource = classLoader.getResource("static/assets/uploads/tick.png");
                    // Image tickImage = Image.getInstance(resource);
                    // tickImage.setAbsolutePosition(50, 650); // Adjust position as needed
                    // tickImage.scaleToFit(100, 100); // Adjust size as needed

                    // // Get the direct content and add the image
                    // PdfContentByte canvas = writer.getDirectContentUnder();
                    // canvas.addImage(tickImage);

                    // Create and format the Paragraph
                    // Paragraph paragraph = new Paragraph();
                    // paragraph.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)); //
                    // Set font style
                    // paragraph.add(new Phrase("Signature Valid",
                    // FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14))); // Header
                    // paragraph.add(Chunk.NEWLINE); // Add a newline
                    // paragraph.add(new Phrase("Digitally Signed by",
                    // FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))); // Subheader
                    // paragraph.add(Chunk.NEWLINE); // Add a newline

                    // // Add Level 1 names
                    // paragraph.add(new Phrase("Level 1 : ",
                    // FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))); // Label
                    // paragraph.add(new Phrase(supervisornamesString,
                    // FontFactory.getFont(FontFactory.HELVETICA, 12))); // Names
                    // paragraph.add(Chunk.NEWLINE); // Add a newline

                    // // Add Level 2 names
                    // paragraph.add(new Phrase("Level 2 : ",
                    // FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12))); // Label
                    // paragraph.add(new Phrase(finamapprovalstring,
                    // FontFactory.getFont(FontFactory.HELVETICA, 12))); // Names

                    // // Add the paragraph to the document

                    // document.add(empTable);
                    // document.add(paragraph);
                    // Create a table to act as the card
                    // Load tick image from resources
                    ClassLoader classLoader = getClass().getClassLoader();
                    URL tickImageUrl = classLoader.getResource("static/assets/uploads/tick.png");
                    if (tickImageUrl == null) {
                        throw new FileNotFoundException("Tick image not found in classpath.");
                    }
                    Image tickImage = Image.getInstance(tickImageUrl);
                    tickImage.scaleToFit(100, 100); // Adjust size as needed

                    // Create a PdfTemplate
                    PdfContentByte canvas = writer.getDirectContentUnder();
                    float x = document.left() + 20;
                    float y = document.bottom() + 150;
                    tickImage.setAbsolutePosition(x, y);
                    canvas.addImage(tickImage);

                    Paragraph textContent = new Paragraph();

                    textContent.add(new Chunk("\nSignature Valid\n", FontFactory.getFont(FontFactory.HELVETICA, 14)));
                    textContent
                            .add(new Chunk("\nDigitally signed by\n", FontFactory.getFont(FontFactory.HELVETICA, 14)));
                    textContent.add(new Chunk("\nLevel 1 :" + supervisornamesString,
                            FontFactory.getFont(FontFactory.HELVETICA, 14)));
                    textContent.add(new Chunk("\nLevel 2 : " + finamapprovalstring,
                            FontFactory.getFont(FontFactory.HELVETICA, 14)));

                    document.add(empTable);
                    document.add(textContent);

                    document.newPage();
                }
            } else {
                document.add(new Paragraph("No records found for the given date."));
            }

            // Add the signature block image and text at the end of the document
            // Image signatureImage = Image.getInstance("static/assets/uploads/logo.png");
            // signatureImage.scaleToFit(200, 100); // Scale the image to fit within the
            // document
            // signatureImage.setAlignment(Element.ALIGN_CENTER);
            // document.add(signatureImage);

            document.close();
            writer.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // // mani code
    // private byte[] generatePdfDAteRange(Map<String, Object> userdata,
    // Map<LocalDate, List<Map<String, Object>>> data, String hours) {
    // try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
    // Document document = new Document();
    // PdfWriter writer = PdfWriter.getInstance(document, outputStream);
    // document.open();

    // if (!userdata.isEmpty()) {
    // PdfReportDTO summaryDTO = (PdfReportDTO) userdata.get("UserDetail");
    // if (summaryDTO != null) {
    // // Add "Personal Details" title
    // Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
    // Paragraph title = new Paragraph("Employee Details", titleFont);
    // title.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
    // document.add(title);

    // // Add a horizontal line
    // PdfPCell lineCell = new PdfPCell(new Phrase(" "));
    // lineCell.setColspan(3); // Set the cell to span across all columns
    // lineCell.setBorder(Rectangle.BOTTOM); // Set the border to only show at the
    // bottom
    // lineCell.setBorderWidthBottom(1); // Set the bottom border width
    // PdfPTable lineTable = new PdfPTable(1);
    // lineTable.setWidthPercentage(100);
    // lineTable.addCell(lineCell);
    // document.add(lineTable);

    // // Add a table for "Personal Details"
    // Font tableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10,
    // Font.NORMAL);
    // PdfPTable personalTable = new PdfPTable(3); // 3 columns
    // personalTable.setWidthPercentage(100); // Set the width of the table to 100%
    // personalTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER); // Set the
    // border to NO_BORDER

    // // Add data to the personal table
    // personalTable.addCell(createTableCell("Name", summaryDTO.getName(),
    // tableFont));
    // personalTable.addCell(createTableCell("Email", summaryDTO.getEmail(),
    // tableFont));
    // personalTable.addCell(createTableCell("D.O.J",
    // summaryDTO.getDate_of_join().toString(), tableFont));
    // personalTable.addCell(createTableCell("Attendance Status",
    // summaryDTO.getAttendanceStatus(), tableFont));

    // personalTable.addCell(createEmptyCell());

    // // Add the personal table to the document
    // document.add(personalTable);

    // // Add a new line
    // document.add(new Paragraph("\n"));

    // // Add "Employee Details" title
    // Paragraph empDetailsTitle = new Paragraph("Time Sheet Details", titleFont);
    // empDetailsTitle.setAlignment(Element.ALIGN_CENTER); // Align the title to the
    // center
    // document.add(empDetailsTitle);

    // PdfPCell lineCell1 = new PdfPCell(new Phrase(" "));
    // lineCell1.setColspan(3); // Set the cell to span across all columns
    // lineCell1.setBorder(Rectangle.BOTTOM); // Set the border to only show at the
    // bottom
    // lineCell1.setBorderWidthBottom(1); // Set the bottom border width
    // PdfPTable lineTable1 = new PdfPTable(1);
    // lineTable1.setWidthPercentage(100);
    // lineTable1.addCell(lineCell);
    // document.add(lineTable1);

    // // Add a table for "Employee Details"
    // PdfPTable empTable = new PdfPTable(new float[]{1f, 2f, 2f, 3f, 2f});
    // empTable.setWidthPercentage(100); // Set the width of the table to 100%

    // // Add headers to the employee table
    // empTable.addCell(createHeaderCell("Sl.No"));
    // empTable.addCell(createHeaderCell("User Name"));
    // empTable.addCell(createHeaderCell("Activity Date"));
    // empTable.addCell(createHeaderCell("Product Name"));
    // empTable.addCell(createHeaderCell("Total.no.Hr"));
    // document.add(new Paragraph("\n"));

    // long index = 1;
    // LocalDate previousDate = null;
    // double totalHoursForDate = 0; // Change to double for precision
    // int totalMinutes = 0;

    // // Iterate over the entry set of the data map
    // for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : data.entrySet())
    // {
    // LocalDate date = entry.getKey();
    // LocalDate currentDate = date;
    // List<Map<String, Object>> reportList = entry.getValue();

    // for (Map<String, Object> report : reportList) {
    // String productName = (String) report.get("name");
    // // Time time = (Time) report.get("hours");
    // // String hoursString = time.toString();
    // Time time = (java.sql.Time) report.get("hours");
    // String timeString = time.toString();
    // String[] timeParts = timeString.split(":");
    // int hours1 = Integer.parseInt(timeParts[0]);
    // int minutes = Integer.parseInt(timeParts[1]);
    // totalMinutes += (hours1 * 60) + minutes;

    // String username = (String) report.get("username");

    // java.sql.Date sqlDate = (java.sql.Date) report.get("activityDate");
    // LocalDate activityDate = sqlDate.toLocalDate();

    // // Convert hoursString to LocalTime
    // LocalTime hoursWorked = LocalTime.parse(timeString);

    // // Check if the date has changed or if it's the last entry
    // if (!currentDate.equals(previousDate) || (currentDate.equals(previousDate) &&
    // index == data.size())) {
    // if (previousDate != null) {
    // empTable.addCell(createEmptyCell());
    // empTable.addCell(createEmptyCell());
    // empTable.addCell(createEmptyCell());

    // // Calculate total hours and minutes from totalMinutes
    // int totalHours = totalMinutes / 60;
    // int remainingMinutes = totalMinutes % 60;

    // // Format total hours and minutes into HH:MM
    // String totalWorkHours = String.format("%02d:%02d", totalHours,
    // remainingMinutes);
    // empTable.addCell(createFooterCell("Total Working Hours "));
    // empTable.addCell(createFooterCell(totalWorkHours));

    // totalMinutes = 0; // Reset total minutes for the next date
    // }
    // previousDate = currentDate;
    // }

    // // Add the duration in hours and minutes to totalHoursForDate
    // totalHoursForDate += hoursWorked.getHour(); // Add hours
    // totalHoursForDate += (double) hoursWorked.getMinute() / 60.0; // Add minutes
    // converted to hours

    // // Add cells to the table
    // if (index % 2 == 0) {
    // empTable.addCell(createGrayCell(String.valueOf(index)));
    // empTable.addCell(createGrayCell(username));
    // empTable.addCell(createGrayCell(String.valueOf(activityDate)));
    // empTable.addCell(createGrayCell(productName));
    // empTable.addCell(createGrayCell(timeString));
    // } else {
    // empTable.addCell(createCell(String.valueOf(index)));
    // empTable.addCell(createCell(username));
    // empTable.addCell(createCell(String.valueOf(activityDate)));
    // empTable.addCell(createCell(productName));
    // empTable.addCell(createCell(timeString));
    // }

    // // Update totalMinutes with the current row's hours
    // index++;
    // }
    // previousDate = date;
    // }

    // // Add total working hours for the last date
    // if (previousDate != null) {
    // empTable.addCell(createEmptyCell());
    // empTable.addCell(createEmptyCell());
    // empTable.addCell(createEmptyCell());
    // empTable.addCell(createFooterCell("Total Working Hours "));
    // empTable.addCell(createFooterCell(String.valueOf(totalHoursForDate)));
    // }
    // // Add the employee table to the document
    // document.add(empTable);

    // // Start a new page for the next report
    // document.newPage();
    // }
    // } else {
    // // If there are no records found, add a message or handle it as needed
    // document.add(new Paragraph("No records found for the given date."));
    // }

    // document.close();
    // writer.close();

    // return outputStream.toByteArray();
    // } catch (DocumentException | IOException e) {
    // e.printStackTrace();
    // return null;
    // }
    // }

    private byte[] generatePdfDAteRangewithusers(Map<String, Object> userdata,
            Map<LocalDate, List<Map<String, Object>>> data, String hours) {
        if (data.isEmpty()) {
            // Handle this case appropriately, e.g., return a PDF with a message indicating
            // no data
            return null;
        }
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            // Add "Personal Details" title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Employee Details", titleFont);
            title.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
            document.add(title);

            // Add a horizontal line
            PdfPCell lineCell = new PdfPCell(new Phrase(" "));
            lineCell.setColspan(3); // Set the cell to span across all columns
            lineCell.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
            lineCell.setBorderWidthBottom(1); // Set the bottom border width
            PdfPTable lineTable = new PdfPTable(1);
            lineTable.setWidthPercentage(100);
            lineTable.addCell(lineCell);
            document.add(lineTable);

            // Add a table for "Personal Details"
            Font tableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);
            PdfPTable personalTable = new PdfPTable(3); // 3 columns
            personalTable.setWidthPercentage(100); // Set the width of the table to 100%
            personalTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER); // Set the border to NO_BORDER

            // Add data to the personal table

            personalTable.addCell(createEmptyCell());

            // Add the personal table to the document
            document.add(personalTable);

            // Add a new line
            document.add(new Paragraph("\n"));

            // Add "Employee Details" title
            Paragraph empDetailsTitle = new Paragraph("Time Sheet Details", titleFont);
            empDetailsTitle.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
            document.add(empDetailsTitle);

            PdfPCell lineCell1 = new PdfPCell(new Phrase(" "));
            lineCell1.setColspan(3); // Set the cell to span across all columns
            lineCell1.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
            lineCell1.setBorderWidthBottom(1); // Set the bottom border width
            PdfPTable lineTable1 = new PdfPTable(1);
            lineTable1.setWidthPercentage(100);
            lineTable1.addCell(lineCell);
            document.add(lineTable1);

            // Add a table for "Employee Details"
            PdfPTable empTable = new PdfPTable(new float[] { 1f, 2f, 2f, 3f, 2f });
            empTable.setWidthPercentage(100); // Set the width of the table to 100%

            // Add headers to the employee table
            empTable.addCell(createHeaderCell("Sl.No"));
            empTable.addCell(createHeaderCell("User Name"));
            empTable.addCell(createHeaderCell("Activity Date"));
            empTable.addCell(createHeaderCell("Product Name"));
            empTable.addCell(createHeaderCell("Task"));
            empTable.addCell(createHeaderCell("Hours"));
            document.add(new Paragraph("\n"));

            // Fetch data from your service and populate the employee table
            long index = 1;

            // Iterate over the entry set of the data map
            for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : data.entrySet()) {
                LocalDate date = entry.getKey();
                List<Map<String, Object>> reportList = entry.getValue();

                for (Map<String, Object> report : reportList) {
                    String productName = (String) report.get("name");
                    String hoursString = report.get("task").toString();
                    String username = (String) report.get("username");
                    String hoursdata = (String) report.get("hours");

                    Date sqlDate = (Date) report.get("activityDate");
                    LocalDate activityDate = sqlDate.toLocalDate();

                    // Convert hoursString to int if needed
                    // LocalTime localTime = LocalTime.parse(hoursString);

                    // Add cells to the table
                    if (index % 2 == 0) {
                        empTable.addCell(createGrayCell(String.valueOf(index)));
                        empTable.addCell(createGrayCell(username));
                        empTable.addCell(createGrayCell(String.valueOf(activityDate)));
                        empTable.addCell(createGrayCell(productName));
                        empTable.addCell(createGrayCell(hoursString));
                        empTable.addCell(createGrayCell(hoursdata));
                    } else {
                        empTable.addCell(createCell(String.valueOf(index)));
                        empTable.addCell(createCell(username));
                        empTable.addCell(createCell(String.valueOf(activityDate)));
                        empTable.addCell(createCell(productName));
                        empTable.addCell(createCell(hoursString));
                        empTable.addCell(createCell(hoursdata));
                    }

                    index++;
                }
            }
            // Add empty cells for the first three columns without borders
            // empTable.addCell(createEmptyCell());
            // empTable.addCell(createEmptyCell());
            // empTable.addCell(createEmptyCell());
            //// For product name column
            // empTable.addCell(createFooterCell("Total Working Hours:"));
            // empTable.addCell(createFooterCell(String.valueOf(hours))); // Total hours
            // column
            // Add the employee table to the document
            document.add(empTable);

            // Start a new page for the next report
            document.newPage();

            document.close();
            writer.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Function to create an empty cell without borders
    private PdfPCell createEmptyCell() {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        return cell;
    }

    private PdfPCell createFooterCell(String text) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.WHITE))); // Increased
        // font
        // size
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Align text horizontally to the center
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Align text vertically to the middle
        cell.setBackgroundColor(BaseColor.RED);
        cell.setPadding(5);
        cell.setBorderWidth(1);
        cell.setBorderColor(BaseColor.BLACK);
        return cell;
    }

    private PdfPCell createGrayCell(String content) {
        PdfPCell cell = new PdfPCell(new Phrase(content));
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderWidth(1); // Set border width
        cell.setPadding(5); // Adjust the padding value as needed
        cell.setBorderColor(BaseColor.BLACK); // Set border color
        return cell;
    }

    private byte[] generatePdf(Map<String, Object> userdata, List<Map<String, Object>> data, LocalDate date,
            String hours) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            document.open();

            if (!userdata.isEmpty()) {
                PdfReportDTO summaryDTO = (PdfReportDTO) userdata.get("UserDetail");
                if (summaryDTO != null) {
                    // Add "Personal Details" title
                    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
                    Paragraph title = new Paragraph("Employee Details", titleFont);
                    title.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
                    document.add(title);

                    // Add a horizontal line
                    PdfPCell lineCell = new PdfPCell(new Phrase(" "));
                    lineCell.setColspan(3); // Set the cell to span across all columns
                    lineCell.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
                    lineCell.setBorderWidthBottom(1); // Set the bottom border width
                    PdfPTable lineTable = new PdfPTable(1);
                    lineTable.setWidthPercentage(100);
                    lineTable.addCell(lineCell);
                    document.add(lineTable);

                    // Add a table for "Personal Details"
                    Font tableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);
                    PdfPTable personalTable = new PdfPTable(3); // 3 columns
                    personalTable.setWidthPercentage(100); // Set the width of the table to 100%
                    personalTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER); // Set the border to NO_BORDER

                    // Add data to the personal table
                    personalTable.addCell(createTableCell("Name", summaryDTO.getName(), tableFont));
                    personalTable.addCell(createTableCell("Email", summaryDTO.getEmail(), tableFont));
                    personalTable.addCell(createTableCell("D.O.J", summaryDTO.getDate_of_join().toString(), tableFont));
                    personalTable
                            .addCell(createTableCell("Attendance Status", summaryDTO.getAttendanceStatus(), tableFont));
                    personalTable.addCell(createTableCell("Activity Date", date.toString(), tableFont));
                    personalTable.addCell(createEmptyCell());

                    // Add the personal table to the document
                    document.add(personalTable);

                    // Add a new line
                    document.add(new Paragraph("\n"));

                    // Add "Employee Details" title
                    Paragraph empDetailsTitle = new Paragraph("Time Sheet Details", titleFont);
                    empDetailsTitle.setAlignment(Element.ALIGN_CENTER); // Align the title to the center
                    document.add(empDetailsTitle);

                    PdfPCell lineCell1 = new PdfPCell(new Phrase(" "));
                    lineCell1.setColspan(3); // Set the cell to span across all columns
                    lineCell1.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
                    lineCell1.setBorderWidthBottom(1); // Set the bottom border width
                    PdfPTable lineTable1 = new PdfPTable(1);
                    lineTable1.setWidthPercentage(100);
                    lineTable1.addCell(lineCell);
                    document.add(lineTable1);

                    // Add a table for "Employee Details"
                    PdfPTable empTable = new PdfPTable(new float[] { 1f, 3f, 2.4f });
                    empTable.setWidthPercentage(100); // Set the width of the table to 100%

                    // Add headers to the employee table
                    empTable.addCell(createHeaderCell("Sl.No"));
                    empTable.addCell(createHeaderCell("Product Name"));
                    empTable.addCell(createHeaderCell("Total.no.Hr"));

                    // Fetch data from your service and populate the employee table
                    long index = 1;
                    PdfPCell emptyCell = new PdfPCell();
                    emptyCell.setBorder(PdfPCell.NO_BORDER);

                    // Initialize total work hours outside the loop
                    // Initialize totalWorkHours to 0.0
                    // Initialize totalWorkHours to 0.0
                    double totalWorkHours = 0.0;
                    int totalMinutes = 0;
                    for (Map<String, Object> report : data) {
                        String productName = (String) report.get("name");
                        Time time = (Time) report.get("hours");
                        String timeString = time.toString();
                        String[] timeParts = timeString.split(":");
                        int hours1 = Integer.parseInt(timeParts[0]);
                        int minutes = Integer.parseInt(timeParts[1]);
                        totalMinutes += (hours1 * 60) + minutes;

                        // Add cells to the table (assuming this part is correct)
                        empTable.addCell(createCell(String.valueOf(index)));
                        empTable.addCell(createCell(productName));
                        empTable.addCell(createCell(timeString));

                        index++;
                    }

                    empTable.addCell(emptyCell);

                    // Add "Total Work Hours" label and the total work hours
                    PdfPCell totalWorkHoursCellheader = new PdfPCell(
                            new Phrase("Total Work Hours", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15)));

                    // Set horizontal alignment to center
                    totalWorkHoursCellheader.setHorizontalAlignment(Element.ALIGN_CENTER);
                    empTable.addCell(totalWorkHoursCellheader);

                    Phrase totalWorkHoursPhrase = new Phrase(hours,
                            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15));
                    PdfPCell totalWorkHoursCell = new PdfPCell(totalWorkHoursPhrase);
                    totalWorkHoursCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    empTable.addCell(totalWorkHoursCell);

                    // Add the employee table to the document
                    document.add(empTable);

                    // Start a new page for the next report
                    document.newPage();
                }
            } else {
                // If there are no records found, add a message or handle it as needed
                document.add(new Paragraph("No records found for the given date."));
            }

            document.close();
            writer.close();

            return outputStream.toByteArray();
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // create cell
    private PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderWidth(1); // Set border width
        cell.setPadding(5); // Adjust the padding value as needed
        cell.setBorderColor(BaseColor.BLACK); // Set border color
        return cell;
    }

    // design member data
    private PdfPCell createHeaderCell(String text) {
        // Create a Phrase with the text and font settings
        Phrase phrase = new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE));

        // Create a PdfPCell with the Phrase
        PdfPCell cell = new PdfPCell(phrase);

        // Set cell properties
        cell.setHorizontalAlignment(Element.ALIGN_CENTER); // Center horizontally
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Center vertically
        cell.setBackgroundColor(BaseColor.RED); // Set background color
        cell.setPadding(5); // Set padding
        cell.setBorderWidth(1); // Set border width
        cell.setBorderColor(BaseColor.BLACK); // Set border color

        return cell;
    }

    // to create a emoty cell

    private PdfPCell createTableCell(String label, String value, Font font) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.addElement(new Paragraph(label + ": ", font)); // Label
        cell.addElement(new Paragraph(value, font)); // Value
        return cell;
    }

    // emaya report
    // @Autowired
    // private MemberSupervisorService memberSupervisorService;
    //
    // public void employeeDetailReport(HttpServletResponse response) throws
    // IOException {
    //
    // PdfWriter writer = new PdfWriter(response.getOutputStream());
    // PdfDocument pdfDocument;
    // pdfDocument = new PdfDocument(writer);
    // Document document = new Document(pdfDocument);
    //
    // try {
    // Boolean status = false;
    // if (status) {
    // document.add(new Paragraph("Time Sheet Entered
    // Data").setBold().setPaddingLeft(200f));
    // } else {
    // document.add(new Paragraph("Time Sheet Not Entered
    // Data").setBold().setPaddingLeft(200f));
    // }
    //
    // Table table = new Table(new float[] { 1, 2, 1, 2, 2, 1, 1 }); // Adjust
    // column widths as per your
    // // requirements
    // table.setWidthPercent(100).setPadding(0).setFontSize(9);
    //
    // Cell cell1 = new Cell(1, 7);
    // cell1.setTextAlignment(TextAlignment.CENTER);
    // cell1.add("Employee Details").setBold();
    // table.addCell(cell1);
    //
    // table.addCell(new Cell().add("Sl.No").setBold());
    // table.addCell(new Cell().add("Activity Date").setBold());
    // table.addCell(new Cell().add("Branch").setBold());
    // table.addCell(new Cell().add("Supervisor Name").setBold());
    // table.addCell(new Cell().add("Member").setBold());
    // table.addCell(new Cell().add("Supervisor Status").setBold());
    // table.addCell(new Cell().add("Total.no.Hr").setBold()); // Corrected typo
    // from "total Hr" to "Total Hr"
    // LocalDate date = LocalDate.now(); // Provide appropriate date
    // ResponseEntity<?> responseEntity =
    // memberSupervisorService.getMembersUnderSupervisorbyall(date);
    // Object responseBody = responseEntity.getBody();
    // ApiResponse apiResponse = (ApiResponse) responseBody;
    // String enterSatatus ="Not Entered";
    // if (apiResponse != null && apiResponse.getData() != null) {
    // Object data = apiResponse.getData();
    // if (data instanceof List) {
    // List<UserTaskActivityResponseStatus> dataList =
    // (List<UserTaskActivityResponseStatus>) data;
    //
    // long index = 1;
    // for (UserTaskActivityResponseStatus emp : dataList) {
    // if(emp.getStatus().equalsIgnoreCase(enterSatatus)) {
    // table.addCell(new Cell().add(String.valueOf(index)));
    // table.addCell(new Cell().add(emp.getActivity_date().toString()));
    // table.addCell(new Cell().add(emp.getBranch()));
    // if (emp.getSupervisorName() != null) {
    // table.addCell(new Cell().add(emp.getSupervisorName()));
    //
    // }else {
    // table.addCell(new Cell().add("-"));
    // }
    // table.addCell(new Cell().add(emp.getUserName()));
    // table.addCell(new Cell().add(emp.getStatus()));
    //
    //
    //
    // if (emp.getHours() != null) {
    //
    // table.addCell(new Cell().add(emp.getHours()));
    // }else {
    // table.addCell(new Cell().add("-"));
    // }
    // index++;
    // }
    // }
    // }
    // }
    //
    // document.add(table);
    //
    // document.close();
    // writer.flush();
    // writer.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    //
    // @Override
    // public ResponseEntity<byte[]> getMembersUnderSupervisor(LocalDate date, int
    // supervisorid, int memberid,
    // String status) {
    // // TODO Auto-generated method stub
    // return null;
    // }
    //

}
