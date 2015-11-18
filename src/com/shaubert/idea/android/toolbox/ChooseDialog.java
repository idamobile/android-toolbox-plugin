package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.MultiLineLabelUI;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ChooseDialog extends DialogWrapper {

    private ComboBox myComboBox;
    private Project project;
    private String message;

    public ChooseDialog(final Project project,
                        String title,
                        String message,
                        String[] values,
                        int initialValueIndex) {
        super(project, true);
        this.project = project;
        this.message = message;
        setTitle(title);
        init();

        //noinspection unchecked
        myComboBox.setModel(new DefaultComboBoxModel<String>(values));
        myComboBox.setSelectedIndex(initialValueIndex);
    }

    public Project getProject() {
        return project;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return null;
    }

    @Override
    protected JComponent createNorthPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));

        JLabel iconLabel = new JLabel(Messages.getQuestionIcon());
        Container container = new Container();
        container.setLayout(new BorderLayout());
        container.add(iconLabel, BorderLayout.NORTH);
        panel.add(container, BorderLayout.WEST);

        JPanel messagePanel = new JPanel(new BorderLayout());
        if (message != null) {
            JLabel textLabel = new JLabel(message);
            textLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            textLabel.setUI(new MultiLineLabelUI());
            messagePanel.add(textLabel, BorderLayout.NORTH);
        }

        myComboBox = new ComboBox(420);
        messagePanel.add(myComboBox, BorderLayout.CENTER);

        setupSouthOfMessagePanel(messagePanel);

        panel.add(messagePanel, BorderLayout.CENTER);

        myComboBox.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.isControlDown() && KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        return panel;
    }

    protected void setupSouthOfMessagePanel(JPanel messagePanel) {
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return myComboBox;
    }

    @Nullable
    public String getSelectedString() {
        if (getExitCode() == 0) {
            return myComboBox.getSelectedItem().toString();
        }
        return null;
    }

    public int getSelectedIndex() {
        if (getExitCode() == 0) {
            return myComboBox.getSelectedIndex();
        }
        return -1;
    }

    public JComboBox getComboBox() {
        return myComboBox;
    }
}
