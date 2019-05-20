package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.wm.ToolWindowAnchor
import training.learn.exceptons.InvalidSdkException
import training.learn.exceptons.NoSdkException

/**
 * @author Sergey Karashevich
 */
interface LangSupport {

    val primaryLanguage: String
    val defaultProjectName: String
    val filename: String
        get() = "Learning"

    companion object {
        val EP_NAME = "training.TrainingLangExtension"
    }

    /**
     * Implement that method to define SDK lookup depending on a given project.
     * 
     * @return an SDK instance which (existing or newly created) should be applied to the project given. Return `null`
     * if no SDK is okay for this project.
     * 
     * @throws NoSdkException in the case no valid SDK is available, yet it's required for the given project
     */
    @Throws(NoSdkException::class)
    fun getSdkForProject(project: Project): Sdk?
    
    fun applyProjectSdk(sdk: Sdk, project: Project)
    
    fun applyToProjectAfterConfigure(): (Project) -> Unit

    /**
     * <p> Implement that method to require some checks for the SDK in the learning project.
     * The easiest check is to check the presence and SdkType, but other checks like language level
     * might be applied as well.
     * 
     * <p> The method should not return anything, but might throw exceptions subclassing InvalidSdkException which
     * will be handled by a UI.
     */
    @Throws(InvalidSdkException::class)
    fun checkSdk(sdk: Sdk?, project: Project)
    
    fun getProjectFilePath(projectName: String): String
    fun getToolWindowAnchor(): ToolWindowAnchor

    //let's replace with importOrOpenLearnProject()
    fun importLearnProject(): Project?

    fun createProject(projectName: String, projectToClose: Project?): Project?

    /** This method is called from ProjectLifecycleListener and the projcet could be initialized only partly */
    fun setProjectListeners(project: Project) {}
}
