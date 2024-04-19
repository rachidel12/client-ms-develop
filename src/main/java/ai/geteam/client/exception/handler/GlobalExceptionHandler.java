package ai.geteam.client.exception.handler;

import ai.geteam.client.dto.ExceptionDTO;
import ai.geteam.client.exception.DuplicationException;
import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.UnAuthorizedException;
import ai.geteam.client.exception.utils.ErrorCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler({DuplicationException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleException(DuplicationException t) {
        HttpStatus internalServerError = HttpStatus.BAD_REQUEST;
        ExceptionDTO exceptionDTO = new ExceptionDTO(
                ErrorCode.EMAIL_ALREADY_EXISTS.get(),
                t.getMessage()
        );
        log.error(t);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }

    // InvalidInputException
    @ExceptionHandler({InvalidInputException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleInvalidInputException(InvalidInputException e) {
        HttpStatus internalServerError = HttpStatus.BAD_REQUEST; // 400
        ExceptionDTO exceptionDTO = ExceptionDTO.builder()
                .error(e.getErrorCode().get())
                .message(e.getMessage())
                .build();
        log.error(e);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }

    // UnAuthorizedException
    @ExceptionHandler({UnAuthorizedException.class})
    public @ResponseBody ResponseEntity<ExceptionDTO> handleUnAuthorizedException(UnAuthorizedException e) {
        HttpStatus internalServerError = HttpStatus.UNAUTHORIZED; // 401
        ExceptionDTO exceptionDTO = ExceptionDTO.builder()
                .error(e.getErrorCode().get())
                .message(e.getMessage())
                .build();
        log.error(e);
        return ResponseEntity.status(internalServerError).body(exceptionDTO);
    }


    @ExceptionHandler(Exception.class)
    public @ResponseBody ResponseEntity<ExceptionDTO> handleException(Exception e) {
        log.info(e);
        ExceptionDTO exceptionDTO = new ExceptionDTO();
        if (e instanceof AccessDeniedException) {
            exceptionDTO.setError(ErrorCode.ACCESS_TOKEN_NOT_ROLE_CLIENT.get());
            exceptionDTO.setMessage("Access Denied: not Role Client");

            HttpStatus internalServerError = HttpStatus.FORBIDDEN; // 403
            log.error("Access Denied");
            return ResponseEntity.status(internalServerError).body(exceptionDTO);
        }
        exceptionDTO.setMessage(e.getMessage());
        exceptionDTO.setError(ErrorCode.GENERAL_EXCEPTION.get());
        return ResponseEntity.internalServerError().body(exceptionDTO);

    }
}