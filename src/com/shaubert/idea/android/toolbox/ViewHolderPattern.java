package com.shaubert.idea.android.toolbox;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class ViewHolderPattern extends AbstractCodeGenerationPattern {

    @Override
    public String getName() {
        return "ViewHolder pattern";
    }

    @Override
    protected void generateBody(List<AndroidView> androidViews, String canonicalPath, BufferedWriter writer) throws IOException {
        String className = ClassNameHelper.getClassNameFromFullQualified(canonicalPath);
        writer.write("\n");

        appendImport("android.widget.View", writer);
        writer.write("\n");
        writer.write("public class " + className + " { ");
        writer.write("\n");
        generateFields(androidViews, writer);
        writer.write("\n");
        generateConstructor(androidViews, writer, className);
        writer.write("\n");
        generateGetters(androidViews, writer);
        writer.write("\n");
        writer.write("}");
    }

    private void generateGetters(List<AndroidView> androidViews, BufferedWriter writer) throws IOException {
        for (AndroidView view : androidViews) {
            writer.write("\n");
            writer.write("    public " + view.getClassSimpleName() + " " + generateGetterName(view.getCamelCaseId()) + "() {");
            writer.write("\n");
            writer.write("        return " + view.getCamelCaseId() + ";");
            writer.write("\n");
            writer.write("    }");
            writer.write("\n");
        }
    }

    private void generateConstructor(List<AndroidView> androidViews, BufferedWriter writer, String className) throws IOException {
        writer.write("    public " + className + "(View view) {");
        writer.write("\n");

        for (AndroidView view : androidViews) {
            writer.write("        this." + view.getCamelCaseId()
                    + " = (" + view.getClassSimpleName() + ") view.findViewById(R.id." + view.getIdValue() + ");");
            writer.write("\n");
        }
        writer.write("    }");
    }

    private void generateFields(List<AndroidView> androidViews, BufferedWriter writer) throws IOException {
        for (AndroidView view : androidViews) {
            writer.write("    private " + view.getClassSimpleName() + " " + view.getCamelCaseId() + ";");
            writer.write("\n");
        }
    }

}
