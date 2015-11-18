package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.search.EverythingGlobalScope;

public class ClassHelper {

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

    public static PsiClass findClass(Project project, String className) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass viewClass = psiFacade.findClass(className, new EverythingGlobalScope(project));
        if (viewClass == null) {
            throw new GenerateViewPresenterAction.CancellationException("Class not found: " + className);
        }
        return viewClass;
    }

    public static PsiField findField(PsiClass psiClass, String fieldName) {
        PsiField[] fields = psiClass.getAllFields();
        for (PsiField field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        throw new GenerateViewPresenterAction.CancellationException("Field not found: " + fieldName);
    }
}
