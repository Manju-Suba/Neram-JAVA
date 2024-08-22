package pm.pdfGenerator;

import pm.pdfInterface.PdfExcelReport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.ReportDTO;
import pm.dto.UserTaskActivityResponseStatus;
import pm.model.users.Users;
import pm.dto.ReportDTO.ProductDetailDTO;
import pm.repository.AttendanceSheetRepository;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.TaskRepository;
import pm.response.ApiResponse;
import pm.service.MemberSupervisorService;

import pm.dto.PdfReportDTO;
import pm.dto.PdfReportDTO.ProductDetailsDto;
import pm.repository.UsersRepository;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Service
public class PdfExcelReportImpl implements PdfExcelReport {
	@Autowired
	private TaskRepository taskRepository;

	// @Autowired
	// private MemberSupervisorService memberSupervisorService;

	@Autowired
	private CommonTimeSheetActivityRepository commontimeRepo;

	@Autowired
	private UsersRepository usersRepository;

	@Autowired
	private AttendanceSheetRepository attendanceSheetRepository;

	@Override
	public List<Map<String, Object>> pdfExcelReport(int id, LocalDate date) {
		List<Object[]> userList = taskRepository.getUsersDataWithIdAndDate(id, date);
		String commonTimeSheet = commontimeRepo.findHoursByUserIdAndActivityDate(id, date);

		return mapToReports(userList, commonTimeSheet);
	}

	private List<Map<String, Object>> mapToReports(List<Object[]> rawResults, String commonTimeSheet) {
		List<Map<String, Object>> reports = new ArrayList<>();

		if (!rawResults.isEmpty()) {
			Object[] firstUser = rawResults.get(0);
			Map<String, Object> userReport = new HashMap<>();
			ReportDTO summaryDTO = mapToBasicDetail(firstUser);
			userReport.put("UserDetail", summaryDTO);

			List<ProductDetailDTO> allDetailedDTOList = new ArrayList<>();

			for (Object[] result : rawResults) {
				List<ProductDetailDTO> detailedDTOList = mapToProductDetails(result, commonTimeSheet);
				allDetailedDTOList.addAll(detailedDTOList);
			}

			userReport.put("TaskDetail", allDetailedDTOList);
			reports.add(userReport);
		}

		return reports;
	}

	private ReportDTO mapToBasicDetail(Object[] result) {
		ReportDTO dto = new ReportDTO();
		dto.setName((String) result[1]);
		dto.setEmail((String) result[2]);
		Optional.ofNullable(result[3]).map(timestamp -> ((Timestamp) timestamp).toLocalDateTime())
				.ifPresent(dto::setDate_of_joining);

		return dto;
	}

	ReportDTO reportdto = new ReportDTO();

	private List<ProductDetailDTO> mapToProductDetails(Object[] result, String commonTimeSheet) {
		List<ProductDetailDTO> productDetailsList = new ArrayList<>();
		Optional<Object> taskDateOptional = Optional.ofNullable(result[5]);
		if (taskDateOptional.isPresent()) {
			ProductDetailDTO dto = reportdto.new ProductDetailDTO();
			Optional.ofNullable(result[5])
					.map(value -> {
						if (value instanceof Date) {
							return ((Date) value).toLocalDate();
						} else if (value instanceof String) {
							return LocalDate.parse((String) value);
						} else {
							return null;
						}
					})
					.ifPresent(localDate -> dto.setDate(localDate));

			Optional.ofNullable(result[5])
					.map(value -> {
						if (value instanceof Date) {
							return ((Date) value).toLocalDate().getDayOfWeek();
						} else {
							return null;
						}
					})
					.ifPresent(day -> {
						if (day != null) {
							dto.setDay(day.toString());
						}
					});
			dto.setProduct((String) result[4]);
			dto.setTask((String) result[6]);
			dto.setDescription((String) result[9]);
			dto.setTask_status((String) result[8]);
			dto.setTask_work_hour((String) result[7]);
			dto.setAttendance_status("present");
			dto.setTotal_work_hours(commonTimeSheet);
			productDetailsList.add(dto);
		}
		return productDetailsList;
	}

