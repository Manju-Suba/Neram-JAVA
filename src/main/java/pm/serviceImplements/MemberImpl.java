package pm.serviceImplements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import pm.dto.MemberCount;
import pm.dto.MemberDTO;
import pm.dto.ProductDTO;
import pm.dto.ProductListDtoOwner;
import pm.dto.UserDTO;
import pm.model.member.Member;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;
import pm.model.task.TaskCategory;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.ProductRepository;
import pm.request.AddMemberRequest;
import pm.request.UpdateMemberRequest;
import pm.response.ApiResponse;
import pm.response.OwnerDetails;
import pm.repository.RolesRepository;
import pm.repository.UsersRepository;
import pm.request.MemberRequest;
import pm.repository.MemberRepository;
import pm.repository.ProdApprovalHistoryRepository;
import pm.service.CommonService;
import pm.service.EmailService;
import pm.service.MemberService;
import pm.utils.AuthUserData;
import pm.utils.CommonFunct;

@Service
public class MemberImpl implements MemberService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private ProdApprovalHistoryRepository approvalHistoryRepository;

    @Autowired
    private EmailService emailservice;
    @Autowired
    private CommonService commonService;

    @Autowired
    private CommonFunct commonFunct;

    @Value("${myapp.customProperty}")
    private String portalUrl;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    // Owner Based Product List API
    // @Override
    // public ResponseEntity<?> getProductList() {
    // int id = AuthUserData.getUserId();
    // Users user = usersRepository.findById(AuthUserData.getUserId()).get();
    // String designation = user.getBranch() + " " + user.getDesignation();
    // List<Product> products = new ArrayList<>();
    // List<Product> prod = productRepository.findAll();
    //
    // for (Product prodct : prod) {
    // String ownersList = designation.equalsIgnoreCase("Technical Owner") ?
    // prodct.getTechOwner()
    // : prodct.getProdOwner();
    // if (ownersList != null && (ownersList.contains(String.valueOf(id)))) {
    // products.add(prodct);
    //
    // }
    // }
    // products.sort(Comparator.comparing(Product::getId).reversed());
    // List<ProductDTO> detailsDTOs = new ArrayList<>();
    // for (Product productss : products) {
    // List<MemberDTO> memberDTOs = new ArrayList<>();
    // List<Member> members =
    // memberRepository.findByProdIdAndBranchAndAssignedBy(productss.getId(),
    // user.getBranch(), id);
    //
    // if (!members.isEmpty()) {
    // for (Member member : members) {
    // MemberDTO memberDTO = new MemberDTO();
    // memberDTO.setId(member.getId());
    // memberDTO.setName(member.getMember().getName());
    // memberDTO.setProfile_pic(member.getMember().getProfile_pic());
    // memberDTOs.add(memberDTO);
    // }
    // }
    // ProductDTO detailsDTO = new ProductDTO();
    // detailsDTO.setId(productss.getId());
    // detailsDTO.setName(productss.getName());
    // detailsDTO.setStartDate(productss.getStartDate());
    // detailsDTO.setEndDate(productss.getEndDate());
    // detailsDTO.setMember(memberDTOs);
    // detailsDTOs.add(detailsDTO);
    // }
    // if (products.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "No
    // Data Found", null));
    // }
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // "fetched successfully", detailsDTOs));
    // }
    @Override
    public ResponseEntity<?> getProductList(int page, int size, boolean search, int value) {

        Sort sortByDescId = Sort.by(Sort.Direction.DESC, "updated_at");
        Pageable pageable = PageRequest.of(page, size, sortByDescId);
        int id = AuthUserData.getUserId();
        String branch = AuthUserData.getBranch();
        Page<Product> products = commonService.getProductListByOwner(pageable, search, value);

        List<ProductListDtoOwner> detailsDTOs = new ArrayList<>();
        List<OwnerDetails> approverDetailsList;

        for (Product productss : products) {
            approverDetailsList = new ArrayList<>();

            List<MemberDTO> memberDTOs = new ArrayList<>();
            List<Member> members = memberRepository.findByProdIdAndBranchAndAssignedByandisdeletedList(
                    productss.getId(),
                    branch, id);

            if (!members.isEmpty()) {
                for (Member member : members) {
                    if (member != null && member.getMember() != null) {
                        Users user = usersRepository.findByUserIdandStatus(member.getMember().getId());
                        if (user != null) {
                            MemberDTO memberDTO = new MemberDTO();
                            memberDTO.setId(member.getId());

                            memberDTO.setName(member.getMember().getName());
                            memberDTO.setProfile_pic(member.getMember().getProfile_pic());
                            List<String> roleNames = member.getMember().getRole_id().stream().map(Roles::getName)
                                    .collect(Collectors.toList());
                            String concatenatedRoles = String.join(",", roleNames);
                            memberDTO.setRole(concatenatedRoles);
                            memberDTOs.add(memberDTO);
                        }
                    }
                }
            }
            ProductListDtoOwner productListDtoOwner = new ProductListDtoOwner();
            productListDtoOwner.setId(productss.getId());
            productListDtoOwner.setProductName(productss.getName());
            productListDtoOwner.setStartDate(productss.getStartDate());
            productListDtoOwner.setEndDate(productss.getEndDate());
            productListDtoOwner.setBudgetDetails(productss.getBudget());
            productListDtoOwner.setCurrency(productss.getCurrency());
            productListDtoOwner.setBussinessCategory(productss.getCategory().getName());
            productListDtoOwner.setFlowName(productss.getFlow().getName());
            if (productss.getProdHead() != null && productss.getProdHead().getId() != -1) {
                List<OwnerDetails> details = new ArrayList<>();
                Users produsers = getUserByIdFromCacheOrDB(productss.getProdHead().getId());
                OwnerDetails owner = new OwnerDetails();
                owner.setId(produsers.getId());
                owner.setName(produsers.getName());

                List<String> roleNameshead = produsers.getRole_id().stream().map(Roles::getName)
                        .collect(Collectors.toList());
                String concatenatedRoleshead = String.join(",", roleNameshead);
                owner.setDesignation(concatenatedRoleshead);
                owner.setProfilePic(produsers.getProfile_pic());
                details.add(owner);

                productListDtoOwner.setProdHead(commonFunct.commonFunctionForProduct(details).get(0));

            }

            if (productss.getTechHead() != null && productss.getTechHead().getId() != -1) {
                List<OwnerDetails> techownerDetailsList = new ArrayList<>();
                Users techowner = getUserByIdFromCacheOrDB(productss.getTechHead().getId());
                OwnerDetails techownerdetails = new OwnerDetails();
                techownerdetails.setId(techowner.getId());
                techownerdetails.setName(techowner.getName());

                List<String> roleNamestechhead = techowner.getRole_id().stream().map(Roles::getName)
                        .collect(Collectors.toList());
                String concatenatedRolestechhead = String.join(",", roleNamestechhead);
                techownerdetails.setDesignation(concatenatedRolestechhead);
                techownerdetails.setProfilePic(techowner.getProfile_pic());
                techownerDetailsList.add(techownerdetails);
                productListDtoOwner.setTechHead(commonFunct.commonFunctionForProduct(techownerDetailsList).get(0));

            }

            // ==================================approval
            for (Integer approvals : productss.getFlow().getApproval_by()) {
                Users approvalsdata = getUserByIdFromCacheOrDB(approvals);
                OwnerDetails approval = new OwnerDetails();
                approval.setId(approvalsdata.getId());
                approval.setName(approvalsdata.getName());
                List<String> roleNamesapproval = approvalsdata.getRole_id().stream().map(Roles::getName)
                        .collect(Collectors.toList());
                String concatenatedapproval = String.join(",", roleNamesapproval);
                approval.setDesignation(concatenatedapproval);
                approval.setProfilePic(approvalsdata.getProfile_pic());
                approverDetailsList.add(approval);
            }

            productListDtoOwner.setApprovalFlow(commonFunct.commonFunctionForProduct(approverDetailsList));

            productListDtoOwner.setMember(memberDTOs);
            detailsDTOs.add(productListDtoOwner);
        }
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "No Data Found", null));
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTOs));
    }

    private Users getUserByIdFromCacheOrDB(int userId) {

        Users user = null;
        Optional<Users> userOptional = usersRepository.findById(userId);
        if (userOptional.isPresent()) {
            user = userOptional.get();
        }

        return user;
    }

    @Override
    public ResponseEntity<?> create(MemberRequest memberRequest) {
        List<Member> createdMembers = new ArrayList<>();
        Users user = usersRepository.findById(AuthUserData.getUserId()).get();

        List<AddMemberRequest> memberRequestList = memberRequest.getMembers();
        // email hide
        executorService.execute(() -> memberAddEmail(memberRequestList));
        for (int i = 0; i < memberRequestList.size(); i++) {
            AddMemberRequest memberData = memberRequestList.get(i);
            try {
                Users member = usersRepository.findById(memberData.getMember()).get();
                if (memberRepository.existsByMemberAndProdId(member, memberData.getProdId())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new ApiResponse(false, "Member already exists", Collections.emptyList()));
                }
                LocalDateTime currDateTime = LocalDateTime.now();
                Member menberEntity = new Member();
                menberEntity.setMember(member);
                menberEntity.setProdId(memberData.getProdId());
                menberEntity.setRole(memberData.getRole());

                menberEntity.setCreatedAt(currDateTime);
                menberEntity.setUpdatedAt(currDateTime);
                menberEntity.setIsDeleted(false);
                menberEntity.setStatus("Active");
                menberEntity.setAssignedBy(AuthUserData.getUserId());

                menberEntity.setBranch(user.getBranch());
                Member savedMember = memberRepository.save(menberEntity);
                createdMembers.add(savedMember);

            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new ApiResponse(false, "Internal Server Error: " + e.getMessage(), Collections.emptyList()));
            }
        }
        String message = "Member Created Successfully.";
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, message, createdMembers));
    }

    private void memberAddEmail(List<AddMemberRequest> memberRequestList) {
        LocalDate currentDate = LocalDate.now();
        for (AddMemberRequest member : memberRequestList) {
            Optional<Product> product = productRepository.findById(member.getProdId());
            Optional<Users> users = usersRepository.findById(member.getMember());
            String productName = product.get().getName();
            String email = users.get().getEmail();

            String htmlContent = "<div>"
                    + "<p style='padding-left:5px'>"
                    + "You have been assigned as a <b>" + member.getRole() + "</b>  " + productName + ", effective "
                    + currentDate +
                    " . We kindly request you to review the details by clicking on the \"View Details\" link provided below:"
                    + "</p>"
                    + "<p style='text-align: center;'><a href='" + portalUrl +
                    "' style='color: #007bff; text-decoration: none;font-weight:bold'>"
                    + "View Details"
                    + "</a></p>"
                    + "</div>";

            // Send email
            emailservice.sendEmail(email, "Assigned in product", htmlContent);

        }
    }

    @Override
    public ResponseEntity<?> view(int id) {
        Users userbranch = usersRepository.findById(AuthUserData.getUserId()).get();
        int userId = AuthUserData.getUserId();
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No products", null));
        }
        List<Member> members;
        if (userbranch.getDesignation().equals("Owner")) {
            members = memberRepository.findByProdIdAndBranchAndAssignedByandisdeletedList(id, userbranch.getBranch(),
                    userId);
        } else {
            members = memberRepository.findByProdIdAndBranch(id, userbranch.getBranch());

        }
        List<MemberDTO> createdMembers = new ArrayList<>();

        for (Member data : members) {
            if (data != null && data.getMember() != null) { // Check if member is not null
                Users user = usersRepository.findByUserIdandStatus(data.getMember().getId());
                if (user != null) {
                    MemberDTO memberDTO = new MemberDTO();
                    memberDTO.setId(data.getId());
                    memberDTO.setName(data.getMember().getName());
                    memberDTO.setUserId(data.getMember().getId());
                    memberDTO.setBranch(data.getMember().getBranch());
                    // Optional<Roles> roleNames = rolesRepository.findById(data.getRole());
                    // memberDTO.setRole(roleNames.get().getName().toString());
                    memberDTO.setRole(data.getRole());
                    memberDTO.setProfile_pic(data.getMember().getProfile_pic());
                    createdMembers.add(memberDTO);
                }
            }
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
        // Users user =
        // usersRepository.findById(product.get().getProdOwner()).orElse(null);
        // Users users =
        // usersRepository.findById(product.get().getTechOwner()).orElse(null);
        detailsDTO.setId(product.get().getId());
        detailsDTO.setName(product.get().getName());
        detailsDTO.setBudget(product.get().getBudget());
        detailsDTO.setCategoryID(product.get().getCategory().getId());
        detailsDTO.setCategoryName(product.get().getCategory().getName());
        // detailsDTO.setProd_headId(product.get().getProdHead().getId());
        // detailsDTO.setProd_name(product.get().getProdHead().getName());
        // detailsDTO.setTech_headId(product.get().getTechHead().getId());
        // detailsDTO.setTech_name(product.get().getTechHead().getName());
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
        detailsDTO.setProdOwner(product.get().getProdOwner());
        detailsDTO.setTechOwner(product.get().getTechOwner());
        detailsDTO.setUpdatedAt(product.get().getUpdatedAt());
        detailsDTO.setApprovalFlow(dtos);
        detailsDTO.setMember(createdMembers);
        // if (users != null) {
        // detailsDTO.setTechOwnerName(users.getName());
        // } else {
        // detailsDTO.setTechOwnerName("Unknown Tech Owner");
        // }
        // if (user != null) {
        // detailsDTO.setProdOwnerName(user.getName());
        // } else {
        // detailsDTO.setProdOwnerName("Unknown Prod Owner");
        // }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "fetched successfully", detailsDTO));
    }

    // @Override
    // public ResponseEntity<?> update(int id, MemberRequest memberRequest) {
    // Optional<Product> product = productRepository.findById(id);
    // if (product.isEmpty()) {
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No
    // products", null));
    // }
    // Users user = usersRepository.findById(AuthUserData.getUserId()).get();
    //
    // List<Member> members = memberRepository.findByProdId(id);
    // List<Member> createdMembers = new ArrayList<>();
    // List<Member> memberRequestList = memberRequest.getMembers();
    // int size = Math.min(members.size(), memberRequestList.size());
    //
    // for (int i = 0; i < size; i++) {
    // Member memberData = memberRequestList.get(i);
    // Member existingMember = members.get(i);
    //
    // LocalDateTime currDateTime = LocalDateTime.now();
    // existingMember.setMember(memberData.getMember());
    // existingMember.setRole(memberData.getRole());
    // existingMember.setCreatedAt(currDateTime);
    // existingMember.setUpdatedAt(currDateTime);
    // existingMember.setIsDeleted(false);
    // existingMember.setStatus("Active");
    // existingMember.setBranch(user.getBranch());
    // Member savedMember = memberRepository.save(existingMember);
    // createdMembers.add(savedMember);
    // }
    // if (members.size() < memberRequestList.size()) {
    //
    // for (int i = members.size(); i < memberRequestList.size(); i++) {
    // Member memberData = memberRequestList.get(i);
    // Member newMember = new Member();
    // newMember.setMember(memberData.getMember());
    // newMember.setRole(memberData.getRole());
    // newMember.setProdId(product.get().getId());
    // newMember.setCreatedAt(LocalDateTime.now());
    // newMember.setUpdatedAt(LocalDateTime.now());
    // newMember.setIsDeleted(false);
    // newMember.setStatus("Active");
    // newMember.setAssignedBy(AuthUserData.getUserId());
    // newMember.setBranch(user.getBranch());
    //
    // // Save the new member
    // Member savedMember = memberRepository.save(newMember);
    // createdMembers.add(savedMember);
    // executorService.execute(() -> memberupdateEmail(product.get().getId(),
    // memberData.getMember().getEmail(), memberData.getMember().getName()));
    // }
    // }
    //
    // return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true,
    // "Updated successfully", createdMembers));
    // }
    @Override
    public ResponseEntity<?> update(int id, MemberRequest memberRequest) {
        // Find the product by ID
        Optional<Product> productOptional = productRepository.findById(id);
        if (productOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(false, "No products found", null));
        }
        Product product = productOptional.get();

        // Find the authenticated user
        Users user = usersRepository.findById(AuthUserData.getUserId()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ApiResponse(false, "User not found", null));
        }

        List<Member> members = memberRepository.findByProdId(id);
        List<Member> createdMembers = new ArrayList<>();
        if (memberRequest.getMembers() != null) {
            List<AddMemberRequest> memberRequestList = memberRequest.getMembers();

            // Iterate through the member request list
            for (AddMemberRequest memberData : memberRequestList) {
                Member data = new Member();
                BeanUtils.copyProperties(memberData, data);
                data.setMember(usersRepository.findById(memberData.getMember()).get());
                Optional<Member> existingMemberOptional = members.stream()
                        .filter(member -> member.getMember() == data.getMember())
                        .findFirst();
                if (existingMemberOptional.isPresent()) {
                    // Update existing member details
                    Member existingMember = existingMemberOptional.get();
                    existingMember.setMember(data.getMember());
                    existingMember.setRole(data.getRole());
                    existingMember.setUpdatedAt(LocalDateTime.now());
                    existingMember.setBranch(user.getBranch());
                    memberRepository.save(existingMember); // Update member details
                    createdMembers.add(existingMember);
                } else {
                    // Create a new member
                    Member newMember = new Member();
                    newMember.setMember(data.getMember());
                    newMember.setRole(data.getRole());
                    newMember.setProdId(product.getId());
                    newMember.setCreatedAt(LocalDateTime.now());
                    newMember.setUpdatedAt(LocalDateTime.now());
                    newMember.setIsDeleted(false);
                    newMember.setStatus("Active");
                    newMember.setAssignedBy(AuthUserData.getUserId());
                    newMember.setBranch(user.getBranch());
                    memberRepository.save(newMember); // Save the new member
                    createdMembers.add(newMember);
                }
            }
            // Delete members that are not in the member request list
            members.stream()
                    .filter(member -> {
                        Users memberUser = member.getMember();
                        return memberUser != null && !memberRequestList.stream().anyMatch(m -> {
                            Integer memberId = m.getMember();
                            return memberId != null && memberId.equals(memberUser.getId());
                        });
                    })
                    .forEach(member -> {
                        member.setIsDeleted(true);
                        memberRepository.save(member); // Soft delete the member
                    });

        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Updated successfully", createdMembers));
    }

    private void memberupdateEmail(Integer product_id, String email, String name) {

        Optional<Product> product = productRepository.findById(product_id);
        String productName = product.get().getName();

        String body = "<html>" + "<body>" + "<p>Hi ,<b>" + name + "</b></p>"
                + "<p>You are Assigned for this project <b>" + productName + " </b></p>" + "</body>" + "</html>";

        // Send email
        emailservice.sendEmail(email, "Notification", body);
    }

    @Override
    public ResponseEntity<?> userList() {

        List<Users> users = usersRepository.findByDesignation("Employee");
        users = users.stream().sorted(Comparator.comparing(Users::getId).reversed()).collect(Collectors.toList());
        List<UserDTO> createUser = new ArrayList<>();
        for (Users member : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(member.getId());
            userDTO.setName(member.getName());
            String roleNames = member.getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.joining(", "));
            userDTO.setRole(roleNames);
            userDTO.setProfile_pic(member.getProfile_pic());
            createUser.add(userDTO);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Updated successfully", createUser));

    }

    @Override
    public ResponseEntity<?> userListid(List<Integer> id) {
        try {
            List<Users> users;
            if (id != null && !id.isEmpty()) {
                users = usersRepository.findByDesignationAndIdNotIn("Employee", id);
            } else {
                users = usersRepository.findByDesignation("Employee");

            }
            users = users.stream().sorted(Comparator.comparing(Users::getId).reversed()).collect(Collectors.toList());
            List<UserDTO> createUser = new ArrayList<>();
            for (Users member : users) {
                UserDTO userDTO = new UserDTO();
                userDTO.setId(member.getId());
                userDTO.setName(member.getName());
                String roleNames = member.getRole_id().stream().map(role -> role.getName())
                        .collect(Collectors.joining(", "));
                userDTO.setRole(roleNames);
                userDTO.setProfile_pic(member.getProfile_pic());
                createUser.add(userDTO);
            }

            if (createUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "No users found", Collections.emptyList()));
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ApiResponse(true, "Updated successfully", createUser));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Internal Server Error: " + e.getMessage(), Collections.emptyList()));
        }

    }

    @Override
    public ResponseEntity<?> memberdataCount() {
        Integer userId = AuthUserData.getUserId();
        Optional<Users> userOptional = usersRepository.findById(userId);

        if (userOptional.isPresent()) {
            Users user = userOptional.get();

            Long memberCount = memberRepository.countByMemberAndRole(user, "Team Member");
            Long teamleaderCount = memberRepository.countByMemberAndRole(user, "Team Lead");
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponse(true, "Member Count", new MemberCount(memberCount, teamleaderCount)));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", Collections.emptyList()));
        }
    }

    @Override
    public ResponseEntity<?> userListbybranch(String branch, Integer id) {
        List<Users> users = usersRepository.findByDesignationAndBranch("Employee", branch);
        List<UserDTO> createUser = new ArrayList<>();
        users = users.stream().sorted(Comparator.comparing(Users::getId).reversed()).collect(Collectors.toList());
        for (Users member : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(member.getId());
            userDTO.setName(member.getName());
            String roleNames = member.getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.joining(", "));
            userDTO.setRole(roleNames);
            userDTO.setProfile_pic(member.getProfile_pic());
            List<Member> membersname = memberRepository.findByProdIdAndBranchAndMember(id, branch, member);
            if (membersname.isEmpty()) {
                // The list is not empty
                createUser.add(userDTO);
                // Perform additional actions with the non-empty list
            }

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Updateds successfully", createUser));

    }

    @Override
    public ResponseEntity<?> allUserListbybranch(String branch) {
        List<Users> users = usersRepository.findByDesignationAndBranch("Employee", branch);
        List<UserDTO> createUser = new ArrayList<>();
        users = users.stream().sorted(Comparator.comparing(Users::getId).reversed()).collect(Collectors.toList());
        for (Users member : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(member.getId());
            userDTO.setName(member.getName());
            String roleNames = member.getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.joining(", "));
            userDTO.setRole(roleNames);
            userDTO.setProfile_pic(member.getProfile_pic());
            createUser.add(userDTO);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Updateds successfully", createUser));

    }

    @Override
    public ResponseEntity<?> allUserListbybranchandid(String branch, Integer id) {
        List<Users> users = usersRepository.findByDesignationAndBranchandDeleteted("Employee", branch);

        List<UserDTO> createUser = new ArrayList<>();
        users = users.stream().sorted(Comparator.comparing(Users::getId).reversed()).collect(Collectors.toList());
        for (Users member : users) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(member.getId());
            userDTO.setName(member.getName());
            String roleNames = member.getRole_id().stream().map(role -> role.getName())
                    .collect(Collectors.joining(", "));
            userDTO.setRole(roleNames);
            userDTO.setProfile_pic(member.getProfile_pic());
            List<Member> membersname = memberRepository.findByProdIdAndBranchAndMember(id, branch, member);
            List<Member> isvalue = memberRepository.findByProdIdAndBranchAndAssignedBy1(id, branch,
                    AuthUserData.getUserId(), member.getId());
            if (membersname.isEmpty() || !isvalue.isEmpty()) {
                // The list is not empty
                createUser.add(userDTO);
                // Perform additional actions with the non-empty list
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse(true, "Updateds successfully", createUser));
    }
}
