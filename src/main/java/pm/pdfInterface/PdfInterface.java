package pm.pdfInterface;

import java.io.IOException;
import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletResponse;

public interface PdfInterface {

	 ResponseEntity<byte[]> getMembersUnderSupervisor(LocalDate date, int supervisorid, int memberid,
			String status);

	ResponseEntity<byte[]> getMembersUnderSupervisorDetail(LocalDate fromDate, LocalDate toDate, int supervisorid, int memberid,String company, String status,String roretype);
}
