package ai.geteam.client.feign.retry;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.stereotype.Component;

@Component
public class Custom5xxErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        Exception exception = new Default().decode(methodKey, response);

        if (exception instanceof RetryableException) {
            return exception;
        }

        int status = response.status();
        if (status >= 500) {
            return new RetryableException(
                    response.status(),
                    exception.getMessage(),
                    response.request().httpMethod(),
                    exception,
                    2000L,
                    response.request());
        }
        return exception;

    }
}