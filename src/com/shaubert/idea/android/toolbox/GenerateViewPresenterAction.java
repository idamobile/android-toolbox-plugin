package com.shaubert.idea.android.toolbox;

import com.intellij.ide.projectView.impl.nodes.PackageUtil;
import com.intellij.ide.util.PackageChooserDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
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
        Project project = e.getData(PlatformDataKeys.PROJECT);
        try {
            if (project == null) {
                throw new CancellationException("Unable to retrieve project");
            }
            VirtualFile layoutFile = getSelectedLayoutFile(e);
            Module module = getModuleOfFile(project, layoutFile);
            AndroidManifest androidManifest = getAndroidManifest(module);
            AndroidView androidView = getAndroidViews(layoutFile);

            CodeGenerationPattern pattern = selectCodeGenerationPattern(project, layoutFile);
            PsiPackage selectedPackage = selectDestinationPackage(module, androidManifest);
            PsiDirectory resultDirectory = getPsiDirectoryFromPackage(selectedPackage);
            String fileName = selectJavaClassName(project, layoutFile);
            throwIfFileAlreadyExists(resultDirectory, fileName);
            String className = extractClassName(fileName);

            String outputClassName = selectedPackage.getQualifiedName() + "." + className;
            PsiClass resultClass = pattern.generateOutput(project, androidManifest, androidView, outputClassName);
            saveClass(resultDirectory, resultClass);
        } catch (CancellationException ignored) {
            if (ignored.getMessage() != null && project != null) {
                Messages.showErrorDialog(project, ignored.getMessage(), "Error");
            }
        }
    }

    private AndroidManifest getAndroidManifest(Module module) {
        VirtualFile manifestFile = getManifestFile(module);
        AndroidManifest androidManifest = new AndroidManifestParser().parse(manifestFile);
        if (androidManifest == null) {
            throw new CancellationException("Failed to read AndroidManifest.xml");
        }
        return androidManifest;
    }

    private VirtualFile getManifestFile(Module module) {
        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        VirtualFile[] contentRoots = moduleRootManager.getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            if (contentRoot.isDirectory()) {
                for (VirtualFile child : contentRoot.getChildren()) {
                    if (!child.isDirectory() && "AndroidManifest.xml".equals(child.getName())) {
                        return child;
                    }
                }
            }
        }
        throw new CancellationException("AndroidManifest.xml not found");
    }

    private AndroidView getAndroidViews(VirtualFile layoutFile) {
        AndroidLayoutParser parser = new AndroidLayoutParser();
        return parser.parse(layoutFile);
    }

    private void throwIfFileAlreadyExists(PsiDirectory resultDirectory, String fileName) {
        for (PsiFile file : resultDirectory.getFiles()) {
            if (file.getName().equalsIgnoreCase(fileName)) {
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
                ApplicationManager.getApplication().runWriteAction(new Runnable() {
                    @Override
                    public void run() {
                        PsiClass added = (PsiClass) resultDirectory.add(resultClass);
                        PsiFile psiFile = added.getNavigationElement().getContainingFile();
                        JavaCodeStyleManager styleManager = JavaCodeStyleManager.getInstance(added.getProject());
                        styleManager.optimizeImports(psiFile);
                        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(added.getProject());
                        PsiClass formatted = (PsiClass) codeStyleManager.reformat(added);
                        formatted.navigate(true);
                    }
                });
            }
        }
    }

    private PsiDirectory getPsiDirectoryFromPackage(PsiPackage selectedPackage) {
        Project project = selectedPackage.getProject();
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
            if (index >= 0) {
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
            throw new CancellationException("Bad java file name: " + fileName);
        }
    }

    private String selectJavaClassName(Project project, VirtualFile layoutFile) {
        String layoutFileName = layoutFile.getName();
        int index = layoutFileName.lastIndexOf(".xml");
        if (index > 0) {
            layoutFileName = layoutFileName.substring(0, index);
        }
        String className = ClassHelper.formatCamelCaseFromUnderscore(layoutFileName);
        className = ClassHelper.upperCaseLetter(className, 0);
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
        public CancellationException() {
        }

        public CancellationException(String message) {
            super(message);
        }
    }
}
