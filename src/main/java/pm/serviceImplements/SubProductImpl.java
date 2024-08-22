package pm.serviceImplements;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.exception.ProductNameAlreadyExistsException;
import pm.model.product.SubProduct;
import pm.repository.SubProductRepository;
import pm.request.SubProductRequest;
import pm.response.ApiResponse;
import pm.service.SubProductService;

@Service
public class SubProductImpl implements SubProductService {
    @Autowired
    private SubProductRepository subprodRepo;

    @Value("${fileBasePath}")
    private String fileBasePath;

    @Value("${getPath}")
    private String getPath;

    @Override
    public ResponseEntity<?> create(SubProductRequest subProductRequest, List<MultipartFile> files) {

        List<SubProduct> createdSubProducts = new ArrayList<>();
        List<SubProduct> subProductRequestList = subProductRequest.getSubProducts();

        for (int i = 0; i < subProductRequestList.size(); i++) {
            SubProduct subprod_data = subProductRequestList.get(i);
            MultipartFile file = (files != null && i < files.size()) ? files.get(i) : null;
            try {
                if (subprodRepo.existsBySubNameAndProdId(subprod_data.getSubName(),
                        subprod_data.getProdId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "Sub Product with the same name already exists",
                                    Collections.emptyList()));
                }

                LocalDateTime currDateTime = LocalDateTime.now();
                SubProduct subprodEntity = new SubProduct();
                subprodEntity.setSubName(subprod_data.getSubName());
                subprodEntity.setProdId(subprod_data.getProdId());
                subprodEntity.setCategory(subprod_data.getCategory());
                subprodEntity.setStartDate(subprod_data.getStartDate());
                subprodEntity.setEndDate(subprod_data.getEndDate());
                subprodEntity.setCreatedAt(currDateTime);
                subprodEntity.setUpdatedAt(currDateTime);
                subprodEntity.setIsDeleted(false);
                subprodEntity.setStatus("Pending");

                if (file != null && !file.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    String formattedTime = currDateTime.format(formatter);
                    String fileName = formattedTime + "_" +
                            file.getOriginalFilename();
                    Path path = Paths.get(fileBasePath + fileName);
                    Files.copy(file.getInputStream(), path,
                            StandardCopyOption.REPLACE_EXISTING);
                    subprodEntity.setFile(fileName);
                }

                SubProduct savedSubProduct = subprodRepo.save(subprodEntity);
                createdSubProducts.add(savedSubProduct);
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new ApiResponse(false, "Internal Server Error: " + e.getMessage(),
                                Collections.emptyList()));
            }
        }

        String message = "Sub Products Created Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, null));
    }

}
