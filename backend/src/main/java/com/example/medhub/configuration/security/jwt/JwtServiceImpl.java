package com.example.medhub.configuration.security.jwt;


import com.example.medhub.entity.UserEntity;
import com.example.medhub.entity.WorkerEntity;
import com.example.medhub.repository.UserRepository;
import com.example.medhub.repository.WorkerRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtKeyProperties jwtKeyProperties;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    @Override
    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        Optional<Long> userId = userRepository.findUserEntitiesByEmail(userDetails.getUsername())
                .map(UserEntity::getUserId);
        Optional<Long> workerId = workerRepository.findWorkerEntitiesByEmail(userDetails.getUsername())
                .map(WorkerEntity::getUserId);
        if (userId.isEmpty() && workerId.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + userDetails.getUsername());
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId.orElseGet(workerId::get));
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

//    public Long getUserIdFromToken(String token) {
//        Claims claims = extractAllClaims(token);
//        return claims.get("userId", Long.class);
//    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtKeyProperties.getKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
