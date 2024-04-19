package ai.geteam.client.exception;


import org.springframework.http.HttpStatus;

public class ValidationException extends BaseException {
    public ValidationException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
