package pm.serviceImplements;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pm.dto.UserDTO;
import pm.dto.ProductDTO;
import pm.dto.ProductListDto;
import pm.dto.ProductNamesDTO;
import pm.dto.ProductTaskDTO;
import pm.model.flow.Flow;
import pm.model.product.EProductApproStatus;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;
import pm.model.product.ProductStatus;
import pm.model.product.BussinessCategory;
import pm.model.task.Task;
import pm.model.users.Users;
import pm.repository.*;
import pm.request.ProductAssignRequest;
import pm.request.ProductCreateRequest;
import pm.response.ApiResponse;
import pm.response.OwnerDetails;
import pm.response.ProductResponse;
import pm.model.member.*;
import pm.response.ProductListResponse;
import pm.response.TaskResponse;
import pm.service.CommonService;
import pm.service.EmailService;
import pm.service.ProductService;
import pm.utils.AuthUserData;
import pm.utils.CommonFunct;

@Service
public class ProductImpl implements ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductImpl.class);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    @Autowired
    ProductRepository productRepository;
    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BussinessCategoriesRepository categoriesRepository;

    @Autowired
    UsersRepository usersRepository;
    @Autowired
    private FlowsRepository flowsRepository;
    @Autowired
    private ProdApprovalHistoryRepository approvalHistoryRepository;
    @Value("${fileBasePath}")
    private String fileBasePath;
    @Value("${getPath}")
    private String getPath;
    @Value("${myapp.customProperty}")
    private String portalUrl;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private RolesRepository rolesRepository;
    @Autowired
    private EmailService emailservice;
    @Autowired
    private CommonService commonService;
    @Autowired
    private CommonFunct commonFunct;

    @Override
    public ResponseEntity<?> getAllCategory() {
        List<BussinessCategory> categories = categoriesRepository.findAll();
        if (categories.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Project Category Fetched ", categories));
        }
    }

    @Override
    public ResponseEntity<?> createProduct(ProductCreateRequest productCreateRequest, String option,
                                           MultipartFile file, String key) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(productCreateRequest);
        if (!violations.isEmpty()) {
            List<String> errorMessages = new ArrayList<>();
            for (ConstraintViolation<ProductCreateRequest> violation : violations) {
                errorMessages.add(violation.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Validation error", errorMessages));
        }
        if (productRepository.existsByName(productCreateRequest.getName())) {
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body(new ApiResponse(false, "Product with the same name already exists", Collections.emptyList()));
        }

        try {
            LocalDateTime currDateTime = LocalDateTime.now();
            int user_id = AuthUserData.getUserId();
            Product product = new Product();

            BeanUtils.copyProperties(productCreateRequest, product);

            product.setFlow(flowsRepository.findById(productCreateRequest.getFlow()).get());
            product.setCategory(categoriesRepository.findById(productCreateRequest.getCategory()).get());

            if (productCreateRequest.getTechHead() != null && productCreateRequest.getTechHead() != 0) {

                if (!validateUserIds(productCreateRequest.getTechHead(), "Head", "Technical")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid Technical Head  ID: " + productCreateRequest.getTechHead(), null));
                }
                Optional<Users> user = usersRepository.findById(productCreateRequest.getTechHead());
                product.setTechHead(user.get());
            }
            if (productCreateRequest.getProdHead() != null && productCreateRequest.getProdHead() != 0) {

                if (!validateUserIds(productCreateRequest.getProdHead(), "Head", "Product")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid Product Head  ID: " + productCreateRequest.getProdHead(), null));
                }

                Optional<Users> userprod = usersRepository.findById(productCreateRequest.getProdHead());
                product.setProdHead(userprod.get());

            }
            if (productCreateRequest.getDataHead() != null && productCreateRequest.getDataHead() != 0) {

                if (!validateUserIds(productCreateRequest.getDataHead(), "Head", "Data")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid Data Head  ID: " + productCreateRequest.getDataHead(), null));
                }

                Optional<Users> userprod = usersRepository.findById(productCreateRequest.getDataHead());
                product.setDataHead(userprod.get());

            }

            // how head
            if (productCreateRequest.getHowHead() != null && productCreateRequest.getHowHead() != 0) {

                if (!validateUserIds(productCreateRequest.getHowHead(), "Head", "HOW")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid HOW Head ID: " + productCreateRequest.getHowHead(), null));
                }

                Optional<Users> userprod = usersRepository.findById(productCreateRequest.getHowHead());
                product.setHowHead(userprod.get());

            }


            // if (productCreateRequest.getCurrency()!=null ){
            // product.setCurrency(productCreateRequest.getCurrency());
            // }else {
            // product.setCurrency("INR");
            // }

            product.setCreatedAt(currDateTime);
            product.setUpdatedAt(currDateTime);
            product.setIsDeleted(false);
            product.setCreatedBy(user_id);
            if (file != null && !file.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

                String formattedTime = currDateTime.format(formatter);
                String fileName = formattedTime + "_" + file.getOriginalFilename();
                Path path = Paths.get(fileBasePath + fileName);
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                File fileexist = path.toFile();
                if (fileexist.exists()) {
                    product.setFile(fileName);
                }
            }
            if (key != null && key.equalsIgnoreCase("admin")) {
                product.setStatus(ProductStatus.CREATED);
                List<Integer> techOwnerIds = productCreateRequest.getTechOwner();
                List<Integer> prodOwnerIds = productCreateRequest.getProdOwner();
                List<Integer> dataOwnerIds = productCreateRequest.getDataOwner();
                List<Integer> howOwnersIds = productCreateRequest.getHowOwner();
                if (techOwnerIds != null && !techOwnerIds.isEmpty()) {
                    product.setTechOwner(techOwnerIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }
                if (prodOwnerIds != null && !prodOwnerIds.isEmpty()) {
                    product.setProdOwner(prodOwnerIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }
                if (dataOwnerIds != null && !dataOwnerIds.isEmpty()) {
                    product.setDataOwner(dataOwnerIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }
                if (howOwnersIds != null && !howOwnersIds.isEmpty()) {
                    product.setHowOwner(howOwnersIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }
            } else {
                if ("DRAFT".equals(option)) {
                    product.setStatus(ProductStatus.DRAFT);
                } else {
                    product.setStatus(ProductStatus.CREATED);
                }
            }

            product = productRepository.save(product);
            String message = "";
            if (product.getStatus().equals(ProductStatus.CREATED)) {
                Optional<Flow> flows = flowsRepository.findById(product.getFlow().getId());
                if (flows.isPresent()) {
                    List<Integer> integers = flows.get().getApproval_by();
                    final Product productName = product;

                    if (key != null && key.equalsIgnoreCase("admin")) {
                        for (Integer integer : integers) {

                            ProdApprovalHistory approvalHistory = new ProdApprovalHistory();

                            approvalHistory.setProduct(product.getId());

                            // executorService.execute(() -> productCreateMail(integer,
                            // productName.getName(), user_id));
                            approvalHistory.setStatus(EProductApproStatus.Approved);
                            approvalHistory.set_deleted(false);
                            approvalHistory.setCreated_at(currDateTime);
                            approvalHistory.setUpdated_at(currDateTime);
                            approvalHistory.setCreated_By(integer);

                            approvalHistoryRepository.save(approvalHistory);
                        }
                        message = "Product Created Successfully.";
                    } else {

                        int index = 0;
                        for (Integer integer : integers) {

                            ProdApprovalHistory approvalHistory = new ProdApprovalHistory();

                            approvalHistory.setProduct(product.getId());
                            if (index == 0) {
                                executorService
                                        .execute(() -> productCreateMail(integer, productName.getName(), user_id));

                                approvalHistory.setStatus(EProductApproStatus.Pending);
                            } else {
                                approvalHistory.setStatus(EProductApproStatus.Not_Yet);
                            }
                            approvalHistory.set_deleted(false);
                            approvalHistory.setCreated_at(currDateTime);
                            approvalHistory.setUpdated_at(currDateTime);
                            approvalHistory.setCreated_By(integer);

                            approvalHistoryRepository.save(approvalHistory);
                            index++;
                        }
                        message = "DRAFT".equals(option) ? "Product put in draft." : "Product Created Successfully.";

                    }
                }

            } else {
                message = "Product put in draft.";
            }

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, product));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }

    }

    private boolean validateUserIds(Integer userId, String designation, String branch) {
        Optional<Users> userOptional = usersRepository.findByActiveEmployeeByIdAndDesignationandBranch(userId,
                designation, branch);
        return userOptional.isPresent();
    }

    private void productCreateMail(Integer approvalData, String ProductName, Integer headId) {
        Optional<Users> approvalPersonOptional = usersRepository.findById(approvalData);
        Optional<Users> headName = usersRepository.findById(headId);
        if (approvalPersonOptional.isPresent()) {
            Users approvalPerson = approvalPersonOptional.get();
            String body = "You have been requested to approve the new product, <b>" + ProductName
                    + "</b>.  Request from <b>" + headName.get().getName()
                    + "</b>. We kindly request you to review the details by clicking on the \"View Details\" link provided below:"
                    + "</p>"
                    + "<p style='text-align: center;'><a href='" + portalUrl +
                    "' style='color: #007bff; text-decoration: none;font-weight:bold'>"
                    + "View Details"
                    + "</a></p>";
            emailservice.sendEmail(approvalPerson.getEmail(), "Approval Access Notification", body);
        }
    }

    // @Override
    // public ResponseEntity<?> getCreatedProductList() {
    // ProductStatus status = ProductStatus.CREATED;
    // int user_id = AuthUserData.getUserId();
    // Users user = new Users();
    // user.setId(user_id);
    // // List<String> role = AuthUserData.getUserRole();
    // Users users = usersRepository.findById(user_id).get();
    // String role = users.getBranch() + " " + users.getDesignation();
    // List<Product> products;
    // if (role.equalsIgnoreCase("Technical Head")) {
    // products = productRepository.findByStatusAndTechHead(status, user);
    // } else if (role.equalsIgnoreCase("Product Head")) {
    // products = productRepository.findByStatusAndProdHead(status, user);
    // } else {
    // return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new
    // ApiResponse(false, "You have Invalid role to Access the List", null));
    // }
    //
    // if (products.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No
    // products", null));
    // }
    // List<ProductDTO> detailsDTOs = new ArrayList<>();
    //
    // products =
    // products.stream().sorted(Comparator.comparing(Product::getId).reversed())
    // .collect(Collectors.toList());
    //
    // for (Product product : products) {
    // boolean hasPending = false;
    // boolean hasRejected = false;
    // boolean hasApproved = false;
    // List<ProdApprovalHistory> approvalHistories =
    // approvalHistoryRepository.findByProdId(product.getId());
    // for (ProdApprovalHistory approvalHistory : approvalHistories) {
    // if (EProductApproStatus.Pending.equals(approvalHistory.getStatus())) {
    // hasPending = true;
    // } else if (EProductApproStatus.Rejected.equals(approvalHistory.getStatus()))
    // {
    // hasRejected = true;
    // } else {
    // hasApproved = true;
    // }
    // }
    // ProductDTO detailsDTO = new ProductDTO();
    // detailsDTO.setId(product.getId());
    // detailsDTO.setName(product.getName());
    // detailsDTO.setBudget(product.getBudget());
    // if (product.getCategory() != null && product.getCategory().getId() != -1
    // && product.getCategory().getName() != null) {
    // detailsDTO.setCategoryID(product.getCategory().getId());
    // detailsDTO.setCategoryName(product.getCategory().getName());
    // }
    //
    // if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
    // detailsDTO.setProd_headId(product.getProdHead().getId());
    // detailsDTO.setProd_name(product.getProdHead().getName());
    // }
    //
    // if (product.getTechHead() != null && product.getTechHead().getId() != -1) {
    // detailsDTO.setTech_headId(product.getTechHead().getId());
    // detailsDTO.setTech_name(product.getTechHead().getName());
    // }
    //
    // detailsDTO.setCurrency(product.getCurrency());
    // detailsDTO.setCreatedAt(product.getCreatedAt());
    // detailsDTO.setEndDate(product.getEndDate());
    // detailsDTO.setStartDate(product.getStartDate());
    // detailsDTO.setStatus(product.getStatus());
    // detailsDTO.setSummary(product.getSummary());
    // // detailsDTO.setProdOwner(product.getProdOwner());
    // // detailsDTO.setTechOwner(product.getTechOwner());
    // detailsDTO.setFile(product.getFile());
    // detailsDTO.setFlow(product.getFlow().getId());
    // detailsDTO.setIsDeleted(product.getIsDeleted());
    // detailsDTO.setUpdatedAt(product.getUpdatedAt());
    // detailsDTO.setCreatedBy(product.getCreatedBy());
    // if (hasRejected) {
    // detailsDTO.setApprovalStatus(EProductApproStatus.Rejected);
    // } else if (hasPending) {
    // detailsDTO.setApprovalStatus(EProductApproStatus.Pending);
    // } else if (hasApproved) {
    // detailsDTO.setApprovalStatus(EProductApproStatus.Approved);
    // }
    // detailsDTOs.add(detailsDTO);
    // }
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // "fetched successfully", detailsDTOs));
    // }
    @Override
    public ResponseEntity<?> getCreatedProductList(int page, int size, boolean search, int vaule, String status,
                                                   String key) {
        try {
            Sort sortByDescId = Sort.by(Sort.Direction.DESC, "id");
            Pageable pageable = PageRequest.of(page, size);
            Page<Product> products = commonService.getProductListByHead(pageable, search, vaule, status, key);
            List<ProductListResponse> productListResponses = new ArrayList<>();
            for (Product product : products) {

                List<OwnerDetails> approverDetailsList = new ArrayList<>();
                List<OwnerDetails> techownerDetailsList = new ArrayList<>();
                List<OwnerDetails> prodownerDetailsList = new ArrayList<>();
                List<OwnerDetails> dataownerDetailsList = new ArrayList<>();
                List<OwnerDetails> howownerDetailsList = new ArrayList<>();
                List<OwnerDetails> techHead = new ArrayList<>();
                List<OwnerDetails> prodHead = new ArrayList<>();
                List<OwnerDetails> dataHead = new ArrayList<>();    
                List<OwnerDetails> howHead = new ArrayList<>();

                // List<ProdApprovalHistory> approvalHistories =
                // approvalHistoryRepository.findByProdId(product.getId());
                List<OwnerDetails> addapproverDetailsList = new ArrayList<>();

                for (Integer approvals : product.getFlow().getApproval_by()) {
                    OwnerDetails techowner = commonService.getUserByIdWithProduct(approvals, product.getId());

                    addapproverDetailsList.add(techowner);
                }
                approverDetailsList = commonFunct.commonFunctionForProduct(addapproverDetailsList);

                if (product.getTechOwner() != null && !product.getTechOwner().isEmpty()) {
                    String[] userIdstech = product.getTechOwner().split(",");
                    List<OwnerDetails> techownerDetails = new ArrayList<>();

                    for (String userIdTech : userIdstech) {

                        int techid = Integer.parseInt(userIdTech.trim());

                        OwnerDetails techOwner = commonService.getUserById(techid);
                        techownerDetails.add(techOwner);

                    }
                    techownerDetailsList.addAll(commonFunct.commonFunctionForProduct(techownerDetails));

                }

                if (product.getHowOwner() != null && !product.getHowOwner().isEmpty()) {
                    String[] userIdstech = product.getHowOwner().split(",");
                    List<OwnerDetails> howownerDetails = new ArrayList<>();

                    for (String userIdTech : userIdstech) {

                        int techid = Integer.parseInt(userIdTech.trim());

                        OwnerDetails howOwner = commonService.getUserById(techid);
                        howownerDetails.add(howOwner);

                    }
                    howownerDetailsList.addAll(commonFunct.commonFunctionForProduct(howownerDetails));

                }

                if (product.getProdOwner() != null && !product.getProdOwner().isEmpty()) {
                    String[] userIds = product.getProdOwner().split(",");
                    List<OwnerDetails> productownerDetails = new ArrayList<>();
                    for (String userId : userIds) {

                        int prodid = Integer.parseInt(userId.trim());

                        OwnerDetails productOwner = commonService.getUserById(prodid);
                        productownerDetails.add(productOwner);

                    }
                    prodownerDetailsList.addAll(commonFunct.commonFunctionForProduct(productownerDetails));

                }
                if (product.getDataOwner() != null && !product.getDataOwner().isEmpty()) {
                    String[] userIds = product.getDataOwner().split(",");
                    List<OwnerDetails> dataOwnerDetails = new ArrayList<>();
                    for (String userId : userIds) {

                        int prodid = Integer.parseInt(userId.trim());

                        OwnerDetails dataOwner = commonService.getUserById(prodid);
                        dataOwnerDetails.add(dataOwner);

                    }
                    dataownerDetailsList.addAll(commonFunct.commonFunctionForProduct(dataOwnerDetails));

                }

                if (product.getTechHead() != null) {
                    List<OwnerDetails> techHeadDetails = new ArrayList<>();
                    techHeadDetails.add(commonService.getUserById(product.getTechHead().getId()));
                    techHead.addAll(commonFunct.commonFunctionForProduct(techHeadDetails));

                }

                if (product.getProdHead() != null) {
                    List<OwnerDetails> productHeadDetails = new ArrayList<>();
                    productHeadDetails.add(commonService.getUserById(
                            product.getProdHead().getId()));
                    prodHead.addAll(commonFunct.commonFunctionForProduct(productHeadDetails));
                }

                if (product.getDataHead() != null) {
                    List<OwnerDetails> dataHeadDetails = new ArrayList<>();
                    dataHeadDetails.add(commonService.getUserById(
                            product.getDataHead().getId()));
                    dataHead.addAll(commonFunct.commonFunctionForProduct(dataHeadDetails));
                }

                if (product.getHowHead() != null) {
                    List<OwnerDetails> howHeadDetails = new ArrayList<>();
                    howHeadDetails.add(commonService.getUserById(
                            product.getHowHead().getId()));
                    howHead.addAll(commonFunct.commonFunctionForProduct(howHeadDetails));
                }

                ProductListResponse productListResponse = ProductListResponse.builder()
                        .id(product.getId())
                        .productName(product.getName())
                        .flowName(product.getFlow().getName())
                        // .prodHead(!prodownerDetailsList.isEmpty() ? prodownerDetailsList.get(0) :
                        // null)
                        .prodHead(prodHead)
                        .techHead(techHead)
                        .dataHead(dataHead)
                        .howHead(howHead)
                        .startDate(product.getStartDate())
                        .endDate(product.getEndDate())
                        .budgetDetails(product.getBudget())
                        .currency(product.getCurrency())
                        .technicalOwners(techownerDetailsList)
                        .productOwners(prodownerDetailsList)
                        .dataOwners(dataownerDetailsList)       
                        .howOwners(howownerDetailsList)
                        .fileName(product.getFile())
                        .bussinessCategory(product.getCategory().getName())
                        .status(product.getStatus())
                        .summary(product.getSummary())
                        .approvalStatus(status)
                        .approvalFlow(approverDetailsList)
                        .build();
                productListResponses.add(productListResponse);
            }

            if (productListResponses != null && !productListResponses.isEmpty()) {
                // Collections.sort(productListResponses,
                // Comparator.comparing(ProductListResponse::getId).reversed());
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Products fetched successfully", productListResponses));

            } else {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "No products available", Collections.emptyList()));

            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while fetching the product list", e.getMessage()));
        }

    }

    @Override
    public ResponseEntity<?> getProductList(String status) {
        try {

            List<Object[]> results = commonService.getAllProductListByHead(status);
            ObjectMapper objectMapper = new ObjectMapper();
            List<ProductNamesDTO> products = new ArrayList<>();

            for (Object[] result : results) {
                int id = (Integer) result[0];
                String name = (String) result[1];
                ProductNamesDTO product = new ProductNamesDTO(id, name);
                products.add(product);
            }
            if (products != null && !products.isEmpty()) {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Products fetched successfully", products));

            } else {

                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "No products available", Collections.emptyList()));

            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while fetching the product list", e.getMessage()));
        }

    }

    @Override
    public ResponseEntity<?> getProduct(int id) {
        Product product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No products", null));
        }
        List<ProdApprovalHistory> approvalHistories = approvalHistoryRepository.findByProdId(id);
        List<UserDTO> dtos = new ArrayList<>();

        for (ProdApprovalHistory approvalHistory : approvalHistories) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(approvalHistory.getId());
            userDTO.setApproStatus(approvalHistory.getStatus());
            userDTO.setUpdated_At(approvalHistory.getUpdated_at());
            Optional<Users> users = usersRepository.findById(approvalHistory.getCreated_By());
            userDTO.setRole(
                    users.get().getRole_id().stream().map(role -> role.getName()).collect(Collectors.joining(", ")));
            userDTO.setProfile_pic(users.get().getProfile_pic());
            userDTO.setName(users.get().getName());
            dtos.add(userDTO);
        }
        ProductDTO detailsDTO = new ProductDTO();
        detailsDTO.setId(product.getId());
        detailsDTO.setName(product.getName());
        detailsDTO.setBudget(product.getBudget());
        detailsDTO.setCategoryID(product.getCategory().getId());
        detailsDTO.setCategoryName(product.getCategory().getName());
        // detailsDTO.setProd_headId(product.getProdHead().getId());
        // detailsDTO.setProd_name(product.getProdHead().getName());

        if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
            detailsDTO.setProd_headId(product.getProdHead().getId());
            detailsDTO.setProd_name(product.getProdHead().getName());
        }

        if (product.getTechHead() != null && product.getTechHead().getId() != -1) {

            detailsDTO.setTech_headId(product.getTechHead().getId());
            detailsDTO.setTech_name(product.getTechHead().getName());
        }
        if (product.getDataHead() != null && product.getDataHead().getId() != -1) {

            detailsDTO.setData_headId(product.getDataHead().getId());
            detailsDTO.setData_name(product.getDataHead().getName());
        }

        if (product.getHowHead() != null && product.getHowHead().getId() != -1) {

            detailsDTO.setHow_headId(product.getHowHead().getId());
            detailsDTO.setHow_name(product.getHowHead().getName());
        }

        // detailsDTO.setTech_headId(product.getTechHead().getId());
        // detailsDTO.setTech_name(product.getTechHead().getName());
        detailsDTO.setCurrency(product.getCurrency());
        detailsDTO.setCreatedAt(product.getCreatedAt());
        detailsDTO.setEndDate(product.getEndDate());
        detailsDTO.setStartDate(product.getStartDate());
        detailsDTO.setStatus(product.getStatus());
        detailsDTO.setSummary(product.getSummary());
        detailsDTO.setFile(product.getFile());
        detailsDTO.setProdOwner(product.getProdOwner());
        detailsDTO.setTechOwner(product.getTechOwner());
        detailsDTO.setDataOwner(product.getDataOwner());
        detailsDTO.setHowOwner(product.getHowOwner());
        String prodOwnerIds = product.getProdOwner();
        List<String> prodOwnerNamesList = new ArrayList<>();
        if (prodOwnerIds != null && !prodOwnerIds.isEmpty()) {
            String[] prodOwnerArray = prodOwnerIds.split(",");
            for (String prodOwnerId : prodOwnerArray) {
                Integer ownerId = Integer.parseInt(prodOwnerId.trim());
                Users prodOwner = usersRepository.findById(ownerId).orElse(null);
                if (prodOwner != null) {
                    prodOwnerNamesList.add(prodOwner.getName());
                }
            }
        }
        detailsDTO.setProdOwnerName(prodOwnerNamesList);
        String techOwnerIds = product.getTechOwner();
        List<String> techOwnerNamesList = new ArrayList<>();
        if (techOwnerIds != null && !techOwnerIds.isEmpty()) {
            String[] techOwnerArray = techOwnerIds.split(",");
            for (String techOwnerId : techOwnerArray) {
                Integer ownerId = Integer.parseInt(techOwnerId.trim());
                Users techOwner = usersRepository.findById(ownerId).orElse(null);
                if (techOwner != null) {
                    techOwnerNamesList.add(techOwner.getName());
                }
            }
        }
        String howOwnersIds = product.getHowOwner();
        List<String> howOwnerNamesList = new ArrayList<>();


        if (howOwnersIds != null && !howOwnersIds.isEmpty()) {
            String[] howOwnerArray = howOwnersIds.split(",");
            for (String howOwnerId : howOwnerArray) {
                Integer ownerId = Integer.parseInt(howOwnerId.trim());
                Users howOwner = usersRepository.findById(ownerId).orElse(null);
                if (howOwner != null) {
                    howOwnerNamesList.add(howOwner.getName());
                }
            }
        }
        detailsDTO.setHowOwnerName(howOwnerNamesList);
        detailsDTO.setTechOwnerName(techOwnerNamesList);
        String dataOwnerIds = product.getDataOwner();
        List<String> dataOwnerNamesList = new ArrayList<>();
        if (dataOwnerIds != null && !dataOwnerIds.isEmpty()) {
            String[] dataOwnerArray = dataOwnerIds.split(",");
            for (String dataOwnerId : dataOwnerArray) {
                Integer ownerId = Integer.parseInt(dataOwnerId.trim());
                Users dataOwner = usersRepository.findById(ownerId).orElse(null);
                if (dataOwner != null) {
                    dataOwnerNamesList.add(dataOwner.getName());
                }
            }
        }
        detailsDTO.setDataOwnerName(dataOwnerNamesList);
        detailsDTO.setFlow(product.getFlow().getId());
        detailsDTO.setFlowName(product.getFlow().getName());
        detailsDTO.setIsDeleted(product.getIsDeleted());
        detailsDTO.setUpdatedAt(product.getUpdatedAt());
        detailsDTO.setApprovalFlow(dtos);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTO));
    }

    @Override
    public ResponseEntity<?> filterProductList(String name) {
        int user_id = AuthUserData.getUserId();
        List<Product> products = productRepository.findByNameQuery(name);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No products", null));
        }
        boolean hasPending = false;
        boolean hasRejected = false;
        List<ProductDTO> detailsDTOs = new ArrayList<>();
        for (Product product : products) {
            List<ProdApprovalHistory> approvalHistories = approvalHistoryRepository.findByProdId(product.getId());
            for (ProdApprovalHistory approvalHistory : approvalHistories) {
                if (EProductApproStatus.Pending.equals(approvalHistory.getStatus())) {
                    hasPending = true;
                } else if (EProductApproStatus.Rejected.equals(approvalHistory.getStatus())) {
                    hasRejected = true;
                }
            }
            ProductDTO detailsDTO = new ProductDTO();
            detailsDTO.setId(product.getId());
            detailsDTO.setName(product.getName());
            detailsDTO.setBudget(product.getBudget());
            detailsDTO.setCategoryID(product.getCategory().getId());
            detailsDTO.setCategoryName(product.getCategory().getName());
            // detailsDTO.setProd_headId(product.getProdHead().getId());
            // detailsDTO.setProd_name(product.getProdHead().getName());
            // detailsDTO.setTech_headId(product.getTechHead().getId());
            // detailsDTO.setTech_name(product.getTechHead().getName());
            if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
                detailsDTO.setProd_headId(product.getProdHead().getId());
                detailsDTO.setProd_name(product.getProdHead().getName());
            }

            if (product.getTechHead() != null && product.getTechHead().getId() != -1) {
                detailsDTO.setTech_headId(product.getTechHead().getId());
                detailsDTO.setTech_name(product.getTechHead().getName());
            }
            detailsDTO.setCurrency(product.getCurrency());
            detailsDTO.setCreatedAt(product.getCreatedAt());
            detailsDTO.setEndDate(product.getEndDate());
            detailsDTO.setStartDate(product.getStartDate());
            detailsDTO.setStatus(product.getStatus());
            detailsDTO.setSummary(product.getSummary());
            detailsDTO.setProdOwner(product.getProdOwner());
            detailsDTO.setTechOwner(product.getTechOwner());
            detailsDTO.setFile(product.getFile());
            detailsDTO.setFlow(product.getFlow().getId());
            detailsDTO.setIsDeleted(product.getIsDeleted());
            detailsDTO.setUpdatedAt(product.getUpdatedAt());
            if (hasRejected) {
                detailsDTO.setApprovalStatus(EProductApproStatus.Rejected);
            } else if (hasPending) {
                detailsDTO.setApprovalStatus(EProductApproStatus.Pending);
            } else {
                detailsDTO.setApprovalStatus(EProductApproStatus.Approved);
            }
            if (approvalHistories.isEmpty()) {
                detailsDTO.setApprovalStatus(EProductApproStatus.Pending);
            }
            detailsDTOs.add(detailsDTO);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTOs));
    }

    public ResponseEntity<?> getDraftList() {
        try {
            ProductStatus status = ProductStatus.DRAFT;
            int user_id = AuthUserData.getUserId();
            Users user = new Users();
            user.setId(user_id);
            Users users = usersRepository.findById(user_id).get();
            String role = users.getBranch() + " " + users.getDesignation();
            List<ProductListDto> detailsDTOs = new ArrayList<>(); // Initialize here
            List<Object[]> results = null;
            int product_id = 0;
            if (role.equalsIgnoreCase("Technical Head")) {
                results = productRepository.getProductsByStatusAndTechHead(status.toString(), user_id);
            } else if (role.equalsIgnoreCase("Product Head")) {
                results = productRepository.getProductsByStatusAndTechHead(status.toString(), user_id);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiResponse(false, "You have Invalid role to Access the List", null));
            }

            List<OwnerDetails> prodownerDetailsList;
            List<OwnerDetails> techownerDetailsList;
            List<OwnerDetails> approverDetailsList;

            for (Object[] result : results) {
                product_id = (Integer) result[0];
                Optional<Product> productall = productRepository.findById(product_id);
                Product product = productall.get();
                product.getFlow();
                prodownerDetailsList = new ArrayList<>();
                techownerDetailsList = new ArrayList<>();
                approverDetailsList = new ArrayList<>();
                boolean hasPending = false;
                boolean hasRejected = false;
                boolean hasApproved = false;
                List<ProdApprovalHistory> approvalHistories = approvalHistoryRepository.findByProdId(product.getId());
                for (ProdApprovalHistory approvalHistory : approvalHistories) {
                    if (EProductApproStatus.Pending.equals(approvalHistory.getStatus())) {
                        hasPending = true;
                    } else if (EProductApproStatus.Rejected.equals(approvalHistory.getStatus())) {
                        hasRejected = true;
                    } else {
                        hasApproved = true;
                    }
                }

                ProductListDto productListDto = new ProductListDto();
                productListDto.setId(product.getId());
                productListDto.setProductName(product.getName());
                productListDto.setBudgetDetails(product.getBudget());
                if (product.getCategory() != null && product.getCategory().getId() != -1
                        && product.getCategory().getName() != null) {
                    productListDto.setBussinessCategory(product.getCategory().getName());
                }

                if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
                    Optional<Users> produsers = usersRepository.findById(product.getProdHead().getId());
                    OwnerDetails owner = new OwnerDetails();
                    owner.setId(produsers.get().getId());
                    owner.setName(produsers.get().getName());
                    owner.setDesignation(produsers.get().getBranch() + " " + produsers.get().getDesignation());
                    owner.setProfilePic(produsers.get().getProfile_pic());
                    productListDto.setProdHead(owner);
                }

                if (product.getTechHead() != null && product.getTechHead().getId() != -1) {
                    Optional<Users> techusers = usersRepository.findById(product.getTechHead().getId());
                    OwnerDetails techowner = new OwnerDetails();
                    techowner.setId(techusers.get().getId());
                    techowner.setName(techusers.get().getName());
                    techowner.setDesignation(techusers.get().getBranch() + " " + techusers.get().getDesignation());
                    techowner.setProfilePic(techusers.get().getProfile_pic());
                    productListDto.setTechHead(techowner);
                }

                if (product.getProdOwner() != null && !product.getProdOwner().isEmpty()) {
                    String[] userIds = product.getProdOwner().split(",");
                    for (String userId : userIds) {
                        // Parse the user ID from string to integer
                        int prodid = Integer.parseInt(userId.trim());

                        // Fetch user details from the repository
                        Optional<Users> userOptional = usersRepository.findById(prodid);

                        // Check if user details are found
                        if (userOptional.isPresent()) {
                            Users ownerpresend = userOptional.get();
                            // Create and populate OwnerDetails object
                            OwnerDetails productowner = new OwnerDetails();
                            productowner.setId(ownerpresend.getId());
                            productowner.setName(ownerpresend.getName());
                            productowner.setDesignation(ownerpresend.getBranch() + " " + ownerpresend.getDesignation());
                            productowner.setProfilePic(ownerpresend.getProfile_pic());
                            prodownerDetailsList.add(productowner);
                        }
                    }
                }

                productListDto.setProductOwners(prodownerDetailsList);

                if (product.getTechOwner() != null && !product.getTechOwner().isEmpty()) {
                    String[] userIdstech = product.getTechOwner().split(",");

                    for (String userIdTech : userIdstech) {
                        // Parse the user ID from string to integer
                        int techid = Integer.parseInt(userIdTech.trim());
                        // Fetch user details from the repository
                        Optional<Users> userOptional = usersRepository.findById(techid);
                        // Check if user details are found
                        if (userOptional.isPresent()) {
                            Users ownerpresend = userOptional.get();
                            // Create and populate OwnerDetails object
                            OwnerDetails productowner = new OwnerDetails();
                            productowner.setId(ownerpresend.getId());
                            productowner.setName(ownerpresend.getName());
                            productowner.setDesignation(ownerpresend.getBranch() + " " + ownerpresend.getDesignation());
                            productowner.setProfilePic(ownerpresend.getProfile_pic());

                            techownerDetailsList.add(productowner);
                        }
                    }
                }
                productListDto.setTechnicalOwners(techownerDetailsList);
                productListDto.setBudgetDetails(product.getCurrency());
                LocalDateTime startDate = product.getCreatedAt();
                LocalDate startDateDate = startDate.toLocalDate();
                productListDto.setStartDate(startDateDate);
                productListDto.setEndDate(product.getEndDate());
                productListDto.setStatus(product.getStatus());
                productListDto.setSummary(product.getSummary());
                productListDto.setFileName(product.getFile());
                productListDto.setFlowName(product.getFlow().getName());
                for (Integer approvals : product.getFlow().getApproval_by()) {
                    Optional<Users> userOptional = usersRepository.findById(approvals);
                    OwnerDetails techowner = new OwnerDetails();
                    techowner.setId(userOptional.get().getId());
                    techowner.setName(userOptional.get().getName());
                    techowner
                            .setDesignation(userOptional.get().getBranch() + " " + userOptional.get().getDesignation());
                    techowner.setProfilePic(userOptional.get().getProfile_pic());
                    approverDetailsList.add(techowner);
                }
                productListDto.setApprovalFlow(approverDetailsList);

                if (hasRejected) {
                    productListDto.setApprovalStatus(EProductApproStatus.Rejected);
                } else if (hasPending) {
                    productListDto.setApprovalStatus(EProductApproStatus.Pending);
                } else if (hasApproved) {
                    productListDto.setApprovalStatus(EProductApproStatus.Approved);
                }
                detailsDTOs.add(productListDto);
            }
            detailsDTOs.sort(Comparator.comparing(ProductListDto::getId).reversed());

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "fetched successfully", detailsDTOs));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while fetching the product list", e.getMessage()));
        }

    }

    @Override
    public ResponseEntity<?> getProductNameList() {
        int authUserId = AuthUserData.getUserId();
        String branch = AuthUserData.getBranch();
        String designation = AuthUserData.getDesignation();
        List<ProductNamesDTO> products;

        if ("Technical".equals(branch) && "Owner".equals(designation)) {

            List<Object[]> results = productRepository.findByTechOwnerSearch(authUserId);

            ObjectMapper objectMapper = new ObjectMapper();
            products = new ArrayList<>();

            for (Object[] result : results) {
                int id = (Integer) result[0];
                String name = (String) result[1];
                ProductNamesDTO product = new ProductNamesDTO(id, name);
                products.add(product);
            }
        } else if ("Product".equals(branch) && "Owner".equals(designation)) {
            List<Object[]> results = productRepository.findByProdOwnerSearch(authUserId);
            ObjectMapper objectMapper = new ObjectMapper();
            products = new ArrayList<>();

            for (Object[] result : results) {
                int id = (Integer) result[0];
                String name = (String) result[1];
                ProductNamesDTO product = new ProductNamesDTO(id, name);
                products.add(product);
            }

        } else if ("Data".equals(branch) && "Owner".equals(designation)) {
            List<Object[]> results = productRepository.findByDataOwnerSearch(authUserId);
            ObjectMapper objectMapper = new ObjectMapper();
            products = new ArrayList<>();

            for (Object[] result : results) {
                int id = (Integer) result[0];
                String name = (String) result[1];
                ProductNamesDTO product = new ProductNamesDTO(id, name);
                products.add(product);
            }

        } else {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Invalid branch or designation", null));
        }
        // Collections.sort(products, Comparator.comparing(ProductNamesDTO::getName));

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Fetched successfully", products));
    }

    @Override
    public ResponseEntity<?> assignProduct(int id, ProductAssignRequest product, String owner) {
        Optional<Product> products = productRepository.findById(id);
        if (products.isPresent()) {
            Product existingProduct = products.get();
            Set<String> existingOwnerIds = new HashSet<>();

            if ("product".equalsIgnoreCase(owner)) {
                String prodOwner = product.getProdOwner();
                if (prodOwner != null) {
                    existingOwnerIds.add(existingProduct.getProdOwner());
                    existingProduct.setProdOwner(prodOwner);
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    executorService
                            .execute(() -> executeAssignProductMail(existingOwnerIds, prodOwner,
                                    products.get().getName(), "Product"));
                }
            } else if ("technical".equalsIgnoreCase(owner)) {
                String techOwner = product.getTechOwner();
                if (techOwner != null) {
                    existingOwnerIds.add(existingProduct.getTechOwner());
                    existingProduct.setTechOwner(techOwner);
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    executorService.execute(
                            () -> executeAssignProductMail(existingOwnerIds, techOwner, existingProduct.getName(),
                                    "Technical"));
                }
            } else if ("data".equalsIgnoreCase(owner)) {
                String dataOwner = product.getDataOwner();
                if (dataOwner != null && !dataOwner.equals("0")) {

                    existingOwnerIds.add(existingProduct.getDataOwner());

                    existingProduct.setDataOwner(dataOwner);
                    existingProduct.setUpdatedAt(LocalDateTime.now());
                    executorService.execute(
                            () -> executeAssignProductMail(existingOwnerIds, dataOwner, existingProduct.getName(),
                                    "Data"));
                }
            }

            // Save the updated product
            Product savedProduct = productRepository.save(existingProduct);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Product Assign successfully", savedProduct));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Data Not Found to Assign", null));
        }
    }

    private void executeAssignProductMail(Set<String> existingOwnerIds, String ownerIds, String prodName,
                                          String branch) {
        String[] ownerIdsArray = ownerIds.split(",");
        for (String ownerId : ownerIdsArray) {
            String trimmedOwnerId = ownerId.trim();
            if (!existingOwnerIds.contains(trimmedOwnerId)) {
                Optional<Users> userOptional = usersRepository.findById(Integer.parseInt(trimmedOwnerId));
                if (userOptional.isPresent()) {
                    Users user = userOptional.get();
                    String htmlContent = "<div>"
                            + "<p style='padding-left:5px'>"
                            + "you have been assigned as a <b>" + branch + " Owner </b> in our <b>" + prodName
                            + "</b> . We kindly request you to review the details by clicking on the \"View Details\" link provided below:"
                            + "</p>"
                            + "<p style='text-align: center;'><a href='" + portalUrl +
                            "' style='color: #007bff; text-decoration: none;font-weight:bold'>"
                            + "View Details"
                            + "</a></p>"
                            + "</div>";
                    emailservice.sendEmail(user.getEmail(), "Notification for Product Assign", htmlContent);
                }
            }
        }
    }

    @Override
    public ResponseEntity<ApiResponse> getProduct() {
        try {
            List<Product> product = productRepository.findAll();
            if (product == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "No such Product Found in Product List", Collections.emptyList()));
            }
            product = product.stream().sorted(Comparator.comparing(Product::getId).reversed())
                    .collect(Collectors.toList());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ApiResponse(true, "Product List Found in Product List", Collections.singletonList(product)));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Product List Not Found", Collections.emptyList()));
        }
    }

    public ResponseEntity<?> updateProduct(int id, ProductCreateRequest productCreateRequest, MultipartFile file,
                                           String option, String key) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<ProductCreateRequest>> violations = validator.validate(productCreateRequest);
        if (!violations.isEmpty()) {
            List<String> errorMessages = new ArrayList<>();
            for (ConstraintViolation<ProductCreateRequest> violation : violations) {
                errorMessages.add(violation.getMessage());
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Validation error", errorMessages));
        }
        try {
            LocalDateTime currDateTime = LocalDateTime.now();
            int user_id = AuthUserData.getUserId();

            Product productrepo = productRepository.findById(id).orElse(null);

            if (key != null && key.equalsIgnoreCase("admin")) {
                productrepo.setStatus(ProductStatus.CREATED);
                List<Integer> techOwnerIds = productCreateRequest.getTechOwner();
                List<Integer> prodOwnerIds = productCreateRequest.getProdOwner();
                List<Integer> DataOwner = productCreateRequest.getDataOwner();
                List<Integer> howOwner = productCreateRequest.getDataOwner();
                if (techOwnerIds != null && !techOwnerIds.isEmpty()) {
                    productrepo.setTechOwner(techOwnerIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }
                if (prodOwnerIds != null && !prodOwnerIds.isEmpty()) {
                    productrepo.setProdOwner(prodOwnerIds.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }

                if (DataOwner != null && !DataOwner.isEmpty()) {
                    productrepo.setDataOwner(DataOwner.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }

                if (howOwner != null && !howOwner.isEmpty()) {
                    productrepo.setHowOwner(howOwner.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
                }

            } else {
                if ("DRAFT".equals(option)) {
                    productrepo.setStatus(ProductStatus.DRAFT);
                } else {
                    productrepo.setStatus(ProductStatus.CREATED);
                }
            }

            productrepo.setFlow(flowsRepository.findById(productCreateRequest.getFlow()).get());
            productrepo.setCategory(categoriesRepository.findById(productCreateRequest.getCategory()).get());
            productrepo.setUpdatedAt(currDateTime);
            productrepo.setIsDeleted(false);
            productrepo.setCreatedBy(user_id);

            productrepo.setBudget(productCreateRequest.getBudget());

            productrepo.setSummary(productCreateRequest.getSummary());
            productrepo.setEndDate(productCreateRequest.getEndDate());
            productrepo.setStartDate(productCreateRequest.getStartDate());
            if (productCreateRequest.getTechHead() != null && productCreateRequest.getTechHead() != 0) {
                if (!validateUserIds(productCreateRequest.getTechHead(), "Head", "Technical")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid Technical Head ID: " + productCreateRequest.getTechHead(), null));
                }
                Optional<Users> user = usersRepository.findById(productCreateRequest.getTechHead());
                productrepo.setTechHead(user.get());
            }
            if (productCreateRequest.getProdHead() != null && productCreateRequest.getProdHead() != 0) {

                if (!validateUserIds(productCreateRequest.getProdHead(), "Head", "Product")) {
                    return ResponseEntity.badRequest()
                            .body(new ApiResponse(false,
                                    "Invalid product Head ID: " + productCreateRequest.getProdHead(), null));
                }

                Optional<Users> userprod = usersRepository.findById(productCreateRequest.getProdHead());
                productrepo.setProdHead(userprod.get());

            }

            if (productCreateRequest.getDataHead() != null && productCreateRequest.getDataHead() != 0) {

                Optional<Users> user = usersRepository.findById(productCreateRequest.getDataHead());
                productrepo.setDataHead(user.get());
            }

            productrepo.setCurrency(productCreateRequest.getCurrency());
            productrepo.setName(productCreateRequest.getName());
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
                Optional<Flow> flows = flowsRepository.findById(product_data.getFlow().getId());
                if (flows.isPresent()) {
                    List<Integer> integers = flows.get().getApproval_by();

                    if (key != null && key.equalsIgnoreCase("admin")) {
                        List<ProdApprovalHistory> approveHistrory = approvalHistoryRepository
                                .findByProdId(product_data.getId());
                        if (approveHistrory != null && approveHistrory.size() > 0) {
                            approvalHistoryRepository.deleteByProdId(product_data.getId());
                        }

                        for (Integer integer : integers) {

                            ProdApprovalHistory approvalHistory = new ProdApprovalHistory();

                            approvalHistory.setProduct(product_data.getId());

                            // executorService.execute(() -> productCreateMail(integer,
                            // product_data.getName(), user_id));
                            approvalHistory.setStatus(EProductApproStatus.Approved);
                            approvalHistory.set_deleted(false);
                            approvalHistory.setCreated_at(currDateTime);
                            approvalHistory.setUpdated_at(currDateTime);
                            approvalHistory.setCreated_By(integer);

                            approvalHistoryRepository.save(approvalHistory);
                        }
                    } else {

                        int index = 0;
                        for (Integer integer : integers) {
                            ProdApprovalHistory approvalHistory = new ProdApprovalHistory();
                            approvalHistory.setProduct(product_data.getId());
                            if (index == 0) {
                                approvalHistory.setStatus(EProductApproStatus.Pending);
                            } else {
                                approvalHistory.setStatus(EProductApproStatus.Not_Yet);
                            }
                            approvalHistory.set_deleted(false);
                            approvalHistory.setCreated_at(currDateTime);
                            approvalHistory.setUpdated_at(currDateTime);
                            approvalHistory.setCreated_By(integer);
                            approvalHistoryRepository.save(approvalHistory);
                            index++;
                        }
                    }
                }

            }

            String message = "Product Updated Successfully.";
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, null));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> getAllProducts() {
        try {
            Integer userid = AuthUserData.getUserId();
            Optional<Users> user = usersRepository.findById(userid);
            List<ProductResponse> productResponse_data = new ArrayList<>();
            List<Member> member = new ArrayList<>();
            String role = "Team Lead";
            List<Member> member2 = memberRepository.findByMemberAndRole(user.get(), role);
            member2 = member2.stream().sorted(Comparator.comparing(Member::getId).reversed())
                    .collect(Collectors.toList());
            List<Product> products_data = new ArrayList<>();

            List<Task> allTasks = new ArrayList<>();
            for (Member member3 : member2) {
                ProductResponse productResponse = new ProductResponse();
                BeanUtils.copyProperties(member3, productResponse);
                Product product_data = productRepository.findById(member3.getProdId()).orElse(null);
                if (product_data != null) {
                    List<Task> tasks = taskRepository.findByProdIdAndCreatedByAndBranch(product_data.getId(), userid,
                            user.get().getBranch());
                    allTasks.addAll(tasks);
                    productResponse.setTaskCount(tasks.size());
                    productResponse.setRole(member3.getRole());
                    productResponse.setProduct(product_data);
                    productResponse_data.add(productResponse);
                }
            }

            if (productResponse_data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(false, "No products", Collections.emptyList()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "fetched successfully", productResponse_data));
        } catch (Exception e) {
            log.error("An error occurred while fetching products.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while processing the request.", e.getMessage()));
        }

    }

    @Override
    public ResponseEntity<?> getAllProductsByAssigned() {
        try {
            List<ProductResponse> productResponse_data = new ArrayList<>();
            List<Product> products_data = productRepository.findAll();
            if (products_data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                        .body(new ApiResponse(false, "No products", Collections.emptyList()));
            }
            products_data = products_data.stream().sorted(Comparator.comparing(Product::getId).reversed())
                    .collect(Collectors.toList());
            for (Product product : products_data) {
                ProductResponse productResponse = new ProductResponse();
                BeanUtils.copyProperties(product, productResponse);
                productResponse_data.add(productResponse);
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "fetched successfully", productResponse_data));
        } catch (Exception e) {
            log.error("An error occurred while fetching products.", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while processing the request.", e.getMessage()));
        }
    }

    public UserDTO fetchUserInfo(Integer userId) {
        Users user = usersRepository.findById(userId).orElse(null);
        if (user != null) {
            UserDTO member = new UserDTO();
            member.setId(user.getId());
            member.setName(user.getName());
            member.setProfile_pic(user.getProfile_pic());
            member.setRole(user.getRole_id().stream().map(role -> role.getName()).collect(Collectors.joining(", ")));
            return member;
        }
        return null;
    }

    @Override
    public ResponseEntity<?> getAllProductsById(int id) {
        try {
            // List<Task> tasks = taskRepository.findByProdId(id);
            Optional<Users> user = usersRepository.findById(AuthUserData.getUserId());
            List<Task> tasks = taskRepository.findByProdIdandbranchAndCreatedBy(id, user.get().getBranch(),
                    user.get().getId());
            List<TaskResponse> taskResponses = new ArrayList<>();
            tasks = tasks.stream().sorted(Comparator.comparing(Task::getId).reversed()).collect(Collectors.toList());
            for (Task task : tasks) {
                TaskResponse taskResponse = new TaskResponse();
                BeanUtils.copyProperties(task, taskResponse);
                List<UserDTO> userInfos = new ArrayList<>();
                List<Integer> userIds = task.getAssignedTo();
                for (Integer userId : userIds) {
                    UserDTO userInfo = fetchUserInfo(userId);
                    userInfos.add(userInfo);
                }
                taskResponse.setUsers(userInfos);
                taskResponses.add(taskResponse);
            }
            if (taskResponses.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(false, "No products", Collections.emptyList()));
            }

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Product Fetched Successfully", taskResponses));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage(), Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> listProductsByStatus(String status) {
        int user_id = AuthUserData.getUserId();
        EProductApproStatus approvalStatus;

        if ("Pending".equalsIgnoreCase(status)) {
            approvalStatus = EProductApproStatus.Pending;
        } else if ("Approved".equalsIgnoreCase(status)) {
            approvalStatus = EProductApproStatus.Approved;
        } else {
            approvalStatus = EProductApproStatus.Rejected;
        }
        List<ProdApprovalHistory> history = approvalHistoryRepository.getByStatus(approvalStatus);
        Set<Integer> printedProductIds = new HashSet<>();
        for (ProdApprovalHistory historyData : history) {
            int productId = historyData.getProduct();
            printedProductIds.add(productId);
        }
        List<ProductDTO> detailsDTOs = new ArrayList<>();

        for (Integer printedProductId : printedProductIds) {
            Optional<Product> productList = productRepository.findByIdAndCreatedBy(printedProductId, user_id);
            if (productList.isPresent()) {
                Product product = productList.get();
                ProductDTO detailsDTO = new ProductDTO();
                detailsDTO.setId(product.getId());
                detailsDTO.setName(product.getName());
                detailsDTO.setBudget(product.getBudget());
                detailsDTO.setCategoryID(product.getCategory().getId());
                detailsDTO.setCategoryName(product.getCategory().getName());
                // detailsDTO.setProd_headId(product.getProdHead().getId());
                // detailsDTO.setProd_name(product.getProdHead().getName());
                // detailsDTO.setTech_headId(product.getTechHead().getId());
                // detailsDTO.setTech_name(product.getTechHead().getName());
                if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
                    detailsDTO.setProd_headId(product.getProdHead().getId());
                    detailsDTO.setProd_name(product.getProdHead().getName());
                }

                if (product.getTechHead() != null && product.getTechHead().getId() != -1) {
                    detailsDTO.setTech_headId(product.getTechHead().getId());
                    detailsDTO.setTech_name(product.getTechHead().getName());
                }
                detailsDTO.setCurrency(product.getCurrency());
                detailsDTO.setCreatedAt(product.getCreatedAt());
                detailsDTO.setEndDate(product.getEndDate());
                detailsDTO.setStartDate(product.getStartDate());
                detailsDTO.setStatus(product.getStatus());
                detailsDTO.setSummary(product.getSummary());
                detailsDTO.setProdOwner(product.getProdOwner());
                detailsDTO.setTechOwner(product.getTechOwner());
                detailsDTO.setFile(product.getFile());
                detailsDTO.setProdOwner(product.getProdOwner());
                detailsDTO.setTechOwner(product.getTechOwner());
                detailsDTO.setFlow(product.getFlow().getId());
                detailsDTO.setIsDeleted(product.getIsDeleted());
                detailsDTO.setUpdatedAt(product.getUpdatedAt());
                detailsDTO.setApprovalStatus(approvalStatus);
                detailsDTOs.add(detailsDTO);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTOs));
    }

    /// dummy
    @Override
    public ResponseEntity<?> listProductsByStatusOriginal(String status) {
        int user_id = AuthUserData.getUserId();
        EProductApproStatus approvalStatus;

        if ("Pending".equalsIgnoreCase(status)) {
            approvalStatus = EProductApproStatus.Pending;
        } else if ("Approved".equalsIgnoreCase(status)) {
            approvalStatus = EProductApproStatus.Approved;
        } else {
            approvalStatus = EProductApproStatus.Rejected;
        }

        List<Product> productListAll = new ArrayList<>();

        List<Product> productList = productRepository.findByCreatedBy(user_id);
        for (Product product : productList) {
            if (ProductStatus.CREATED.equals(product.getStatus())) {
                List<ProdApprovalHistory> history = approvalHistoryRepository.findByProdId(product.getId());

                // Check if all history entries match the desired status
                boolean allApproved = history.stream()
                        .allMatch(approval -> approvalStatus.equals(approval.getStatus()));

                if (allApproved) {
                    productListAll.add(product);
                }
            }
        }

        List<ProductDTO> detailsDTOs = new ArrayList<>();

        for (Product product : productListAll) {

            ProductDTO detailsDTO = new ProductDTO();
            detailsDTO.setId(product.getId());
            detailsDTO.setName(product.getName());
            detailsDTO.setBudget(product.getBudget());
            detailsDTO.setCategoryID(product.getCategory().getId());
            detailsDTO.setCategoryName(product.getCategory().getName());
            // detailsDTO.setProd_headId(product.getProdHead().getId());
            // detailsDTO.setProd_name(product.getProdHead().getName());
            // detailsDTO.setTech_headId(product.getTechHead().getId());
            // detailsDTO.setTech_name(product.getTechHead().getName());
            if (product.getProdHead() != null && product.getProdHead().getId() != -1) {
                detailsDTO.setProd_headId(product.getProdHead().getId());
                detailsDTO.setProd_name(product.getProdHead().getName());
            }

            if (product.getTechHead() != null && product.getTechHead().getId() != -1) {
                detailsDTO.setTech_headId(product.getTechHead().getId());
                detailsDTO.setTech_name(product.getTechHead().getName());
            }
            detailsDTO.setCurrency(product.getCurrency());
            detailsDTO.setCreatedAt(product.getCreatedAt());
            detailsDTO.setEndDate(product.getEndDate());
            detailsDTO.setStartDate(product.getStartDate());
            detailsDTO.setStatus(product.getStatus());
            detailsDTO.setSummary(product.getSummary());
            detailsDTO.setProdOwner(product.getProdOwner());
            detailsDTO.setTechOwner(product.getTechOwner());
            detailsDTO.setFile(product.getFile());
            detailsDTO.setProdOwner(product.getProdOwner());
            detailsDTO.setTechOwner(product.getTechOwner());
            detailsDTO.setFlow(product.getFlow().getId());
            detailsDTO.setIsDeleted(product.getIsDeleted());
            detailsDTO.setUpdatedAt(product.getUpdatedAt());
            detailsDTO.setApprovalStatus(approvalStatus);
            detailsDTOs.add(detailsDTO);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTOs));
    }

    @Override
    public ResponseEntity<?> getProductwithtask(int id) {
        Product product = productRepository.findById(id).orElse(null);
        ProductTaskDTO productTaskDTOS = new ProductTaskDTO();
        if (product == null) {
            return new ResponseEntity<>(new ApiResponse(false, "Product not found"), HttpStatus.NOT_FOUND);
        }
        List<Task> tasks = taskRepository.findByProdId(id);
        BeanUtils.copyProperties(product, productTaskDTOS);
        productTaskDTOS.setProduct(product);
        productTaskDTOS.setTask(tasks);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "fetched successfully", productTaskDTOS));

    }

    @Override
    public ResponseEntity<?> getApprovedList(String status, String filter) {

        List<Product> products = null;
        if (filter != null) {
            products = productRepository.findByProductName(filter);
        } else {
            products = productRepository.findAll();

        }
        products = products.stream().sorted(Comparator.comparing(Product::getId).reversed())
                .collect(Collectors.toList());
        Users user = usersRepository.findById(AuthUserData.getUserId()).get();
        List<Map<String, Object>> responseList = new ArrayList<>();

        for (Product product : products) {
            if (ProductStatus.CREATED.equals(product.getStatus())) {
                List<ProdApprovalHistory> history = approvalHistoryRepository.findByProdId(product.getId());
                boolean allApproved = history.stream()
                        .allMatch(approval -> EProductApproStatus.Approved.equals(approval.getStatus()));

                if (allApproved) {
                    boolean userExists = memberRepository.existsByMemberAndProdId(user, product.getId());
                    Map<String, Object> response = new HashMap<>();
                    response.put("name", product.getName());
                    response.put("id", product.getId());
                    if (userExists) {
                        response.put("status", "Active");
                    } else {
                        response.put("status", "InActive");
                    }
                    // Add filtering based on the 'status' parameter
                    if ("Active".equalsIgnoreCase(status)) {
                        if (response.get("status").equals("Active")) {
                            responseList.add(response);
                        }
                    } else if ("InActive".equalsIgnoreCase(status)) {
                        if (response.get("status").equals("InActive")) {
                            responseList.add(response);
                        }
                    } else {
                        responseList.add(response);
                    }
                }
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", responseList));
    }

    @Override
    public ResponseEntity<?> checkProductUserExists(int id) {
        Users user = usersRepository.findById(AuthUserData.getUserId()).get();
        boolean userExists = memberRepository.existsByMemberAndProdId(user, id);
        Map<String, Object> response = new HashMap<>();
        response.put("status", userExists);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "User exists in Product or not Status", response));
    }

    @Override
    public ResponseEntity<?> deleteaProduct(List<Integer> id) {
        productRepository.updateIsDeleted(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Product deleted Succesfully", null));
    }
}
