package ai.geteam.client.exception;

import ai.geteam.client.exception.utils.ErrorCode;

public class DuplicationException extends RuntimeException {

    private final ErrorCode errorCode;

    public DuplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }


}
