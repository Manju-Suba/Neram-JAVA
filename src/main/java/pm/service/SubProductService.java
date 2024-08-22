package pm.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.model.product.SubProduct;
import pm.request.SubProductRequest;

@Service
public interface SubProductService {

    ResponseEntity<?> create(SubProductRequest subProductRequest, List<MultipartFile> files);

}
