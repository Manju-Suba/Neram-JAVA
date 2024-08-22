package pm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pm.model.users.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Token findByAccessToken(String accessToken);

    boolean existsByAccessToken(String accessToken);

    boolean existsByRefreshToken(String accessToken);
}