	public byte[] generateExcelFile(int id, LocalDate date) throws IOException {
		List<Map<String, Object>> reports = pdfExcelReport(id, date);
		return generateExcelFile(reports, id, date);
	}

	@Override
	public byte[] generateExcelFile(List<Map<String, Object>> reports, int id, LocalDate date) {

		if (reports.isEmpty()) {
			throw new IllegalArgumentException("No records found for the specified criteria.");
		}

		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Report");

			CellStyle headerStyle = createHeaderStyle(workbook, 173, 216, 230); // Very light blue
			CellStyle headerStyleBasicDetail = createSubHeaderStyle(workbook, 255, 218, 185); // PeachPuff
			CellStyle headerStyleTaskDetail = createHeaderStyle(workbook, 173, 216, 230); // Very light blue

			int rowNum = 1;

			// Adjust the start index for the column to be left null
			int startColumnIndex = 1;

			int columnWidth = getColumnWidth("Time Sheet - Cavin Infotech");
			for (int i = 2; i <= 3; i++) {
				sheet.setColumnWidth(i, columnWidth);
			}

			for (Map<String, Object> report : reports) {
				ReportDTO userDetail = (ReportDTO) report.get("UserDetail");

				Row headerRowUserDetail = sheet.createRow(rowNum++);
				Cell headerCell = headerRowUserDetail.createCell(startColumnIndex);
				headerCell.setCellValue("Time Sheet - Cavin Infotech");
				headerCell.setCellStyle(headerStyle);
				sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, startColumnIndex, 8));

				rowNum = addBasicDetails(sheet, rowNum, userDetail, headerStyleBasicDetail, workbook, startColumnIndex);
				rowNum++;

