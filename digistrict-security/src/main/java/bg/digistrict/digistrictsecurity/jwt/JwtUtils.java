package bg.digistrict.digistrictsecurity.jwt;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtUtils {

    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    
    private final JwtParser jwtParser;
    private final int refreshTokenValidityMin;
    private final int accessTokenValidityMin;
    private final String secretKey;
    
    public JwtUtils(
		@Value("${jwt.refresh_token_validity_min}") int refreshTokenValidityMin,
		@Value("${jwt.access_token_validity_min}") int accessTokenValidityMin,
		@Value("${jwt.secret_key}") String secretKey
	) {
		this.jwtParser = Jwts.parser().setSigningKey(secretKey);
		this.refreshTokenValidityMin = refreshTokenValidityMin;
		this.accessTokenValidityMin = accessTokenValidityMin;
		this.secretKey = secretKey;
    }
    
    public ResponseCookie generateJwtRefreshCookie(String token) {
    	return ResponseCookie.from("refresh_token", token).path("/digistrict-security/refresh-token").maxAge(TimeUnit.MINUTES.toMillis(refreshTokenValidityMin)).build();
    }
    
    public ResponseCookie generateCleanJwtRefreshCookie() {
    	return ResponseCookie.from("refresh_token", null).build();
    }
    
    public ResponseCookie generateJwtAccessCookie(String email, List<String> authorities) {
    	return ResponseCookie.from("access_token", createAccessToken(email, authorities)).build();
    }
    
    public ResponseCookie generateCleanJwtAccessCookie() {
    	return ResponseCookie.from("access_token", null).build();
    }
    
    public String createAccessToken(String email, List<String> authorities) {
        Claims claims = Jwts.claims().setSubject(email);
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(accessTokenValidityMin));
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(tokenValidity)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .claim("roles", authorities)
                .compact();
    }
    
	public String getUserNameFromJwtToken(String token) {
		return jwtParser.parseClaimsJws(token).getBody().getSubject();
	}
    
	public boolean validateJwtToken(String authToken) {
		try {
			jwtParser.parseClaimsJws(authToken);
			
			return true;
		} catch (Exception e) {
			
			return false;
		}
	}

    private Claims parseJwtClaims(String token) {
        return jwtParser.parseClaimsJws(token).getBody();
    }

    public Claims resolveClaims(HttpServletRequest req) {
        try {
            String token = resolveToken(req);
            if (token != null) {
                return parseJwtClaims(token);
            }
            return null;
        } catch (ExpiredJwtException ex) {
            req.setAttribute("expired", ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            req.setAttribute("invalid", ex.getMessage());
            throw ex;
        }
    }

    public String resolveToken(HttpServletRequest request) {

        String bearerToken = request.getHeader(TOKEN_HEADER);
        if (bearerToken != null && bearerToken.startsWith(TOKEN_PREFIX)) {
            return bearerToken.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    public boolean validateClaims(Claims claims) throws AuthenticationException {
        try {
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            throw e;
        }
    }

    public String getEmail(Claims claims) {
        return claims.getSubject();
    }

    private List<String> getRoles(Claims claims) {
        return (List<String>) claims.get("roles");
    }


}
