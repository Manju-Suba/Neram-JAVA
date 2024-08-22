package pm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pm.model.product.BussinessCategory;
import pm.model.users.Roles;

@Repository
public interface BussinessCategoriesRepository extends JpaRepository<BussinessCategory, Integer> {

    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query(value = "UPDATE bussiness_categories b SET b.is_deleted = 1 WHERE b.id IN :id", nativeQuery = true)
    void updateIsDeleted(List<Integer> id);

    @Query(value = "select * from bussiness_categories where is_deleted =false and id =:id", nativeQuery = true)
    Optional<BussinessCategory> getActiveBussinessActivityById(@Param("id") Integer id);

    @Query(value = "select * from bussiness_categories where is_deleted= false", nativeQuery = true)
    List<BussinessCategory> getActiveBussinessCategory();

    @Query(value = "select * from bussiness_categories where is_deleted= false", nativeQuery = true)
    Page<BussinessCategory> getActiveBussinessCategoryWithPage(Pageable pageable);

    @Query(value = "SELECT * FROM bussiness_categories WHERE id =:id AND is_deleted = false", nativeQuery = true)
    Optional<BussinessCategory> findByActiveBussinessCateId(@Param("id") int id);

    Optional<BussinessCategory> findByName(String name);

    @Query(value = "SELECT * from bussiness_categories b WHERE b.name = :name AND b.is_deleted = false", nativeQuery = true)
    Optional<BussinessCategory> findByBussinessNameAndIs_deletedFalse(@Param("name") String name);

    @Query(value = "SELECT * FROM bussiness_categories WHERE LOWER(name) REGEXP LOWER(:nameRegex) AND is_deleted = false", nativeQuery = true)
    Page<BussinessCategory> getActiveBussinessCategoryWithSearch(@Param("nameRegex") String nameRegex, Pageable Pageable);

    @Query(value = "SELECT COUNT(*) FROM bussiness_categories WHERE is_deleted = false", nativeQuery = true)
    Long countByBusiness();
}
