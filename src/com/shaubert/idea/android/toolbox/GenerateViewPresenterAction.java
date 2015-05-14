package com.shaubert.idea.android.toolbox;

import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.JavaProjectRootsUtil;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.ui.CheckboxTreeBase;
import com.intellij.ui.CheckedTreeNode;

import java.util.ArrayList;
import java.util.List;


public class GenerateViewPresenterAction extends AnAction {

    private final CodeGenerationPattern[] generationPatterns = new CodeGenerationPattern[] {
            new ViewHolderPattern(),
            new ViewPresenterPattern(),
    };

    private final String[] patternNames;
    {
        patternNames = new String[generationPatterns.length];
        for (int i = 0; i < generationPatterns.length; i++) {
            patternNames[i] = generationPatterns[i].getName();
        }
    }

    private PsiPackage lastSelectedPackage;

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        try {
            if (project == null) {
                throw new CancellationException("Unable to retrieve project");
            }
            final VirtualFile layoutFile = getSelectedLayoutFile(e);
            Module module = getModuleOfFile(project, layoutFile);
            final AndroidManifest androidManifest = getAndroidManifest(module, layoutFile);
            AndroidView androidView = getAndroidViews(layoutFile);

            final CodeGenerationPattern pattern = selectCodeGenerationPattern(project, layoutFile);
            final AndroidView filteredViews = selectViews(project, androidView);
            PsiPackage selectedPackage = selectDestinationPackage(module, androidManifest);
            final PsiDirectory resultDirectory = getPsiDirectoryFromPackage(selectedPackage);
            String fileName = selectJavaClassName(project, layoutFile, pattern);
            throwIfFileAlreadyExists(resultDirectory, fileName);
            String className = FileUtil.removeExtension(fileName);

            final String outputClassName = selectedPackage.getQualifiedName() + "." + className;
            pattern.setup(project);
            new WriteCommandAction.Simple(project) {
                @Override
                protected void run() throws Throwable {
                    PsiClass resultClass = pattern.generateOutput(project, androidManifest, filteredViews,
                            layoutFile.getName(), outputClassName);
                    saveClass(resultDirectory, resultClass);
                }
            }.execute();
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                }
            });
        } catch (CancellationException ignored) {
            if (ignored.getMessage() != null && project != null) {
                Messages.showErrorDialog(project, ignored.getMessage(), "Error");
            }
        }
    }

    private AndroidView selectViews(Project project, AndroidView androidView) {
        MultichoiceTreeDialog treeDialog = new MultichoiceTreeDialog(project, "Views To Find", androidView,
                new CheckboxTreeBase.CheckPolicy(false, false, false, false));
        if (treeDialog.showAndGet()) {
            CheckedTreeNode node = treeDialog.getResult();
            AndroidView result = new AndroidView();
            buildTree(result, node);
            return result;
        } else {
            throw new CancellationException();
        }
    }

    private void buildTree(AndroidView parent, CheckedTreeNode node) {
        AndroidView current = parent;
        if (node.isChecked()) {
            AndroidView view = (AndroidView) node.getUserObject();
            if (view.getParent() != null) {
                current = new AndroidView();
                current.setIdValue(view.getIdValue());
                current.setTagName(view.getTagName());
                parent.addSubView(current);
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            buildTree(current, (CheckedTreeNode) node.getChildAt(i));
        }
    }

    private String getOptionNameOfView(AndroidView view) {
        StringBuilder builder = new StringBuilder();
        for (AndroidView p = view.getParent(); p != null; p = p.getParent()) {
            builder.append("  ");
        }
        builder.append(view.getIdValue());
        builder.append(" - ").append(view.getClassName());
        return builder.toString();
    }

    private AndroidManifest getAndroidManifest(Module module, VirtualFile layoutFile) {
        VirtualFile manifestFile = getManifestFile(module, layoutFile);
        AndroidManifest androidManifest = new AndroidManifestParser().parse(manifestFile);
        if (androidManifest == null) {
            throw new CancellationException("Failed to read AndroidManifest.xml");
        }
        return androidManifest;
    }

    private VirtualFile getManifestFile(Module module, VirtualFile layoutFile) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
        VirtualFile result = lookupForManifest(layoutFile.getParent().getParent(), contentRoots);
        if (result == null) {
            throw new CancellationException("AndroidManifest.xml not found");
        }
        return result;
    }

    private VirtualFile lookupForManifest(VirtualFile dir, VirtualFile[] topDirs) {
        //noinspection UnsafeVfsRecursion
        for (VirtualFile file : dir.getChildren()) {
            if (!file.isDirectory() && "AndroidManifest.xml".equals(file.getName())) {
                return file;
            }
        }

        for (VirtualFile topDir : topDirs) {
            if (topDir.equals(dir)) {
                return null;
            }
        }

        VirtualFile parent = dir.getParent();
        if (!dir.isDirectory()) {
            return null;
        }

        return lookupForManifest(parent, topDirs);
    }

    private AndroidView getAndroidViews(VirtualFile layoutFile) {
        AndroidLayoutParser parser = new AndroidLayoutParser();
        return parser.parse(layoutFile);
    }

    private void throwIfFileAlreadyExists(PsiDirectory resultDirectory, String fileName) {
        for (PsiFile file : resultDirectory.getFiles()) {
            String name = file.getName();
            if (name != null && name.equalsIgnoreCase(fileName)) {
                throw new CancellationException("File \"" + fileName + "\" already exists");
            }
        }
    }

    private VirtualFile getSelectedLayoutFile(AnActionEvent e) {
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            throw new CancellationException("Select android layout file");
        }
    }

    private void saveClass(final PsiDirectory resultDirectory, final PsiClass resultClass) {
        if (resultDirectory != null) {
            if (resultClass != null) {
                PsiClass added = (PsiClass) resultDirectory.add(resultClass);
                PsiFile psiFile = added.getNavigationElement().getContainingFile();
                JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(added.getProject());
                styleManager.optimizeImports(psiFile);
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(added.getProject());
                PsiClass formatted = (PsiClass) codeStyleManager.reformat(added);
                formatted.navigate(true);
            }
        }
    }

    private PsiDirectory getPsiDirectoryFromPackage(PsiPackage selectedPackage) {
        PsiDirectory[] allDirectories = PackageUtil.getDirectories(selectedPackage, null, false);
        List<PsiDirectory> directories = new ArrayList<PsiDirectory>(allDirectories.length);
        for (PsiDirectory directory : allDirectories) {
            if (!JavaProjectRootsUtil.isInGeneratedCode(directory.getVirtualFile(), selectedPackage.getProject())) {
                directories.add(directory);
            }
        }

        if (directories.size() > 1) {
            String[] dirs = new String[directories.size()];
            for (int i = 0; i < directories.size(); i++) {
                dirs[i] = directories.get(i).getVirtualFile().getPath();
            }
            ChooseDialog dialog = new ChooseDialog(selectedPackage.getProject(), "Directory Selection",
                    "Package referenced to several folders. Select result destination",
                    dirs,
                    0);
            if (dialog.showAndGet()) {
                int index = dialog.getSelectedIndex();
                if (index >= 0) {
                    return directories.get(index);
                }
            }
        } else if (directories.size() == 1) {
            return directories.get(0);
        }
        throw new CancellationException();
    }

    private String selectJavaClassName(Project project, VirtualFile layoutFile, CodeGenerationPattern pattern) {
        String className = pattern.getSuggestedClassName(layoutFile.getName());
        String fileName = Messages.showInputDialog(project, "Input class name", "Creating File",
                Messages.getQuestionIcon(), className, null);
        if (fileName == null || fileName.length() == 0) {
            throw new CancellationException("Incorrect file name");
        }
        if (!fileName.contains(".java")) {
            fileName += ".java";
        }
        return fileName;
    }

    private PsiPackage selectDestinationPackage(Module module, AndroidManifest manifest) {
        PackageChooserDialog packageChooserDialog = new PackageChooserDialog("Destination Package", module);
        if (lastSelectedPackage != null) {
            packageChooserDialog.selectPackage(lastSelectedPackage.getQualifiedName());
        } else {
            packageChooserDialog.selectPackage(manifest.getPackageName());
        }
        if (packageChooserDialog.showAndGet()) {
            return lastSelectedPackage = packageChooserDialog.getSelectedPackage();
        } else {
            throw new CancellationException();
        }
    }

    private Module getModuleOfFile(Project project, VirtualFile layoutFile) {
        ProjectRootManager rootManager = ProjectRootManager.getInstance(project);
        Module module = rootManager.getFileIndex().getModuleForFile(layoutFile);
        if (module == null) {
            throw new CancellationException("Failed to determine module with selected layout");
        }
        return module;
    }

    private CodeGenerationPattern selectCodeGenerationPattern(Project project, VirtualFile first) {
        ChooseDialog dialog = new ChooseDialog(project, "Generate View Code",
                "Choose view code generation style for " + first.getName(),
                patternNames,
                0);
        if (dialog.showAndGet()) {
            int index = dialog.getSelectedIndex();
            if (index >= 0) {
                return generationPatterns[index];
            }
        }
        throw new CancellationException();
    }

    public void update(AnActionEvent e) {
        super.update(e);
        boolean visible = false;
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if (project != null) {
            VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
            if (files != null && files.length == 1) {
                String path = files[0].getPath();
                visible = path.contains("res/layout") && path.endsWith(".xml");
            }
        }
        e.getPresentation().setVisible(visible);
    }

    public static class CancellationException extends RuntimeException {
        public CancellationException() {
        }

        public CancellationException(String message) {
            super(message);
        }
    }
}
