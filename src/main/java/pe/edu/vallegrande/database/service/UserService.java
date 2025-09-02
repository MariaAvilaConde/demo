package pe.edu.vallegrande.database.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.database.dto.UserDTO;
import pe.edu.vallegrande.database.modal.User;
import pe.edu.vallegrande.database.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final FirebaseAuth firebaseAuth;

    public Mono<UserDTO> registerUser(User user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new RuntimeException("Email ya registrado"));
                    }

                    // Registro en Firebase
                    return Mono.fromCallable(() -> {
                                UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                                        .setEmail(user.getEmail())
                                        .setPassword(user.getPassword());

                                UserRecord firebaseUser = firebaseAuth.createUser(request);

                                user.setFirebaseUid(firebaseUser.getUid());
                                user.setPassword(passwordEncoder.encode(user.getPassword()));
                                user.setCreatedAt(LocalDateTime.now());
                                user.setEnabled(true);
                                user.setRole("USER");

                                return user;
                            })
                            .flatMap(userRepository::save)
                            .map(savedUser -> new UserDTO(
                                    savedUser.getUsername(),
                                    savedUser.getEmail(),
                                    savedUser.getRole()
                            ));
                });
    }

    public Mono<UserDTO> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> new UserDTO(
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole()
                ));
    }
}