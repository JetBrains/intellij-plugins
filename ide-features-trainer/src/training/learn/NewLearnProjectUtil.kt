package training.learn

import com.intellij.ide.impl.NewProjectUtil
import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.IdeFrameEx
import com.intellij.openapi.wm.impl.IdeFrameImpl
import training.learn.dialogs.LearnProjectWarningDialog
import java.io.File
import java.io.IOException

/**
 * Created by karashevich on 24/09/15.
 */
object NewLearnProjectUtil {

    @Throws(IOException::class)
    fun createLearnProject(projectName: String, projectToClose: Project?, applyProjectSdk: (Project) -> Unit): Project? {
        val projectManager = ProjectManagerEx.getInstanceEx()
        val allProjectsDir = ProjectUtil.getBaseDir()
        val projectBuilder = JavaModuleBuilder()

        try {
            val projectFilePath = allProjectsDir + File.separator + projectName //Project dir
            val projectDir = File(projectFilePath).parentFile        //dir where project located
            FileUtil.ensureExists(projectDir)

            val ideaDir = File(projectFilePath, Project.DIRECTORY_STORE_FOLDER)
            FileUtil.ensureExists(ideaDir)

            val newProject: Project? =
                    if (!projectBuilder.isUpdate) projectBuilder.createProject(projectName, projectFilePath)
                    else projectToClose

            if (newProject == null) return projectToClose!!

            applyProjectSdk(newProject)

            if (!ApplicationManager.getApplication().isUnitTestMode) {
                newProject.save()
            }

            if (!projectBuilder.validate(projectToClose, newProject)) {
                return projectToClose
            }

            //close previous project if needed
            if (newProject !== projectToClose && !ApplicationManager.getApplication().isUnitTestMode && projectToClose != null)
                NewProjectUtil.closePreviousProject(projectToClose)

            projectBuilder.commit(newProject, null, ModulesProvider.EMPTY_MODULES_PROVIDER)

            if (newProject !== projectToClose) {
                ProjectUtil.updateLastProjectLocation(projectFilePath)

                if (WindowManager.getInstance().isFullScreenSupportedInCurrentOS) {
                    val instance = IdeFocusManager.findInstance()
                    val lastFocusedFrame = instance.lastFocusedFrame
                    if (lastFocusedFrame is IdeFrameEx) {
                        val fullScreen = lastFocusedFrame.isInFullScreen
                        if (fullScreen) {
                            newProject.putUserData(IdeFrameImpl.SHOULD_OPEN_IN_FULL_SCREEN, java.lang.Boolean.TRUE)
                        }
                    }
                }
                if (ApplicationManager.getApplication().isUnitTestMode)
                    return newProject
                else
                    projectManager.openProject(newProject)
            }

            newProject.save()

            return newProject
        } finally {
            projectBuilder.cleanup()
        }
    }

    fun showDialogOpenLearnProject(project: Project): Boolean {
        //        final SdkProblemDialog dialog = new SdkProblemDialog(project, "at least JDK 1.6 or IDEA SDK with corresponding JDK");
        val dialog = LearnProjectWarningDialog(project)
        dialog.show()
        return dialog.isOK
    }


}
