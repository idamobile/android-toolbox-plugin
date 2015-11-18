package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.EverythingGlobalScope;

public class ButterKnife {

    private static final String BUTTERKNIFE_INJECT_VIEW = "butterknife.InjectView";
    private static final String BUTTERKNIFE_BIND = "butterknife.Bind";
    private static final String BUTTERKNIFE_BIND_METHOD = "bind";
    private static final String BUTTERKNIFE_INJECT_METHOD = "inject";
    private static final String BUTTERKNIFE_VIEWS = "butterknife.Views";
    private static final String BUTTERKNIFE_BUTTERKNIFE = "butterknife.ButterKnife";

    private PsiClass injectorClass;
    private PsiClass injectViewClass;
    private String methodName;

    private ButterKnife(PsiClass injectorClass, PsiClass injectViewClass, String methodName) {
        this.injectorClass = injectorClass;
        this.injectViewClass = injectViewClass;
        this.methodName = methodName;
    }

    public PsiClass getInjectorPsiClass() {
        return injectorClass;
    }

    public PsiClass getInjectViewClass() {
        return injectViewClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public static ButterKnife find(Project project) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        PsiClass injectViewsClass = psiFacade.findClass(BUTTERKNIFE_INJECT_VIEW, new EverythingGlobalScope(project));
        String methodName = null;
        if (injectViewsClass == null) {
            injectViewsClass = psiFacade.findClass(BUTTERKNIFE_BIND, new EverythingGlobalScope(project));
            if (injectViewsClass != null) {
                methodName = BUTTERKNIFE_BIND_METHOD;
            }
        } else {
            methodName = BUTTERKNIFE_INJECT_METHOD;
        }
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
        return new ButterKnife(injector, injectViewsClass, methodName);
    }

}
