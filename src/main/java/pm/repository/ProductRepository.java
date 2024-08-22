package pm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import pm.model.product.Product;
import pm.model.product.ProductStatus;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

        boolean existsByName(String productName);

        @Query("SELECT p FROM Product p WHERE p.prodHead.id = :prod_head AND p.name = :name AND p.status = :status")
        List<Product> findByUserIdAndNameAndStatus(
                        @Param("prod_head") int prodHeadId,
                        @Param("name") String name,
                        @Param("status") ProductStatus status);

        @Query("SELECT p FROM Product p WHERE p.createdBy = :user_id AND p.status = :status")
        List<Product> findByStatusAndCreatedBy(@Param("status") ProductStatus status, @Param("user_id") int user_id);

        @Query("SELECT p FROM Product p WHERE  p.status = :status")
        List<Product> findByStatus(@Param("status") ProductStatus status);

        List<Product> findByName(String name);

        List<Product> findByNameAndCreatedBy(String name, int user_id);

        @Query(value = "SELECT * FROM products p WHERE p.name LIKE %:name%", nativeQuery = true)
        List<Product> findByNameQuery(String name);

        @Query("SELECT p FROM Product p WHERE p.id = :product")
        List<Product> findByProduct(int product);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.tech_owner) > 0", nativeQuery = true)
        List<Product> findByTechOwner(@Param("id") int id);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.prod_owner) > 0", nativeQuery = true)
        List<Product> findByProdOwner(@Param("id") int id);

        @Query("SELECT p FROM Product p WHERE p.createdBy = :user_id AND p.id = :printedProductId")
        Optional<Product> findByIdAndCreatedBy(Integer printedProductId, int user_id);

        @Query("SELECT p FROM Product p WHERE p.createdBy = :user_id ")
        List<Product> findByCreatedBy(int user_id);

        // @Query(value = "SELECT * FROM products where status =:status and
        // prod_head=:prod_head ", nativeQuery = true)
        // List<Product> getproductidBasedProdhead(@Param("status") ProductStatus
        // status, @Param("prod_head") Users prod_head);
        //
        // @Query(value = "SELECT * FROM products WHERE status = ?1 AND tech_head = ?2",
        // nativeQuery = true)
        // List<Product> getProductsByStatusAndTechHead(ProductStatus status, Users
        // techHead);
        //
        // List<Product> findByStatusAndProdHead(ProductStatus status, Users prodHead);
        //
        // List<Product> findByStatusAndTechHead(ProductStatus status, Users techHead);
        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.prod_owner) > 0", nativeQuery = true)
        List<Product> findByIdAndProdOwner(@Param("id") Integer id);

        @Query(value = "SELECT id FROM products WHERE FIND_IN_SET(?1, tech_owner) > 0 OR FIND_IN_SET(?1, prod_owner) > 0 OR FIND_IN_SET(?1, data_owner) > 0 OR FIND_IN_SET(?1, how_owner) > 0", nativeQuery = true)
        List<Integer> findByOwnerInTechOwnerOrProdOwner(Integer owner);

        @Modifying
        @Transactional
        @Query(value = "UPDATE products SET is_deleted = true WHERE id IN :id AND status = 'DRAFT'", nativeQuery = true)
        void updateIsDeleted(@Param("id") List<Integer> id);

        @Query(value = "SELECT id, name FROM products WHERE status = :status AND prod_head = :prod_head AND is_deleted = false", nativeQuery = true)
        List<Object[]> getproductidBasedProdhead(@Param("status") String status, @Param("prod_head") Integer prod_head);

        @Query(value = "SELECT id, name FROM products WHERE status = :status AND tech_head = :tech_head AND is_deleted = false", nativeQuery = true)
        List<Object[]> getProductsByStatusAndTechHead(@Param("status") String status,
                        @Param("tech_head") Integer tech_head);

        @Query(value = "SELECT status from prod_approval_history where prod_id =:product_id and created_by=:approvals", nativeQuery = true)
        String getStatusfromApprovalHistroy(@Param("approvals") Integer approvals, @Param("product_id") int product_id);

        // @Query(value = "SELECT id,
        // name,budget,currency,end_date,file,name,start_date,status,summary,category_id,flow,prod_head,tech_head,tech_owner,prod_owner
        // FROM products WHERE status = :status AND tech_head = :tech_head", nativeQuery
        // = true)
        // List<Object[]> getProductsByStatusAndTechHead(@Param("status") String status,
        // @Param("tech_head") Integer tech_head);\
        @Query(value = "SELECT * FROM products WHERE FIND_IN_SET(:id, prod_owner) > 0  AND id=:pro_id", nativeQuery = true)
        Page<Product> getproductWithSearchProd(@Param("id") int id, @Param("pro_id") int proId, Pageable Pageable);

        @Query(value = "SELECT * FROM products WHERE FIND_IN_SET(:id, tech_owner) > 0 AND id=:pro_id", nativeQuery = true)
        Page<Product> getproductWithSearchTech(@Param("id") int id, @Param("pro_id") int proId, Pageable Pageable);

        @Query(value = "SELECT * FROM products WHERE FIND_IN_SET(:id, data_owner) > 0  AND id=:pro_id", nativeQuery = true)
        Page<Product> getproductWithSearchData(@Param("id") int id, @Param("pro_id") int proId, Pageable Pageable);

        @Query(value = "SELECT * FROM products WHERE FIND_IN_SET(:id, how_owner) > 0  AND id=:pro_id", nativeQuery = true)
        Page<Product> getproductWithSearchHow(@Param("id") int id, @Param("pro_id") int proId, Pageable Pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.tech_owner) > 0", nativeQuery = true)
        Page<Product> findByTechOwnerWithPage(@Param("id") int id, Pageable pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.prod_owner) > 0", nativeQuery = true)
        Page<Product> findByProdOwnerWithPage(@Param("id") int id, Pageable pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.data_owner) > 0", nativeQuery = true)
        Page<Product> findBydataOwnerWithPage(@Param("id") int id, Pageable pageable);
        
        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.how_owner) > 0", nativeQuery = true)
        Page<Product> findByHowOwnerWithPage(@Param("id") int id, Pageable pageable);

        // owner dropdown

        @Query(value = "SELECT  id,name FROM products p WHERE FIND_IN_SET(:id, p.tech_owner) > 0  and p.is_deleted = false ORDER BY p.updated_at", nativeQuery = true)
        List<Object[]> findByTechOwnerSearch(@Param("id") int id);

        @Query(value = "SELECT id,name FROM products p WHERE FIND_IN_SET(:id, p.prod_owner) > 0  and p.is_deleted = false ORDER BY p.updated_at;", nativeQuery = true)
        List<Object[]> findByProdOwnerSearch(@Param("id") int id);

        @Query(value = "SELECT id,name FROM products p WHERE FIND_IN_SET(:id, p.data_owner) > 0  and p.is_deleted = false ORDER BY p.updated_at;", nativeQuery = true)
        List<Object[]> findByDataOwnerSearch(@Param("id") int id);

        // owner dropdown sreach product

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.prod_head) > 0 and  p.status = :status  and p.is_deleted = false  ORDER BY id DESC", nativeQuery = true)
        Page<Product> getByProdHeadAndStatusWithPage(@Param("id") int id, @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.data_head) > 0 and  p.status = :status  and p.is_deleted = false  ORDER BY id DESC", nativeQuery = true)
        Page<Product> getByDataHeadAndStatusWithPage(@Param("id") int id, @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.how_head) > 0 and  p.status = :status  and p.is_deleted = false  ORDER BY id DESC", nativeQuery = true)
        Page<Product> getByHowHeadAndStatusWithPage(@Param("id") int id, @Param("status") String status,
                        Pageable pageable);    

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.tech_head) > 0 and  p.status = :status and  p.is_deleted = false  ORDER BY id DESC ", nativeQuery = true)
        Page<Product> getByTechHeadAndStatusWithPage(@Param("id") int id, @Param("status") String status,
                        Pageable pageable);

        @Query(value = "SELECT * FROM products p WHERE FIND_IN_SET(:id, p.tech_head) > 0 and  p.status = :status  ORDER BY id DESC", nativeQuery = true)
        List<Product> getByTechHeadAndStatusWithPageOnlyId(@Param("id") int id, @Param("status") String status);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.prod_head = :prodHead AND p.is_deleted = false "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndProdHeadWithPage(@Param("prodHead") Integer prodHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.data_head = :dataHead AND p.is_deleted = false "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndDataHeadWithPage(@Param("dataHead") Integer dataHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.how_head = :howHead AND p.is_deleted = false "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndHowHeadWithPage(@Param("howHead") Integer howHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.is_deleted = false "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndProdHeadAndAdminWithPage(Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.prod_head = :prodHead AND p.is_deleted = false "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndProdHeadWithPage(@Param("prodHead") Integer prodHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.data_head = :dataHead AND p.is_deleted = false "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndDataHeadWithPage(@Param("dataHead") Integer dataHead, Pageable pageable);

        @Query(value = "SELECT * "
                + "FROM products p "
                + "WHERE p.how_head = :howHead AND p.is_deleted = false "
                + "AND EXISTS ( "
                + "    SELECT 1 "
                + "    FROM prod_approval_history ph "
                + "    WHERE ph.prod_id = p.id "
                + "    AND ph.status = 'Pending' "
                + ") "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM prod_approval_history ph "
                + "    WHERE ph.prod_id = p.id "
                + "    AND ph.status = 'Rejected' "
                + ") "
                + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndHowHeadWithPage(@Param("howHead") Integer howHead, Pageable pageable);

        @Query(value = "SELECT p.* "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id, MAX(updated_at) AS max_updated_at "
                        + "    FROM prod_approval_history "
                        + "    WHERE status = 'Rejected' "
                        + "    GROUP BY prod_id "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' "
                        + "AND p.prod_head = :prodHead  "
                        + "ORDER BY ph.max_updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndProdHeadWithPage(@Param("prodHead") Integer prodHead, Pageable pageable);

        @Query(value = "SELECT p.* "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id, MAX(updated_at) AS max_updated_at "
                        + "    FROM prod_approval_history "
                        + "    WHERE status = 'Rejected' "
                        + "    GROUP BY prod_id "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' "
                        + "AND p.data_head = :dataHead  "
                        + "ORDER BY ph.max_updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndDataHeadWithPage(@Param("dataHead") Integer dataHead, Pageable pageable);

        @Query(value = "SELECT p.* "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id, MAX(updated_at) AS max_updated_at "
                        + "    FROM prod_approval_history "
                        + "    WHERE status = 'Rejected' "
                        + "    GROUP BY prod_id "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' "
                        + "AND p.how_head = :howHead  "
                        + "ORDER BY ph.max_updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndHowHeadWithPage(@Param("howHead") Integer howHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.tech_head = :techHead AND p.is_deleted = false "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndTechHeadWithPage(@Param("techHead") Integer techHead, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.tech_head = :techHead AND p.is_deleted = false "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndTechHeadWithPage(@Param("techHead") Integer techHead, Pageable pageable);

        @Query(value = "SELECT p.* " +
                        "FROM products p " +
                        "JOIN ( " +
                        "    SELECT prod_id, MAX(updated_at) AS max_updated_at " +
                        "    FROM prod_approval_history " +
                        "    WHERE status = 'Rejected' " +
                        "    GROUP BY prod_id " +
                        ") ph ON p.id = ph.prod_id " +
                        "WHERE p.status = 'created' " +
                        "AND p.tech_head = :techHead AND p.is_deleted = false " +
                        "ORDER BY ph.max_updated_at DESC", countQuery = "SELECT COUNT(p.id) " +
                                        "FROM products p " +
                                        "JOIN ( " +
                                        "    SELECT prod_id, MAX(updated_at) AS max_updated_at " +
                                        "    FROM prod_approval_history " +
                                        "    WHERE status = 'Rejected' " +
                                        "    GROUP BY prod_id " +
                                        ") ph ON p.id = ph.prod_id " +
                                        "WHERE p.status = 'created' " +
                                        "AND p.tech_head = :techHead AND p.is_deleted = false ", nativeQuery = true)
        Page<Product> getProductsByRejectedAndTechHeadWithPage(@Param("techHead") Integer techHead, Pageable pageable);

        @Query(value = "SELECT id,name "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.prod_head = :prodHead AND p.is_deleted = false "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByApprovedAndProdHeadSearch(@Param("prodHead") Integer prodHead);

        @Query(value = "SELECT id,name "
                        + "FROM products p "
                        + "WHERE p.prod_head = :prodHead AND p.is_deleted = false "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByPendingAndProdHeadSearch(@Param("prodHead") Integer prodHead);

        @Query(value = "SELECT id,name "
                        + " FROM products p "
                        + "WHERE EXISTS ("
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.prod_head = :prodHead "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByRejectedAndProdHeadSearch(@Param("prodHead") Integer prodHead);

        @Query(value = "SELECT id, name "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.tech_head = :techHead AND p.is_deleted = false "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByApprovedAndTechHeadSearch(@Param("techHead") Integer techHead);

        /////////// ===========================================for Internal Admin
        @Query(value = "SELECT id, name "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.is_deleted = false "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByInternalAdmin();

        // ================================================================

        @Query(value = "SELECT id,name "
                        + "FROM products p "
                        + "WHERE p.tech_head = :techHead AND p.is_deleted = false "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByPendingAndTechHeadSearch(@Param("techHead") Integer techHead);

        @Query(value = "SELECT id,name "
                        + "FROM products p "
                        + "WHERE EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.tech_head = :techHead AND p.is_deleted = false "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Object[]> getAllProductsByRejectedAndTechHeadSearch(@Param("techHead") Integer techHead);
        // search product

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.prod_head = :prodHead AND p.is_deleted = false AND p.id = :value  "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndProdHeadSearch(@Param("prodHead") Integer prodHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.data_head = :dataHead AND p.is_deleted = false AND p.id = :value  "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndDataHeadSearch(@Param("dataHead") Integer dataHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                + "FROM products p "
                + "JOIN ( "
                + "    SELECT prod_id "
                + "    FROM prod_approval_history "
                + "    GROUP BY prod_id "
                + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'approved' THEN 1 ELSE 0 END) "
                + ") ph ON p.id = ph.prod_id "
                + "WHERE p.status = 'created' AND p.how_head = :howHead AND p.is_deleted = false AND p.id = :value  "
                + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndHowHeadSearch(@Param("howHead") Integer howHead,@Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.prod_head = :prodHead AND p.is_deleted = false AND p.id = :value  "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndProdHeadSerach(@Param("prodHead") Integer prodHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.data_head = :dataHead AND p.is_deleted = false AND p.id = :value  "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndDataHeadSerach(@Param("dataHead") Integer dataHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                + "FROM products p "
                + "WHERE p.how_head = :howHead AND p.is_deleted = false AND p.id = :value  "
                + "AND EXISTS ( "
                + "    SELECT 1 "
                + "    FROM prod_approval_history ph "
                + "    WHERE ph.prod_id = p.id "
                + "    AND ph.status = 'Pending' "
                + ") "
                + "AND NOT EXISTS ( "
                + "    SELECT 1 "
                + "    FROM prod_approval_history ph "
                + "    WHERE ph.prod_id = p.id "
                + "    AND ph.status = 'Rejected' "
                + ") "
                + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndHowHeadSerach(@Param("howHead") Integer howHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + " FROM products p "
                        + "WHERE EXISTS ("
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.prod_head = :prodHead  AND p.id = :value "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndProdHeadSerach(@Param("prodHead") Integer prodHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + " FROM products p "
                        + "WHERE EXISTS ("
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.data_head = :dataHead  AND p.id = :value "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndDataHeadSerach(@Param("dataHead") Integer dataHead,
                        @Param("value") int value, Pageable pageable);


        @Query(value = "SELECT * "
                        + " FROM products p "
                        + "WHERE EXISTS ("
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.how_head = :howHead  AND p.id = :value "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByRejectedAndHowHeadSerach(@Param("howHead") Integer howHead, @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.tech_head = :techHead AND p.is_deleted = false AND p.id = :value  "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByApprovedAndTechHeadSerach(@Param("techHead") Integer techHead,
                        @Param("value") int value, Pageable pageable);

        /// ===============================for internal Admin

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "JOIN ( "
                        + "    SELECT prod_id "
                        + "    FROM prod_approval_history "
                        + "    GROUP BY prod_id "
                        + "    HAVING COUNT(*) = SUM(CASE WHEN status = 'Approved' THEN 1 ELSE 0 END) "
                        + ") ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND p.is_deleted = false AND p.id = :value  "
                        + "ORDER BY p.updated_at DESC", nativeQuery = true)
        Page<Product> getProductsByInternalAdminSerach(@Param("value") int value, Pageable pageable);

        // =====================================

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE p.tech_head = :techHead AND p.is_deleted = false AND p.id = :value "
                        + "AND EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Pending' "
                        + ") "
                        + "AND NOT EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        Page<Product> getProductsByPendingAndTechHeadSerach(@Param("techHead") Integer techHead,
                        @Param("value") int value, Pageable pageable);

        @Query(value = "SELECT * "
                        + "FROM products p "
                        + "WHERE EXISTS ( "
                        + "    SELECT 1 "
                        + "    FROM prod_approval_history ph "
                        + "    WHERE ph.prod_id = p.id "
                        + "    AND ph.status = 'Rejected' "
                        + ") "
                        + "AND p.status = 'created' "
                        + "AND p.tech_head = :techHead AND p.is_deleted = false AND p.id = :value  "
                        + "ORDER BY p.updated_at Desc", nativeQuery = true)
        Page<Product> getProductsByRejectedAndTechHeadSearch(@Param("techHead") Integer techHead,
                        @Param("value") int value, Pageable pageable);

        // Approved get product search
        @Query(value = "SELECT p.* FROM products p "
                        + "JOIN prod_approval_history ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND ph.created_By = :createdBy "
                        + "AND ph.status = :status  AND p.id = :pro_id "
                        + "GROUP BY p.id "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Product> findProductsByCreatedByApproverSerach(@Param("createdBy") int createdBy,
                        @Param("status") String status, @Param("pro_id") int proId);

        // Approved get product not pending search
        @Query(value = "SELECT p.* FROM products p "
                        + "JOIN prod_approval_history ph ON p.id = ph.prod_id "
                        + "WHERE p.status = 'created' AND ph.created_By = :createdBy "
                        + "AND ph.status != 'pending' AND p.id = :pro_id "
                        + "GROUP BY p.id "
                        + "ORDER BY p.id DESC", nativeQuery = true)
        List<Product> findProductsByCreatedByApproverAndNotPendingSerach(@Param("createdBy") int createdBy,
                        @Param("pro_id") int proId);

        @Query(value = "SELECT * FROM products p WHERE p.id IN :ids", nativeQuery = true)
        List<Product> findProductsByIdIn(List<Integer> ids);

        @Query(value = "SELECT id,name FROM products p WHERE p.id IN :ids", nativeQuery = true)
        List<Object[]> getIdAndNameProductsByIdIn(List<Integer> ids);

        @Query(value = "SELECT * FROM products WHERE LOWER(name) LIKE LOWER(CONCAT('%', :filter, '%'))", nativeQuery = true)
        List<Product> findByProductName(String filter);

        // Approved Product Count
        @Query(value = "SELECT COUNT(*)" +
                        " FROM (" +
                        " SELECT p.id" +
                        " FROM products p" +
                        " JOIN prod_approval_history pah ON pah.prod_id = p.id" +
                        " WHERE p.created_by = :user_id" +
                        " GROUP BY p.id" +
                        " HAVING COUNT(CASE WHEN pah.status != 'Approved' THEN 1 ELSE NULL END) = 0" +
                        " AND COUNT(pah.status) > 0" +
                        ") subquery", nativeQuery = true)
        Long countByApprovedProductAndStatus(@Param(value = "user_id") Integer user_id);

        // Rejected product count
        @Query(value = "SELECT COUNT(*)" +
                        " FROM (" +
                        " SELECT p.id" +
                        " FROM products p" +
                        " JOIN prod_approval_history pah ON pah.prod_id = p.id" +
                        " WHERE p.created_by = :userId" +
                        " GROUP BY p.id" +
                        " HAVING COUNT(CASE WHEN pah.status = 'Rejected' THEN 1 ELSE NULL END) > 0" +
                        ") subquery", nativeQuery = true)
        Long countByRejectedProductAndStatus(@Param(value = "userId") Integer userId);

        // Pending Product count
        @Query(value = "SELECT COUNT(*)" +
                        " FROM (" +
                        " SELECT p.id" +
                        " FROM products p" +
                        " JOIN prod_approval_history pah ON pah.prod_id = p.id" +
                        " WHERE p.created_by = :userId" +
                        " GROUP BY p.id" +
                        " HAVING COUNT(CASE WHEN pah.status = 'Rejected' THEN 1 ELSE NULL END) = 0" +
                        " AND COUNT(CASE WHEN pah.status = 'Pending' THEN 1 ELSE NULL END) > 0" +
                        ") subquery", nativeQuery = true)
        Long countByPendingProductAndStatus(@Param(value = "userId") Integer userId);

        @Query(value = "SELECT COUNT(DISTINCT p.id) AS total_product_count " +
                        "FROM products p " +
                        "JOIN prod_approval_history pah ON pah.prod_id = p.id " +
                        "WHERE p.created_by = :userId ", nativeQuery = true)
        Long countByProductAndStatusAll(@Param(value = "userId") Integer userId);

        @Query(value = "SELECT tech_owner FROM products WHERE created_by = :userId AND tech_owner IS NOT NULL", nativeQuery = true)
        List<String> findTechOwnersByUserId(@Param("userId") Integer userId);

        @Query(value = "SELECT prod_owner FROM products WHERE created_by = :userId AND prod_owner IS NOT NULL", nativeQuery = true)
        List<String> findProductOwnersByUserId(@Param("userId") Integer userId);

        @Query(value = "SELECT how_owner FROM products WHERE created_by = :userId AND how_owner IS NOT NULL", nativeQuery = true)
        List<String> findHowOwnersByUserId(@Param("userId") Integer userId);

        @Query(value = "SELECT data_owner FROM products WHERE created_by = :userId AND data_owner IS NOT NULL", nativeQuery = true)
        List<String> findDataOwnersByUserId(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
                        "FROM products AS p " +
                        "JOIN members AS m ON m.prod_id = p.id " +
                        "WHERE m.assigned_by = :userId " +
                        "AND (FIND_IN_SET(:userId, p.tech_owner) > 0 OR p.tech_owner = :userId)", nativeQuery = true)
        Long findAssignedProductCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
        "FROM products AS p " +
        "JOIN members AS m ON m.prod_id = p.id " +
        "WHERE m.assigned_by = :userId " +
        "AND (FIND_IN_SET(:userId, p.data_owner) > 0 OR p.data_owner = :userId)", nativeQuery = true)
Long findAssignedProductCountData(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
                        "FROM products AS p " +
                        "JOIN members AS m ON m.prod_id = p.id " +
                        "WHERE m.assigned_by = :userId " +
                        "AND (FIND_IN_SET(:userId, p.prod_owner) > 0 OR p.prod_owner = :userId)", nativeQuery = true)
        Long findAssignedProdProductCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
        "FROM products AS p " +
        "JOIN members AS m ON m.prod_id = p.id " +
        "WHERE m.assigned_by = :userId " +
        "AND (FIND_IN_SET(:userId, p.how_owner) > 0 OR p.how_owner = :userId)", nativeQuery = true)
Long findAssignedProdHowCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
                        "FROM products AS p " +
                        "LEFT JOIN members AS m ON m.prod_id = p.id " +
                        "WHERE (m.prod_id IS NULL OR m.assigned_by != :userId) AND (FIND_IN_SET(:userId, p.tech_owner) > 0 OR p.tech_owner = :userId)", nativeQuery = true)
        Long findUnAssignedTechProductCount(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
        "FROM products AS p " +
        "LEFT JOIN members AS m ON m.prod_id = p.id " +
        "WHERE (m.prod_id IS NULL OR m.assigned_by != :userId) AND (FIND_IN_SET(:userId, p.data_owner) > 0 OR p.data_owner = :userId)", nativeQuery = true)
Long findUnAssignedDataCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
                        "FROM products AS p " +
                        "LEFT JOIN members AS m ON m.prod_id = p.id " +
                        "WHERE (m.prod_id IS NULL OR m.assigned_by != :userId) AND (FIND_IN_SET(:userId, p.prod_owner) > 0 OR p.prod_owner = :userId)", nativeQuery = true)
        Long findUnAssignedProdProductCount(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(DISTINCT p.id) AS product_count " +
        "FROM products AS p " +
        "LEFT JOIN members AS m ON m.prod_id = p.id " +
        "WHERE (m.prod_id IS NULL OR m.assigned_by != :userId) AND (FIND_IN_SET(:userId, p.how_owner) > 0 OR p.how_owner = :userId)", nativeQuery = true)
Long findUnAssignedProdHowCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT id) FROM products " +
                        "WHERE (FIND_IN_SET(:userId, tech_owner) > 0 OR tech_owner = :userId)", nativeQuery = true)
        Long findTotalTechProductCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT id) FROM products " +
        "WHERE (FIND_IN_SET(:userId, data_owner) > 0 OR data_owner = :userId)", nativeQuery = true)
Long findTotalTechDataCount(@Param("userId") Integer userId);

        @Query(value = "SELECT COUNT(DISTINCT id) FROM products " +
                        "WHERE (FIND_IN_SET(:userId, prod_owner) > 0 OR prod_owner = :userId)", nativeQuery = true)
        Long findTotalProdProductCount(@Param("userId") Integer userId);


        @Query(value = "SELECT COUNT(DISTINCT id) FROM products " +
        "WHERE (FIND_IN_SET(:userId, how_owner) > 0 OR how_owner = :userId)", nativeQuery = true)
Long findTotalProdHowCount(@Param("userId") Integer userId);

}
