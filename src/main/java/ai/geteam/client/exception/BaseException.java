package ai.geteam.client.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
@Getter
@Setter
public class BaseException extends RuntimeException {

    private final HttpStatus status;

    private final String message;

    public BaseException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
