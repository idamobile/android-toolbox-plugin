package com.shaubert.idea.android.toolbox;

import java.util.ArrayList;
import java.util.List;

public class AndroidView implements TreeData {
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
            className = tagName.replaceAll("\\$", ".");
            classSimpleName = ClassHelper.getClassNameFromFullQualified(tagName).replaceAll("\\$", ".");
        } else {
            if (tagName.equalsIgnoreCase("View")
                    || tagName.equals("ViewStub")
                    || tagName.equals("TextureView")
                    || tagName.equals("Surface")
                    || tagName.equals("SurfaceView")
                    ) {
                className = "android.view." + tagName;
            } else if (tagName.equals("GestureOverlayView")) {
                className = "android.gesture." + tagName;
            } else if (tagName.equals("WebView")) {
                className = "android.webkit." + tagName;
            } else {
                className = "android.widget." + tagName;
            }
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

    public void addSubView(AndroidView view) {
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

    @Override
    public List<AndroidView> getChildNodes() {
        return subViews;
    }

    public List<AndroidView> getAllChildViews() {
        List<AndroidView> result = new ArrayList<AndroidView>();
        collectViews(result);
        return result;
    }

    private void collectViews(List<AndroidView> result) {
        for (AndroidView view : subViews) {
            result.add(view);
            view.collectViews(result);
        }
    }

    public AndroidView getParent() {
        return parent;
    }

    public void setParent(AndroidView parent) {
        this.parent = parent;
    }

    @Override
    public String getNodeName() {
        return idValue + " - " +  className;
    }

}