package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

public class ViewPresenterPattern extends AbstractCodeGenerationPattern {

    @Override
    public String getName() {
        return "ViewPresenter pattern";
    }

    @Override
    protected void generateBody(AndroidView androidView, PsiClass psiClass, Project project) {
    }

}
