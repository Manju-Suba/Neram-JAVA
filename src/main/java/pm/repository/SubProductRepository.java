package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pm.model.product.SubProduct;

@Repository
public interface SubProductRepository extends JpaRepository<SubProduct, Integer> {
    boolean existsBySubNameAndProdId(String subproductName, int prodId);

}
