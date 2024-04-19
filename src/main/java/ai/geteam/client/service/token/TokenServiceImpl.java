package ai.geteam.client.service.token;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService{

    @Override
    public Jwt getToken(Authentication authentication) {
        if(authentication==null) return null;
        return (Jwt) authentication.getPrincipal();

    }

    @Override
    public String getRealm(Jwt token) {
        return token.getClaim("azp");
    }

    @Override
    public List<String> getRole(Jwt token) {
        Map<String, Object> realmAccess= (Map<String, Object>) token.getClaims().get("realm_access");
        return (List<String>) realmAccess.get("roles");

    }

}
