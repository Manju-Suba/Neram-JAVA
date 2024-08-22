package pm.jwt;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${pm.app.secret}")
    private String secret;

    @Value("${pm.app.ExpirationTime}")
    private int ExpirationMs;
    @Value("${pm.app.RefreshExpirationTime}")
    private int refreshJwtExpirationMs;

    @Value("${pm.app.CookieName}")
    private String Cookie;

    public static final String TOKEN_TYPE_CLAIM = "tokenType";
    public static final String ACCESS_TOKEN_TYPE = "access";
    public static final String REFRESH_TOKEN_TYPE = "refresh";
    public static final String CODE = "code";

    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, Cookie);
        if (cookie != null) {
            return cookie.getValue();
        } else {
            return null;
        }
    }

    public ResponseCookie generateJwtCookie(Map<String, Object> userToken) {
        String jwt = userToken.get("Token").toString(); // Access token
        ResponseCookie cookie = ResponseCookie.from(Cookie, jwt)
                .path("/")
                .maxAge(24 * 60 * 60)
                .httpOnly(true)
                .build();
        return cookie;
    }

    public ResponseCookie getCleanJwtCookie() {
        ResponseCookie cookie = ResponseCookie.from(Cookie, null).path("/").build();
        return cookie;
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }
    public Integer getUserIdFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().get("id", Integer.class);
    }
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String validateRefresh(String authToken) {

        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody();
            Date expirationDate = claims.getExpiration();
            Date currentDate = new Date();
            if (expirationDate == null || expirationDate.before(currentDate)) {
                return "JWT token is expired or invalid.";
            }

            String tokenType = (String) claims.get(TOKEN_TYPE_CLAIM);

            if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {

                return "Token type is invalid";
            }
            return "Token is valid";

        } catch (MalformedJwtException e) {

            return "Invalid JWT token: " + e.getMessage();
        } catch (ExpiredJwtException e) {

            return "JWT token is expired: " + e.getMessage();
        } catch (UnsupportedJwtException e) {

            return "JWT token is unsupported: " + e.getMessage();
        } catch (IllegalArgumentException e) {

            return "JWT claims string is empty: " + e.getMessage();
        }
    }

    /// Normal Based on Token Claims
    public String generateTokenFromUsernameintoClaims(String username, Map<String, Object> data) {
        Integer id = (Integer) data.get("id");

        return Jwts.builder()
                .setSubject((username))
                .claim("id", id) // Include the ID as a claim
                .addClaims(data)
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + ExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }



    public String generateRefreshTokenFromUsernameintoClaims(String username) {
        return Jwts.builder()
                .setSubject((username))
                .claim(CODE, 200)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .setIssuedAt(new Date())
//                .setExpiration(new Date((new Date()).getTime() + refreshJwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /// SSO Based on Token Creation Claims
    public String generateTokenFromUsernameintoClaimsSSO(String email, Map<String, Object> data) {
        return Jwts.builder()
                .setSubject((email))
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .addClaims(data)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + ExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshTokenFromUsernameintoClaimsSSO(String email) {
        return Jwts.builder()
                .setSubject((email))
                .claim(CODE, 200)
                .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + refreshJwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Date getExpirationDateFromJwtToken(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return claims.getExpiration();
    }

    public String validateJwtToken1(String authToken) {

        try {

            Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(authToken).getBody();

            String tokenType = (String) claims.get(TOKEN_TYPE_CLAIM);

            if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
                return "Token type is invalid";
            }
            return "Token is valid";
        } catch (MalformedJwtException e) {

            return "Invalid JWT token: " + e.getMessage();
        } catch (ExpiredJwtException e) {

            return "JWT token is expired: " + e.getMessage();
        } catch (UnsupportedJwtException e) {

            return "JWT token is unsupported: " + e.getMessage();
        } catch (IllegalArgumentException e) {

            return "JWT claims string is empty: " + e.getMessage();
        }
    }

    public Claims decodeJwt(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return claimsJws.getBody();
        } catch (ExpiredJwtException e) {

            return e.getClaims();
        } catch (JwtException e) {

            return null;
        }
    }

}
