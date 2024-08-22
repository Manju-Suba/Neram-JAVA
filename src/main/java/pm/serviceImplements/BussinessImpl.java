package pm.serviceImplements;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import pm.model.product.BussinessCategory;
import pm.model.users.Roles;
import pm.repository.BussinessCategoriesRepository;
import pm.response.ApiResponse;
import pm.service.BussinessService;

@Service
@RequiredArgsConstructor
public class BussinessImpl implements BussinessService {

    private final BussinessCategoriesRepository bussinessCategoryRepository;

    @Override
    public ResponseEntity<?> create(String name) {
        LocalDateTime currDateTime = LocalDateTime.now();
//        if (name == null) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(new ApiResponse(false, "Name cannot be null", null));
//        }
        // Check for blank name
        if (name == null || name.trim().isEmpty()) { 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name cannot be blank", null));
        }
        // Validate the name field using the regular expression
        if (name.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name exceeds maximum length of 255 characters", null));
        }
//        Pattern pattern = Pattern.compile("^[a-zA-Z\\s-]+$");
//        Matcher matcher = pattern.matcher(name);
//        if (!matcher.matches()) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                .body(new ApiResponse(false, "Name must contain only alphabetic letters", null));
//        }
        String trimmedName = name.replaceAll("\\s+", " ");

        Optional<BussinessCategory> bussinessCate_Name = bussinessCategoryRepository.findByBussinessNameAndIs_deletedFalse(trimmedName);
        if (bussinessCate_Name.isPresent()) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body(new ApiResponse(false, "Bussiness Category already exist ", Collections.emptyList()));
        }
        BussinessCategory bussinessCategory = new BussinessCategory();
        bussinessCategory.setName(name);
        bussinessCategory.setCreatedAt(currDateTime);
        bussinessCategory.setUpdatedAt(currDateTime);
        bussinessCategory.setIsDeleted(false);
        bussinessCategory.setStatus(true);
        bussinessCategory= bussinessCategoryRepository.save(bussinessCategory);

        String message = "Bussiness Category Created Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, bussinessCategory));
    }

    @Override

    public ResponseEntity<?> list(int page, int size, boolean search, String value) {
        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);

        Page<BussinessCategory> bussinessCategory = null;
        if (search) {
            bussinessCategory = bussinessCategoryRepository.getActiveBussinessCategoryWithSearch(value, pageable);

        } else {

            bussinessCategory = bussinessCategoryRepository.getActiveBussinessCategoryWithPage(pageable);
        }
        List<BussinessCategory> Bussiness = new ArrayList<BussinessCategory>();

        for (BussinessCategory bussinessCategory2 : bussinessCategory) {
            BussinessCategory dto = new BussinessCategory();
            dto.setId(bussinessCategory2.getId());
            dto.setCreatedAt(bussinessCategory2.getCreatedAt());
            dto.setName(bussinessCategory2.getName());
            dto.setStatus(bussinessCategory2.getStatus());
            dto.setUpdatedAt(bussinessCategory2.getUpdatedAt());
            Bussiness.add(dto);
        }
        String message = "Bussiness Category Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, bussinessCategory));
    }

    @Override
    public ResponseEntity<?> byId(int id) {
        // BussinessCategory bussinessCategory = bussinessCategoryRepository.findById(id).orElse(null);
        Optional<BussinessCategory> bussinessCategory = bussinessCategoryRepository.findByActiveBussinessCateId(id);
        if (bussinessCategory.isEmpty()) {
            String message = "Bussiness Category not found for the id " + id;
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, message, null));
        }
        String message = "Bussiness Category Fetched Successfully.";
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, message, bussinessCategory));
    }

    @Override
    public ResponseEntity<?> update(String bussinessname, int id) {
        // Get the name from the updated business category

        // Validate the name field using the regular expression
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9\\s-]*$");
        Matcher matcher = pattern.matcher(bussinessname);
        if (!matcher.matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name must contain only alphabetic letters", null));
        }
        if (bussinessname.length() > 255) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Name exceeds maximum length of 255 characters", null));
        }

        // Check if a business category with the same name already exists
        String trimmedName = bussinessname.replaceAll("\\s+", " ");

        Optional<BussinessCategory> existingCategoryOptional = bussinessCategoryRepository.findByBussinessNameAndIs_deletedFalse(trimmedName);

        // If the category with the same name exists and it has a different ID than the current one being updated
        if (existingCategoryOptional.isPresent() && existingCategoryOptional.get().getId() != id) {
            String errorMessage = "Business Category with name '" + bussinessname + "' already exists.";
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse(false, errorMessage, null));
        }

//        String newName = bussinessname;
//        if (!bussinessname.equals(newName)) {
//            Optional<BussinessCategory> bussinessCate_Name = bussinessCategoryRepository.findByBussinessNameAndIs_deletedFalse(newName);
//            if (bussinessCate_Name.isPresent() && bussinessCate_Name.get().getId() != bussinessCategory.getId()) {
//                String message = "Bussiness category name is already exists.";
//                return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).body(new ApiResponse(false, message, null));
//            }
//
//        }

        // Retrieve the existing category by ID
        Optional<BussinessCategory> existingCategoryOptionalById = bussinessCategoryRepository.getActiveBussinessActivityById(id);
        if (existingCategoryOptionalById.isPresent()) {
            BussinessCategory existingCategory = existingCategoryOptionalById.get();

            // Update the existing category fields
            existingCategory.setName(bussinessname);
            existingCategory.setUpdatedAt(LocalDateTime.now());

            // Save the updated category
            existingCategory= bussinessCategoryRepository.save(existingCategory);

            String message = "Business Category Updated Successfully.";
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, message, existingCategory));
        } else {
            String errorMessage = "Business Category with ID " + id + " not found.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, errorMessage, Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> deleteTheBussinessCategory(List<Integer> id) {
        bussinessCategoryRepository.updateIsDeleted(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Succesfully Deleted the Bussiness Category", null));
    }

}
