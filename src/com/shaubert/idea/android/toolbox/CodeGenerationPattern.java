package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

public interface CodeGenerationPattern {

    String getName();

    String getSuggestedClassName(String layoutFileName);

    PsiClass generateOutput(Project project, AndroidManifest androidManifest, AndroidView androidView, String layoutFileName, String outputClass);

    void setup(Project project);

    void setRecyclerViewSupport(boolean support);

    boolean hasRecyclerViewSupport();

}
