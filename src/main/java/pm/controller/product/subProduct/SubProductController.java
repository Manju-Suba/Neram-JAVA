package pm.controller.product.subProduct;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import pm.model.product.SubProduct;
import pm.request.SubProductRequest;
import pm.service.SubProductService;

@RequestMapping("/sub-product")
@CrossOrigin("*")
@RestController
public class SubProductController {

    @Autowired
    private SubProductService subService;
    @Operation(summary = "Generates a report date range",hidden = true)
    @PostMapping("/create")
    public ResponseEntity<?> create(SubProductRequest subProductRequest,@RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        return subService.create(subProductRequest,files);
    }

}
