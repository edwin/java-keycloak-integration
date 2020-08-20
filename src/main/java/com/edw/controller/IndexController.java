package com.edw.controller;

import com.auth0.jwk.Jwk;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import com.edw.service.JwtService;
import com.edw.service.KeycloakRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.security.interfaces.RSAPublicKey;
import java.util.*;

/**
 * <pre>
 *     com.edw.controller.StudentController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 17 Agt 2020 21:15
 */
@RestController
public class IndexController {

    private Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private KeycloakRestService restService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/student")
    public HashMap student(@RequestHeader("Authorization") String authHeader) {
        try {
            List<String> roles = restService.getRoles(authHeader);
            if(!roles.contains("student"))
                throw new Exception("not a student role");
            return new HashMap (){{
                put("role", "student");
            }};
        } catch (Exception e) {
            logger.error("exception : {} ", e.getMessage());
            return new HashMap (){{
                put("status", "forbidden");
            }};
        }
    }

    @GetMapping("/teacher")
    public HashMap teacher(@RequestHeader("Authorization") String authHeader) {
        try {
            DecodedJWT jwt = JWT.decode(authHeader.replace("Bearer", "").trim());

            // check JWT is valid
            Jwk jwk = jwtService.getJwk();
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            algorithm.verify(jwt);

            // check JWT role is correct
            List<String> roles = ((List)jwt.getClaim("realm_access").asMap().get("roles"));
            if(!roles.contains("teacher"))
                throw new Exception("not a teacher role");

            // check JWT is still active
            Date expiryDate = jwt.getExpiresAt();
            if(expiryDate.before(new Date()))
                throw new Exception("token is expired");

            // all validation passed
            return new HashMap() {{
                put("role", "teacher");
            }};
        } catch (Exception e) {
            logger.error("exception : {} ", e.getMessage());
            return new HashMap() {{
                put("status", "forbidden");
            }};
        }
    }

    @GetMapping("/valid")
    public HashMap valid(@RequestHeader("Authorization") String authHeader) {
        try {
            restService.checkValidity(authHeader);
            return new HashMap (){{
                put("is_valid", "true");
            }};
        } catch (Exception e) {
            logger.error("token is not valid, exception : {} ", e.getMessage());
            return new HashMap (){{
                put("is_valid", "false");
            }};
        }
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public String login(String username, String password) {
        return restService.login(username, password);
    }

    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map logout(@RequestParam(value = "refresh_token", name = "refresh_token") String refreshToken) {
        try {
            restService.logout(refreshToken);
            return new HashMap (){{
                put("logout", "true");
            }};
        } catch (Exception e) {
            logger.error("unable to logout, exception : {} ", e.getMessage());
            return new HashMap (){{
                put("logout", "false");
            }};
        }
    }

    @RequestMapping("/")
    public HashMap index() {
        return new HashMap (){{
            put("status", "hello world");
        }};
    }
}
