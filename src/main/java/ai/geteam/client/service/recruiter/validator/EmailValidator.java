package ai.geteam.client.service.recruiter.validator;


import ai.geteam.client.exception.InvalidInputException;
import ai.geteam.client.exception.utils.ErrorCode;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.function.Predicate;
import java.util.regex.Pattern;

@Service
@Log4j2
public class EmailValidator implements Predicate<String> {

    static final String EMAIL_REGEX = "^[A-Z0-9.%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE);

    @Override
    public boolean test(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            log.error("The email '" + email + "' is invalid.");
            throw new InvalidInputException(ErrorCode.EMAIL_INVALID,"The email is invalid.");
        }
        return true;
    }
}

