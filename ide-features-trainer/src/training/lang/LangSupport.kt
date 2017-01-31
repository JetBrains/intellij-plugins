package training.lang

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import training.learn.Lesson

/**
 * @author Sergey Karashevich
 */
interface LangSupport {

    val FILE_EXTENSION: String

    companion object {
        val LANG_SUPPORT_EP_NAME = ExtensionPointName.create<LangSupport>("training.TrainingLangExtension")
    }

    fun getLangName(): String
    fun openLessonInFile(lesson: Lesson)
    fun initLearnProject(project: Project?): Project
    fun acceptExtension(ext: String): Boolean
    fun applyProjectSdk(): (Project) -> Unit

}