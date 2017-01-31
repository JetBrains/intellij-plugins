package training.lang

import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ex.ProjectRootManagerEx
import training.learn.Lesson

/**
 * @author Sergey Karashevich
 */
class PythonLangSupport: LangSupport {

    private val acceptableExtensions = setOf("py", "html")

    override fun acceptExtension(ext: String) = acceptableExtensions.contains(ext)

    override val FILE_EXTENSION: String
        get() = "py"

    override fun applyProjectSdk(): (Project) -> Unit = { project ->
        val rootManager = ProjectRootManagerEx.getInstanceEx(project)
//        rootManager.projectSdk = sdk

    }

    override fun applyToProjectAfterConfigure(): (Project) -> Unit = {}

}