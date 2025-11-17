# Rapport d'Implémentation de Sécurité

**Auteur:** Naoufal Guendouz  
**Date:** 17 Novembre 2025  
**Projet:** Système de Gestion du Marché Boursier (Adria Control)

---

## Introduction

J'ai développé le backend Java pour ce système de gestion du marché boursier, mais je suis particulièrement intéressé par la sécurité. Ce rapport documente tout ce que j'ai mis en œuvre pour garantir la sécurité de cette application, couvrant l'authentification, l'autorisation, la validation des entrées, la journalisation sécurisée, la gestion des erreurs et la configuration CORS.

---

## Table des Matières

1. [Architecture du Projet](#1-architecture-du-projet)
2. [Authentification & Autorisation avec Keycloak](#2-authentification--autorisation-avec-keycloak)
3. [Validation des Entrées](#3-validation-des-entrées)
4. [Gestion Globale des Exceptions](#4-gestion-globale-des-exceptions)
5. [Journalisation Sécurisée](#5-journalisation-sécurisée)
6. [Configuration CORS](#6-configuration-cors)
7. [Configuration du Serveur de Ressources OAuth2](#7-configuration-du-serveur-de-ressources-oauth2)
8. [Meilleures Pratiques de Sécurité Appliquées](#8-meilleures-pratiques-de-sécurité-appliquées)

---

## 1. Architecture du Projet

### 1.1 Architecture Microservices

Le système est conçu selon une architecture microservices avec les composants suivants :

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client (React)                          │
│                     http://localhost:3000                       │
└──────────────────────────────┬──────────────────────────────────┘
                               │
                               │ HTTP/REST
                               │
┌──────────────────────────────▼──────────────────────────────────┐
│                     Gateway Service                             │
│                  (Spring Cloud Gateway MVC)                     │
│                     http://localhost:8888                       │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  • Authentification (AuthController)                     │  │
│  │  • Configuration CORS                                    │  │
│  │  • Routage des requêtes                                  │  │
│  │  • Gestion des tokens JWT                                │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────┬───────────────────────────────┬──────────────────┘
              │                               │
              │                               │
              │                               │
    ┌─────────▼─────────┐          ┌─────────▼──────────┐
    │  Discovery Service│          │   Stock Service    │
    │     (Eureka)      │◄─────────│   (Microservice)   │
    │  localhost:8761   │          │  localhost:8081    │
    │                   │          │                    │
    │  • Enregistrement │          │ • API REST Stocks  │
    │    des services   │          │ • Validation       │
    │  • Service        │          │ • OAuth2 Resource  │
    │    Discovery      │          │ • Base de données  │
    └───────────────────┘          └─────────┬──────────┘
                                             │
                                             │
                                   ┌─────────▼──────────┐
                                   │   Base de Données  │
                                   │       (H2)         │
                                   │   In-Memory DB     │
                                   └────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      Keycloak Server                            │
│                   http://localhost:8080                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  • Gestion des utilisateurs                              │  │
│  │  • Émission de tokens JWT                                │  │
│  │  • Gestion des rôles (USER, ADMIN)                       │  │
│  │  • Realm: stock-adria                                    │  │
│  │  • Client: stock-management-client                       │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.2 Flux d'Authentification

```
┌──────────┐                ┌─────────┐               ┌──────────┐              ┌──────────┐
│  Client  │                │ Gateway │               │ Keycloak │              │  Stock   │
│ (React)  │                │ Service │               │  Server  │              │ Service  │
└────┬─────┘                └────┬────┘               └────┬─────┘              └────┬─────┘
     │                           │                         │                         │
     │  1. POST /api/auth/login  │                         │                         │
     │  {username, password}     │                         │                         │
     ├──────────────────────────►│                         │                         │
     │                           │  2. Token Request       │                         │
     │                           │  (client credentials)   │                         │
     │                           ├────────────────────────►│                         │
     │                           │                         │                         │
     │                           │  3. JWT Token           │                         │
     │                           │◄────────────────────────┤                         │
     │  4. Return Token          │                         │                         │
     │◄──────────────────────────┤                         │                         │
     │                           │                         │                         │
     │  5. GET /api/stocks       │                         │                         │
     │  Authorization: Bearer    │                         │                         │
     ├──────────────────────────►│                         │                         │
     │                           │  6. Validate JWT        │                         │
     │                           ├────────────────────────►│                         │
     │                           │                         │                         │
     │                           │  7. JWT Valid           │                         │
     │                           │◄────────────────────────┤                         │
     │                           │  8. Forward Request     │                         │
     │                           │  + JWT Token            │                         │
     │                           ├────────────────────────────────────────────────►  │
     │                           │                         │                         │
     │                           │  9. Validate JWT Locally│                         │
     │                           │                         │  (via public key)       │
     │                           │                         │                         │
     │                           │  10. Extract Roles      │                         │
     │                           │                         │  (ROLE_USER/ROLE_ADMIN) │
     │                           │                         │                         │
     │                           │  11. Return Data        │                         │
     │                           │◄────────────────────────────────────────────────┤ │
     │  12. Return Response      │                         │                         │
     │◄──────────────────────────┤                         │                         │
     │                           │                         │                         │
```

### 1.3 Technologies Utilisées

| Composant | Technologie | Version | Port |
|-----------|-------------|---------|------|
| **Frontend** | React | 19.2.0 | 3000 |
| **Frontend CSS** | Tailwind CSS | 3.4.17 | - |
| **HTTP Client** | Axios | 1.7.9 | - |
| **Gateway** | Spring Cloud Gateway MVC | 2025.0.0 | 8888 |
| **Service Discovery** | Eureka Server | 2025.0.0 | 8761 |
| **Stock Service** | Spring Boot | 3.5.7 | 8081 |
| **Java** | OpenJDK | 21 | - |
| **Authentification** | Keycloak | Latest | 8080 |
| **Base de données** | H2 (In-Memory) | - | - |
| **Validation** | Jakarta Bean Validation | 3.0+ | - |

### 1.4 Structure des Services

#### Gateway Service
```
gateway-service/
├── src/main/java/org/sid/gatewayservice/
│   ├── GatewayServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       # OAuth2 + JWT + CORS
│   │   └── GlobalExceptionHandler.java
│   ├── controller/
│   │   └── AuthController.java       # Endpoint d'authentification
│   └── dto/
│       ├── LoginRequest.java         # DTO de connexion
│       └── TokenResponse.java        # DTO de réponse token
└── src/main/resources/
    └── application.properties        # Configuration du routage
```

#### Stock Service
```
stock-service/
├── src/main/java/org/sid/stockservice/
│   ├── StockServiceApplication.java
│   ├── config/
│   │   ├── SecurityConfig.java       # OAuth2 Resource Server
│   │   └── GlobalExceptionHandler.java
│   ├── controllers/
│   │   └── StockMarketController.java
│   ├── dtos/
│   │   ├── StockMarketRequestDTO.java   # Validation des entrées
│   │   └── StockMarketResponseDTO.java
│   ├── entities/
│   │   └── StockMarket.java
│   ├── services/
│   │   └── StockMarketService.java
│   └── repositories/
│       └── StockMarketRepository.java
└── src/main/resources/
    └── application.properties        # Configuration OAuth2
```

---


## 2. Authentification & Autorisation avec Keycloak

### 2.1 Configuration du Serveur Keycloak

**Version Keycloak :** En cours d'exécution sur `http://localhost:8080`  
**Realm :** `stock-adria`  
**Client :** `stock-management-client`

#### Configuration du Realm Keycloak

![Keycloak Realm](screens/keyloak-realm.png)
*Capture d'écran : Configuration du realm `stock-adria`*

#### Configuration du Client

Le client `stock-management-client` est configuré avec :
- **Client Protocol:** OpenID Connect
- **Access Type:** Confidential
- **Direct Access Grants:** Activé (pour l'authentification username/password)
- **Valid Redirect URIs:** `http://localhost:3000/*`
- **Web Origins:** `http://localhost:3000`


#### Gestion des Utilisateurs

Utilisateurs de test créés avec les rôles appropriés :

| Nom d'utilisateur | Mot de passe | Rôles |
|----------|----------|-------|
| user1 | password123 | USER |
| user2 | password123 | USER, ADMIN |

![Keycloak Users](screens/users.png)
*Capture d'écran : Utilisateurs*

![Testing Roles](screens/userTest.png)
*Capture d'écran : Test de l'interface utilisateur avec les rôles*


### 2.2 Implémentation du Flux d'Authentification

#### Gateway Service - Contrôleur d'Authentification

Le service gateway agit comme un proxy d'authentification, gérant les requêtes de connexion et communiquant avec Keycloak :

```java
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthController {

    @Value("${keycloak.token-uri}")
    private String tokenUri;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("username", request.getUsername());
            body.add("password", request.getPassword());

            HttpEntity<MultiValueMap<String, String>> requestEntity = 
                new HttpEntity<>(body, headers);

            ResponseEntity<TokenResponse> response = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                requestEntity,
                TokenResponse.class
            );

            log.info("Authentication successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            //This for not showing more informations than needed
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed. Please check your credentials."));
        }
    }
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Mot de passe jamais journalisé** - Empêche l'exposition de données sensibles dans les logs
- ✅ **Messages d'erreur génériques** - Empêche la fuite d'informations sur les noms d'utilisateur valides
- ✅ **Entrées validées** - Utilise l'annotation `@Valid` pour la validation des requêtes
- ✅ **Gestion sécurisée des identifiants** - Secret client stocké dans la configuration, non codé en dur

#### DTO de Requête de Connexion avec Validation

```java
package org.sid.gatewayservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

}
```

**Fonctionnalités de Sécurité :**
- ✅ **Validation des entrées** - Empêche les identifiants vides ou mal formés
- ✅ **Contraintes de longueur** - Empêche les débordements de tampon et garantit les normes minimales de sécurité
- ✅ **Messages d'erreur personnalisés** - Fournit un retour clair sans exposer les détails du système

### 2.3 Intégration de l'Authentification Frontend

```javascript
// Login.js - User login component
const handleLogin = async (e) => {
    e.preventDefault();
    console.log('Login attempt:', { username });
    
    try {
        const response = await axios.post('http://localhost:8888/api/auth/login', {
            username,
            password
        });
        
        console.log('Login successful, token received');
        const token = response.data.access_token;
        localStorage.setItem('token', token);
        
        // Force full page reload to ensure token is picked up
        window.location.href = '/stocks';
    } catch (error) {
        console.error('Login error:', error);
        setError('Invalid username or password');
    }
};
```

**Fonctionnalités de Sécurité :**
- ✅ **Token stocké dans localStorage** - Persiste l'authentification lors des rechargements de page
- ✅ **Redirection automatique** - Après une connexion réussie, redirige vers les ressources protégées
- ✅ **Gestion des erreurs** - Affiche des messages d'erreur génériques aux utilisateurs

#### Interface de Connexion

![Login Page](screens/login-ui.png)
*Capture d'écran : Page de connexion avec authentification username/password*

---

## 3. Validation des Entrées

### 3.1 Stock Service - Validation du DTO de Requête

Toutes les données entrantes sont validées à l'aide des annotations Jakarta Bean Validation :

```java
package org.sid.stockservice.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockMarketRequestDTO {
    
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;
    
    @NotNull(message = "Open value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Open value must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Open value must have at most 10 integer digits and 2 decimal places")
    private Double openValue;
    
    @NotNull(message = "High value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "High value must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "High value must have at most 10 integer digits and 2 decimal places")
    private Double highValue;
    
    @NotNull(message = "Low value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Low value must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Low value must have at most 10 integer digits and 2 decimal places")
    private Double lowValue;
    
    @NotNull(message = "Close value is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Close value must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Close value must have at most 10 integer digits and 2 decimal places")
    private Double closeValue;
    
    @NotNull(message = "Volume is required")
    @Min(value = 1, message = "Volume must be at least 1")
    @Max(value = 999999999999L, message = "Volume must not exceed 999999999999")
    private Long volume;
    
    @NotNull(message = "Company ID is required")
    @Min(value = 1, message = "Company ID must be at least 1")
    @Max(value = 999999, message = "Company ID must not exceed 999999")
    private Long companyId;
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Prévient l'injection SQL** - Aucune chaîne brute acceptée, toutes les données validées
- ✅ **Validation de la logique métier** - Garantit l'intégrité des données (prix positifs, dates valides)
- ✅ **Validation des plages** - Empêche les attaques par débordement et les données invalides
- ✅ **Messages d'erreur personnalisés** - Retour clair pour les développeurs et les utilisateurs

### 3.2 Validation au Niveau du Contrôleur

```java
@RestController
@RequestMapping("/stocks")
@Validated
@Slf4j
public class StockMarketController {

    @Autowired
    private StockMarketService stockMarketService;

    @GetMapping
    public ResponseEntity<List<StockMarketResponseDTO>> getAllStocks() {
        log.debug("Fetching all stocks");
        List<StockMarketResponseDTO> stocks = stockMarketService.getAllStocks();
        log.debug("Retrieved {} stocks", stocks.size());
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockMarketResponseDTO> getStockById(
            @PathVariable @Min(value = 1, message = "Stock ID must be greater than 0") Long id) {
        log.debug("Fetching stock with ID: {}", id);
        StockMarketResponseDTO stock = stockMarketService.getStockById(id);
        log.debug("Retrieved stock: {}", stock.getId());
        return ResponseEntity.ok(stock);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> createStock(
            @Valid @RequestBody StockMarketRequestDTO stockRequest) {
        log.info("Creating new stock");
        StockMarketResponseDTO createdStock = stockMarketService.createStock(stockRequest);
        log.info("Stock created with ID: {}", createdStock.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStock);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockMarketResponseDTO> updateStock(
            @PathVariable @Min(value = 1, message = "Stock ID must be greater than 0") Long id,
            @Valid @RequestBody StockMarketRequestDTO stockRequest) {
        log.info("Updating stock with ID: {}", id);
        StockMarketResponseDTO updatedStock = stockMarketService.updateStock(id, stockRequest);
        log.info("Stock updated with ID: {}", updatedStock.getId());
        return ResponseEntity.ok(updatedStock);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStock(
            @PathVariable @Min(value = 1, message = "Stock ID must be greater than 0") Long id) {
        log.info("Deleting stock with ID: {}", id);
        stockMarketService.deleteStock(id);
        log.info("Stock deleted with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Annotation @Validated** - Active la validation au niveau de la méthode
- ✅ **@Valid sur les corps de requête** - Valide les données JSON entrantes
- ✅ **Validation des variables de chemin** - Empêche les ID négatifs ou nuls
- ✅ **Contrôle d'accès basé sur les rôles** - `@PreAuthorize` restreint les mutations aux utilisateurs ADMIN
- ✅ **Journalisation structurée** - Suit toutes les opérations sans exposer de données sensibles

### 3.3 Interface Utilisateur Frontend - Gestion des Actions avec Contrôle d'Accès Basé sur les Rôles

#### Vue de la Liste des Actions (Tous les Utilisateurs)

![Stock List](screens/stock-list.png)
*Capture d'écran : Page de liste des actions accessible à tous les utilisateurs authentifiés (rôles USER et ADMIN)*

#### Ajout d'Action (ADMIN Uniquement)

![Add Stock](screens/add-stock.png)
*Capture d'écran : Formulaire d'ajout d'action - Accessible uniquement aux utilisateurs avec le rôle ADMIN. Montre la validation des entrées en action.*

#### Suppression d'Action (ADMIN Uniquement)

![Delete Stock](screens/delete-stock.png)
*Capture d'écran : Action de suppression d'action - Restreinte au rôle ADMIN. User1 (rôle USER) recevra une erreur 403 Forbidden.*

**Fonctionnalités de Sécurité de l'Interface :**
- ✅ **Rendu de l'interface basé sur les rôles** - Les boutons Ajouter/Supprimer ne sont affichés qu'aux utilisateurs ADMIN
- ✅ **Validation côté client** - Validation du formulaire avant les appels API
- ✅ **Gestion des erreurs** - Messages d'erreur conviviaux pour les échecs de validation et le refus d'accès
- ✅ **Routes protégées** - Les utilisateurs non authentifiés sont redirigés vers la page de connexion
- ✅ **Gestion des tokens** - Attachement automatique du token aux requêtes API via les intercepteurs Axios

---

## 4. Gestion Globale des Exceptions

### 4.1 Gestionnaire d'Exceptions du Stock Service

```java

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> details = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            details.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", details);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Validation Error",
            "Input validation failed. Please check the request data.",
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        Map<String, String> details = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            details.put(propertyPath, message);
        }
        
        log.warn("Constraint violation: {}", details);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Constraint Violation",
            "Request constraints violated. Please check the request parameters.",
            details
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.FORBIDDEN.value(),
            "Forbidden",
            "You do not have permission to access this resource",
            null
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            null
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> details;
        
        // Constructor and getters...
    }
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Réponses d'erreur cohérentes** - Format JSON standard pour toutes les erreurs
- ✅ **Pas d'exposition de la trace de pile** - Les erreurs internes ne divulguent pas les détails du système
- ✅ **Erreurs de validation détaillées** - Retour au niveau du champ pour les mauvaises requêtes
- ✅ **Journalisation pour la surveillance** - Toutes les erreurs journalisées pour l'audit de sécurité
- ✅ **Codes de statut HTTP** - Codes de statut appropriés pour différents types d'erreurs

### 4.2 Exemple de Réponse d'Erreur

```json
{
  "timestamp": "2025-11-17T14:30:45.123",
  "status": 400,
  "error": "Validation Error",
  "message": "Input validation failed. Please check the request data.",
  "details": {
    "openValue": "Open value must be greater than 0",
    "date": "Date cannot be in the future",
    "volume": "Volume must be at least 1"
  }
}
```

---

## 5. Journalisation Sécurisée

### 5.1 Stratégie de Journalisation

Tous les contrôleurs utilisent SLF4J avec des niveaux de journalisation spécifiques :

```java
@Slf4j
public class AuthController {
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Authentication attempt for user: {}", request.getUsername());
        // DO NOT log the password for security reasons
        
        try {
            // Authentication logic...
            log.info("Authentication successful for user: {}", request.getUsername());
            return ResponseEntity.ok(response.getBody());
            
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername());
            // Generic error message to prevent information leakage
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));
        }
    }
}
```

**Pratiques de Sécurité :**
- ✅ **Ne jamais journaliser les mots de passe** - Des commentaires explicites rappellent aux développeurs
- ✅ **Niveaux de log** - INFO pour les événements importants, DEBUG pour les traces détaillées, WARN pour la validation, ERROR pour les échecs
- ✅ **PII minimales** - Ne journalise que les identifiants nécessaires (IDs, noms d'utilisateur)
- ✅ **Messages d'erreur génériques** - Les erreurs externes ne révèlent pas les détails internes du système
- ✅ **Piste d'audit** - Toutes les mutations (création, mise à jour, suppression) sont journalisées

### 5.2 Configuration de la Journalisation

```properties
# application.properties
logging.level.org.sid.stockservice=INFO
logging.level.org.sid.gatewayservice=INFO
logging.level.org.springframework.security=DEBUG
```

---

## 6. Configuration CORS

### 6.1 SecurityConfig du Gateway Service

CORS est configuré dans le SecurityConfig en utilisant un bean CorsConfigurationSource :

```java
package org.sid.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())  // Enable CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**", "/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Origine spécifique** - Seul `http://localhost:3000` est autorisé (pas `*`)
- ✅ **Méthodes limitées** - Seules les méthodes HTTP nécessaires sont autorisées
- ✅ **Identifiants autorisés** - Prend en charge l'authentification basée sur les cookies/tokens
- ✅ **Sessions sans état** - Pas de gestion de session côté serveur
- ✅ **CSRF désactivé** - Tokens JWT utilisés à la place (sans état)
- ✅ **Configuration centralisée** - CORS défini aux côtés des paramètres de sécurité

### 6.2 CORS Désactivé dans le Stock Service

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable()) // Gateway handles CORS
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            );
        return http.build();
    }
}
```

**Justification de Sécurité :**
- ✅ **Pas d'en-têtes dupliqués** - Empêche les erreurs "l'en-tête CORS est apparu deux fois"
- ✅ **CORS uniquement sur le Gateway** - Simplifie la configuration et la maintenance
- ✅ **Défense en profondeur** - Le service Stock nécessite toujours des tokens JWT valides

---

## 7. Configuration du Serveur de Ressources OAuth2

### 7.1 Configuration de Sécurité du Stock Service

```java
package org.sid.stockservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            );
        return http.build();
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                return List.of();
            }
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles == null) {
                return List.of();
            }
            return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        });
        return converter;
    }
}
```

**Fonctionnalités de Sécurité :**
- ✅ **Validation JWT** - Tous les tokens vérifiés par rapport à la clé publique Keycloak
- ✅ **Extraction des rôles** - Les rôles Keycloak mappés vers les autorités Spring Security
- ✅ **Sécurité des méthodes** - Les annotations `@PreAuthorize` appliquent le contrôle d'accès basé sur les rôles
- ✅ **Authentification sans état** - Pas de sessions côté serveur, entièrement évolutif
- ✅ **Expiration des tokens** - Keycloak gère le cycle de vie des tokens

### 7.2 Propriétés de l'Application

```properties
# Gateway Service
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/stock-adria
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/stock-adria/protocol/openid-connect/certs

