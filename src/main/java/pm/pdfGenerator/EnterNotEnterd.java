package pm.pdfGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.UserTaskActivityResponseStatus;
import pm.pdfInterface.PdfInterface;
import pm.response.ApiResponse;
import pm.serviceImplements.MemberSupervisorServiceImp;

@Service
public class EnterNotEnterd implements PdfInterface {

	@Autowired
	private MemberSupervisorServiceImp memberSupervisorServiceImp;

	public ResponseEntity<byte[]> getMembersUnderSupervisor(LocalDate date, int supervisorid, int memberid,
			String status) {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("MembersUnderSupervisor");
			// Set column widths
			sheet.setColumnWidth(0, 2000);
			sheet.setColumnWidth(1, 5000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 4000);
			sheet.setColumnWidth(4, 3000);
			sheet.setColumnWidth(5, 3000);
			sheet.setColumnWidth(6, 3000);

			// Create header row
			Row headerRow = sheet.createRow(0);
			String[] headers = { "ID", "Activity Date", "User Name", "Branch", "Role Type", "Status", "Hours",
					"Supervisor Name", "Supervisor Status" };
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(createHeaderStyle(workbook));
			}

			// Populate data rows
			List<UserTaskActivityResponseStatus> dataList = getDataList(date, supervisorid, memberid, status); // Fetch
																												// data
			int rowNum = 1;
			for (UserTaskActivityResponseStatus rowData : dataList) {
				if (rowData.getStatus().equalsIgnoreCase(status)) {
					Row row = sheet.createRow(rowNum);
					row.createCell(0).setCellValue(rowNum); // Adjust the index if necessary
					row.createCell(1)
							.setCellValue(rowData.getActivity_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					row.createCell(2).setCellValue(rowData.getUserName());
					row.createCell(3).setCellValue(rowData.getBranch());
					row.createCell(4).setCellValue(rowData.getRoleType());

					row.createCell(5).setCellValue(rowData.getStatus());

					row.createCell(6).setCellValue(rowData.getHours());
					row.createCell(7).setCellValue(rowData.getSupervisorName());
					row.createCell(8).setCellValue(rowData.getSupervisorStatus());
					rowNum++; // Increment rowNum only when a new row is created
				}
			}

			// Write workbook to ByteArrayOutputStream
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);

			// Set response headers
			HttpHeaders headers1 = new HttpHeaders();
			headers1.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers1.setContentDispositionFormData("attachment", "timeSheetList.xlsx");

			return ResponseEntity.ok().headers(headers1).body(outputStream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	private CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle headerStyle = workbook.createCellStyle();
		headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		headerStyle.setBorderTop(BorderStyle.THIN);
		headerStyle.setBorderBottom(BorderStyle.THIN);
		headerStyle.setBorderLeft(BorderStyle.THIN);
		headerStyle.setBorderRight(BorderStyle.THIN);
		return headerStyle;
	}

	private List<UserTaskActivityResponseStatus> getDataList(LocalDate date, Integer supervisorid, Integer memberId,
			String status) {
		ResponseEntity<?> responseEntity = null;

		if (date != null && supervisorid == 0 && memberId == 0) {

			responseEntity = memberSupervisorServiceImp.getMembersUnderSupervisorbyalldownlode(date, status);
		} else if (date != null && supervisorid != -1 && memberId == 0) {

			responseEntity = memberSupervisorServiceImp.getMembersUnderSupervisordownload(supervisorid, date, status);
		} else if (date != null && supervisorid != -1 && memberId != -1) {

			responseEntity = memberSupervisorServiceImp.getReport(memberId, date, status);
		}

		Object responseBody = responseEntity.getBody();
		ApiResponse apiResponse = (ApiResponse) responseBody;
		if (apiResponse != null && apiResponse.getData() != null && apiResponse.getData() instanceof List) {
			return (List<UserTaskActivityResponseStatus>) apiResponse.getData();
		} else {
			return List.of(); // Return an empty list or handle appropriately
		}
	}

