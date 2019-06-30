package tanvd.grazi.spellcheck

import com.intellij.lang.Language
import com.intellij.lang.LanguageNamesValidation
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.SpellCheckerManager
import com.intellij.spellchecker.tokenizer.LanguageSpellchecking
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy

object IdeaSpellchecker {
    fun getSpellcheckingStrategy(element: PsiElement): SpellcheckingStrategy? {
        for (strategy in LanguageSpellchecking.INSTANCE.allForLanguage(element.language)) {
            if (strategy.isMyContext(element)) return strategy
        }
        return null
    }

    fun hasProblem(word: String, project: Project, language: Language): Boolean {
        val spellchecker = SpellCheckerManager.getInstance(project)
        val keyworder = LanguageNamesValidation.INSTANCE.forLanguage(language)
        return !keyworder.isKeyword(word, project) && spellchecker.hasProblem(word)

    }
}
