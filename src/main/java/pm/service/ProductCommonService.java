package pm.service;

import org.springframework.http.ResponseEntity;

public interface ProductCommonService {
	ResponseEntity<?> getProductHead();

	ResponseEntity<?> getTechHead();

	ResponseEntity<?> getDataHead();

	ResponseEntity<?> getProductOwner();

	ResponseEntity<?> getTechOwner();

	ResponseEntity<?> getDataOwner();

	ResponseEntity<?> approvalFlow(int id);

    ResponseEntity<?> getHowHead();

	ResponseEntity<?> gethowOwner();
}
