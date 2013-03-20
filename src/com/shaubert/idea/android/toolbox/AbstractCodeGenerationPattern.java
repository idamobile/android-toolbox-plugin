package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.vfs.VirtualFile;
import org.codehaus.groovy.runtime.StringBufferWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractCodeGenerationPattern implements CodeGenerationPattern {

    @Override
    public String generateOutput(VirtualFile xmlLayoutFile, String outputClass) {
        AndroidLayoutParser parser = new AndroidLayoutParser();
        List<AndroidView> androidViews = parser.parse(xmlLayoutFile);
        if (!androidViews.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new StringBufferWriter(buffer));
                generateOutput(androidViews, outputClass, writer);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ignored) {
                    }
                }
            }
            return buffer.toString();
        }
        return null;
    }

    protected void generateOutput(List<AndroidView> androidViews,
                                  String canonicalPath, BufferedWriter writer) throws IOException {
        generatePackage(canonicalPath, writer);
        generateImports(androidViews, writer);
        generateBody(androidViews, canonicalPath, writer);
    }

    protected abstract void generateBody(List<AndroidView> androidViews,
                                         String canonicalPath, BufferedWriter writer) throws IOException;

    protected void generatePackage(String packageName, BufferedWriter writer) throws IOException {
        writer.write("package " + ClassNameHelper.extractPackageName(packageName) + ";");
        writer.write("\n\n");
    }

    protected void generateImports(List<AndroidView> androidViews, BufferedWriter writer) throws IOException {
        Set<String> foundClasses = new HashSet<String>();
        for (AndroidView view : androidViews) {
            foundClasses.add(view.getClassName());
        }
        for (String className : foundClasses) {
            appendImport(className, writer);
        }
    }

    protected void appendImport(String className, BufferedWriter writer) throws IOException {
        writer.write("import " + className + ";");
        writer.write("\n");
    }

    protected String generateGetterName(String field) {
        StringBuilder buffer = new StringBuilder(field);
        buffer.replace(0, 1, String.valueOf(buffer.charAt(0)).toUpperCase());
        buffer.insert(0, "get");
        return buffer.toString();
    }

}
