package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;

public class ViewHolderPattern extends AbstractCodeGenerationPattern {

    @Override
    public String getName() {
        return "ViewHolder pattern";
    }

    @Override
    protected void generateBody(AndroidView androidView, PsiClass psiClass, Project project) {
        generateFields(androidView, psiClass);
        generateConstructor(androidView, psiClass);
        generateGetters(psiClass);
    }

    private void generateGetters(PsiClass psiClass) {
        for (PsiField psiField : psiClass.getAllFields()) {
            psiClass.add(PropertyUtil.generateGetterPrototype(psiField));
        }
    }

    private void generateConstructor(AndroidView androidView, PsiClass psiClass) {
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
        addFindViewStatements(factory, constructor, androidView);

        psiClass.add(constructor);
    }

    @SuppressWarnings("ConstantConditions")
    private void addFindViewStatements(PsiElementFactory factory, PsiMethod constructor, AndroidView view) {
        if (view.getParent() != null) {
            String viewGroupName = view.getParent().getCamelCaseId();
            PsiStatement assignmentStatement = getFindViewStatement(factory, constructor, viewGroupName, view);
            constructor.getBody().add(assignmentStatement);
        }
        for (AndroidView child : view.getSubViews()) {
            addFindViewStatements(factory, constructor, child);
        }
    }

    private PsiStatement getFindViewStatement(PsiElementFactory factory, PsiMethod constructor,
                                              String viewGroupName, AndroidView view) {
        return factory.createStatementFromText(
                "this." + view.getCamelCaseId() + " = ("
                        + view.getClassSimpleName() + ") "
                        + viewGroupName + ".findViewById(R.id." + view.getIdValue() + ");",
                constructor.getContext());
    }

    private void generateFields(AndroidView androidView, PsiClass psiClass) {
        if (androidView.getParent() != null) {
            PsiField field = createField(psiClass.getProject(), androidView);
            psiClass.add(field);
        }
        for (AndroidView view : androidView.getSubViews()) {
            generateFields(view, psiClass);
        }
    }

    private PsiField createField(Project project, AndroidView view) {
        PsiClass viewClass = ClassHelper.findClass(project, view.getClassName());
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        PsiField field = factory.createField(view.getCamelCaseId(), factory.createType(viewClass));
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            modifierList.setModifierProperty(PsiModifier.PRIVATE, true);
        } else {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create field");
        }
        return field;
    }

}