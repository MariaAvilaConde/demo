package pe.edu.vallegrande.database.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FirebaseAuthService {
    private final FirebaseAuth firebaseAuth;

    public Mono<String> verifyIdToken(String idToken) {
        return Mono.fromCallable(() -> {
            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            return decodedToken.getUid();
        });
    }
}