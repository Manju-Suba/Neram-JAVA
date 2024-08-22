package pm.service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;

import jakarta.transaction.Transactional;
import pm.exception.ValidationException;
import pm.model.users.RefreshToken;
import pm.repository.RefreshTokenRepository;
import pm.repository.UsersRepository;

@Service
public class RefreshTokenService {

    @Value("${pm.app.RefreshExpirationTime}")
    private int refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsersRepository usersRepository;

    public Optional<RefreshToken> findBytoken(String token){
        return refreshTokenRepository.findByToken(token);
    }

//    public RefreshToken createRefreshToken(int user_id){
//        RefreshToken refreshToken = new RefreshToken();
//
//        refreshToken.setUsers(usersRepository.findById(user_id).get());
//        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
//        refreshToken.setToken(UUID.randomUUID().toString());
//
//        refreshToken = refreshTokenRepository.save(refreshToken);
//        return refreshToken;
//    }

    
    
    public RefreshToken createRefreshToken(int user_id) {
        Optional<RefreshToken> existingTokenOptional = refreshTokenRepository.findByUserId(user_id);
        if (existingTokenOptional.isPresent()) {
            RefreshToken existingToken = existingTokenOptional.get();
            existingToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            return refreshTokenRepository.save(existingToken);
        } else {
            RefreshToken newRefreshToken = new RefreshToken();
            newRefreshToken.setUsers(usersRepository.findById(user_id).orElseThrow(() -> new UsernameNotFoundException("User not found")));
            newRefreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            newRefreshToken.setToken(UUID.randomUUID().toString());
            return refreshTokenRepository.save(newRefreshToken);
        }
    }

    
    
    public RefreshToken verifyExpiration(RefreshToken token){
        if(token.getExpiryDate().compareTo(Instant.now()) < 0){
            refreshTokenRepository.delete(token);
            throw new ValidationException(token.getToken() +"Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

	

    @Transactional
    public int deleteByUserId(int user_id){
        return refreshTokenRepository.deleteByUsers(usersRepository.findById(user_id).get());
    }


}
