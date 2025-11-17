package org.sid.gatewayservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.sid.gatewayservice.dto.LoginRequest;
import org.sid.gatewayservice.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Value("${keycloak.auth-server-url:http://localhost:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm:stock-adria}")
    private String realm;

    @Value("${keycloak.client-id:stock-management-client}")
    private String clientId;

    @Value("${keycloak.client-secret:}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Attempting login for user: {}", loginRequest.getUsername());
            log.debug("Keycloak URL: {}/realms/{}/protocol/openid-connect/token", keycloakServerUrl, realm);
            
            String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token",
                    keycloakServerUrl, realm);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "password");
            map.add("client_id", clientId);
            map.add("username", loginRequest.getUsername());
            map.add("password", loginRequest.getPassword());
            
            // Add client secret if provided (for confidential clients)
            if (clientSecret != null && !clientSecret.isEmpty()) {
                map.add("client_secret", clientSecret);
                log.debug("Using client secret for authentication");
            } else {
                log.debug("Using public client (no client secret)");
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

            log.debug("Sending request to Keycloak...");
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    tokenUrl,
                    request,
                    TokenResponse.class
            );

            log.info("Login successful for user: {}", loginRequest.getUsername());
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            log.error("Login failed for user: {}. Error: {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid username or password. Please check your credentials and ensure Keycloak is configured correctly."));
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    static class ErrorResponse {
        private String message;
    }
}
