package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.learn.exceptons.InvalidSdkException

/**
 * @author Sergey Karashevich
 */
interface LangSupport {

    val FILE_EXTENSION: String
    val defaultProjectName: String
    companion object {
        val EP_NAME = "training.TrainingLangExtension"
    }

    fun acceptLang(ext: String): Boolean
    fun applyProjectSdk(project: Project): Unit
    fun applyToProjectAfterConfigure(): (Project) -> Unit

    /**
     * Implement that method to require some checks for the SDK in the learning project.
     * The easiest check is to check the presence and SdkType, but other checks like language level
     * might be applied as well.
     * 
     * The method should not return anything, but might throw exceptions subclassing InvalidSdkException which
     * will be handled by a UI.
     */
    @Throws(InvalidSdkException::class)
    fun checkSdk(sdk: Sdk?)
    
    fun getProjectFilePath(projectName: String): String
    fun getToolWindowAnchor(): ToolWindowAnchor

    //let's replace with importOrOpenLearnProject()
    fun importLearnProject(): Project?

    fun createProject(projectName: String, projectToClose: Project?): Project?
}
