package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.EverythingGlobalScope;

import javax.swing.*;
import java.awt.*;

public class ChooseGenerationPatternDialog extends ChooseDialog {

    private JCheckBox recyclerViewCheckbox;

    public ChooseGenerationPatternDialog(final Project project,
                                         String name,
                                         String[] values,
                                         int initialValueIndex) {
        super(project, "Generate View Code",
                "Choose view code generation style for " + name,
                values,
                initialValueIndex);
    }

    @Override
    protected void setupSouthOfMessagePanel(JPanel messagePanel) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(getProject());
        PsiClass rvClass = psiFacade.findClass(AbstractCodeGenerationPattern.ANDROID_RECYCLER_VIEW_CLASS,
                new EverythingGlobalScope(getProject()));
        if (rvClass != null) {
            recyclerViewCheckbox = new JCheckBox("RecyclerView support", true);
            messagePanel.add(recyclerViewCheckbox, BorderLayout.SOUTH);
        }
    }

    public boolean hasRecyclerViewSupport() {
        return recyclerViewCheckbox != null && recyclerViewCheckbox.isSelected();
    }

}
