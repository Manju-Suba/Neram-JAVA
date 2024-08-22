package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import pm.model.users.Login_history;

import java.time.LocalDate;

@Repository
public interface Login_historyRepository extends JpaRepository<Login_history, Integer> {
//boolean existsByUseridandDate(int userid, LocalDate date);
}