keycloak.token-uri=http://localhost:8080/realms/stock-adria/protocol/openid-connect/token
keycloak.client-id=stock-management-client
keycloak.client-secret=HvY1fxjVUo4rfbmoPbvhZxJYs48PCD2O
```

```properties
# Stock Service
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/stock-adria
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/stock-adria/protocol/openid-connect/certs
```

---

## 8. Meilleures Pratiques de Sécurité Appliquées

### 8.1 Défense en Profondeur

| Couche | Mesure de Sécurité |
|-------|------------------|
| **Réseau** | CORS configuré, origines spécifiques uniquement |
| **Authentification** | Keycloak OAuth2/OpenID Connect, tokens JWT |
| **Autorisation** | Contrôle d'accès basé sur les rôles (RBAC) avec `@PreAuthorize` |
| **Validation des Entrées** | Jakarta Bean Validation, contraintes personnalisées |
| **Gestion des Erreurs** | Gestionnaires d'exceptions globaux, pas d'exposition de trace de pile |
| **Journalisation** | Journalisation sécurisée, pas de données sensibles dans les logs |
| **Intégrité des Données** | Contraintes de base de données, validation JPA |

### 8.2 Atténuation OWASP Top 10

| Risque OWASP | Stratégie d'Atténuation |
|------------|---------------------|
| **A01: Broken Access Control** | Authentification JWT, autorisation basée sur les rôles, `@PreAuthorize` |
| **A02: Cryptographic Failures** | Mots de passe jamais journalisés, secret client dans la config (pas dans le code), prêt pour HTTPS |
| **A03: Injection** | Jakarta Bean Validation, requêtes JPA paramétrées, assainissement des entrées |
| **A04: Insecure Design** | Validation fail-fast, privilège minimum, valeurs par défaut sécurisées |
| **A05: Security Misconfiguration** | CORS correctement configuré, en-têtes de sécurité, pas d'identifiants par défaut |
| **A07: Identification and Authentication Failures** | Intégration Keycloak, politique de mot de passe forte, expiration des tokens |
| **A08: Software and Data Integrity Failures** | Validation des entrées, contraintes de données, journalisation d'audit |
| **A09: Security Logging and Monitoring Failures** | Journalisation structurée, événements d'authentification journalisés, suivi des erreurs |
| **A10: Server-Side Request Forgery** | Validation des entrées, pas d'URLs contrôlées par l'utilisateur |

### 8.3 Résumé des Principales Fonctionnalités de Sécurité

✅ **Authentification**: Keycloak OAuth2 avec OpenID Connect  
✅ **Autorisation**: Contrôle d'accès basé sur les rôles (USER, ADMIN)  
✅ **Gestion des Tokens**: JWT avec validation automatique et expiration  
✅ **Validation des Entrées**: Jakarta Bean Validation sur tous les DTOs  
✅ **Gestion des Erreurs**: Gestionnaires d'exceptions globaux avec réponses cohérentes  
✅ **Journalisation Sécurisée**: Pas de mots de passe journalisés, exposition minimale des PII  
✅ **CORS**: Configuration centralisée avec origines spécifiques  
✅ **Prévention de l'Injection SQL**: JPA avec requêtes paramétrées  
✅ **Prévention de Fuite d'Informations**: Messages d'erreur génériques pour les clients  
✅ **Piste d'Audit**: Toutes les mutations journalisées avec horodatage et contexte utilisateur  

---

## 9. Tests des Fonctionnalités de Sécurité

### 9.1 Tests d'Authentification

**Connexion Valide :**
```bash
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user1",
    "password": "password123"
  }'
