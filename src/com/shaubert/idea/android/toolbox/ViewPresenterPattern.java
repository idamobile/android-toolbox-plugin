package com.shaubert.idea.android.toolbox;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

public class ViewPresenterPattern extends AbstractCodeGenerationPattern {

    @Override
    public String getName() {
        return "ViewPresenter pattern";
    }

    @Override
    protected void generateBody(List<AndroidView> androidViews, String canonicalPath, BufferedWriter writer) throws IOException {
    }
}
