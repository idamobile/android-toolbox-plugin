package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.vfs.VirtualFile;

public interface CodeGenerationPattern {

    public String getName();

    public String generateOutput(VirtualFile xmlLayoutFile, String outputClass);

}
