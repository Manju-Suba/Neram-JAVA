package pm.serviceImplements;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.config.TimeConfig;
import pm.dto.ProductDTO;
import pm.dto.ProductListDto;
import pm.dto.ProductNamesDTO;
import pm.model.activityrequest.ActivityRequest;
import pm.model.attendanceSheet.AttendanceSheet;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;
import pm.model.product.ProductStatus;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.*;
import pm.request.ApprovalRequest;
import pm.response.ApiResponse;
import pm.response.OwnerDetails;
import pm.response.ProductListResponse;
import pm.response.ProductResponseRejected;
import pm.service.ApprovalService;
import pm.service.CommonService;
import pm.service.EmailService;
import pm.utils.AuthUserData;

@Service
public class ApprovalImpl implements ApprovalService {
    @Autowired
    private TimeConfig timeConfig;

    @Autowired
    private ProdApprovalHistoryRepository approvalHistoryRepository;

    @Autowired
    private ActivityRequestRepository activityRequestRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;
    @Autowired
    private UsersRepository usersRepository;

    @Value("${fileBasePath}")
    private String fileBasePath;

    @Autowired
    private TaskActivityRepository taskActivityRepository;

    @Autowired
    private AttendanceSheetRepository attendanceSheetRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private EmailService emailservice;

