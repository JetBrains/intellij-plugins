package training.learn

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.ProjectManagerEx
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.annotations.SystemDependent
import training.lang.LangSupport
import training.learn.dialogs.LearnProjectWarningDialog
import training.learn.exceptons.NoSdkException

/**
 * Created by karashevich on 24/09/15.
 */
object NewLearnProjectUtil {

  fun createLearnProject(projectToClose: Project?, langSupport: LangSupport): Project {
    val unitTestMode = ApplicationManager.getApplication().isUnitTestMode
    val projectManager = ProjectManagerEx.getInstanceEx()

    val projectFilePath = projectFilePath(langSupport)
//    val projectDir = File(projectFilePath).parentFile
//    FileUtil.ensureExists(projectDir)
//    val ideaDir = File(projectFilePath, Project.DIRECTORY_STORE_FOLDER)
//    FileUtil.ensureExists(ideaDir)

    val newProject: Project =
        langSupport.createProject(langSupport.defaultProjectName, projectToClose) ?: error("Could not create project for " + langSupport.primaryLanguage)

    try {
      val sdkForProject = langSupport.getSdkForProject(newProject)
      if (sdkForProject != null) {
        langSupport.applyProjectSdk(sdkForProject, newProject)
      }
    }
    catch (e: NoSdkException) {
      Messages.showMessageDialog(newProject, e.localizedMessage, LearnBundle.message("dialog.noSdk.title"), Messages.getErrorIcon())
    }

    if (!unitTestMode) newProject.save()

    //close previous project if needed
    if (newProject !== projectToClose && !unitTestMode && projectToClose != null) {
      ProjectUtil.closeAndDispose(projectToClose)
    }
    newProject.save()
    return newProject

  }

  fun projectFilePath(langSupport: LangSupport): @SystemDependent String =
      FileUtil.join(ProjectUtil.getBaseDir(), langSupport.defaultProjectName)

  fun showDialogOpenLearnProject(project: Project): Boolean {
    //        final SdkProblemDialog dialog = new SdkProblemDialog(project, "at least JDK 1.6 or IDEA SDK with corresponding JDK");
    val dialog = LearnProjectWarningDialog(project)
    dialog.show()
    return dialog.isOK
  }
}
