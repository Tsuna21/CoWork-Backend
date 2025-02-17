package edu.stage.backend.utils;

import io.jsonwebtoken.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import java.util.HashSet;
import java.util.Set;

import edu.stage.backend.model.Role;


@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private long expirationTime;

    private final Set<String> blacklistedTokens = new HashSet<>();

    public void blacklistToken(String token) {
        if (token != null && !token.isEmpty()) {
            blacklistedTokens.add(token);
            System.out.println("🚫 Token ajouté à la blacklist : " + token);
        }
    }

    public String generateToken(String email, Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    

      public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public Role extractRole(String token) {
        try {
            String roleName = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .get("role", String.class);
    
            return Role.valueOf(roleName);
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'extraction du rôle : " + e.getMessage());
            return null;
        }
    }
    

    public boolean validateToken(String token, String userEmail) {
        if (token == null || token.isEmpty()) {
            System.out.println("⚠️ Erreur : Token vide ou manquant !");
            return false;
        }
    
        if (blacklistedTokens.contains(token)) {
            System.out.println("🚫 Token blacklisté !");
            return false;
        }
    
        System.out.println("🛠 Validation du token : " + token);
        System.out.println("📩 Comparaison avec l'email : " + userEmail);
    
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            Date expirationDate = claims.getExpiration();
            if (expirationDate.before(new Date())) {
                System.out.println("Le token a expiré !");
                return false;
            }
    
            String tokenEmail = claims.getSubject();
            if (!tokenEmail.equals(userEmail)) {
                System.out.println("Le token ne correspond pas à l'utilisateur !");
                return false;
            }
    
            return true;
    
        } catch (ExpiredJwtException e) {
            System.out.println("Le token a expiré : " + e.getMessage());
        } catch (JwtException e) { 
            System.out.println("Erreur JWT : " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Le token est vide ou invalide : " + e.getMessage());
        }
        return false;
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException("La clé secrète doit avoir une longueur d'au moins 256 bits (32 caractères en Base64).");
        }
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
