package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;

import java.util.Map;

public class ViewHolderPattern extends AbstractCodeGenerationPattern {

    @Override
    public String getName() {
        return "ViewHolder pattern";
    }

    @Override
    public String getSuggestedClassName(String layoutFileName) {
        return super.getSuggestedClassName(layoutFileName) + "Holder";
    }

    @Override
    protected void generateBody(AndroidView androidView, String layoutFileName, final PsiClass psiClass, Project project) {
        FieldGenerator fieldGenerator = new FieldGenerator();
        Map<AndroidView, PsiField> fieldMappings = fieldGenerator.generateFields(
                androidView, project, new FieldGenerator.AddToPsiClassCallback(psiClass));
        generateConstructor(androidView, fieldMappings, psiClass);
        generateGetters(psiClass);
    }

    private void generateGetters(PsiClass psiClass) {
        for (PsiField psiField : psiClass.getAllFields()) {
            psiClass.add(PropertyUtil.generateGetterPrototype(psiField));
        }
    }

    private void generateConstructor(AndroidView androidView, Map<AndroidView, PsiField> fieldMappings, PsiClass psiClass) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        PsiMethod constructor = factory.createConstructor();
        PsiClass viewClass = ClassHelper.findClass(psiClass.getProject(), ANDROID_VIEW_CLASS);
        PsiParameter viewParam = factory.createParameter("view", factory.createType(viewClass));
        constructor.getParameterList().add(viewParam);

        if (constructor.getBody() == null) {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create ViewHolder constructor");
        }

        androidView.setTagName(ANDROID_VIEW_CLASS);
        androidView.setIdValue(viewParam.getName());
        addFindViewStatements(factory, constructor, androidView, fieldMappings);

        psiClass.add(constructor);
    }

    @SuppressWarnings("ConstantConditions")
    private void addFindViewStatements(final PsiElementFactory factory, final PsiMethod constructor,
                                       final AndroidView view, final Map<AndroidView, PsiField> fieldMappings) {
        FindViewByIdStatementGenerator findViewByIdStatementGenerator = new FindViewByIdStatementGenerator();
        FindViewByIdStatementGenerator.ClassFieldAssigner fieldAssigner =
                new FindViewByIdStatementGenerator.ClassFieldAssigner(fieldMappings, view.getIdValue()) {
                    @Override
                    protected void onStatementCreated(String statement, PsiField field, AndroidView view) {
                        PsiStatement assignmentStatement =
                                factory.createStatementFromText(statement, constructor.getContext());
                        constructor.getBody().add(assignmentStatement);
                    }

                    @Override
                    public boolean shouldProcessView(AndroidView view) {
                        return view.getParent() != null;
                    }
                };
        findViewByIdStatementGenerator.createFindViewStatements(view, fieldAssigner);
    }

}