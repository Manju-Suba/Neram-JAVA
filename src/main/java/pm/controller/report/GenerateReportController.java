package pm.controller.report;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import pm.pdfGenerator.EnterNotEnterd;
import pm.pdfGenerator.PdfGenerator;
import pm.pdfInterface.PdfExcelReport;
import pm.response.ApiResponse;
import pm.service.GenerateReport;

@RequestMapping("/GenerateReport")
@RestController
public class GenerateReportController {

    @Autowired
    private GenerateReport genReport;
    @Autowired
    private PdfExcelReport pdfExcelReport1;

    
    @Autowired
    private PdfGenerator pdfGenerator;
    
    @Autowired
    private EnterNotEnterd enterNotEnterd;
    private static final int DEFAULT_USER_ID = 0;

    // To get contract person under supervisor
    @Operation(summary = "Supervisor Based  Contract Persons List" ,hidden = true)
    @GetMapping("/Supervisorcontract/{date}")
    public ResponseEntity<?> report(@PathVariable("date") LocalDate date) {
        return genReport.generateReport(date);
    }

    @Operation(summary = "Supervisor Based  Contract Persons List")
    @GetMapping("/Supervisorcontract/daterange/{fromdate}/{todate}/{page}/{size}")
    public ResponseEntity<?> reportDateRange(
            @PathVariable("fromdate") LocalDate fromdate,
            @PathVariable("todate") LocalDate todate,
            @PathVariable("page") int page,
            @PathVariable("size") int size,
            @RequestParam(value = "userid", required = false) String userIdStr,
            @RequestParam(value = "roletype", required = false) String roleType) {

        int userId = userIdStr != null ? Integer.parseInt(userIdStr) : DEFAULT_USER_ID;
        return genReport.generateReportDateRange(fromdate, todate, userId,page,size,roleType);
    }
    @GetMapping("/Supervisorcontract/daterange/pdf/report/{userids}/{fromdate}/{todate}")
    public ResponseEntity<?>  getReportDateRange(LocalDate fromdate, LocalDate todate,List<Integer>userids){
        return genReport.generateReportDateRange(fromdate, todate, userids);
    }


    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("contractmembers")
    public ResponseEntity<?> getContractPerson(@RequestParam("roletype")String roletype ) {
        return genReport.getContractPersonList(roletype);
    }


