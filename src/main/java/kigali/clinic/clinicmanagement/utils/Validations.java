package kigali.clinic.clinicmanagement.utils;

import java.math.BigInteger;

public final class Validations {

    private Validations() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static String normalize(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }

    public static boolean containsDigit(String value) {
        return value != null && value.matches(".*\\d.*");
    }

    // office numbers can be text like "A-12", so only plain numbers are range checked
    public static boolean isNotPositiveNumber(String value) {

        if (value == null || !value.matches("-?\\d+")) {
            return false;
        }

        return new BigInteger(value).signum() <= 0;
    }
}
