package com.shaubert.idea.android.toolbox;

import java.util.ArrayList;
import java.util.List;

public class AndroidView {
    private String tagName;
    private String classSimpleName;
    private String className;
    private String idValue;
    private String camelCaseId;

    private AndroidView parent;
    private List<AndroidView> subViews = new ArrayList<AndroidView>();

    public AndroidView() {
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName.trim();
        if (tagName.contains(".")) {
            className = tagName;
            classSimpleName = ClassHelper.getClassNameFromFullQualified(className);
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
        camelCaseId = ClassHelper.formatCamelCaseFromUnderscore(idValue);
    }

    public void addSubView(AndroidView view, AndroidLayoutParser.DuplicateIdPolicy duplicateIdPolicy) {
        if (duplicateIdPolicy == AndroidLayoutParser.DuplicateIdPolicy.REMOVE) {
            for (AndroidView subView : subViews) {
                if (subView.getIdValue().endsWith(view.getIdValue())) {
                    return;
                }
            }
        }
        view.setParent(this);
        this.subViews.add(view);
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

    public List<AndroidView> getSubViews() {
        return subViews;
    }

    public AndroidView getParent() {
        return parent;
    }

    public void setParent(AndroidView parent) {
        this.parent = parent;
    }
}
