package pe.edu.vallegrande.database.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.database.config.JwtTokenProvider;
import pe.edu.vallegrande.database.dto.UserDTO;
import pe.edu.vallegrande.database.service.UserService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class TokenValidationController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/verify-token")
    public Mono<ResponseEntity<UserDTO>> verifyToken(@RequestHeader("Authorization") String authHeader) {
        // Extraer el token de la cabecera de autorización
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Validar el token
            if (jwtTokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getUsernameFromToken(token);
                return userService.findByEmail(email)
                        .map(ResponseEntity::ok)
                        .defaultIfEmpty(ResponseEntity.status(401).build());
            }
        }

        return Mono.just(ResponseEntity.status(401).build());
    }
}