package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.EverythingGlobalScope;

public class ButterKnife {

    private static final String BUTTERKNIFE_INJECT_VIEW = "butterknife.InjectView";
    private static final String BUTTERKNIFE_VIEWS = "butterknife.Views";
    private static final String BUTTERKNIFE_BUTTERKNIFE = "butterknife.ButterKnife";

    private PsiClass injectorClass;
    private PsiClass injectViewClass;

    private ButterKnife(PsiClass injectorClass, PsiClass injectViewClass) {
        this.injectorClass = injectorClass;
        this.injectViewClass = injectViewClass;
    }

    public PsiClass getInjectorPsiClass() {
        return injectorClass;
    }

    public PsiClass getInjectViewClass() {
        return injectViewClass;
    }

    public static ButterKnife find(Project project) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass injectViewsClass = psiFacade.findClass(BUTTERKNIFE_INJECT_VIEW, new EverythingGlobalScope(project));
        if (injectViewsClass == null) {
            return null;
        }
        PsiClass injector = psiFacade.findClass(BUTTERKNIFE_VIEWS, new EverythingGlobalScope(project));
        if (injector == null) {
            injector = psiFacade.findClass(BUTTERKNIFE_BUTTERKNIFE, new EverythingGlobalScope(project));
        }
        if (injector == null) {
            return null;
        }
        return new ButterKnife(injector, injectViewsClass);
    }

}
