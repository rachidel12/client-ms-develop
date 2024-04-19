package ai.geteam.client.utils;

import java.util.regex.Pattern;

public class RecruiterValidation {
    private RecruiterValidation() {

    }



    public static boolean isValidPhone(String phone) {
        return phone != null
                && Pattern.matches("^\\+(\\d{1,3})\\d{4,12}$", phone);
    }


    public static boolean isValidName(String firstName) {
        return firstName != null
                && Pattern.matches("^[a-zA-Z0-9 ]{1,30}$", firstName);
    }
}
