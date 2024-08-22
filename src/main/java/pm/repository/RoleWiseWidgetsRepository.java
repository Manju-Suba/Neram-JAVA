package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pm.model.users.RoleWiseWidgets;

import java.util.List;
import java.util.Objects;

@Repository
public interface RoleWiseWidgetsRepository extends JpaRepository<RoleWiseWidgets, Integer> {


    @Query(value = "SELECT * FROM role_wise_widgets WHERE role IN :roles",nativeQuery = true)
    List<Object[]> findByRoles(@Param("roles") List<String> roles);

}
