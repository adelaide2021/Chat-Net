package com.neu.chatApp.centralServer.peerToPeer.data;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TokenGenerator {
    // none expired time
    private static final long EXPIRE_TIME = Integer.MAX_VALUE;

    // private key
    private static final String KEY = "dafsdvbzeewrvcz56432!@*#$";

    /**
     * Generate a token with payload userId, hostname, and port.
     *
     * @param userId   id
     * @param hostname hostname
     * @param port port
     * @return signed token
     */
    public static String generateToken(Long userId, String hostname, int port) {
        // get time and set expire time
        Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
        // set token algorithm
        Algorithm algorithm = Algorithm.HMAC256(KEY);
        // set token header
        Map<String, Object> header = new HashMap<>(2);
        header.put("type", "jwt");
        header.put("algorithm", "HMAC256");
        // return token
        return JWT.create().withHeader(header)
                // add payload
                .withClaim("id", userId)
                .withClaim("hostname", hostname)
                .withClaim("port", port)
                // set expire time
                .withExpiresAt(date)
                // signed algorithm
                .sign(algorithm);
    }
    /**
     * Verify if the token is valid, then return the payloads
     *
     * @param token token to be verified
     * @return payloads if the token is valid, otherwise null
     */
    public static Map<String, Object> verify(String token) {
        try {
            // get algorithm
            Algorithm algorithm = Algorithm.HMAC256(KEY);
            // get verifier
            JWTVerifier jwtVerifier = JWT.require(algorithm).build();
            // verify token
            DecodedJWT verify = jwtVerifier.verify(token);
            HashMap<String, Object> payload = new HashMap<>();
            payload.put("id", verify.getClaim("id").asLong());
            payload.put("hostname", verify.getClaim("hostname").asString());
            payload.put("port", verify.getClaim("port").asInt());
            return payload;
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}
