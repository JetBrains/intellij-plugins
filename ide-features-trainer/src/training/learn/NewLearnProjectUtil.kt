package training.learn

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.IdeFrameEx
import com.intellij.openapi.wm.impl.IdeFrameImpl
import training.lang.LangSupport
import training.learn.dialogs.LearnProjectWarningDialog
import java.io.File
import java.lang.Boolean.TRUE

/**
 * Created by karashevich on 24/09/15.
 */
object NewLearnProjectUtil {

  fun createLearnProject(projectToClose: Project?, langSupport: LangSupport): Project? {
    val unitTestMode = ApplicationManager.getApplication().isUnitTestMode
    val projectManager = ProjectManagerEx.getInstanceEx()

    val allProjectsDir = ProjectUtil.getBaseDir()
    val projectName = langSupport.defaultProjectName
    val projectFilePath = allProjectsDir / projectName
//    val projectDir = File(projectFilePath).parentFile
//    FileUtil.ensureExists(projectDir)
//    val ideaDir = File(projectFilePath, Project.DIRECTORY_STORE_FOLDER)
//    FileUtil.ensureExists(ideaDir)

    val newProject: Project =
        langSupport.createProject(projectName, projectToClose) ?: return projectToClose

    langSupport.applyProjectSdk(newProject)

    if (!unitTestMode) newProject.save()

    //close previous project if needed
    if (newProject !== projectToClose && !unitTestMode && projectToClose != null) {
      ProjectUtil.closeAndDispose(projectToClose)
    }

    if (newProject !== projectToClose) {
      ProjectUtil.updateLastProjectLocation(projectFilePath)
      if (WindowManager.getInstance().isFullScreenSupportedInCurrentOS) {
        val lastFocusedFrame = IdeFocusManager.findInstance().lastFocusedFrame
        if (lastFocusedFrame is IdeFrameEx) {
          val fullScreen = lastFocusedFrame.isInFullScreen
          if (fullScreen) newProject.putUserData(IdeFrameImpl.SHOULD_OPEN_IN_FULL_SCREEN, TRUE)
        }
      }
      if (unitTestMode) return newProject
      else projectManager.openProject(newProject)
    }

    newProject.save()
    return newProject

  }

  fun showDialogOpenLearnProject(project: Project): Boolean {
    //        final SdkProblemDialog dialog = new SdkProblemDialog(project, "at least JDK 1.6 or IDEA SDK with corresponding JDK");
    val dialog = LearnProjectWarningDialog(project)
    dialog.show()
    return dialog.isOK
  }
}

//overload div operator as a path separator
private operator fun String.div(path: String): String? {
  return this + File.separatorChar + path
}
