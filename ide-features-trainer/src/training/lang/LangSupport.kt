package training.lang

import com.intellij.lang.LanguageExtensionPoint
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import training.learn.Lesson

/**
 * @author Sergey Karashevich
 */
interface LangSupport {

    val FILE_EXTENSION: String

    companion object {
        val EP_NAME = "training.TrainingLangExtension"
    }

    fun acceptExtension(ext: String): Boolean
    fun applyProjectSdk(): (Project) -> Unit
    fun applyToProjectAfterConfigure(): (Project) -> Unit

}