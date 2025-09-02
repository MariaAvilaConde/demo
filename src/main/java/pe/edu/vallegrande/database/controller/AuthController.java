package pe.edu.vallegrande.database.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.database.config.JwtTokenProvider;
import pe.edu.vallegrande.database.dto.AuthRequestDTO;
import pe.edu.vallegrande.database.dto.JwtResponseDTO;
import pe.edu.vallegrande.database.dto.UserDTO;
import pe.edu.vallegrande.database.modal.User;
import pe.edu.vallegrande.database.service.UserService;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ReactiveAuthenticationManager authenticationManager;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserDTO>> registerUser(@RequestBody User user) {
        return userService.registerUser(user)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<JwtResponseDTO>> login(@RequestBody AuthRequestDTO request) {
        return userService.findByEmail(request.getEmail())
                .flatMap(user -> {
                    return authenticationManager
                            .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()))
                            .flatMap(authentication -> {
                                String jwt = jwtTokenProvider.generateToken(authentication);
                                return Mono.just(ResponseEntity.ok(new JwtResponseDTO(
                                        jwt,
                                        user.getUsername(),
                                        user.getRole()
                                )));
                            });
                })
                .switchIfEmpty(Mono.error(new RuntimeException("Usuario no registrado")));
    }
}