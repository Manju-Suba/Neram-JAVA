package pm.repository;

import com.azure.core.http.rest.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pm.model.Budgie.BudgieApi;

import java.util.Date;
import java.util.List;

public interface BudgieRepository extends JpaRepository<BudgieApi,String> {

    @Query(value = "SELECT * FROM swipeinout WHERE empid = :empId AND logdate = :date", nativeQuery = true)
    BudgieApi findByEmployeeIdAndDate(@Param("empId") String employeeId, @Param("date") Date date);


//    @Query(value = "SELECT * FROM swipe WHERE employeeid = :employeeId AND date = :date AND shortfall_hours > '00:00'",
//            countQuery = "SELECT count(*) FROM swipe WHERE employeeid = :employeeId AND date = :date AND shortfall_hours > '00:00'",
//            nativeQuery = true)
//    BudgieApi findByEmployeeIdAndDateWithShortfallHours(@Param("employeeId") String employeeId,
//                                                              @Param("date") String date
//                                                              );
//
//    @Query(value = "SELECT * FROM swipe WHERE employeeid = :employeeId AND date = :date AND excess_hours > '00:00'",
//            countQuery = "SELECT count(*) FROM swipe WHERE employeeid = :employeeId AND date = :date AND excess_hours > '00:00'",
//            nativeQuery = true)
//    BudgieApi findByEmployeeIdAndDateWithExcessHours(@Param("employeeId") String employeeId,
//                                                              @Param("date") String date
//                                                              );
}
