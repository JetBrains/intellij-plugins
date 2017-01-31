package training.lang

import com.intellij.openapi.project.Project
import training.learn.Lesson

/**
 * @author Sergey Karashevich
 */
class MockLangSupport(override val FILE_EXTENSION: String) : LangSupport {

    override fun applyToProjectAfterConfigure(): (Project) -> Unit {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun acceptExtension(ext: String): Boolean {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun applyProjectSdk(): (Project) -> Unit {
        throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}