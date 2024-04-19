package ai.geteam.client.security;

import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.exception.utils.ErrorCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    private final JwtConverterProperties properties;

    public JwtConverter(JwtConverterProperties properties) {
        this.properties = properties;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        if (jwt.getTokenValue() == null || jwt.getTokenValue().isEmpty()) {
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_EMPTY, "Token is empty");
        }

        try {

            // Check if the token has an expiration time and if it's expired
            Instant expirationTime = jwt.getExpiresAt();
            if (expirationTime == null || expirationTime.isBefore(Instant.now())) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_EXPIRED, "Token is expired");
            }

            if (jwt.getTokenValue() == null || jwt.getTokenValue().isEmpty()) {
                throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_EMPTY, "Token is empty");
            }

            // Convert Jwt to authorities if convert() does not return null
            Collection<GrantedAuthority> jwtAuthorities = jwtGrantedAuthoritiesConverter.convert(jwt);
            Collection<GrantedAuthority> authorities = new HashSet<>(jwtAuthorities);


            // Extract resource roles if not null and add them to authorities
            Collection<GrantedAuthority> resourceRoles = (Collection<GrantedAuthority>) extractResourceRoles(jwt);
            authorities.addAll(resourceRoles);

            // Create JwtAuthenticationToken with authorities
            return new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    getPrincipalClaimName(jwt)
            );
        } catch (JwtValidationException e) {
            throw new UnAuthorizedException(ErrorCode.ACCESS_TOKEN_INVALID, "Invalid access Token");
        }
    }


    private String getPrincipalClaimName(Jwt jwt) {
        String claimName = JwtClaimNames.SUB;
        if (properties.getPrincipalAttribute() != null) {
            claimName = properties.getPrincipalAttribute();
        }
        return jwt.getClaim(claimName);
    }

    private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
        Map<String, Object> resourceAccess;
        Collection<String> resourceRoles;

        if (jwt.getClaim("resource_access") == null) {
            return Set.of();
        }
        resourceAccess = (Map<String, Object>) jwt.getClaim("resource_access");
        Map<String, Object> clientResource = (Map<String, Object>) resourceAccess.get("client");
        if (clientResource == null) {
            return Set.of();
        }
        resourceRoles = (Collection<String>) clientResource.get("roles");

        return resourceRoles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super AbstractAuthenticationToken, ? extends U> after) {
        return Converter.super.andThen(after);
    }

}
