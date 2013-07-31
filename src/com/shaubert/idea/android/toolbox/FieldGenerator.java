package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldGenerator {

    public interface CreateFieldCallback {
        void onFieldCreated(PsiField field);
    }

    public static class AddToPsiClassCallback implements CreateFieldCallback {
        private PsiClass psiClass;

        public AddToPsiClassCallback(PsiClass psiClass) {
            this.psiClass = psiClass;
        }

        @Override
        public void onFieldCreated(PsiField field) {
            psiClass.add(field);
        }
    }

    public Map<AndroidView, PsiField> generateFields(AndroidView androidView, Project project,
                                                     boolean useButterKnife, @Nullable CreateFieldCallback createFieldCallback)
            throws GenerateViewPresenterAction.CancellationException {
        List<AndroidView> views = androidView.getAllChildViews();
        Map<String, Integer> idsCount = new HashMap<String, Integer>();
        Map<AndroidView, PsiField> fieldMappings = new HashMap<AndroidView, PsiField>();
        for (AndroidView view : views) {
            Integer count = idsCount.get(view.getIdValue());
            if (count == null) {
                count = 1;
            } else {
                count = count + 1;
            }
            idsCount.put(view.getIdValue(), count);
            PsiField field = createField(project, view, count);
            if (useButterKnife) {
                addButterKnifeAnnotation(field, view);
            }
            fieldMappings.put(view, field);
            if (createFieldCallback != null) {
                createFieldCallback.onFieldCreated(field);
            }
        }
        return fieldMappings;
    }

    private PsiField createField(Project project, AndroidView view, int count) {
        PsiClass viewClass = ClassHelper.findClass(project, view.getClassName());
        String fieldName = view.getCamelCaseId();
        if (count > 1) {
            fieldName = fieldName + count;
        }
        return createField(viewClass, fieldName);
    }

    public PsiField createField(PsiClass viewClass, String fieldName) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(viewClass.getProject());
        PsiField field = factory.createField(fieldName, factory.createType(viewClass));
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            modifierList.setModifierProperty(PsiModifier.PRIVATE, true);
        } else {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create field");
        }
        return field;
    }

    public void addButterKnifeAnnotation(PsiField field, AndroidView view) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(field.getProject());
        PsiModifierList modifierList = field.getModifierList();
        if (modifierList != null) {
            modifierList.setModifierProperty(PsiModifier.PRIVATE, false);
            PsiAnnotation annotation = modifierList.addAnnotation(AbstractCodeGenerationPattern.BUTTERKNIFE_INJECT_VIEW);
            annotation.setDeclaredAttributeValue("value",
                    factory.createExpressionFromText("R.id." + view.getIdValue(), annotation));
        } else {
            throw new GenerateViewPresenterAction.CancellationException("Failed to create field");
        }
    }

}