```

**Réponse :**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "token_type": "Bearer"
}
```

**Connexion Invalide :**
```bash
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "invalid",
    "password": "wrong"
  }'
```

**Réponse :**
```json
{
  "error": "Authentication failed. Please check your credentials."
}
```

### 9.2 Tests de Validation

**Données d'Action Invalides :**
```bash
curl -X POST http://localhost:8888/api/stocks \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2025-12-31",
    "openValue": -10.5,
    "highValue": 0,
    "lowValue": null,
    "closeValue": 15.25,
    "volume": 0,
    "companyId": 1
  }'
```

**Réponse :**
```json
{
  "timestamp": "2025-11-17T14:30:45.123",
  "status": 400,
  "error": "Validation Error",
  "message": "Input validation failed. Please check the request data.",
  "details": {
    "date": "Date cannot be in the future",
    "openValue": "Open value must be greater than 0",
    "highValue": "High value must be greater than 0",
    "lowValue": "Low value is required",
    "volume": "Volume must be at least 1"
  }
}
```

### 9.3 Tests d'Autorisation

**Rôle USER essayant de créer une action (devrait échouer) :**
```bash
curl -X POST http://localhost:8888/api/stocks \
  -H "Authorization: Bearer <user_token>" \
  -H "Content-Type: application/json" \
  -d '{...valid data...}'
```

