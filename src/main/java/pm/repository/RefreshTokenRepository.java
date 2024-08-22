package pm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import pm.model.users.RefreshToken;
import pm.model.users.Users;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

	Optional<RefreshToken> findByToken(String token);

	
	  @Modifying
	  int deleteByUsers(Users user);

	    @Query(value = "SELECT * FROM refresh_token WHERE user_id= :user_id ORDER BY id DESC limit 1", nativeQuery = true)
		Optional<RefreshToken> findByUserId(int user_id);
}
