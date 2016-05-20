package training.learn;

import com.intellij.ide.impl.NewProjectUtil;
import com.intellij.ide.impl.ProjectUtil;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ProjectBuilder;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ex.ProjectManagerEx;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.*;
import com.intellij.openapi.wm.ex.IdeFrameEx;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import training.learn.dialogs.LearnProjectWarningDialog;

import java.io.File;
import java.io.IOException;

/**
 * Created by karashevich on 24/09/15.
 */
public class NewLearnProjectUtil {

    public static Project createLearnProject(@NotNull String projectName, @Nullable Project projectToClose, @Nullable final Sdk projectSdk) throws IOException {
        final ProjectManagerEx projectManager = ProjectManagerEx.getInstanceEx();

        String allProjectsDir = "/Users/jetbrains/IdeaProjects";
        allProjectsDir = ProjectUtil.getBaseDir();
        final ProjectBuilder projectBuilder = new JavaModuleBuilder();

        try {
            String projectFilePath = allProjectsDir + //IdeaProjects dir
                    File.separator + projectName;//Project dir
            File projectDir = new File(projectFilePath).getParentFile();        //dir where project located
            FileUtil.ensureExists(projectDir);
            final File ideaDir = new File(projectFilePath, Project.DIRECTORY_STORE_FOLDER);
            FileUtil.ensureExists(ideaDir);

            final Project newProject;

            if (!projectBuilder.isUpdate()) {
                newProject = projectBuilder.createProject(projectName, projectFilePath);
            } else {
                newProject = projectToClose;
            }

            if (newProject == null) return projectToClose;


            if (projectSdk != null) {
                CommandProcessor.getInstance().executeCommand(newProject, () -> ApplicationManager.getApplication().runWriteAction(() -> {
                    NewProjectUtil.applyJdkToProject(newProject, projectSdk);
                }), null, null);
            }

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

            if (newProject != projectToClose) {
                ProjectUtil.updateLastProjectLocation(projectFilePath);

                if (WindowManager.getInstance().isFullScreenSupportedInCurrentOS()) {
                    IdeFocusManager instance = IdeFocusManager.findInstance();
                    IdeFrame lastFocusedFrame = instance.getLastFocusedFrame();
                    if (lastFocusedFrame instanceof IdeFrameEx) {
                        boolean fullScreen = ((IdeFrameEx) lastFocusedFrame).isInFullScreen();
                        if (fullScreen) {
                            newProject.putUserData(IdeFrameImpl.SHOULD_OPEN_IN_FULL_SCREEN, Boolean.TRUE);
                        }
                    }
                }
                if (ApplicationManager.getApplication().isUnitTestMode()) return newProject;
                else projectManager.openProject(newProject);
            }

            newProject.save();

            return newProject;
        } finally {
            projectBuilder.cleanup();
        }
    }

    public static boolean showDialogOpenLearnProject(Project project){
        //        final SdkProblemDialog dialog = new SdkProblemDialog(project, "at least JDK 1.6 or IDEA SDK with corresponding JDK");
        final LearnProjectWarningDialog dialog = new LearnProjectWarningDialog(project);
        dialog.show();
        return dialog.isOK();
    }


}
