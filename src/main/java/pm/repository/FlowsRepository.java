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

import jakarta.transaction.Transactional;
import pm.model.flow.Flow;

@Repository
public interface FlowsRepository extends JpaRepository<Flow, Integer> {

	@Query(value = "SELECT * FROM poduct_flows WHERE access_to IN (:userId)", nativeQuery = true)
	List<Flow> findByUserIdInAccessTo(@Param("userId") int userId);

	boolean existsByName(String name);

	@Query(value = "SELECT EXISTS (SELECT 1 FROM poduct_flows WHERE name = :name AND is_deleted = false)", nativeQuery = true)
	Integer existsByNameAndIsDeletedFalse(@Param("name") String name);

	@Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Flow f WHERE f.name = :name AND f.id <> :id AND is_deleted = false")
	boolean existsByNameAndNotId(@Param("name") String name, @Param("id") int id);

	@Transactional
	@Modifying
	@Query(value = "UPDATE poduct_flows  SET is_deleted = true WHERE id IN :id", nativeQuery = true)
	void softDeleteById(@Param("id") List<Integer> id);

	@Query(value = "SELECT * FROM poduct_flows WHERE is_deleted = false", nativeQuery = true)
	List<Flow> getAllActiveFlow();

	@Query(value = "SELECT * FROM poduct_flows WHERE is_deleted = false and id=:id ", nativeQuery = true)
	Optional<Flow> getActiveFlowById(@Param("id") int id);

	@Query(value = "SELECT  * FROM poduct_flows p WHERE FIND_IN_SET(:id, p.access) > 0  and p.is_deleted = false ORDER by id DESC", nativeQuery = true)
	Page<Flow> getByTechOwnerSearch(@Param("id") int id, Pageable pageable);

	@Query(value = "SELECT  * FROM poduct_flows p WHERE FIND_IN_SET(:id, p.access) > 0 and name REGEXP :search and p.is_deleted = false ORDER by id DESC", nativeQuery = true)
	Page<Flow> findByTechOwnerSearch(@Param("id") int id, @Param("search") String search, Pageable pageable);

	@Query(value = "SELECT COUNT(*) FROM poduct_flows WHERE is_deleted = 0", nativeQuery = true)
	Long countByFlows();

	@Query(value = "SELECT COUNT(DISTINCT pf.id) FROM poduct_flows pf "
			+ "JOIN products p ON pf.id = p.flow WHERE pf.is_deleted = 0", nativeQuery = true)
	Long countByActiveData();

	@Query(value = "SELECT COUNT(*) FROM poduct_flows pf "
			+ "LEFT JOIN products p ON pf.id = p.flow "
			+ "WHERE p.flow IS NULL  AND pf.is_deleted = 0", nativeQuery = true)
	Long countByInactiveData();

}
