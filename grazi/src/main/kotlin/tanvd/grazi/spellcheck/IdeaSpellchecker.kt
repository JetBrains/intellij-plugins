package tanvd.grazi.spellcheck

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.SpellCheckerManager
import com.intellij.spellchecker.tokenizer.LanguageSpellchecking
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy

object IdeaSpellchecker {
    private var hasProblemChecker = ThreadLocal.withInitial<(String) -> Boolean> { { true } }

    fun init(project: Project) {
        val manager = SpellCheckerManager.getInstance(project)
        hasProblemChecker.set { manager.hasProblem(it) }
    }


    fun getSpellcheckingStrategy(element: PsiElement): SpellcheckingStrategy? {
        for (strategy in LanguageSpellchecking.INSTANCE.allForLanguage(element.language)) {
            if (strategy.isMyContext(element)) return strategy
        }
        return null
    }

    fun hasProblem(word: String) = hasProblemChecker.get()(word)
}
