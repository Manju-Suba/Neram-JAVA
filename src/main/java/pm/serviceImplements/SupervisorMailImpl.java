package pm.serviceImplements;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pm.dto.CommonResponse;
import pm.dto.SupervisorResponse;
import pm.mobileAppDto.MembersActivityUserDto;
import pm.model.users.Users;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.UsersRepository;
import pm.response.ApiResponse;
import pm.service.EmailService;
import pm.utils.AuthUserData;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupervisorMailImpl {

    @Value("${myapp.customProperty}")
    private String portalUrl;


    @Value("${fileBasePath}")
    private String fileBasePath;

    private final UsersRepository usersRepository;
    private final CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;
    private final EmailService emailService;

    public SupervisorMailImpl(UsersRepository usersRepository, CommonTimeSheetActivityRepository commonTimeSheetActivityRepository, EmailService emailService) {
        this.usersRepository = usersRepository;
        this.commonTimeSheetActivityRepository = commonTimeSheetActivityRepository;
        this.emailService = emailService;
    }

    public ResponseEntity<?> dailySupervisorReport() throws IOException, MessagingException {
        List<String> supervisors = usersRepository.findAllSupervisor();
        LocalDate yesterday = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String formattedDate = yesterday.format(formatter);

        for (String supervisorId : supervisors) {
            if (!supervisorId.equals("0")&&!supervisorId.equals("110101")) {
                System.out.println(supervisorId);
                List<Object[]> userdata = commonTimeSheetActivityRepository.getSupervisorBasedUsersName(supervisorId, yesterday);
                Optional<Users> loginuser = usersRepository.findByUsername(supervisorId);
                int check = commonTimeSheetActivityRepository.usertimesheetcheck(supervisorId, yesterday);

                List<SupervisorResponse> alldataResponse = userdata.stream().map(data -> {
                    SupervisorResponse response = new SupervisorResponse();
                    response.setName((String) data[0]);
                    response.setActivitydate(convertToDateTime(data[1]).toLocalDate());
                    Time time = (Time) data[2];
                    String formattedTime = time.toLocalTime().toString(); // Format the time as desired
                    response.setHours(formattedTime); // Set the formatted time
                    return response;
                }).collect(Collectors.toList());


                List<Object[]> notenterdata = commonTimeSheetActivityRepository.notEnteredTimeSheet(supervisorId, yesterday);


                List<MembersActivityUserDto> allnotenterddata = notenterdata.stream().map(data -> {
                    return new MembersActivityUserDto((String) data[0]);

                }).collect(Collectors.toList());
                boolean isSaturdayOrSunday = yesterday.getDayOfWeek() == DayOfWeek.SATURDAY || yesterday.getDayOfWeek() == DayOfWeek.SUNDAY;
//                if(loginuser.get().getUsername().equalsIgnoreCase("900142")) { //hide
                    byte[] pdfBytes = generatePdf(alldataResponse,formattedDate,loginuser.get().getName());
                    byte[] pdfNotEntered = notgeneratePdf(allnotenterddata, formattedDate,loginuser.get().getName());
                    boolean pdfBytesPresent = (pdfBytes != null && pdfBytes.length > 0);
                    boolean pdfNotEnteredPresent = ( pdfNotEntered != null && pdfNotEntered.length > 0);

                    String emailText =  generateEmailText(pdfBytesPresent, pdfNotEnteredPresent,formattedDate);
                    // If PDF generation is successful, send the email
                    Map<String, byte[]> attachments = new HashMap<>();
                    if (pdfBytesPresent) {
                        System.out.println("entered");
                            attachments.put("Time Sheet Entered.pdf", pdfBytes);
                    }
                    if ( !isSaturdayOrSunday && pdfNotEnteredPresent) {
                        System.out.println(" not entered");
                        attachments.put("Not Entered Timesheet.pdf", pdfNotEntered);
                    }
                System.out.println(loginuser.get().getName());

                    if (!attachments.isEmpty()) {
                        emailService.sendEmailWithAttachments(loginuser.get().getEmail(), "Daily Team Member Reports", emailText, attachments);
                        System.out.println("email send successfully");
                    }

//            }//hide
             }

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Mail Sent Successfully.", null));
    }

    private String generateEmailText(boolean pdfBytesPresent, boolean pdfNotEnteredPresent, String formattedDate) {
        StringBuilder emailText = new StringBuilder("<html><body>");

        if (pdfBytesPresent && pdfNotEnteredPresent) {
            emailText.append("Please review the details of the timesheets entered and not entered by the members for <b>")
                    .append(formattedDate)
                    .append("</b> by clicking on the \"View Details\" link below.");
        } else if (pdfBytesPresent) {
            emailText.append("Please review the timesheet details for the members entered on <b>")
                    .append(formattedDate)
                    .append("</b> by clicking on the \"View Details\" link below.");
        } else if (pdfNotEnteredPresent) {
            emailText.append("Please review the details of the timesheets not entered by the members for <b>")
                    .append(formattedDate)
                    .append("</b> by clicking on the \"View Details\" link below.");
        }

        emailText.append("<p style='text-align: center;'>")
                .append("<a href='")
                .append(portalUrl)
                .append("' style='color: #007bff; text-decoration: none; font-weight: bold;'>")
                .append("View Details")
                .append("</a>")
                .append("</p>")
                .append("</body></html>");

        return emailText.toString();
    }



    private String savePdfReport(byte[] pdfData) throws IOException {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");

        // Specify the path to the Downloads folder
        String downloadsFolderPath = userHome + File.separator + "Downloads";

        // Create the Downloads folder if it doesn't exist
        File downloadsFolder = new File(downloadsFolderPath);
        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs();
        }

        // Save PDF report to the Downloads folder
        String fileName = "report_" + System.currentTimeMillis() + ".pdf";
        String filePath = downloadsFolderPath + File.separator + fileName;

        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(pdfData);
        }
        System.out.println(filePath);
        return filePath;
    }


    private LocalDateTime convertToDateTime(Object dateObject) {
        if (dateObject instanceof Timestamp) {
            return ((Timestamp) dateObject).toLocalDateTime();
        } else if (dateObject instanceof Date) {
            return ((Date) dateObject).toLocalDate().atStartOfDay();
        } else {
            throw new IllegalArgumentException("Unsupported date type");
        }
    }

    public byte[] generatePdf(List<SupervisorResponse> commonResponse,String date,String supervisorname) {
        try {
            if (commonResponse.isEmpty()) {
                System.out.println("No data to generate PDF.");
                return null;
            }
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();


            // Add supervisor's name
//            Font supervisorFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
//            Paragraph supervisorParagraph = new Paragraph("Reporting Supervisor : " + supervisorname, supervisorFont);
//            supervisorParagraph.setAlignment(Element.ALIGN_LEFT);
//            document.add(supervisorParagraph);
//
//
//            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//            String formattedDate = LocalDate.now().format(dateFormatter);
//            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
//            Paragraph dateParagraph = new Paragraph("Date: " + formattedDate, dateFont);
//            dateParagraph.setAlignment(Element.ALIGN_RIGHT);
//            document.add(dateParagraph);
            Font supervisorFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
            DateTimeFormatter headerformatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            String formattedDate = LocalDate.now().format(headerformatter);
            Paragraph headerParagraph = new Paragraph();
            headerParagraph.setAlignment(Element.ALIGN_LEFT);

            Chunk supervisorChunk = new Chunk("Reporting Person : " + supervisorname, supervisorFont);
            Chunk spacerChunk = new Chunk("                                                             "); // Adjust the number of spaces as needed
            Chunk dateChunk = new Chunk("Date: " + formattedDate, dateFont);

            headerParagraph.add(supervisorChunk);
            headerParagraph.add(spacerChunk);
            headerParagraph.add(dateChunk);

            document.add(headerParagraph);

            // Add a line separator
            document.add(new Paragraph("\n"));

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
            PdfPCell lineCell = new PdfPCell(new Phrase(" "));
            lineCell.setColspan(3); // Set the cell to span across all columns
            lineCell.setBorder(Rectangle.BOTTOM); // Set the border to only show at the bottom
            lineCell.setBorderWidthBottom(1); // Set the bottom border width
            Paragraph empDetailsTitle = new Paragraph("Time Sheets Entered Members " +"( "+ date+" )", titleFont);
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
            PdfPTable empTable = new PdfPTable(new float[]{1f, 3f, 3f, 2.1f});
            empTable.setWidthPercentage(100); // Set the width of the table to 100%

            // Add headers to the employee table
            empTable.addCell(createHeaderCell("Sl.No", Element.ALIGN_CENTER));
            empTable.addCell(createHeaderCell("Activity Date", Element.ALIGN_CENTER));
            empTable.addCell(createHeaderCell("Name", Element.ALIGN_CENTER));
            empTable.addCell(createHeaderCell("Total.no.Hr", Element.ALIGN_CENTER));

            // Fetch data from your service and populate the employee table
            long index = 1;
            PdfPCell emptyCell = new PdfPCell();
            emptyCell.setBorder(PdfPCell.NO_BORDER);
            Font tableFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Font.NORMAL);

            document.add(new Paragraph("\n"));
            PdfPTable personalTable = new PdfPTable(3); // 3 columns
            personalTable.setWidthPercentage(100); // Set the width of the table to 100%
            personalTable.getDefaultCell().setBorder(PdfPCell.NO_BORDER); // Set the border to NO_BORDER

            // Add supervisor name above the table

            // Create table

            for (SupervisorResponse employeeDetails : commonResponse) {
                empTable.addCell(createCell(String.valueOf(index)));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy"); // Choose your desired date
                // format
                empTable.addCell(createCell(employeeDetails.getActivitydate().format(formatter)));
                empTable.addCell(createCell(employeeDetails.getName()));
                empTable.addCell(createCell(employeeDetails.getHours()));

                index++;
            }
            document.add(empTable);

            document.close();

            // Save the PDF file to the specified file path
//            String fileName = "timesheet_report.pdf";
//            Path pdfFilePath = Paths.get(filePath, fileName);
//            Files.write(pdfFilePath, byteArrayOutputStream.toByteArray(), StandardOpenOption.CREATE);

            // Return the PDF content
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    public byte[] notgeneratePdf(List<MembersActivityUserDto> commonResponse, String date,String supervisorname) {
        try {


            if (commonResponse.isEmpty()) {
                System.out.println("No data to generate PDF.");
                return null;
            }

            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            // Add supervisor's name
            Font supervisorFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
            DateTimeFormatter headerformatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            String formattedDate = LocalDate.now().format(headerformatter);
            Paragraph headerParagraph = new Paragraph();
            headerParagraph.setAlignment(Element.ALIGN_LEFT);

            Chunk supervisorChunk = new Chunk("Reporting Person : " + supervisorname, supervisorFont);
            Chunk spacerChunk = new Chunk("                                                             "); // Adjust the number of spaces as needed
            Chunk dateChunk = new Chunk("Date: " + formattedDate, dateFont);

            headerParagraph.add(supervisorChunk);
            headerParagraph.add(spacerChunk);
            headerParagraph.add(dateChunk);

            document.add(headerParagraph);



            // Add a line separator
            document.add(new Paragraph("\n"));

            // Add title "Time Sheet Details"
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD);
            Paragraph titleParagraph = new Paragraph("Time Sheet Not Entered Members " +"( "+ date+" )", titleFont);
            titleParagraph.setAlignment(Element.ALIGN_CENTER);
            document.add(titleParagraph);

            // Add another line separator
            document.add(new Paragraph("\n"));

            // Add a table for "Employee Details"
            PdfPTable empTable = new PdfPTable(new float[]{1f, 3f});
            empTable.setWidthPercentage(100);

            // Add headers to the employee table
            empTable.addCell(createHeaderCell("Sl.No", Element.ALIGN_CENTER));
            empTable.addCell(createHeaderCell("Name", Element.ALIGN_CENTER));

            // Populate the employee table with data
            long index = 1;
            for (MembersActivityUserDto employeeDetails : commonResponse) {
                empTable.addCell(createCell(String.valueOf(index)));
                empTable.addCell(createCell(employeeDetails.getUsername()));


                index++;
            }
            document.add(empTable);

            document.close();

            // Return the PDF content
            return byteArrayOutputStream.toByteArray();
        } catch (DocumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    private PdfPCell createHeaderCell(String headerName, int alignment) {
        Phrase phrase = new Phrase(headerName, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE));
        PdfPCell headerCell = new PdfPCell(phrase);
        headerCell.setHorizontalAlignment(alignment); // Set horizontal alignment
        headerCell.setBackgroundColor(BaseColor.RED); // Set background color
        headerCell.setUseBorderPadding(true); // Enable padding for borders
        headerCell.setBorderColor(BaseColor.BLACK); // Set border color
        headerCell.setBorderWidth(1); // Set border width

        headerCell.setPadding(5); // Set padding
        headerCell.setPaddingTop(8); // Set top padding
        headerCell.setPaddingBottom(8); // Set bottom padding
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Set vertical alignment
        return headerCell;
    }

    private PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderWidth(1); // Set border width
        cell.setPadding(5); // Adjust the padding value as needed
        cell.setBorderColor(BaseColor.BLACK); // Set border color
        return cell;
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE))); // Set
        // cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.RED);
        cell.setPadding(5);
        cell.setBorderWidth(1);
        cell.setBorderColor(BaseColor.BLACK);
        return cell;
    }

    public String remainderall() {
        List<Users> user = usersRepository.getActiveEmployeesForEmail();
        LocalDate currentDate = LocalDate.now();
        if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            LocalDate previousMonday = currentDate.minusWeeks(1);
            for (Users userdata : user) {
                String htmlContent = "<div>" + "<p style='padding-left:5px'>" + "Reminder Mail:  Your weekly timesheet <b>" + previousMonday + "</b> is scheduled to be blocked by  <b>" + currentDate + "</b>. We kindly request that you complete the timesheets for the week before <b> 12:00 PM </b>." + "</p>" + "<p style='text-align: center;'><a href='" + portalUrl + "' style='color: #007bff; text-decoration: none;font-weight:bold'>" + "View Details" + "</a></p>" + "</div>";

                emailService.sendEmail(userdata.getEmail(), " Reminder Mail on Neram tool", htmlContent);


            }
        }
        return "Mail Sended Successfully";
    }

    public String weeklyTimeSheetExpireMail() {
        List<Users> user = usersRepository.getActiveEmployeesForEmail();
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();
        if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY && currentTime.isAfter(LocalTime.of(6, 29))) {
            for (Users userdata : user) {
                String htmlContent = "<div>" + "<p style='padding-left:10px'>" + "Reminder Mail: Your Last weekly timesheet submission time has been blocked." + "</p>" + "<p>We kindly request you to review the details by clicking on the \"View Details\" link provided below:</p>" + "<p style='text-align: center;'><a href='" + portalUrl + "' style='color: #007bff; text-decoration: none;font-weight:bold'>" + "View Details" + "</a></p>" + "</div>";

                emailService.sendEmail(userdata.getEmail(), "Reminder Mail on Neram tool", htmlContent);
            }
        }
        return "Mail Sended Successfully";
    }

    public String notEnterdMail() {
        List<Users> userList = usersRepository.getnotEnteredMail();
        LocalDate today = LocalDate.now();
        LocalDate yesterday;

        if (today.getDayOfWeek() == DayOfWeek.MONDAY) {
            // If today is Monday, subtract 2 days
            yesterday = today.minusDays(3);
        } else {
            // For other days, subtract 1 day
            yesterday = today.minusDays(1);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        String formattedDate = yesterday.format(formatter);
        for (Users user : userList) {
            long size = commonTimeSheetActivityRepository.findYesterdayTimesheetRecord(user.getId(), yesterday);
            if (size == 0 && !user.getUsername().equalsIgnoreCase("SoftwareSupport@hepl.com") && !user.getUsername().equalsIgnoreCase("900055") && !user.getUsername().equalsIgnoreCase("900045") && !user.getUsername().equalsIgnoreCase("900033")) {
                String htmlContent = "<div>" + "<p style='padding-left:10px'>" + "Dear <b>" + user.getName() + ",</b> This is a gentle reminder to please fill out your timesheet for <b>" + formattedDate + "</b> . Your cooperation in completing this task is greatly appreciated" + "</p>" + "<p style='padding-left:10px'>We kindly request you to review the details by clicking on the \"View Details\" link provided below:</p>" + "<p style='text-align: center;'><a href='" + portalUrl + "' style='color: #007bff; text-decoration: none;font-weight:bold'>" + "View Details" + "</a></p>" + "</div>";
                emailService.sendEmail(user.getEmail(), "Reminder mail on Neram tool", htmlContent);
            }
        }
        return "Mail Sended Successfully";
    }
}