    // To get contract person
    @GetMapping("/contract/{fromdate}/{todate}/{page}/{size}")
    public ResponseEntity<?> getContract(@PathVariable("fromdate") LocalDate fromdate,@PathVariable("todate") LocalDate todate,@PathVariable("page") int page,@PathVariable("size")int size) {
        return genReport.getContractPerson(fromdate, todate, page, size);

    }

    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("/{id}/{date}")
    public List<Map<String, Object>> getPdfExcelReport(@PathVariable("id") int id,
            @PathVariable("date") LocalDate date) {
        return genReport.pdfExcelReport(id, date);
    }

    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("/id/{id}/date/{date}")
    public ResponseEntity<?> getPersonWorkDetail(@PathVariable("id") int id, @PathVariable("date") LocalDate date) {
        return genReport.getUsersDataList(id, date);
    }
    @Operation(summary = "Persons Details  View based on date range and Id ")
    @GetMapping("/id/{id}/date/{fromdate}/{todate}")
    public ResponseEntity<?> getPersonWorkDetailDateRange(@PathVariable("id") int id, @PathVariable("fromdate") LocalDate fromdate,@PathVariable("todate") LocalDate todate) {
        return genReport.getUsersDataListdaterange(id, fromdate,todate);
    }

    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("/generate/{id}/{date}")
    public ResponseEntity<byte[]> generateExcelReport(
            @PathVariable int id,
            @PathVariable LocalDate date) {

        try {
            List<Map<String, Object>> reports = pdfExcelReport1.pdfExcelReport(id, date);

            // Generate Excel file using the service method
            byte[] excelBytes = pdfExcelReport1.generateExcelFile(reports, id, date);

            // Set the file name dynamically
            String fileName = "report_" + date + ".xlsx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);

            // Return ResponseEntity with the file content, headers, and status
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);

        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions appropriately, e.g., return an error response
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("/downloadExcelFile")
    public void downloadExcelFile(HttpServletResponse response,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Integer supervisorid,
            @RequestParam(required = false) Integer memberid,
            @RequestParam String status) throws java.io.IOException {
        int supervisorIdValue = 0; // Default value in case supervisorid is null
        int memberIdValue = 0; // Default value in case memberid is null

        if (supervisorid != null) {
            supervisorIdValue = supervisorid.intValue();
        }
        if (memberid != null) {
            memberIdValue = memberid.intValue();
        }

        ResponseEntity<byte[]> byteArrayResponse = enterNotEnterd.getMembersUnderSupervisor(date, supervisorIdValue,
                memberIdValue, status);
        byte[] byteArray = byteArrayResponse.getBody();

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=contacts.xlsx");

        // Write the byte array to the response output stream
        response.getOutputStream().write(byteArray);
    }

    // daterange mani code
    @Operation(summary = "Report Downlode in Excel File Format Enter and Not Enter ")
    @GetMapping("/downloadExcel")
    public void downloadExcel(HttpServletResponse response, @RequestParam(required = false) LocalDate fromDate,  
                @RequestParam(required = false) LocalDate toDate,
                @RequestParam(required = false) Integer supervisorid,
                @RequestParam(required = false) Integer memberid,
                              @RequestParam(required = false) String companyid,@RequestParam(required = false) String roleType,
                @RequestParam String status) throws IOException{
                int supervisorIdValue = 0; // Default value in case supervisorid is null
                int memberIdValue = 0; // Default value in case memberid is null

                if (supervisorid != null) {
                    supervisorIdValue = supervisorid.intValue();
                }
                if ( memberid!= null) {
                    memberIdValue = memberid.intValue();
                }
                ResponseEntity<byte[]> byteArrayResponse = enterNotEnterd.getMembersUnderSupervisorDetail(fromDate, toDate, supervisorIdValue, memberIdValue,companyid,status,roleType);
                byte[] byteArray = byteArrayResponse.getBody();

                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename=contacts.xlsx");
        
                // Write the byte array to the response output stream
                response.getOutputStream().write(byteArray);
    }
    @Operation(summary = "Generates a report date range",hidden = true)
    @GetMapping("pdf/{id}/{date}")
    ResponseEntity<?> getPersonPdf(@PathVariable("id") int id, @PathVariable("date") LocalDate date) {

        try {
            ResponseEntity<?> response = pdfGenerator.gettheUserData(id, date);

            if (response != null && response.getBody() instanceof ApiResponse apiResponse) {

                if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() instanceof byte[] pdfBytes) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "report.pdf");
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Invalid response format", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error generating or processing PDF", null));
        }
    }

    @Operation(summary = "PDF Report Downlode for Date Range Based User ", description = "Generates and downloads a PDF report containing data within the specified date range. This endpoint accepts a start date and an end date to define the range. The report includes information such as user activities, tasks completed, and other relevant data. The generated PDF report is available for download by the user.")

    @GetMapping("pdf/{id}/{fromdate}/{todate}")
    ResponseEntity<?> getContractPersonPdfDateRange(@PathVariable("id") Integer id, @PathVariable("fromdate") LocalDate fromdate,@PathVariable("todate") LocalDate todate) {

        try {
            ResponseEntity<?> response = pdfGenerator.userDataDateRange(id, fromdate, todate);

            if (response != null && response.getBody() instanceof ApiResponse apiResponse) {

                if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() instanceof byte[] pdfBytes) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "report.pdf");
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Invalid response format", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error generating or processing PDF", null));
        }
    }


    @Operation(summary = "Contract Persond PDF For all ")
    @GetMapping("contractpdf/{ids}/{fromdate}/{todate}")
    ResponseEntity<?> getContractPersonPdfDateRangeUserId(@PathVariable("ids") List<Integer> id, @PathVariable("fromdate") LocalDate fromdate,@PathVariable("todate") LocalDate todate) {

        try {
            ResponseEntity<?> response = pdfGenerator.userDataDateRangeUsers(id, fromdate, todate);

            if (response != null && response.getBody() instanceof ApiResponse apiResponse) {

                if (apiResponse != null && apiResponse.isStatus() && apiResponse.getData() instanceof byte[] pdfBytes) {
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_PDF);
                    headers.setContentDispositionFormData("attachment", "report.pdf");
                    return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
                }
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Invalid response format", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error generating or processing PDF", null));
        }
    }


}