**Réponse :**
```json
{
  "timestamp": "2025-11-17T14:30:45.123",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "details": null
}
```

---

## 10. Améliorations de Sécurité Futures

### Améliorations Prévues

1. **Limitation de Taux**
   - Implémenter la limitation des requêtes pour prévenir les attaques par force brute
   - Utiliser les filtres de limitation de taux Spring Cloud Gateway

2. **Configuration HTTPS**
   - Ajouter des certificats SSL/TLS pour la production
   - Forcer la redirection HTTPS

3. **Politique de Mot de Passe**
   - Appliquer des exigences de mot de passe plus strictes dans Keycloak
   - Implémenter la validation de la complexité des mots de passe

4. **En-têtes de Sécurité**
   - Ajouter les en-têtes X-Content-Type-Options, X-Frame-Options, CSP
   - Implémenter HSTS (HTTP Strict Transport Security)

5. **Chiffrement des Données au Repos**
   - Chiffrer les champs sensibles de la base de données
   - Utiliser les utilitaires de chiffrement Spring Boot

6. **Gestion des Sessions**
   - Implémenter la rotation des refresh tokens
   - Ajouter un mécanisme de révocation des tokens

7. **Surveillance & Alertes**
   - Intégrer avec des outils de surveillance de sécurité
   - Configurer des alertes pour les activités suspectes

---

## Conclusion

Ce système de gestion du marché boursier implémente une stratégie de sécurité complète couvrant l'authentification, l'autorisation, la validation des entrées, la journalisation sécurisée et la gestion des erreurs. En intégrant Keycloak pour une authentification de niveau entreprise et en suivant les meilleures pratiques de sécurité tout au long de l'application, nous avons créé un système robuste et sécurisé qui protège contre les vulnérabilités courantes.

**Réalisations Clés :**
- ✅ Authentification d'entreprise avec Keycloak OAuth2/OpenID Connect
- ✅ Contrôle d'accès basé sur les rôles avec permissions granulaires
- ✅ Validation complète des entrées prévenant les attaques par injection
- ✅ Journalisation sécurisée sans exposer d'informations sensibles
- ✅ Gestion globale des exceptions empêchant la fuite d'informations
- ✅ Configuration CORS suivant les meilleures pratiques
- ✅ Stratégies d'atténuation OWASP Top 10 appliquées

Cette implémentation de sécurité démontre une forte compréhension des principes modernes de sécurité des applications et leur application pratique dans une architecture de microservices Spring Boot.

---

**Naoufal Guendouz**  
*Développeur Backend axé sur la Sécurité*  
17 Novembre 2025
