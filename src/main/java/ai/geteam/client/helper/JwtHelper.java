package ai.geteam.client.helper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;

import ai.geteam.client.exception.RequestException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class JwtHelper {

    public List<String> extractRole(String token){
        DecodedJWT jwt = jwtDecoder(token);
        // check JWT role is correct

        Claim realmAccess = jwt.getClaim("realm_access");
        if (realmAccess.isNull()){
            // return empty list if realm_access is null
            return List.of();
        }
        return (List<String>) realmAccess.asMap().get("roles");
    }
    public String extractEmail(String token){
        DecodedJWT jwt = jwtDecoder(token);
        return jwt.getClaim("email").asString();
    }
    public boolean isTokenExpired(String token) {
        DecodedJWT jwt = jwtDecoder(token);
        Instant expirationTime = Instant.ofEpochSecond(jwt.getExpiresAt().getTime());
        return Instant.now().isBefore(expirationTime);
    }

    public DecodedJWT jwtDecoder(String token) throws RequestException {
        return JWT.decode(token.substring(7));
    }
}
