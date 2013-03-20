package com.shaubert.idea.android.toolbox;

public class AndroidView {
    private String tagName;
    private String classSimpleName;
    private String className;
    private String idValue;
    private String camelCaseId;

    public AndroidView() {
    }

    public AndroidView(String tagName, String idValue) {
        this.tagName = tagName;
        this.idValue = idValue;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName.trim();
        if (tagName.contains(".")) {
            className = tagName;
            classSimpleName = ClassNameHelper.getClassNameFromFullQualified(className);
        } else {
            className = "android.widget." + tagName;
            classSimpleName = tagName;
        }
    }

    public String getIdValue() {
        return idValue;
    }

    public void setIdValue(String idValue) {
        this.idValue = idValue;
        camelCaseId = ClassNameHelper.formatCamelCaseFromUnderscore(idValue);
    }

    public String getCamelCaseId() {
        return camelCaseId;
    }

    public String getClassSimpleName() {
        return classSimpleName;
    }

    public String getClassName() {
        return className;
    }
}
