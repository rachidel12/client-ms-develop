package ai.geteam.client.utils;

import ai.geteam.client.dto.RecruiterDTO;

import java.util.regex.Pattern;

public class RecruiterValidator {
    private RecruiterValidator() {

    }

    public static boolean isValid(RecruiterDTO recruiterDTO) {
        return recruiterDTO != null
                && isValidName(recruiterDTO.getFirstName())
                && isValidName(recruiterDTO.getLastName())
                && isValidEmail(recruiterDTO.getEmail())
                && (recruiterDTO.getPhone() == null || isValidPhone(recruiterDTO.getPhone()))
                && recruiterDTO.getStatus() != null
                && (recruiterDTO.getCompanyId() == null || recruiterDTO.getCompanyId() > 0);
    }

    private static boolean isValidPhone(String phone) {
        return phone != null
                && Pattern.matches("^\\+(\\d{1,3})\\d{4,12}$", phone);
    }

    private static boolean isValidEmail(String email) {
        return email != null
                && Pattern.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", email);
    }

    private static boolean isValidName(String firstName) {
        return firstName != null
                && Pattern.matches("^[a-zA-Z0-9]{1,30}$", firstName);
    }
}
