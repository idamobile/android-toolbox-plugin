package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;

public abstract class AbstractCodeGenerationPattern implements CodeGenerationPattern {

    public static final String ANDROID_VIEW_CLASS = "android.view.View";
    public static final String ANDROID_VIEW_GROUP_CLASS = "android.view.ViewGroup";
    public static final String ANDROID_LAYOUT_INFLATER_CLASS = "android.view.LayoutInflater";
    public static final String ANDROID_CONTEXT_CLASS = "android.content.Context";
    public static final String ANDROID_RECYCLER_VIEW_CLASS = "android.support.v7.widget.RecyclerView";
    public static final String ANDROID_RECYCLER_VIEW_VIEWHOLDER_CLASS = "android.support.v7.widget.RecyclerView.ViewHolder";
    public static final String ANDROID_RECYCLER_VIEW_VIEWHOLDER_VIEW_FIELD_NAME = "itemView";

    private boolean recyclerViewSupport;

    @Override
    public void setRecyclerViewSupport(boolean support) {
        recyclerViewSupport = support;
    }

    @Override
    public final boolean hasRecyclerViewSupport() {
        return recyclerViewSupport;
    }

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
        if (recyclerViewSupport) {
            PsiClass rvClass = ClassHelper.findClass(project, ANDROID_RECYCLER_VIEW_CLASS);
            PsiClass rvHolderClass = ClassHelper.findClass(project, ANDROID_RECYCLER_VIEW_VIEWHOLDER_CLASS);

            addImport(psiClass, rvClass);
            psiClass.getExtendsList().add(factory.createClassReferenceElement(rvHolderClass));
            generateRecyclerViewCompatConstructor(layoutFileName, psiClass);
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

    protected void generateRecyclerViewCompatConstructor(String layoutFileName, PsiClass psiClass) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod constructor = factory.createConstructor();
        PsiClass layoutInflaterClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_LAYOUT_INFLATER_CLASS);
        PsiParameter inflaterParam = factory.createParameter("inflater", factory.createType(layoutInflaterClass));
        PsiClass viewGroupClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_VIEW_GROUP_CLASS);
        PsiParameter viewParentParam = factory.createParameter("parent", factory.createType(viewGroupClass));
        constructor.getParameterList().add(inflaterParam);
        constructor.getParameterList().add(viewParentParam);

        if (constructor.getBody() == null) {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create recyclerView compat constructor");
        }

        PsiStatement callPrimaryConstructorStatement =
                factory.createStatementFromText("this(" + inflaterParam.getName() + ".inflate(R.layout."
                        + FileUtil.removeExtension(layoutFileName)
                        + ", " + viewParentParam.getName()
                        + ", false));", constructor.getContext());
        constructor.getBody().add(callPrimaryConstructorStatement);
        psiClass.add(constructor);
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
