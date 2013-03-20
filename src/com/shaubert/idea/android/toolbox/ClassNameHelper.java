package com.shaubert.idea.android.toolbox;

public class ClassNameHelper {

    public static String getClassNameFromFullQualified(String fullQualified) {
        int lastPoint = fullQualified.lastIndexOf(".");
        if (lastPoint >= 0) {
            return fullQualified.substring(lastPoint + 1);
        } else {
            return fullQualified;
        }
    }

    public static String extractPackageName(String className) {
        int lastPoint = className.lastIndexOf(".");
        if (lastPoint >= 0) {
            return className.substring(0, lastPoint);
        } else {
            return "";
        }
    }

    public static String formatCamelCaseFromUnderscore(String srtWithUnderscores) {
        if (srtWithUnderscores == null) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder(srtWithUnderscores);
            for (int i = 0; i < builder.length(); i++) {
                char ch = builder.charAt(i);
                if (ch == '_') {
                    Character nextChar = i < builder.length() - 1 ? builder.charAt(i + 1) : null;
                    if (nextChar != null && nextChar != '_') {
                        upperCaseLetter(builder, i + 1);
                    }
                    builder.delete(i, i + 1);
                }
            }
            return builder.toString();
        }
    }

    public static String upperCaseLetter(String string, int charIndex) {
        StringBuilder builder = new StringBuilder(string);
        upperCaseLetter(builder, charIndex);
        return builder.toString();
    }

    public static void upperCaseLetter(StringBuilder builder, int charIndex) {
        builder.replace(charIndex, charIndex + 1, String.valueOf(builder.charAt(charIndex)).toUpperCase());
    }
}
