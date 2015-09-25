package training.lesson;

import com.intellij.ide.highlighter.ProjectFileType;
import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.ide.projectWizard.NewProjectWizard;
import com.intellij.ide.util.newProjectWizard.AbstractProjectWizard;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.components.StorageScheme;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.roots.ui.configuration.ModulesConfigurator;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.wm.*;
import com.intellij.openapi.wm.ex.IdeFrameEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

/**
 * Created by karashevich on 24/09/15.
 */
public class NewEduProjectUtil {

    public NewEduProjectUtil() {
    }

    public static Project createEduProject(@NotNull String projectName, @Nullable Project projectToClose) throws IOException {
        final ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();

        NewProjectWizard wizardDefaultSettings = new NewProjectWizard(null, ModulesProvider.EMPTY_MODULES_PROVIDER, null);
//        final String allProjectsDir = ProjectUtil.getBaseDir();
        final String allProjectsDir = "/Users/jetbrains/IdeaProjects";
//        FileUtil.ensureExists();

        final ProjectBuilder projectBuilder = new JavaModuleBuilder();

        try {
            String projectFilePath = allProjectsDir + //IdeaProjects dir
                    File.separator + projectName;//Project dir
            File projectDir = new File(projectFilePath).getParentFile();        //dir where project located
            FileUtil.ensureExists(projectDir);
            if (StorageScheme.DIRECTORY_BASED == wizardDefaultSettings.getStorageScheme()) {
                final File ideaDir = new File(projectFilePath, Project.DIRECTORY_STORE_FOLDER);
                FileUtil.ensureExists(ideaDir);
            }

            final Project newProject;

            if (!projectBuilder.isUpdate()) {
                String name = wizardDefaultSettings.getProjectName();
                newProject = projectBuilder.createProject(name, projectFilePath);
            }
            else {
                newProject = projectToClose;
            }

            if (newProject == null) return projectToClose;

            final Sdk jdk = wizardDefaultSettings.getNewProjectJdk();

            if (jdk != null) {
                CommandProcessor.getInstance().executeCommand(newProject, new Runnable() {
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(new Runnable() {
                            public void run() {
                                NewProjectUtil.applyJdkToProject(newProject, jdk);
                            }
                        });
                    }
                }, null, null);
            }

            final String compileOutput = wizardDefaultSettings.getNewCompileOutput();
            CommandProcessor.getInstance().executeCommand(newProject, new Runnable() {
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        public void run() {
                            String canonicalPath = compileOutput;
                            try {
                                canonicalPath = FileUtil.resolveShortWindowsName(compileOutput);
                            }
                            catch (IOException e) {
                                //file doesn't exist
                            }
                            canonicalPath = FileUtil.toSystemIndependentName(canonicalPath);
                            CompilerProjectExtension.getInstance(newProject).setCompilerOutputUrl(VfsUtilCore.pathToUrl(canonicalPath));
                        }
                    });
                }
            }, null, null);

            if (!ApplicationManager.getApplication().isUnitTestMode()) {
                newProject.save();
            }

            if (!projectBuilder.validate(projectToClose, newProject)) {
                return projectToClose;
            }

            if (newProject != projectToClose && !ApplicationManager.getApplication().isUnitTestMode()) {
                NewProjectUtil.closePreviousProject(projectToClose);
            }

            projectBuilder.commit(newProject, null, ModulesProvider.EMPTY_MODULES_PROVIDER);

            final boolean need2OpenProjectStructure = projectBuilder.isOpenProjectSettingsAfter();
            StartupManager.getInstance(newProject).registerPostStartupActivity(new Runnable() {
                public void run() {
                    // ensure the dialog is shown after all startup activities are done
                    //noinspection SSBasedInspection
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            if (newProject.isDisposed() || ApplicationManager.getApplication().isUnitTestMode()) return;
                            if (need2OpenProjectStructure) {
                                ModulesConfigurator.showDialog(newProject, null, null);
                            }
                            ApplicationManager.getApplication().invokeLater(new Runnable() {
                                public void run() {
                                    if (newProject.isDisposed()) return;
                                    final ToolWindow toolWindow = ToolWindowManager.getInstance(newProject).getToolWindow(ToolWindowId.PROJECT_VIEW);
                                    if (toolWindow != null) {
                                        toolWindow.activate(null);
                                    }
                                }
                            }, ModalityState.NON_MODAL);
                        }
                    });
                }
            });

            if (newProject != projectToClose) {
                ProjectUtil.updateLastProjectLocation(projectFilePath);

                if (WindowManager.getInstance().isFullScreenSupportedInCurrentOS()) {
                    IdeFocusManager instance = IdeFocusManager.findInstance();
                    IdeFrame lastFocusedFrame = instance.getLastFocusedFrame();
                    if (lastFocusedFrame instanceof IdeFrameEx) {
                        boolean fullScreen = ((IdeFrameEx)lastFocusedFrame).isInFullScreen();
                        if (fullScreen) {
                            newProject.putUserData(IdeFrameImpl.SHOULD_OPEN_IN_FULL_SCREEN, Boolean.TRUE);
                        }
                    }
                }

                projectManager.openProject(newProject);
            }
            if (!ApplicationManager.getApplication().isUnitTestMode()) {
                newProject.save();
            }
            return newProject;
        }
        finally {
            projectBuilder.cleanup();
        }
    }

}
