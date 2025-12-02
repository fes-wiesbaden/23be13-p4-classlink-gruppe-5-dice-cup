package de.dicecup.classlink.features.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final String DEFAULT_TOKEN_TYPE = "ACCESS";

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails, jwtExpiration);
    }

    public String generateToken(UserDetails userDetails, long customExpirationMillis) {
        return generateToken(new HashMap<>(), userDetails, customExpirationMillis);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return generateToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long customExpirationMillis
    ) {
        return buildToken(extraClaims, userDetails, customExpirationMillis);
    }

    public long getExpirationTime() {
        return jwtExpiration;
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        String jti = (String) claims.computeIfAbsent("jti", key -> UUID.randomUUID().toString());
        long now = System.currentTimeMillis();
        Date issuedAt = new Date(now);
        Date expirationDate = new Date(now + expiration);

        String token = Jwts
                .builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(issuedAt)
                .expiration(expirationDate)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();

        log.info(
                "JWT-DEBUG: type={} subject={} jti={} issuedAt={} expiresAt={}",
                resolveTokenType(claims),
                userDetails.getUsername(),
                jti,
                issuedAt.toInstant(),
                expirationDate.toInstant()
        );

        return token;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String resolveTokenType(Map<String, Object> claims) {
        Object type = claims.get("tokenType");
        if (type == null) {
            return DEFAULT_TOKEN_TYPE;
        }
        return String.valueOf(type).toUpperCase();
    }
}
