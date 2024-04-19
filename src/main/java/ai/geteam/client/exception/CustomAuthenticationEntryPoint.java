package ai.geteam.client.exception;

import ai.geteam.client.dto.ExceptionDTO;
import ai.geteam.client.exception.utils.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;

import java.io.IOException;
@Slf4j
public class CustomAuthenticationEntryPoint {
    public void  handle(HttpServletResponse response, AuthenticationException authException) throws IOException {
        if (authException.getCause() instanceof InvalidBearerTokenException) {
            handleInvalidToken(response);
        }else if (authException.getCause() instanceof JwtValidationException) {
            handleExpiredToken(response);
        }else if (authException.getCause()==null){
            handleNullToken(response);
        }
        else {
            handleInvalidToken(response);
        }

    }

    private void handleInvalidToken(HttpServletResponse response) throws IOException {
        ExceptionDTO exceptionDTO = new ExceptionDTO(ErrorCode.ACCESS_TOKEN_INVALID.get(), "Invalid Token");
        ResponseEntity<ExceptionDTO> responseEntity = new ResponseEntity<>(exceptionDTO, HttpStatus.UNAUTHORIZED);
        log.error("Invalid Token");
        writeResponse(response, responseEntity);
    }
    private void handleExpiredToken(HttpServletResponse response) throws IOException {
        ExceptionDTO exceptionDTO = new ExceptionDTO(ErrorCode.ACCESS_TOKEN_EXPIRED.get(), "Token has expired");
        ResponseEntity<ExceptionDTO> responseEntity = new ResponseEntity<>(exceptionDTO, HttpStatus.UNAUTHORIZED);
        log.error("Token has expired");
        writeResponse(response, responseEntity);
    }
    private void handleNullToken(HttpServletResponse response) throws IOException {
        ExceptionDTO exceptionDTO = new ExceptionDTO(ErrorCode.ACCESS_TOKEN_EMPTY.get(), "Null bearer token");
        ResponseEntity<ExceptionDTO> responseEntity = new ResponseEntity<>(exceptionDTO, HttpStatus.UNAUTHORIZED);
        log.error("Token is null");
        writeResponse(response, responseEntity);
    }

    private void writeResponse(HttpServletResponse response, ResponseEntity<ExceptionDTO> responseEntity) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(responseEntity.getStatusCode().value());
        response.getWriter().write(new ObjectMapper().writeValueAsString(responseEntity.getBody()));
        response.getWriter().flush();
        response.getWriter().close();
    }

}
