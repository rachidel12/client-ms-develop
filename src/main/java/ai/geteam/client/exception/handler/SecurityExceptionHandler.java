package ai.geteam.client.exception.handler;


import ai.geteam.client.dto.ExceptionDTO;
import ai.geteam.client.exception.utils.ErrorCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Log4j2
public class SecurityExceptionHandler {

    // Spring security core exception: AuthenticationException
    @ExceptionHandler({AuthenticationException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleAuthenticationException(AuthenticationException e) {
        HttpStatus internalServerError = HttpStatus.UNAUTHORIZED; // 401
        ExceptionDTO exceptionDTO = new ExceptionDTO(
                ErrorCode.ACCESS_TOKEN_INVALID.get(),
                e.getMessage()
        );
        log.error(e);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }

    // Spring security core exception: BadCredentialsException
    @ExceptionHandler({BadCredentialsException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleBadCredentialsException(BadCredentialsException e) {
        HttpStatus internalServerError = HttpStatus.UNAUTHORIZED; // 401
        ExceptionDTO exceptionDTO = new ExceptionDTO(
                ErrorCode.ACCESS_TOKEN_INVALID.get(),
                e.getMessage()
        );
        log.error(e);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }

    // Spring security core exception: AccessDeniedException
    @ExceptionHandler({AccessDeniedException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleAccessDeniedException(AccessDeniedException e) {
        HttpStatus internalServerError = HttpStatus.FORBIDDEN; // 403
        ExceptionDTO exceptionDTO = new ExceptionDTO(
                ErrorCode.ACCESS_TOKEN_NOT_ROLE_CLIENT.get(),
                e.getMessage()
        );
        log.error(e);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }
}
