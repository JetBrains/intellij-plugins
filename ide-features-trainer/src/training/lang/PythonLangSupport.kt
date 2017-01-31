package training.lang

import com.intellij.openapi.project.Project
import training.learn.Lesson

/**
 * @author Sergey Karashevich
 */
class PythonLangSupport: LangSupport {


    private val acceptableExtensions = setOf("py", "html")

    override fun acceptExtension(ext: String) = acceptableExtensions.contains(ext)

    override val FILE_EXTENSION: String
        get() = "py"

    override fun getLangName() = "Python"

    override fun applyProjectSdk(): (Project) -> Unit = {}

    override fun initLearnProject(project: Project?): Project {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun openLessonInFile(lesson: Lesson) {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}