package com.shaubert.idea.android.toolbox;

import com.intellij.psi.PsiField;

import java.util.Map;

public class FindViewByIdStatementGenerator {

    public interface FindViewByIdCallback {
        void onStatementCreated(String statement, AndroidView view);
        String getViewGroupNameFor(AndroidView view);
        boolean shouldProcessView(AndroidView view);
    }

    public static abstract class ClassFieldAssigner implements FindViewByIdCallback {
        private Map<AndroidView, PsiField> fieldMappings;
        private String defaultViewGroup;

        public ClassFieldAssigner(Map<AndroidView, PsiField> fieldMappings, String defaultViewGroup) {
            this.fieldMappings = fieldMappings;
            this.defaultViewGroup = defaultViewGroup;
        }

        @Override
        public void onStatementCreated(String statement, AndroidView view) {
            PsiField psiField = fieldMappings.get(view);
            String resultStatement = "this." + psiField.getName() + " = " + statement;
            onStatementCreated(resultStatement, psiField, view);
        }

        protected abstract void onStatementCreated(String statement, PsiField field, AndroidView view);

        @Override
        public String getViewGroupNameFor(AndroidView view) {
            if (view.getParent() == null) {
                return defaultViewGroup;
            } else {
                PsiField field = fieldMappings.get(view.getParent());
                if (field == null) {
                    return defaultViewGroup;
                }
                return field.getName();
            }
        }

        @Override
        public boolean shouldProcessView(AndroidView view) {
            return true;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void createFindViewStatements(AndroidView view, FindViewByIdCallback callback) {
        if (callback.shouldProcessView(view)) {
            String viewGroupName = callback.getViewGroupNameFor(view);
            String statement = createFindViewStatement(viewGroupName, view);
            callback.onStatementCreated(statement, view);
        }
        for (AndroidView subView : view.getChildNodes()) {
            createFindViewStatements(subView, callback);
        }
    }

    public String createFindViewStatement(String viewGroupName, AndroidView view) {
        return "(" + view.getClassSimpleName() + ") "
                + viewGroupName + ".findViewById(R.id." + view.getIdValue() + ");";
    }

}
