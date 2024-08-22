package pm.pdfInterface;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;

public interface ContracterBasedPdf {

	ResponseEntity<?> gettheUserData(int id, LocalDate date);

}
