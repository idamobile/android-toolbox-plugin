package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;

public interface CodeGenerationPattern {

    public String getName();

    public String getSuggestedClassName(String layoutFileName);

    public PsiClass generateOutput(Project project, AndroidManifest androidManifest, AndroidView androidView, String layoutFileName, String outputClass);

    public void setup(Project project);

}