	@Override
	public ResponseEntity<byte[]> getMembersUnderSupervisorDetail(LocalDate fromDate, LocalDate toDate,
			int supervisorid, int memberid, String company, String status,String roretype) {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("MembersUnderSupervisor");
			// Set column widths
			sheet.setColumnWidth(0, 2000);
			sheet.setColumnWidth(1, 4000);
			sheet.setColumnWidth(2, 5000);
			sheet.setColumnWidth(3, 3000);
			sheet.setColumnWidth(4, 3000);
			sheet.setColumnWidth(5, 3000);
			sheet.setColumnWidth(6, 3000);
			sheet.setColumnWidth(7, 7000);
			sheet.setColumnWidth(8, 5000);

			CellStyle enteredStyle = workbook.createCellStyle();
			enteredStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			enteredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// Create a custom color for a lighter shade of red
			XSSFColor lightRed = new XSSFColor(new byte[] { (byte) 255, (byte) 192, (byte) 192 }, null);

			CellStyle notEnteredStyle = workbook.createCellStyle();
			notEnteredStyle.setFillForegroundColor(lightRed);
			notEnteredStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			CellStyle leaveStyle = workbook.createCellStyle();
			leaveStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			leaveStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

			// Create header row
			Row headerRow = sheet.createRow(0);
			String[] headers = { "ID", "Activity Date", "User Name", "Branch", "Role Type", "Status", "Hours",
					"Supervisor Name", "Supervisor Status" };
			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(createHeaderStyle(workbook));
			}
			// Populate data rows
			Map<LocalDate, List<UserTaskActivityResponseStatus>> dataList = getDataLists(fromDate, toDate, supervisorid,
					memberid, status, company,roretype); // Fetch data
			int rowNum = 1;
			for (Map.Entry<LocalDate, List<UserTaskActivityResponseStatus>> entry : dataList.entrySet()) {
				LocalDate date = entry.getKey();
				List<UserTaskActivityResponseStatus> rowDataList = entry.getValue();

				for (UserTaskActivityResponseStatus rowData : rowDataList) {
					System.out.println(rowData);

					// if (rowData.getStatus().equalsIgnoreCase(status)) {
					Row row = sheet.createRow(rowNum);
					row.createCell(0).setCellValue(rowNum); // Adjust the index if necessary
					row.createCell(1).setCellValue(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
					row.createCell(2).setCellValue(rowData.getUserName());
					row.createCell(3).setCellValue(rowData.getBranch());
					row.createCell(4).setCellValue(rowData.getRoleType());
					String rowstatus = rowData.getStatus();

					if ("entered".equalsIgnoreCase(rowstatus)) {
						row.createCell(5).setCellValue(rowstatus);
						row.getCell(5).setCellStyle(enteredStyle);
					} else if ("not entered".equalsIgnoreCase(rowstatus)) {
						row.createCell(5).setCellValue(rowstatus);
						row.getCell(5).setCellStyle(notEnteredStyle);
					} else if ("leave".equalsIgnoreCase(rowstatus)) {
						row.createCell(5).setCellValue(rowstatus);
						row.getCell(5).setCellStyle(leaveStyle);
					} else {
						row.createCell(5).setCellValue(rowstatus); // Default case
					}
					// row.createCell(5).setCellValue(rowData.getStatus());
					row.createCell(6).setCellValue(rowData.getHours());
					row.createCell(7).setCellValue(rowData.getSupervisorName());
					row.createCell(8).setCellValue(rowData.getSupervisorStatus());
					rowNum++; // Increment rowNum only when a new row is created
					// }
				}
			}
			// Write workbook to ByteArrayOutputStream
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			workbook.write(outputStream);
			// Set response headers
			HttpHeaders headers1 = new HttpHeaders();
			headers1.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			headers1.setContentDispositionFormData("attachment", "timeSheetList.xlsx");

			return ResponseEntity.ok().headers(headers1).body(outputStream.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}

	// Entered and Not Enterd Excel Downlode
	private Map<LocalDate, List<UserTaskActivityResponseStatus>> getDataLists(LocalDate fromDate, LocalDate toDate,
			int supervisorId, int memberId, String status, String company,String roletype) {
		ResponseEntity<?> responseEntity = null;
		Map<LocalDate, List<UserTaskActivityResponseStatus>> resultMap = new HashMap<>();

		if (fromDate != null && toDate != null && supervisorId == 0 && memberId == 0 && company == null&&roletype==null) {
			responseEntity = memberSupervisorServiceImp.getMembersalldownlode(fromDate, toDate, status);
		} else if (fromDate != null && toDate != null && company != null && supervisorId == 0 && memberId == 0&&roletype==null) {

			responseEntity = memberSupervisorServiceImp.getMembersalldownlodecompany(fromDate, toDate, status, company);
		}

		else if (fromDate != null && toDate != null && supervisorId != -1 && memberId == 0&&roletype==null) {

			responseEntity = memberSupervisorServiceImp.getMembersUnderSupervisordownloadDetail(supervisorId, fromDate,
					toDate, status);

		} else if (fromDate != null && toDate != null && supervisorId != -1 && memberId != -1&&roletype==null) {

			responseEntity = memberSupervisorServiceImp.getReportDetail(memberId, fromDate, toDate, status);
		}
		else if (roletype!=null && company==null) {
			responseEntity =	memberSupervisorServiceImp.downloadRetrieveDataRoleBasedWithoutCompany(roletype,fromDate,toDate,company);
		}
		else if (roletype!=null && company!=null) {
		responseEntity =	memberSupervisorServiceImp.downloadRetrieveDataRoleBasedWithoutCompany(roletype,fromDate,toDate,company);
		}

		if (responseEntity != null && responseEntity.getBody() instanceof ApiResponse apiResponse) {
			System.out.println(responseEntity.getBody().toString());
			Object data = apiResponse.getData();
			if (data instanceof Map) {
				System.out.println("111111");
				Map<LocalDate, List<UserTaskActivityResponseStatus>> dataMap = (Map<LocalDate, List<UserTaskActivityResponseStatus>>) data;
				System.out.println("22222");
				resultMap.putAll(dataMap);
			}
		}

		return resultMap;
	}

}
