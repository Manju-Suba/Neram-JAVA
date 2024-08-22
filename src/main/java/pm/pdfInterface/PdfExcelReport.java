package pm.pdfInterface;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

public interface PdfExcelReport {
	List<Map<String, Object>> pdfExcelReport(int id, LocalDate date);

	byte[] generateExcelFile(List<Map<String, Object>> reports, int id, LocalDate date);

}
