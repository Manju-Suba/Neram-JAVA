package pm.serviceImplements;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pm.model.product.Product;
import pm.model.users.Roles;
import pm.model.users.Users;
import pm.repository.CommonTimeSheetActivityRepository;
import pm.repository.ProdApprovalHistoryRepository;
import pm.repository.ProductRepository;
import pm.repository.UsersRepository;
import pm.response.OwnerDetails;
import pm.service.CommonService;
import pm.utils.AuthUserData;

@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProdApprovalHistoryRepository prodApprovalHistoryRepository;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private CommonTimeSheetActivityRepository commonTimeSheetActivityRepository;

    @Override
    public Page<Product> getProductListByOwner(Pageable pageable, boolean search, int value) {

        int authUserId = AuthUserData.getUserId();
        String branch = AuthUserData.getBranch();
        String designation = AuthUserData.getDesignation();
        Page<Product> products;

        if ("Technical".equals(branch) && "Owner".equals(designation)) {

            if (search) {

                products = productRepository.getproductWithSearchTech(authUserId, value, pageable);

            } else {

                products = productRepository.findByTechOwnerWithPage(authUserId, pageable);
            }

        } else if ("Product".equals(branch) && "Owner".equals(designation)) {
            if (search) {
                products = productRepository.getproductWithSearchProd(authUserId, value, pageable);

            } else {

                products = productRepository.findByProdOwnerWithPage(authUserId, pageable);
            }

        } else if ("Data".equals(branch) && "Owner".equals(designation)) {
            if (search) {
                products = productRepository.getproductWithSearchData(authUserId, value, pageable);

            } else {

                products = productRepository.findBydataOwnerWithPage(authUserId, pageable);
            }

        } else if ("HOW".equals(branch) && "Owner".equals(designation)) {
            if (search) {
                products = productRepository.getproductWithSearchHow(authUserId, value, pageable);

            } else {

                products = productRepository.findByHowOwnerWithPage(authUserId, pageable);
            }
        }
            else {

            return Page.empty();
        }
        return products;
    }

    @Override
    public Page<Product> getProductListByHead(Pageable pageable, boolean search, int value, String status, String key) {

        String branch = AuthUserData.getBranch();
        String designation = AuthUserData.getDesignation();
        int authUserId = AuthUserData.getUserId();

        status = status.toLowerCase();
        Page<Product> products;

        if (key != null && key.equalsIgnoreCase("admin")) {
            if (search) {
                products = productRepository.getProductsByInternalAdminSerach(value, pageable);

            } else {
                products = productRepository.getProductsByApprovedAndProdHeadAndAdminWithPage(pageable);

            }
            return products;
        } else {
            if (search) {
                if ("Technical".equals(branch) && "Head".equals(designation)) {
                    products = handleTechnicalHead(authUserId, status, pageable, value);
                } else if ("Product".equals(branch) && "Head".equals(designation)) {
                    products = handleProductHead(authUserId, status, pageable, value);
                } else if ("Data".equals(branch) && "Head".equals(designation)) {
                    products = handleDataHead(authUserId, status, pageable, value);
                } else if ("HOW".equals(branch) && "Head".equals(designation)) {
                    products = handleHowHead(authUserId, status, pageable, value);
                } else {

                    return Page.empty();
                }
                return products;
            } else {
                if ("Technical".equals(branch) && "Head".equals(designation)) {
                    products = handleTechnicalHead(authUserId, status, pageable);
                } else if ("Product".equals(branch) && "Head".equals(designation)) {
                    products = handleProductHead(authUserId, status, pageable);
                } else if ("Data".equals(branch) && "Head".equals(designation)) {
                    products = handleDataHead(authUserId, status, pageable);
                } else if ("HOW".equals(branch) && "Head".equals(designation)) {
                    products = handleHowHead(authUserId, status, pageable);
                } else {

                    return Page.empty();
                }
                return products;
            }
        }

    }

    // get product on head pagination
    private Page<Product> handleTechnicalHead(int authUserId, String status, Pageable pageable) {

        Page<Product> product = null;
        switch (status) {

            case "draft" -> {
                product = productRepository.getByTechHeadAndStatusWithPage(authUserId, status, pageable);
                break;
            }
            case "approved" -> {
                product = productRepository.getProductsByApprovedAndTechHeadWithPage(authUserId, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndTechHeadWithPage(authUserId, pageable);
                break;
            }
            case "rejected" -> {
                product = productRepository.getProductsByRejectedAndTechHeadWithPage(authUserId, pageable);
                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }
        return product;
    }

    private Page<Product> handleProductHead(int authUserId, String status, Pageable pageable) {
        Page<Product> product = null;
        switch (status) {

            case "draft" -> product = productRepository.getByProdHeadAndStatusWithPage(authUserId, status, pageable);
            case "approved" -> {
                product = productRepository.getProductsByApprovedAndProdHeadWithPage(authUserId, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndProdHeadWithPage(authUserId, pageable);
                break;
            }
            case "rejected" -> {

                product = productRepository.getProductsByRejectedAndProdHeadWithPage(authUserId, pageable);

                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }
    // get product on head with search and pagination

    private Page<Product> handleDataHead(int authUserId, String status, Pageable pageable) {
        Page<Product> product = null;
        switch (status) {

            case "draft" -> product = productRepository.getByDataHeadAndStatusWithPage(authUserId, status, pageable);
            case "approved" -> {
                product = productRepository.getProductsByApprovedAndDataHeadWithPage(authUserId, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndDataHeadWithPage(authUserId, pageable);
                break;
            }
            case "rejected" -> {

                product = productRepository.getProductsByRejectedAndDataHeadWithPage(authUserId, pageable);

                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }

    private Page<Product> handleHowHead(int authUserId, String status, Pageable pageable) {
        Page<Product> product = null;
        switch (status) {

            case "draft" -> product = productRepository.getByHowHeadAndStatusWithPage(authUserId, status, pageable);
            case "approved" -> {
                product = productRepository.getProductsByApprovedAndHowHeadWithPage(authUserId, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndHowHeadWithPage(authUserId, pageable);
                break;
            }
            case "rejected" -> {

                product = productRepository.getProductsByRejectedAndHowHeadWithPage(authUserId, pageable);

                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }

    // get product on head with search and pagination
    private Page<Product> handleTechnicalHead(int authUserId, String status, Pageable pageable, int vaule) {

        Page<Product> product = null;
        switch (status) {

            case "approved" -> {
                product = productRepository.getProductsByApprovedAndTechHeadSerach(authUserId, vaule, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndTechHeadSerach(authUserId, vaule, pageable);
                break;
            }
            case "rejected" -> {
                product = productRepository.getProductsByRejectedAndTechHeadSearch(authUserId, vaule, pageable);
                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }
        return product;
    }

    private Page<Product> handleProductHead(int authUserId, String status, Pageable pageable, int vaule) {

        Page<Product> product = null;
        switch (status) {

            case "approved" -> {
                product = productRepository.getProductsByApprovedAndProdHeadSearch(authUserId, vaule, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndProdHeadSerach(authUserId, vaule, pageable);
                break;
            }
            case "rejected" -> {

                product = productRepository.getProductsByRejectedAndProdHeadSerach(authUserId, vaule, pageable);

                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }
    // get allproduct on head

    private Page<Product> handleDataHead(int authUserId, String status, Pageable pageable, int vaule) {

        Page<Product> product = null;
        switch (status) {

            case "approved" -> {
                product = productRepository.getProductsByApprovedAndDataHeadSearch(authUserId, vaule, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndDataHeadSerach(authUserId, vaule, pageable);
                break;
            }
            case "rejected" -> {

                product = productRepository.getProductsByRejectedAndDataHeadSerach(authUserId, vaule, pageable);

                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }

    private Page<Product> handleHowHead(int authUserId, String status, Pageable pageable, int vaule) {

        Page<Product> product = null;
        switch (status) {

            case "approved" -> {
                product = productRepository.getProductsByApprovedAndHowHeadSearch(authUserId, vaule, pageable);
                break;
            }
            case "pending" -> {
                product = productRepository.getProductsByPendingAndHowHeadSerach(authUserId, vaule, pageable);
                break;
            }
            case "rejected" -> {
                product = productRepository.getProductsByRejectedAndHowHeadSerach(authUserId, vaule, pageable);
                break;
            }
            default -> {
                product = Page.empty();
                break;
            }
        }

        return product;
    }
    // get allproduct on head

    @Override
    public List<Object[]> getAllProductListByHead(String status) {

        String branch = AuthUserData.getBranch();

        String designation = AuthUserData.getDesignation();
        int authUserId = AuthUserData.getUserId();

        status = status.toLowerCase();
        List<Object[]> products;

        if ("Technical".equals(branch) && "Head".equals(designation)) {
            products = allHandleTechnicalHead(authUserId, status);
        } else if ("Product".equals(branch) && "Head".equals(designation)) {
            products = allHandleProductHead(authUserId, status);
        } else {
            products = productRepository.getAllProductsByInternalAdmin();
        }
        return products;
    }

    private List<Object[]> allHandleTechnicalHead(int authUserId, String status) {
        List<Object[]> product;
        switch (status) {

            case "approved" -> {
                product = productRepository.getAllProductsByApprovedAndTechHeadSearch(authUserId);
                break;
            }
            case "pending" -> {
                product = productRepository.getAllProductsByPendingAndTechHeadSearch(authUserId);
                break;
            }
            case "rejected" -> {
                product = productRepository.getAllProductsByRejectedAndTechHeadSearch(authUserId);
                break;
            }
            default -> {
                product = null;
                break;
            }
        }
        return product;
    }

    private List<Object[]> allHandleProductHead(int authUserId, String status) {
        List<Object[]> product = null;
        switch (status) {

            case "approved" -> {
                product = productRepository.getAllProductsByApprovedAndProdHeadSearch(authUserId);
                break;
            }
            case "pending" -> {
                product = productRepository.getAllProductsByPendingAndProdHeadSearch(authUserId);
                break;
            }
            case "rejected" -> {

                product = productRepository.getAllProductsByRejectedAndProdHeadSearch(authUserId);

                break;
            }
            default -> {
                product = null;
                break;
            }
        }

        return product;
    }

    @Override
    public OwnerDetails getUserByIdWithProduct(int userId, int productId) {

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid userId");
        }

        if (productId <= 0) {
            throw new IllegalArgumentException("Invalid productId");
        }

        Optional<Users> userOptional = usersRepository.findById(userId);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        Users user = userOptional.get();

        String approvalStatusflow = productRepository.getStatusfromApprovalHistroy(userId, productId);

        OwnerDetails techowner = new OwnerDetails();
        techowner.setId(user.getId());
        techowner.setName(user.getName());
        techowner.setApprovalStatus(approvalStatusflow);

        List<String> roleNamesapproval = user.getRole_id().stream().map(Roles::getName)
                .collect(Collectors.toList());
        String concatenatedowner = String.join(",", roleNamesapproval);
        techowner.setDesignation(concatenatedowner);

        techowner.setProfilePic(user.getProfile_pic());
        return techowner;
    }

    public OwnerDetails getUserById(int userId) {
        Optional<Users> userOptional = usersRepository.findById(userId);
        Users user = userOptional.get();
        if (userOptional.isPresent()) {
            OwnerDetails techowner = new OwnerDetails();
            techowner.setId(user.getId());
            techowner.setName(user.getName());

            List<String> roleNamesapproval = user.getRole_id().stream().map(Roles::getName)
                    .collect(Collectors.toList());
            String concatenatedowner = String.join(",", roleNamesapproval);
            techowner.setDesignation(concatenatedowner);

            techowner.setProfilePic(user.getProfile_pic());
            return techowner;
        }
        return null;

    }

    @Override
    public void updateCommonTaskActivity() {

        List<Integer> users = commonTimeSheetActivityRepository.findDistinctUserIds();
        for (Integer user : users) {

            Optional<Users> userData = usersRepository.findById(user);

            commonTimeSheetActivityRepository.updateSupervisorForActivities(userData.get().getId(),
                    userData.get().getSupervisor());

        }

    }

}
