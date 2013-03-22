package com.shaubert.idea.android.toolbox;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public final class MultichoiceTreeDialog extends DialogWrapper{

    private Tree myTree;
    private TreeData treeData;
    private CheckedTreeNode root;

    @Nullable
    private CheckboxTreeBase.CheckPolicy checkPolicy;

    public MultichoiceTreeDialog(final Project project,
                                 String title,
                                 TreeData treeData,
                                 @Nullable CheckboxTreeBase.CheckPolicy checkPolicy) {
        super(project, true);
        this.treeData = treeData;
        this.checkPolicy = checkPolicy;
        setTitle(title);
        init();
    }

    private CheckedTreeNode buildNodes(TreeData data) {
        CheckedTreeNode node = new CheckedTreeNode(data);
        for (TreeData child : data.getChildNodes()) {
            node.add(buildNodes(child));
        }
        return node;
    }

    protected JComponent createCenterPanel() {
        root = buildNodes(treeData);
        if (checkPolicy != null) {
            myTree = new CheckboxTree(createCellRenderer(), root, checkPolicy);
        } else {
            myTree = new CheckboxTree(createCellRenderer(), root);
        }

        myTree.setRootVisible(false);
        for (int i = 0; i < myTree.getRowCount(); i++) {
            myTree.expandRow(i);
        }
        UIUtil.setLineStyleAngled(myTree);

        final JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(myTree);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        myTree.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent e) {
                if (e.isControlDown() && KeyEvent.VK_ENTER == e.getKeyCode()) {
                    doOKAction();
                }
            }
        });

        return scrollPane;
    }

    private CheckboxTree.CheckboxTreeCellRenderer createCellRenderer() {
        return new CheckboxTree.CheckboxTreeCellRenderer(false, false) {
            @Override
            public void customizeRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus);
                if (getCheckbox().isVisible() && value instanceof CheckedTreeNode) {
                    TreeData userObject = (TreeData) ((CheckedTreeNode) value).getUserObject();
                    getCheckbox().setText(userObject.getNodeName());
                }
            }
        };
    }

    public void showDialog() {
        show();
    }

    public CheckedTreeNode getResult() {
        return root;
    }

    protected String getDimensionServiceKey() {
        return "#com.shaubert.idea.android.toolbox.MultichoiceTreeDialog";
    }

    public JComponent getPreferredFocusedComponent() {
        return myTree;
    }
}