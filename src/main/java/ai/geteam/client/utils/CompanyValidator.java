package ai.geteam.client.utils;

import ai.geteam.client.dto.CompanyDTO;

import java.util.regex.Pattern;

public class CompanyValidator {
    private CompanyValidator() {

    }

    public static boolean isValid(CompanyDTO company) {


        return company != null
                && company.getCityId() > 0
                && isValidName(company.getName())
                && isValidSize(company.getSize())
                && isValidWebsite(company.getWebsite())
                && RecruiterValidator.isValid(company.getRecruiter());
    }

    private static boolean isValidWebsite(String website) {
        return website != null
                && Pattern.matches("^https://[a-zA-Z0-9.\\-/]+$", website);
    }

    private static boolean isValidSize(String size) {
        return size != null
                && Pattern.matches("^\\d+-\\d+$", size)
                && Integer.parseInt(size.split("-")[0]) < Integer.parseInt(size.split("-")[1]);
    }

    private static boolean isValidName(String name) {
        return name != null
                && Pattern.matches("^[a-zA-Z0-9]{1,30}$", name);
    }





}
