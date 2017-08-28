package training.lang

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.wm.ToolWindowAnchor

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

    fun checkSdkCompatibility(sdk: Sdk, sdkTypeId: SdkTypeId)
    fun needToCheckSDK(): Boolean
    fun getProjectFilePath(projectName: String): String
    fun getToolWindowAnchor(): ToolWindowAnchor

    //let's replace with importOrOpenLearnProject()
    fun importLearnProject(): Project?

    fun createProject(projectName: String, projectToClose: Project?): Project?
    fun getModuleBuilder(): ModuleBuilder?
}
