package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

public abstract class AbstractCodeGenerationPattern implements CodeGenerationPattern {

    public static final String ANDROID_VIEW_CLASS = "android.view.View";
    public static final String ANDROID_VIEW_GROUP_CLASS = "android.view.ViewGroup";
    public static final String ANDROID_LAYOUT_INFLATER_CLASS = "android.view.LayoutInflater";
    public static final String ANDROID_CONTEXT_CLASS = "android.content.Context";

    @Override
    public String getSuggestedClassName(String layoutFileName) {
        String layoutName = FileUtil.removeExtension(layoutFileName);
        String className = ClassHelper.formatCamelCaseFromUnderscore(layoutName);
        return ClassHelper.upperCaseLetter(className, 0);
    }

    @Override
    public PsiClass generateOutput(Project project, AndroidManifest androidManifest, AndroidView androidView,
                                   String layoutFileName, String outputClass) {
        if (!androidView.getChildNodes().isEmpty()) {
            return generateOutput(androidView, layoutFileName, androidManifest, outputClass, project);
        }
        return null;
    }

    protected PsiClass generateOutput(AndroidView androidView, String layoutFileName, AndroidManifest androidManifest, String canonicalPath, Project project) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiClass psiClass = factory.createClass(ClassHelper.getClassNameFromFullQualified(canonicalPath));
        ButterKnife butterKnife = ButterKnife.find(project);
        if (butterKnife != null) {
            addImport(psiClass, butterKnife.getInjectViewClass());
            addImport(psiClass, butterKnife.getInjectorPsiClass());
        }
        generateBody(androidView, layoutFileName, butterKnife, psiClass, project);
        addRClassImport(psiClass, androidManifest);

        return psiClass;
    }

    private void addRClassImport(PsiClass psiClass, AndroidManifest androidManifest) {
        PsiClass rClass = ClassHelper.findClass(psiClass.getProject(), androidManifest.getPackageName() + ".R");
        addImport(psiClass, rClass);
    }

    protected void addImport(PsiClass psiClass, PsiClass importClass) {
        PsiFile containingFile = psiClass.getNavigationElement().getContainingFile();
        JavaCodeStyleManager manager = JavaCodeStyleManager.getInstance(psiClass.getProject());
        manager.addImport((PsiJavaFile) containingFile, importClass);
    }

    protected abstract void generateBody(AndroidView androidView, String layoutFileName, ButterKnife butterKnife, PsiClass psiClass, Project project);

    protected String generateGetterName(String field) {
        StringBuilder buffer = new StringBuilder(field);
        buffer.replace(0, 1, String.valueOf(buffer.charAt(0)).toUpperCase());
        buffer.insert(0, "get");
        return buffer.toString();
    }

    @Override
    public void setup(Project project) {
    }

}
