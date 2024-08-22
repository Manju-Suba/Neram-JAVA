package pm.repository;

import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pm.model.product.EProductApproStatus;
import pm.model.product.ProdApprovalHistory;
import pm.model.product.Product;

@Repository
public interface ProdApprovalHistoryRepository extends JpaRepository<ProdApprovalHistory, Integer> {

        @Query(value = "SELECT * FROM prod_approval_history WHERE prod_id = ?1", nativeQuery = true)
        List<ProdApprovalHistory> findByProdId(int prodId);

        @Transactional
        @Modifying
        @Query(value = "DELETE FROM prod_approval_history WHERE prod_id = ?1", nativeQuery = true)
        void deleteByProdId(int prodId);

        @Query(value = "SELECT * FROM prod_approval_history WHERE created_by = :user_id AND status = :status", nativeQuery = true)
        List<ProdApprovalHistory> findByCreatedByAndStatus(int user_id, String status);

        @Query(value = "SELECT * FROM prod_approval_history WHERE created_by = :user_id AND prod_id = :id", nativeQuery = true)
        List<ProdApprovalHistory> findByProdIdAndUserId(int id, int user_id);

        @Query(value = "SELECT * FROM prod_approval_history WHERE status = :status AND prod_id = :id", nativeQuery = true)
        ProdApprovalHistory findByProdIdAndStatus(int id, String status);

        List<ProdApprovalHistory> getByStatus(EProductApproStatus approvalStatus);

        ProdApprovalHistory findByIdAndProduct(int i, int id);

        @Query(value = "SELECT * FROM prod_approval_history WHERE created_by = :user_id AND prod_id IN :ids", nativeQuery = true)
        List<ProdApprovalHistory> findByProductInAndUserId(List<Integer> ids, int user_id);

        @Query(value = "SELECT CASE WHEN NOT EXISTS ("
                        + "SELECT 1 FROM prod_approval_history "
                        + "WHERE prod_id = :productId AND status != :status"
                        + ") THEN TRUE ELSE FALSE END", nativeQuery = true)
        Long areAllStatusesForProduct(@Param("productId") int productId, @Param("status") String status);

        @Query(value = "SELECT created_by FROM prod_approval_history WHERE prod_id = ?1", nativeQuery = true)
        List<Integer> getByProdId(int prodId);

        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE created_by = :user_id AND status = :status ORDER BY updated_at DESC", nativeQuery = true)
        Page<Integer> findProductsByCreatedByApprover(@Param("user_id") int createdBy, @Param("status") String status,
                        Pageable pageable);

        // Approved get product not pending
        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE created_by = :user_id AND status NOT IN ('pending', 'Not_Yet') ORDER BY updated_at DESC", nativeQuery = true)
        Page<Integer> findProductsByCreatedByApproverAndNotPending(@Param("user_id") int createdBy, Pageable pageable);

        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE prod_id = :id AND status = :status ORDER BY updated_at DESC", nativeQuery = true)
        Page<Integer> getProduct(@Param("id") int createdBy, @Param("status") String status, Pageable pageable);

        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE prod_id = :id AND status NOT IN ('pending', 'Not_Yet') ORDER BY updated_at DESC", nativeQuery = true)
        Page<Integer> getProductNotPending(@Param("id") int vaule, Pageable pageable);

        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE created_by = :user_id AND status = :status ORDER BY updated_at DESC", nativeQuery = true)
        List<Integer> findProductsByCreatedByApproverSearch(@Param("user_id") int createdBy,
                        @Param("status") String status);

        // Approved get product not pending
        @Query(value = "SELECT prod_id FROM prod_approval_history WHERE created_by = :user_id AND status NOT IN ('pending', 'Not_Yet') ORDER BY updated_at DESC", nativeQuery = true)
        List<Integer> findProductsByCreatedByApproverAndNotPendingSearch(@Param("user_id") int createdBy);

        @Query(value = "SELECT COUNT(*) FROM prod_approval_history WHERE created_by = :user_id ", nativeQuery = true)
        Long countByApproverDataAndStatusAll(@Param(value = "user_id") Integer user_id);

        @Query(value = "SELECT COUNT(*) FROM prod_approval_history WHERE created_by = :user_id AND status = :status", nativeQuery = true)
        Long countByApproverDataAndStatus(@Param(value = "user_id") Integer user_id, @Param("status") String status);

}
