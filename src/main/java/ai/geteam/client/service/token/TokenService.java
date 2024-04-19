package ai.geteam.client.service.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public interface TokenService {
    Jwt getToken(Authentication authentication);
    String getRealm(Jwt token);
    List<String> getRole(Jwt token);

}