    @Value("${myapp.customProperty}")
    private String portalUrl;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Override
    public ResponseEntity<?> getProductList(int page, int size, boolean search, int vaule, String status) {
        try {

            Pageable pageable = PageRequest.of(page, size);
            int authUserId = AuthUserData.getUserId();

            List<Product> products = null;
            Page<Integer> productsApp = null;
            status = status.toLowerCase();
            if (search) {
                productsApp = handleApproverProductWithSearch(status, vaule, pageable);

            } else {
                productsApp = handleApproverProduct(authUserId, status, pageable);
            }
            products = productRepository.findProductsByIdIn(productsApp.getContent());

            List<Integer> content = productsApp.getContent();

            Map<Integer, Integer> idToOrderMap = content.stream().distinct()
                    .collect(Collectors.toMap(id -> id, id -> content.indexOf(id)));

            products.sort((p1, p2) -> Integer.compare(idToOrderMap.get(p1.getId()), idToOrderMap.get(p2.getId())));

            List<ProductListResponse> productListResponses = new ArrayList<>();
            for (Product product : products) {

                List<OwnerDetails> approverDetailsList = new ArrayList<>();
                List<OwnerDetails> techownerDetailsList = new ArrayList<>();
                List<OwnerDetails> prodownerDetailsList = new ArrayList<>();
                List<OwnerDetails> dataownerDetailsList = new ArrayList<>();

                List<OwnerDetails> techHead = new ArrayList<>();
                List<OwnerDetails> prodHead = new ArrayList<>();
                List<OwnerDetails> dataHead = new ArrayList<>();

                List<ProdApprovalHistory> approvalHistories = approvalHistoryRepository.findByProdId(product.getId());
                List<ProdApprovalHistory> approverStatus = approvalHistoryRepository.findByProdIdAndUserId(
                        product.getId(),
                        authUserId);

                for (Integer approvals : product.getFlow().getApproval_by()) {
                    OwnerDetails techowner = commonService.getUserByIdWithProduct(approvals, product.getId());

                    approverDetailsList.add(techowner);
                }

                if (product.getTechOwner() != null && !product.getTechOwner().isEmpty()) {
                    String[] userIdstech = product.getTechOwner().split(",");

                    for (String userIdTech : userIdstech) {

                        int techid = Integer.parseInt(userIdTech.trim());

                        OwnerDetails techOwner = commonService.getUserById(techid);

                        techownerDetailsList.add(techOwner);

                    }
                }

                if (product.getProdOwner() != null && !product.getProdOwner().isEmpty()) {
                    String[] userIds = product.getProdOwner().split(",");
                    for (String userId : userIds) {

                        int prodid = Integer.parseInt(userId.trim());

                        OwnerDetails productOwner = commonService.getUserById(prodid);

                        prodownerDetailsList.add(productOwner);

                    }
                }

                if (product.getDataOwner() != null && !product.getDataOwner().isEmpty()) {
                    String[] userIdstech = product.getDataOwner().split(",");

                    for (String userIdTech : userIdstech) {

                        int dataOwnerId = Integer.parseInt(userIdTech.trim());

                        OwnerDetails dataOwner = commonService.getUserById(dataOwnerId);

                        dataownerDetailsList.add(dataOwner);

                    }
                }

                if (product.getTechHead() != null) {
                    techHead.add(commonService.getUserById(product.getTechHead().getId()));

                }

                if (product.getProdHead() != null) {
                    prodHead.add(commonService.getUserById(product.getProdHead().getId()));

                }

                if (product.getDataHead() != null) {
                    dataHead.add(commonService.getUserById(product.getDataHead().getId()));

                }

                EProductApproStatus approveSts = approverStatus.get(0).getStatus();

                ProductListResponse productListResponse = ProductListResponse.builder()
                        .id(product.getId())
                        .productName(product.getName())
                        .flowName(product.getFlow().getName())
                        .prodHead(prodHead)
                        .techHead(techHead)
                        .dataHead(dataHead)
                        .startDate(product.getStartDate())
                        .endDate(product.getEndDate())
                        .budgetDetails(product.getBudget())
                        .currency(product.getCurrency())
                        .technicalOwners(techownerDetailsList)
                        .productOwners(prodownerDetailsList)
                        .dataOwners(dataownerDetailsList)
                        .fileName(product.getFile())
                        .bussinessCategory(product.getCategory().getName())
                        .status(product.getStatus())
                        .summary(product.getSummary())
                        .approvalStatus(approveSts.toString())
                        .approvalFlow(approverDetailsList)
                        .build();
                productListResponses.add(productListResponse);
            }
            if (!productListResponses.isEmpty()) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Products fetched successfully", productListResponses));

            } else {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "No products available", Collections.emptyList()));

            }
        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(true, "Not Found", Collections.emptyList()));
        }
    }

    private Page<Integer> handleApproverProduct(int authUserId, String status, Pageable pageable) {
        Page<Integer> product;
        switch (status) {
            case "both":
                product = approvalHistoryRepository.findProductsByCreatedByApproverAndNotPending(authUserId, pageable);
                break;
            case "approved":
            case "pending":
            case "rejected":
                product = approvalHistoryRepository.findProductsByCreatedByApprover(authUserId, status, pageable);
                break;
            default:
                product = Page.empty();
                break;
        }
        return product;
    }

    private Page<Integer> handleApproverProductWithSearch(String status, int vaule, Pageable pageable) {

        Page<Integer> product = null;
        switch (status) {

            case "both":

                product = approvalHistoryRepository.getProductNotPending(vaule, pageable);
                break;

            case "approved":
            case "pending":
            case "rejected":
                product = approvalHistoryRepository.getProduct(vaule, status, pageable);
                break;
            default:
                product = Page.empty();
                break;

        }
        return product;
    }

    // @Override
    // public ResponseEntity<?> approveProduct(int id, ApprovalRequest
    // approvalRequest) {
    // int user_id = AuthUserData.getUserId();
    // List<ProdApprovalHistory> approvalHistoryData =
    // approvalHistoryRepository.findByProdIdAndUserId(id, user_id);
    // if (approvalHistoryData.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.NOT_FOUND)
    // .body(new ApiResponse(false, "No products found for approval.", null));
    // } else {
    // ProdApprovalHistory prodApprovalHistory = approvalHistoryData.get(0);
    // LocalDateTime currenTime = LocalDateTime.now();
    // approvalRequest.setUpdated_at(currenTime);
    // BeanUtils.copyProperties(approvalRequest, prodApprovalHistory);
    // approvalHistoryRepository.save(prodApprovalHistory);
    // List<ProdApprovalHistory> approvalData =
    // approvalHistoryRepository.findByProdId(id);
    // for (ProdApprovalHistory prodApprovalData : approvalData) {
    // if (prodApprovalData.getStatus().equals(EProductApproStatus.Approved)) {
    // ProdApprovalHistory nextProdApprovalData = approvalHistoryRepository
    // .findByIdAndProduct(prodApprovalData.getId() + 1, id);
    // if (nextProdApprovalData != null) {
    // if (nextProdApprovalData.getStatus().equals(EProductApproStatus.Not_Yet)) {
    // nextProdApprovalData.setStatus(EProductApproStatus.Pending);
    // approvalHistoryRepository.save(nextProdApprovalData);
    // break;
    // }
    // }
    //
    // }
    // }
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // "Updated Successfully", null));
    // }
    // }
    @Override
    public ResponseEntity<?> approveProduct(List<Integer> ids, ApprovalRequest approvalRequest) {
        int user_id = AuthUserData.getUserId();
        List<ProdApprovalHistory> approvalHistoryData = approvalHistoryRepository.findByProductInAndUserId(ids,
                user_id);
        if (approvalHistoryData.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "No products found for approval.", null));
        } else {
            LocalDateTime currentTime = LocalDateTime.now();
            for (ProdApprovalHistory prodApprovalHistory : approvalHistoryData) {

                BeanUtils.copyProperties(approvalRequest, prodApprovalHistory);
                prodApprovalHistory.setUpdated_at(currentTime);
                approvalHistoryRepository.save(prodApprovalHistory);
            }

            for (Integer id : ids) {
                List<ProdApprovalHistory> approvalData = approvalHistoryRepository.findByProdId(id);
                for (ProdApprovalHistory prodApprovalData : approvalData) {
                    if (prodApprovalData.getStatus().equals(EProductApproStatus.Approved)) {
                        ProdApprovalHistory nextProdApprovalData = approvalHistoryRepository
                                .findByIdAndProduct(prodApprovalData.getId() + 1, id);

                        if (nextProdApprovalData != null
                                && nextProdApprovalData.getStatus().equals(EProductApproStatus.Not_Yet)) {
                            nextProdApprovalData.setStatus(EProductApproStatus.Pending);
                            Optional<Product> productName = productRepository.findById(prodApprovalData.getProduct());

                            executorService.execute(() -> productCreateMail(
                                    prodApprovalData.getCreated_By(), productName.get().getName(),
                                    productName.get().getCreatedBy()));
                            approvalHistoryRepository.save(nextProdApprovalData);
                            break;
                        }
                    } else if (prodApprovalData.getStatus().equals(EProductApproStatus.Rejected)) {
                        Optional<Product> productName = productRepository.findById(prodApprovalData.getProduct());

                        executorService.execute(() -> productHeadMail(productName.get().getCreatedBy(),
                                prodApprovalData.getCreated_By(), productName.get().getName()));
                    }

                }

            }
            String message;
            if (approvalRequest.getStatus().equals(EProductApproStatus.Approved)) {
                message = "Product Approved Successfully";
            } else {
                message = "Product Rejected Successfully";
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));
        }
    }

    private void productCreateMail(Integer approvalData, String ProductName, Integer headId) {
        Optional<Users> approvalPersonOptional = usersRepository.findById(approvalData);
        Optional<Users> headName = usersRepository.findById(headId);
        if (approvalPersonOptional.isPresent()) {
            Users approvalPerson = approvalPersonOptional.get();
            String body = "You have been requested to approve the new product, <b>" + ProductName
                    + "</b>.  Request from " + headName.get().getName()
                    + ". We kindly request you to review the details by clicking on the \"View Details\" link provided below: <p style='text-align: center;'><a href='"
                    + portalUrl +
                    "' style='color: #007bff; text-decoration: none;font-weight:'bold'>"
                    + "View Details"
                    + "</a></p>";
            emailservice.sendEmail(approvalPerson.getEmail(), "Approval Access Notification", body);
        }
    }

    private void productHeadMail(Integer createdby, Integer rejecteedBy, String name) {
        Optional<Users> approvalPersonOptional = usersRepository.findById(rejecteedBy);
        Optional<Users> headName = usersRepository.findById(createdby);

        if (approvalPersonOptional.isPresent()) {
            Users approvalPerson = approvalPersonOptional.get();
            String body = "Your Product " + name + " on " + LocalDate.now() + " has been rejected By "
                    + approvalPerson.getName()
                    + ". We kindly request you to review the details by clicking on the View Details link provided below:<p style='text-align: center;'><a href='"
                    +
                    portalUrl + "' style='color: #007bff; text-decoration: none;'>View Details</a></p>";

            emailservice.sendEmail(headName.get().getEmail(), "Reject Notification", body);
        }
    }

    @Override
    public ResponseEntity<?> getRejectProduct(int id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No products", null));
        }
        String status = EProductApproStatus.Rejected.toString();
        ProdApprovalHistory approvalHistory = approvalHistoryRepository.findByProdIdAndStatus(id, status);
        Optional<Users> users = usersRepository.findById(approvalHistory.getCreated_By());
        ProductResponseRejected detailsDTO = new ProductResponseRejected();
        detailsDTO.setId(product.get().getId());
        detailsDTO.setName(product.get().getName());
        detailsDTO.setBudget(product.get().getBudget());
        detailsDTO.setCategoryID(product.get().getCategory().getId());
        detailsDTO.setCategoryName(product.get().getCategory().getName());
        if (product.get().getProdHead() != null && product.get().getProdHead().getId() != -1) {
            detailsDTO.setProd_headId(product.get().getProdHead().getId());
            detailsDTO.setProd_name(product.get().getProdHead().getName());
        }

        if (product.get().getTechHead() != null && product.get().getTechHead().getId() != -1) {
            detailsDTO.setTech_headId(product.get().getTechHead().getId());
            detailsDTO.setTech_name(product.get().getTechHead().getName());
        }
        detailsDTO.setCurrency(product.get().getCurrency());
        detailsDTO.setCreatedAt(product.get().getCreatedAt());
        detailsDTO.setEndDate(product.get().getEndDate());
        detailsDTO.setStartDate(product.get().getStartDate());
        detailsDTO.setStatus(product.get().getStatus());
        detailsDTO.setSummary(product.get().getSummary());
        detailsDTO.setFile(product.get().getFile());
        detailsDTO.setFlow(product.get().getFlow().getId());
        detailsDTO.setFlowName(product.get().getFlow().getName());
        detailsDTO.setIsDeleted(product.get().getIsDeleted());
        detailsDTO.setUpdatedAt(product.get().getUpdatedAt());
        detailsDTO.setApprovalby(users.get().getName());
        List<OwnerDetails> approverDetailsList = new ArrayList<>();
        for (Integer approvals : product.get().getFlow().getApproval_by()) {
            OwnerDetails techowner = commonService.getUserByIdWithProduct(approvals, product.get().getId());

            approverDetailsList.add(techowner);
        }
        detailsDTO.setApprovalFlow(approverDetailsList);
        String roleNames = users.get().getRole_id().stream()
                .map(role -> role.getName())
                .collect(Collectors.toList())
                .stream()
                .collect(Collectors.joining(", "));

        detailsDTO.setApprovalRole(roleNames);
        detailsDTO.setApprovalStatus(approvalHistory.getStatus());
        if (EProductApproStatus.Rejected.equals(approvalHistory.getStatus())) {
            detailsDTO.setApprovalremarks(approvalHistory.getRemarks());
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
                "fetched successfully", detailsDTO));
    }

    @Override
    public ResponseEntity<?> updateProductAndHistory(int id, Product product, MultipartFile file) {
        try {
            LocalDateTime currDateTime = LocalDateTime.now();
            int user_id = AuthUserData.getUserId();

            Product productrepo = productRepository.findById(id).orElse(null);
            productrepo.setStatus(ProductStatus.CREATED);
            productrepo.setUpdatedAt(currDateTime);
            productrepo.setIsDeleted(false);
            productrepo.setCreatedBy(user_id);
            productrepo.setBudget(product.getBudget());
            productrepo.setCategory(product.getCategory());
            productrepo.setSummary(product.getSummary());
            productrepo.setEndDate(product.getEndDate());
            productrepo.setStartDate(product.getStartDate());
            productrepo.setProdHead(product.getProdHead());
            productrepo.setTechHead(product.getTechHead());
            productrepo.setCurrency(product.getCurrency());
            productrepo.setName(product.getName());
            if (file != null && !file.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                String formattedTime = currDateTime.format(formatter);
                String fileName = formattedTime + "_" + file.getOriginalFilename();
                Path path = Paths.get(fileBasePath + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                File fileexist = path.toFile();
                if (fileexist.exists()) {
                    productrepo.setFile(fileName);
                }
            }

            Product product_data = productRepository.save(productrepo);
            if (product_data.getStatus().equals(ProductStatus.CREATED)) {
                String status = EProductApproStatus.Rejected.toString();
                List<ProdApprovalHistory> approvalHistoryData = approvalHistoryRepository.findByProdId(id);
                int index = 0;
                for (ProdApprovalHistory data : approvalHistoryData) {
                    if (index == 0) {
                        data.setStatus(EProductApproStatus.Pending);
                    } else {
                        data.setStatus(EProductApproStatus.Not_Yet);
                    }

                    data.setUpdated_at(currDateTime);
                    data.setRemarks(null);
                    approvalHistoryRepository.save(data);
                    index++;
                }

            }

            String message = "Product Updated Successfully.";
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, message, null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }

    }

    @Override
    public ResponseEntity<?> getApprovalDate(LocalDate date) {
        Integer userid = AuthUserData.getUserId();
        LocalDate previousDate = date.minusDays(1);
        Long count = taskActivityRepository.countByActivityDateAndActivityIdAndStatus(previousDate, userid);
        return ResponseEntity.ok(Collections.singletonMap("count", count));
    }

    // Sunday Check for activity
    @Override
    public ResponseEntity<?> getApprovalDateSunday(LocalDate date) {

        int userId = AuthUserData.getUserId();
        LocalTime dynamicTime = timeConfig.getComparisonTime();

        AttendanceSheet attendanceSheetdata = attendanceSheetRepository
                .findByUseridAndAppliedDate(AuthUserData.getUserId(), date);

        Map<String, Object> response = new HashMap<>();

        ActivityRequest activityRequest = activityRequestRepository.findbyUserIdAndRequestDate(userId, date);
        Long commonSheetCount = commonTimeSheetActivityRepository.countByActivityDateAndActivityIdAndStatus(date,
                userId);
        String commonSheetHours = commonTimeSheetActivityRepository.hoursByActivityDateAndActivityIdAndStatus(date,
                userId);

        // dailyCount += commonSheetCount;
        response.put("count", commonSheetCount);
        response.put("hours", commonSheetHours);
        LocalDate currentDate = LocalDate.now(); // Current date
        // LocalDate currentDate = LocalDate.of(2024, 7, 27);
        System.out.println(currentDate);
        LocalTime currentTime = LocalTime.now(); // Current time
        if (activityRequest != null) {
            response.put("status", activityRequest.getStatus());
        } else if (currentDate.getDayOfWeek() == DayOfWeek.MONDAY) {
            if (currentTime.isBefore(dynamicTime)) {
                // If it's Monday and before 4 PM, include Friday, Saturday, Sunday from the
                // previous week
                LocalDate previousFriday = currentDate.minusDays(3);
                LocalDate previousSaturday = currentDate.minusDays(2);
                LocalDate previousSunday = currentDate.minusDays(1);

                // Add dates to datesInRange if the provided date matches
                if (date.isEqual(previousFriday) || date.isEqual(previousSaturday) || date.isEqual(previousSunday)
                        || date.isEqual(currentDate)) {
                    response.put("status", "Permission");
                } else {
                    response.put("status", "");
                }
            } else {
                // If it's Monday and after 4 PM, only include today's date
                if (date.isEqual(currentDate)) {
                    response.put("status", "Permission");
                } else {
                    response.put("status", "");
                }
            }
        } else {
            // If today is not Monday
            if (currentTime.isBefore(dynamicTime)) {
                // If it's before 10:30 AM, include yesterday's date
                LocalDate yesterday = currentDate.minusDays(1);

                if (date.isEqual(yesterday) || date.isEqual(currentDate)) {
                    response.put("status", "Permission");
                } else {
                    response.put("status", "");
                }
            } else {
                // If it's after 4 PM, include today's date
                if (date.isEqual(currentDate)) {
                    response.put("status", "Permission");
                } else {
                    response.put("status", "");
                }
            }
        }
        response.put("attendanceStatus", attendanceSheetdata != null ? attendanceSheetdata.getStatus() : "yet");

        // if (attendanceSheetdata != null) {
        // response.put("attendanceStatus", attendanceSheetdata.getStatus());
        // } else {
        // response.put("attendanceStatus", "yet");
        // }

        // response.put("isSunday", isSunday);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<?> getProductListSearch(String status) {
        int authUserId = AuthUserData.getUserId();
        List<Integer> productId = null;
        switch (status) {
            case "both":
                productId = approvalHistoryRepository.findProductsByCreatedByApproverAndNotPendingSearch(authUserId);
                break;
            case "Approved":
            case "Pending":
            case "Rejected":
                productId = approvalHistoryRepository.findProductsByCreatedByApproverSearch(authUserId, status);
                break;

        }
        List<Object[]> results = productRepository.getIdAndNameProductsByIdIn(productId);
        Map<Integer, Integer> idToIndexMap = new HashMap<>();
        for (int i = 0; i < productId.size(); i++) {
            idToIndexMap.put(productId.get(i), i);
        }

        // Sort the results based on the order of IDs in the productId list
        results.sort(Comparator.comparingInt(arr -> idToIndexMap.getOrDefault((Integer) arr[0], Integer.MAX_VALUE)));
        ObjectMapper objectMapper = new ObjectMapper();
        List<ProductNamesDTO> products = new ArrayList<>();

        for (Object[] result : results) {
            int id = (Integer) result[0];
            String name = (String) result[1];
            ProductNamesDTO product = new ProductNamesDTO(id, name);
            products.add(product);
        }
        if (!products.isEmpty()) {

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Products fetched successfully", products));

        } else {

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "No products available", Collections.emptyList()));

        }

    }

}
