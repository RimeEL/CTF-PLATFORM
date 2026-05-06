package com.ctf.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.UUID;

public class JwtUtil {
    private static final String SECRET = "TON_SECRET_ICI_A_CHANGER";
    private static final long EXPIRATION_MS = 86400000L; // 24h
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET);

    public static String generateToken(UUID userId, String role) {
        return JWT.create()
                .withSubject(userId.toString())
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .sign(ALGORITHM);
    }

    public static DecodedJWT validateToken(String token) throws JWTVerificationException {
        return JWT.require(ALGORITHM).build().verify(token);
    }

    public static UUID extractUserId(String token) {
        return UUID.fromString(validateToken(token).getSubject());
    }

    public static String extractRole(String token) {
        return validateToken(token).getClaim("role").asString();
    }
}