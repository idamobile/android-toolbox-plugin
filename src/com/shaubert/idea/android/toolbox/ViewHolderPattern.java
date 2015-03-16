package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;

import java.util.Collection;
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
    protected void generateBody(AndroidView androidView, String layoutFileName, ButterKnife butterKnife, final PsiClass psiClass, Project project) {
        FieldGenerator fieldGenerator = new FieldGenerator();
        Map<AndroidView, PsiField> fieldMappings = fieldGenerator.generateFields(
                androidView, project, butterKnife, new FieldGenerator.AddToPsiClassCallback(psiClass));
        generateConstructor(androidView, butterKnife, fieldMappings, psiClass);
        generateGetters(psiClass, fieldMappings.values());
    }

    private void generateGetters(PsiClass psiClass, Collection<PsiField> psiFields) {
        for (PsiField psiField : psiFields) {
            PsiMethod method = PropertyUtil.generateGetterPrototype(psiField);
            if (method != null) {
                psiClass.add(method);
            }
        }
    }

    private void generateConstructor(AndroidView androidView, ButterKnife butterKnife, Map<AndroidView, PsiField> fieldMappings, PsiClass psiClass) {
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
        if (butterKnife != null) {
            String injectorClassName = butterKnife.getInjectorPsiClass().getName();
            PsiStatement injectStatement =
                    factory.createStatementFromText(injectorClassName + ".inject(this, " + viewParam.getName() + ");", constructor.getContext());
            constructor.getBody().add(injectStatement);
        } else {
            addFindViewStatements(factory, constructor, androidView, fieldMappings);
        }

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