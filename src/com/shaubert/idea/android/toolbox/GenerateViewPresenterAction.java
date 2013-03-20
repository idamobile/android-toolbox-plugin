package com.shaubert.idea.android.toolbox;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;


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
        try {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            VirtualFile layoutFile = getSelectedLayoutFile(e);
            CodeGenerationPattern pattern = selectCodeGenerationPattern(project, layoutFile);
            PsiPackage selectedPackage = selectDestinationPackage(project);
            PsiDirectory resultDirectory = getPsiDirectoryFromPackage(project, selectedPackage);
            String fileName = selectJavaClassName(project, layoutFile);
            throwIfFileAlreadyExists(project, resultDirectory, fileName);
            String className = extractClassName(fileName);
            String classBody = pattern.generateOutput(layoutFile, selectedPackage.getQualifiedName() + "." + className);
            saveBodyToFile(project, resultDirectory, fileName, classBody);
        } catch (CancellationException ignored) {
        }
    }

    private void throwIfFileAlreadyExists(Project project, PsiDirectory resultDirectory, String fileName) {
        for (PsiFile file : resultDirectory.getFiles()) {
            if (file.getName().equalsIgnoreCase(fileName)) {
                Messages.showErrorDialog(project, "File \"" + fileName + "\" already exists", "Failed To Create File");
                throw new CancellationException();
            }
        }
    }

    private VirtualFile getSelectedLayoutFile(AnActionEvent e) {
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if (files != null && files.length > 0) {
            return files[0];
        } else {
            throw new CancellationException();
        }
    }

    private void saveBodyToFile(final Project project, final PsiDirectory resultDirectory,
                                final String fileName, final String classBody) {
        if (resultDirectory != null) {
            if (classBody != null) {
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PsiFile file = PsiFileFactory.getInstance(project).createFileFromText(
                                fileName, JavaFileType.INSTANCE, classBody);
                        PsiFile added = (PsiFile) resultDirectory.add(file);
                        added.navigate(true);
                    }
                });
            }
        }
    }

    private PsiDirectory getPsiDirectoryFromPackage(Project project, PsiPackage selectedPackage) {
        PsiDirectory[] directories = PackageUtil.getDirectories(selectedPackage, project, null, false);
        if (directories.length > 1) {
            String[] dirs = new String[directories.length];
            for (int i = 0; i < directories.length; i++) {
                dirs[i] = directories[i].getVirtualFile().getPath();
            }
            int index = Messages.showChooseDialog(project, "Package referenced to several folders. Select result destination",
                    "Directory Selection",
                    Messages.getQuestionIcon(),
                    dirs,
                    dirs[0]);
            if (index > 0) {
                return directories[index];
            }
        } else if (directories.length == 1) {
            return directories[0];
        }
        throw new CancellationException();
    }

    private String extractClassName(String fileName) {
        int index = fileName.lastIndexOf(".java");
        if (index > 0) {
            return fileName.substring(0, index);
        } else {
            throw new CancellationException();
        }
    }

    private String selectJavaClassName(Project project, VirtualFile layoutFile) {
        String layoutFileName = layoutFile.getName();
        int index = layoutFileName.lastIndexOf(".xml");
        if (index > 0) {
            layoutFileName = layoutFileName.substring(0, index);
        }
        String className = ClassNameHelper.formatCamelCaseFromUnderscore(layoutFileName);
        className = ClassNameHelper.upperCaseLetter(className, 0);
        String fileName = Messages.showInputDialog(project, "Input class name", "Creating File",
                Messages.getQuestionIcon(), className, null);
        if (fileName == null || fileName.length() == 0) {
            throw new CancellationException();
        }
        if (!fileName.contains(".java")) {
            fileName += ".java";
        }
        return fileName;
    }

    private PsiPackage selectDestinationPackage(Project project) {
        PackageChooserDialog packageChooserDialog = new PackageChooserDialog("Destination Package", project);
        if (lastSelectedPackage != null) {
            packageChooserDialog.selectPackage(lastSelectedPackage.getQualifiedName());
        }
        if (packageChooserDialog.showAndGet()) {
            return lastSelectedPackage = packageChooserDialog.getSelectedPackage();
        } else {
            throw new CancellationException();
        }
    }

    private CodeGenerationPattern selectCodeGenerationPattern(Project project, VirtualFile first) {
        int index = Messages.showChooseDialog(project, "Choose view code generation style for " + first.getName(),
                "Generate View Code",
                Messages.getQuestionIcon(),
                patternNames,
                patternNames[0]);
        if (index >= 0) {
            return generationPatterns[index];
        } else {
            throw new CancellationException();
        }
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
    }
}
