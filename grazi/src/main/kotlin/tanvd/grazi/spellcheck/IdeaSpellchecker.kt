package tanvd.grazi.spellcheck

import com.intellij.openapi.project.Project
import com.intellij.spellchecker.SpellCheckerManager
import org.jetbrains.annotations.TestOnly

object IdeaSpellchecker {
    private var hasProblemChecker = ThreadLocal<(String) -> Boolean>()

    fun init(project: Project) {
        val manager = SpellCheckerManager.getInstance(project)
        hasProblemChecker.set { manager.hasProblem(it) }
    }

    @TestOnly
    fun init(hasProblem: (String) -> Boolean) {
        hasProblemChecker.set { hasProblem(it) }
    }

    fun hasProblem(word: String) = hasProblemChecker.get()(word)
}