				rowNum = addTaskDetails(sheet, rowNum, report, headerStyleTaskDetail, workbook, id, date,
						startColumnIndex);
				rowNum++;
			}

			// Setting column width
			for (int i = 1; i <= 8; i++) {
				sheet.setColumnWidth(i, 5000);
			}

			// Apply cell outlines
			int startRow = 1; // Assuming "Time Sheet - Cavin Infotech" is the first row
			int lastCol = 8; // Up to column 7
			applyOutline(sheet, lastCol, workbook, startRow, startColumnIndex); // Apply the outline starting from the
																				// specified startRow

			workbook.write(out);
			return out.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;

		}

	}

	private int getColumnWidth(String content) {
		int contentLength = content.length();
		return (contentLength + 2) * 256;
	}

	// for header
	private CellStyle createHeaderStyle(Workbook workbook, int red, int green, int blue) {
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 18);
		headerStyle.setFont(headerFont);

		byte[] rgb = new byte[3];
		rgb[0] = (byte) red;
		rgb[1] = (byte) green;
		rgb[2] = (byte) blue;
		XSSFColor xssfColor = new XSSFColor(rgb, null);
		headerStyle.setFillForegroundColor(xssfColor);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		return headerStyle;
	}

	// for Sub Headers Basic detail
	private CellStyle createSubHeaderStyle(Workbook workbook, int red, int green, int blue) {
		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 16);
		headerStyle.setFont(headerFont);

		byte[] rgb = new byte[3];
		rgb[0] = (byte) red;
		rgb[1] = (byte) green;
		rgb[2] = (byte) blue;
		XSSFColor xssfColor = new XSSFColor(rgb, null);
		headerStyle.setFillForegroundColor(xssfColor);
		headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		headerStyle.setAlignment(HorizontalAlignment.LEFT);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		return headerStyle;
	}

	// This method will set only BG color
	private CellStyle createTaskValueStyle(Workbook workbook, int red, int green, int blue) {
		CellStyle valueStyle = workbook.createCellStyle();
		valueStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) red, (byte) green, (byte) blue }, null));
		valueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return valueStyle;
	}

	// This method will sets the content bold and BG color
	private CellStyle createValueStyle(Workbook workbook, int red, int green, int blue) {
		// Create a new cell style
		CellStyle valueStyle = workbook.createCellStyle();
		// Create a new font and set it to bold
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		// Set the bold font to the cell style
		valueStyle.setFont(boldFont);
		// Set the fill foreground color and fill pattern for the cell style
		valueStyle.setFillForegroundColor(new XSSFColor(new byte[] { (byte) red, (byte) green, (byte) blue }, null));
		valueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		// Return the created cell style
		return valueStyle;
	}

	private int addBasicDetails(Sheet sheet, int rowNum, ReportDTO userDetail, CellStyle headerStyle, Workbook workbook,
			int startColumnIndex) {
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		CellStyle boldCellStyle = workbook.createCellStyle();
		boldCellStyle.setFont(boldFont);

		Row headerRowUserDetail = sheet.createRow(rowNum++);
		Cell headerCell = headerRowUserDetail.createCell(startColumnIndex); // Use startColumnIndex
		headerRowUserDetail.createCell(startColumnIndex).setCellValue("Basic Detail");
		headerCell.setCellStyle(headerStyle);
		sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, startColumnIndex, startColumnIndex + 7)); // Adjust
																														// column
																														// indexes
		// Create a new cell style for the Basic Details values with custom background
		// color
		CellStyle valueStyle = createTaskValueStyle(workbook, 212, 239, 223); // Light grayish cyan-lime green

		// Add Name
		Row nameRow = sheet.createRow(rowNum);
		Cell nameCellLabel = nameRow.createCell(startColumnIndex);
		nameCellLabel.setCellValue("Name");
		nameCellLabel.setCellStyle(boldCellStyle);
		Cell nameCellValue = nameRow.createCell(startColumnIndex + 1);
		nameCellValue.setCellValue(userDetail.getName());
		nameCellValue.setCellStyle(valueStyle);

		// Merge columns 3 and 4 for NAME
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startColumnIndex + 1, startColumnIndex + 2)); // Adjusted
																													// column
																													// indexes
		// Increment rowNum by 1 to move to the next row
		rowNum++;

		// Add Email
		Row emailRow = sheet.createRow(rowNum);
		Cell emailCellLabel = emailRow.createCell(startColumnIndex);
		emailCellLabel.setCellValue("Email");
		emailCellLabel.setCellStyle(boldCellStyle);
		Cell emailCellValue = emailRow.createCell(startColumnIndex + 1);
		emailCellValue.setCellValue(userDetail.getEmail());
		emailCellValue.setCellStyle(valueStyle);

		// Merge columns 3 and 4 for EMAIL
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startColumnIndex + 1, startColumnIndex + 2)); // Adjusted
																													// column
																													// indexes

		// Increment rowNum by 1 to move to the next row
		rowNum++;

		// Add DOJ
		Row dojRow = sheet.createRow(rowNum);
		Cell dojCellLabel = dojRow.createCell(startColumnIndex);
		dojCellLabel.setCellValue("DOJ");
		dojCellLabel.setCellStyle(boldCellStyle);
		Cell dojCellValue = dojRow.createCell(startColumnIndex + 1);
		dojCellValue.setCellValue(userDetail.getDate_of_joining().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		dojCellValue.setCellStyle(valueStyle);

		// Merge columns 3 and 4 for DOJ
		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startColumnIndex + 1, startColumnIndex + 2)); // Adjusted
																													// column
																													// indexes

		// Increment rowNum by 1 to move to the next row
		rowNum++;

		return rowNum;
	}

	private int addTaskDetails(Sheet sheet, int rowNum, Map<String, Object> report, CellStyle mildOrangeStyle,
			Workbook workbook, int id, LocalDate date, int startColumnIndex) {
		String totalWorkHours = commontimeRepo.findHoursByUserIdAndActivityDate(id, date);

		Row headerRowTaskDetails = sheet.createRow(rowNum++);
		Cell headerCell = headerRowTaskDetails.createCell(startColumnIndex); // Use startColumnIndex
		headerCell.setCellValue("Task Detail");
		headerCell.setCellStyle(mildOrangeStyle);
		sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, startColumnIndex, startColumnIndex + 7)); // Adjust
																														// column
																														// indexes

		CellStyle headerStyle = createValueStyle(workbook, 173, 216, 230);// Very light blue
		Row headerRowTaskDetail = sheet.createRow(rowNum++);

		String[] headers = { "Date", "Day", "Attendance Status", "Product", "Task", "Description", "Status",
				"Work Hour" };
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRowTaskDetail.createCell(startColumnIndex + i); // Use startColumnIndex
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerStyle); // Apply the header style
		}

		// Create a new cell style for the AttendanceStatus and work hours
		CellStyle valueStyle = createTaskValueStyle(workbook, 212, 239, 223);// Light grayish cyan-lime green
		List<ProductDetailDTO> taskDetailList = (List<ProductDetailDTO>) report.get("TaskDetail");
		for (ProductDetailDTO taskDetail : taskDetailList) {
			Row rowTaskDetail = sheet.createRow(rowNum++);
			rowTaskDetail.createCell(startColumnIndex).setCellValue(taskDetail.getDate().toString()); // Use
																										// startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 1).setCellValue(taskDetail.getDay()); // Use startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 2).setCellValue(taskDetail.getAttendance_status()); // Use
																											// startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 3).setCellValue(taskDetail.getProduct()); // Use
																									// startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 4).setCellValue(taskDetail.getTask()); // Use startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 5).setCellValue(taskDetail.getDescription()); // Use
																										// startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 6).setCellValue(taskDetail.getTask_status()); // Use
																										// startColumnIndex
			rowTaskDetail.createCell(startColumnIndex + 7).setCellValue(taskDetail.getTask_work_hour()); // Use
																											// startColumnIndex

			// Apply the custom background color style to "Attendance Status" and "Work
			// Hour" cells
			for (int i = 2; i <= 7; i++) {
				if (i == 2 || i == 7) {
					rowTaskDetail.getCell(startColumnIndex + i).setCellStyle(valueStyle); // Use startColumnIndex
				}
			}
		}

		CellStyle headerStyle1 = createValueStyle(workbook, 225, 141, 84);// Light Orange
		Row totalWorkHoursRow = sheet.createRow(rowNum++);
		Cell totalWorkHoursCell = totalWorkHoursRow.createCell(startColumnIndex + 6); // Create the cell
		totalWorkHoursCell.setCellValue("Total Work Hours"); // Set the value
		// totalWorkHoursCell.setCellStyle(boldCellStyle); // Apply bold style
		totalWorkHoursCell.setCellStyle(headerStyle1); // Apply background color
		// totalWorkHoursRow.createCell(7).setCellValue(totalWorkHours);

		Cell totalWorkHoursCellValue = totalWorkHoursRow.createCell(startColumnIndex + 7); // Create the cell for value
		totalWorkHoursCellValue.setCellValue(totalWorkHours); // Set the value
		totalWorkHoursCellValue.setCellStyle(createTaskValueStyle(workbook, 225, 141, 84)); // Apply background color to
																							// the value

		return rowNum;
	}

	// SETTING OUTLINE
	private void applyOutline(Sheet sheet, int lastCol, Workbook workbook, int startRow, int startCol) {
		// Iterate through each row in the sheet starting from the specified startRow
		for (int rowIdx = startRow; rowIdx <= sheet.getLastRowNum(); rowIdx++) {
			Row row = sheet.getRow(rowIdx);
			if (row == null) {
				row = sheet.createRow(rowIdx); // Create the row if it doesn't exist
			}
			// Iterate through each cell in the row up to the last column
			for (int colIdx = startCol; colIdx <= lastCol; colIdx++) {
				Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				CellStyle cellStyle = cell.getCellStyle();

				// Create a new cell style or clone the existing one to avoid modifying it
				CellStyle newStyle = (cellStyle != null) ? workbook.createCellStyle() : null;

				// Apply existing style if it exists
				if (cellStyle != null) {
					newStyle.cloneStyleFrom(cellStyle);
				} else {
					newStyle = workbook.createCellStyle();
				}

				// Apply the border style to the new style based on the cell's position
				if (rowIdx == startRow) {
					// Very first row: top line thick
					newStyle.setBorderTop(BorderStyle.MEDIUM);
				} else if (rowIdx == sheet.getLastRowNum()) {
					// Last row: bottom line medium thick
					newStyle.setBorderBottom(BorderStyle.MEDIUM);
				} else {
					// Other rows: thin lines
					newStyle.setBorderTop(BorderStyle.THIN);
					newStyle.setBorderBottom(BorderStyle.THIN);
				}

				if (colIdx == startCol) {
					// First column: left line medium thick
					newStyle.setBorderLeft(BorderStyle.MEDIUM);
				} else if (colIdx == lastCol) {
					// Last column: right line medium thick
					newStyle.setBorderRight(BorderStyle.MEDIUM);
				} else {
					// Other columns: thin lines
					newStyle.setBorderLeft(BorderStyle.THIN);
					newStyle.setBorderRight(BorderStyle.THIN);
				}

				// Set the new style to the cell
				cell.setCellStyle(newStyle);
			}
		}
	}
}

