package com.barangay.presentation.util;

public class StringUtil {

    private StringUtil() {
    }

    public static String capitalize(String word) {
        if (word == null || word.isEmpty()) {
            return word;
        }
        String lower = word.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static String toTitleCase(String snakeCase) {
        if (snakeCase == null || snakeCase.isEmpty()) {
            return snakeCase;
        }
        String[] words = snakeCase.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty())
                continue;
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(capitalize(word));
        }

        return result.toString();
    }

}