// pdf report
// @Override
// public ResponseEntity<?> gettheUserData(int id, LocalDate date) {
// try {
// List<Object[]> userList = usersRepository.getUsersDataWithIdAndDate(id,
// date);
// String commonTimeSheet =
// commontimeRepo.findHoursByPersonIdAndActivityDate(id, date);
// List<Map<String, Object>> reports = maptoReports(userList, commonTimeSheet ,
// id , date);
// String message = "Data Fetched Successfully.";
// Map<String, Object> response = new HashMap<>();
// response.put("report", reports);
// byte[] pdfBytes = generatePdf(reports);
//
// return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
// message, pdfBytes));
// } catch (Exception e) {
// e.printStackTrace();
// return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
// .body(new ApiResponse(false, "Error generating or processing PDF", null));
// }
//
// }
//
// private List<Map<String, Object>> maptoReports(List<Object[]> rawResults,
// String commonTimeSheet , int id , LocalDate date) {
// List<Map<String, Object>> reports = new ArrayList<>();
// Map<String, Object> userReport = new HashMap<>();
//
// if(!rawResults.isEmpty()){
// Object[] firstUser = rawResults.get(0);
// PdfReportDTO summaryDTO = mapToBasicDetail(firstUser, commonTimeSheet, id ,
// date);
// PdfReportDTO summaryDTOList = mapToBasicDetail(firstUser, commonTimeSheet,
// id, date );
// if(summaryDTOList == null){
//
// summaryDTOList = new PdfReportDTO();
// Users userData = usersRepository.findById(id).orElse(null);
// String preset = attendanceSheetRepository.findStatusByUserId(id, date);
// DayOfWeek dayOfWeek = date.getDayOfWeek();
// summaryDTOList.setName(userData.getName());
// summaryDTOList.setEmail(userData.getEmail());
// summaryDTOList.setAttendanceStatus(preset);
// summaryDTOList.setDate_of_join(userData.getCreated_at().toLocalDate());
// summaryDTOList.setDay(dayOfWeek.toString());
//
// }
// userReport.put("UserDetail", summaryDTOList);
// List<ProductDetailsDto> allDetailsDtos = new ArrayList<>();
// for (Object[] result : rawResults) {
// List<ProductDetailsDto> detailedDtoList = mapToProductDetails(result);
// allDetailsDtos.addAll(detailedDtoList);
// }
// userReport.put("TaskDetail", allDetailsDtos);
// reports.add(userReport);
// }else{
// Optional<Users> userdata = usersRepository.findById(id);
// String preset = attendanceSheetRepository.findStatusByUserId(id, date);
//
// DayOfWeek dayOfWeek = date.getDayOfWeek();
// PdfReportDTO basicdetail = new
// PdfReportDTO(userdata.get().getName(),userdata.get().getEmail(),
// userdata.get().getCreated_at().toLocalDate(), dayOfWeek.toString(),preset,
// "");
// userReport.put("UserDetail", basicdetail);
// reports.add(userReport);
//
// }
//
// return reports;
// }
//
// private PdfReportDTO mapToBasicDetail(Object[] result, String commonTimeSheet
// ,int id , LocalDate date) {
// PdfReportDTO dto = new PdfReportDTO();
// dto.setName((String) result[1]);
// dto.setEmail((String) result[2]);
// Optional.ofNullable(result[3])
// .map(timestamp -> ((Timestamp) timestamp).toLocalDateTime())
// .ifPresent(dateTime -> {
// dto.setDate_of_join(dateTime.toLocalDate()); // Extracting date part and
// setting it in DTO
// });
// String preset = "Present";
// dto.setAttendanceStatus(preset);
// // dto.setAttendanceStatus((String) result[4]);
// Optional.ofNullable(result[5])
// .map(sqlDate -> ((Date) sqlDate).toLocalDate())
// .map(LocalDate::getDayOfWeek)
// .ifPresent(day -> dto.setDay(day.toString()));
// dto.setTotal_work_hours(commonTimeSheet);
// return dto;
// }
//
// PdfReportDTO reportDTO = new PdfReportDTO();
//
// private List<ProductDetailsDto> mapToProductDetails(Object[] result) {
// List<ProductDetailsDto> productDetailsList = new ArrayList<>();
//
// ProductDetailsDto dto = reportDTO.new ProductDetailsDto();
// dto.setProduct_Name((String) result[4]);
// Optional.ofNullable(result[5]).map(sqlDate -> ((Date) sqlDate).toLocalDate())
// .ifPresent(dto::setActivity_date);
// dto.setTask((String) result[6]);
// dto.setDescription((String) result[9]);
// dto.setTask_Status((String) result[8]);
// dto.setWork_hour((String) result[7]);
//
// productDetailsList.add(dto);
// return productDetailsList;
// }
//
//
//// private byte[] generatePdf(List<Map<String, Object>> data) {
//// try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//// Document document = new Document();
//// PdfWriter writer = PdfWriter.getInstance(document, outputStream);
//// document.open();
////
//// if (!data.isEmpty()) {
//// for (Map<String, Object> report : data) {
//// if (!report.isEmpty()) {
//// PdfReportDTO summaryDTO = (PdfReportDTO) report.get("UserDetail");
//// document.add(new Paragraph("Name: " + summaryDTO.getName()));
//// document.add(new Paragraph("Email: " + summaryDTO.getEmail()));
//// document.add(new Paragraph("Date of Joining: " +
//// summaryDTO.getDate_of_join().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//// document.add(new Paragraph("Attendance Status: " +
//// summaryDTO.getAttendanceStatus()));
//// document.add(new Paragraph("Day: " + summaryDTO.getDay()));
//// document.add(new Paragraph("Total Work Hours: " +
// summaryDTO.getTotal_work_hours()));
////
//// List<?> rawTaskDetails = (List<?>) report.get("TaskDetail");
////
//// if (rawTaskDetails != null && !rawTaskDetails.isEmpty()) {
//// List<ProductDetailsDto> taskDetails = rawTaskDetails.stream()
//// .map(task -> (ProductDetailsDto) task)
//// .collect(Collectors.toList());
////
//// // Create a table
//// PdfPTable table = new PdfPTable(6);
////
//// // Add table headers
//// table.addCell("Product Name");
//// table.addCell("Activity Date");
//// table.addCell("Task");
//// table.addCell("Work Hour");
//// table.addCell("Task Status");
//// table.addCell("Description");
////
//// // Add data to the table
//// for (ProductDetailsDto task : taskDetails) {
//// table.addCell(task.getProduct_Name());
//// table.addCell(task.getActivity_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//// table.addCell(task.getTask());
//// table.addCell(task.getWork_hour());
//// table.addCell(task.getTask_Status());
//// table.addCell(task.getDescription());
//// }
////
//// // Add the table to the document
//// document.add(table);
//// }
////
//// // Start a new page for the next report
//// document.newPage();
//// } else {
//// PdfReportDTO summaryDTO = (PdfReportDTO) report.get("UserDetail");
//// document.add(new Paragraph("Name: " + summaryDTO.getName()));
//// document.add(new Paragraph("Email: " + summaryDTO.getEmail()));
//// document.add(new Paragraph("Date of Joining: " +
//// summaryDTO.getDate_of_join().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//// document.add(new Paragraph("Attendance Status: " +
//// summaryDTO.getAttendanceStatus()));
//// document.add(new Paragraph("Day: " + summaryDTO.getDay()));
//// document.add(new Paragraph("Total Work Hours: " +
// summaryDTO.getTotal_work_hours()));
////
//// // Start a new page for the next report
//// document.newPage();
//// }
//// }
//// } else {
//// // If there are no records found, add a message or handle it as needed
//// document.add(new Paragraph("No records found for the given date."));
//// }
////
//// document.close();
//// writer.close();
////
//// return outputStream.toByteArray();
//// } catch (DocumentException | IOException e) {
//// e.printStackTrace();
//// }
//// return null;
//// }
//
//
//
//
// private byte[] generatePdf(List<Map<String, Object>> data) {
// try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
// Document document = new Document();
// PdfWriter writer = PdfWriter.getInstance(document, outputStream);
// document.open();
//
// if (!data.isEmpty()) {
// for (Map<String, Object> report : data) {
// if (!report.isEmpty()) {
// // Create a table with two columns for the card layout
// PdfReportDTO summaryDTO = (PdfReportDTO) report.get("UserDetail");
// PdfPTable table = new PdfPTable(2);
// table.setWidthPercentage(100); // Set the width of the table to 100%
//
// // Add data to the first card (left column)
// PdfPCell cell1 = new PdfPCell(new Paragraph("Name: " +
// summaryDTO.getName()));
// cell1.setColspan(2); // Span the entire row
// table.addCell(cell1);
// table.addCell("Email:");
// table.addCell(summaryDTO.getEmail());
// table.addCell("Date of Joining:");
// table.addCell(summaryDTO.getDate_of_join().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
//
// // Add data to the second card (right column)
// PdfPCell cell2 = new PdfPCell(new Paragraph("Attendance Status: " +
// summaryDTO.getAttendanceStatus()));
// cell2.setColspan(2); // Span the entire row
// table.addCell(cell2);
// table.addCell("Day:");
// table.addCell(summaryDTO.getDay());
// table.addCell("Total Work Hours:");
// table.addCell(summaryDTO.getTotal_work_hours());
//
// // Add the table to the document
// document.add(table);
//
// // Add some space between cards
// document.add(new Paragraph("\n"));
// List<?> rawTaskDetails = (List<?>) report.get("TaskDetail");
//
// if (rawTaskDetails != null && !rawTaskDetails.isEmpty()) {
// List<ProductDetailsDto> taskDetails = rawTaskDetails.stream()
// .map(task -> (ProductDetailsDto) task)
// .collect(Collectors.toList());
//
// // Create a table
// PdfPTable table1 = new PdfPTable(6);
//
// // Add table headers
// table1.addCell("Product Name");
// table1.addCell("Activity Date");
// table1.addCell("Task");
// table1.addCell("Work Hour");
// table1.addCell("Task Status");
// table1.addCell("Description");
//
// // Add data to the table
// for (ProductDetailsDto task : taskDetails) {
// table1.addCell(task.getProduct_Name());
// table1.addCell(task.getActivity_date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
// table1.addCell(task.getTask());
// table1.addCell(task.getWork_hour());
// table1.addCell(task.getTask_Status());
// table1.addCell(task.getDescription());
// }
//
// // Add the table to the document
// document.add(table1);
// }
//
// // Start a new page for the next report
// document.newPage();
// } else {
// PdfReportDTO summaryDTO = (PdfReportDTO) report.get("UserDetail");
// PdfPTable table = new PdfPTable(2);
// table.setWidthPercentage(100); // Set the width of the table to 100%
//
// // Add data to the first card (left column)
// PdfPCell cell1 = new PdfPCell(new Paragraph("Name: " +
// summaryDTO.getName()));
// cell1.setColspan(2); // Span the entire row
// table.addCell(cell1);
//
//
// // Add the table to the document
// document.add(table);
// // Start a new page for the next report
// document.newPage();
// }
// }
// } else {
// // If there are no records found, add a message or handle it as needed
// document.add(new Paragraph("No records found for the given date."));
// }
//
// document.close();
// writer.close();
//
// return outputStream.toByteArray();
// } catch (DocumentException | IOException e) {
// e.printStackTrace();
// }
// return null;
// }
