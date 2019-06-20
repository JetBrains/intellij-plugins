package tanvd.grazi.ide.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection

abstract class LanguageElementSupport(private val disabledRules: Set<String> = emptySet()) {
    companion object {
        val all: Set<LanguageElementSupport>
            get() = GraziInspection.EP_ELEMENT_NAME.extensionList.toSet()
    }

    open fun isSupported(language: Language): Boolean = true

    open fun isRelevant(element: PsiElement): Boolean = true

    fun getFixes(element: PsiElement): Set<Typo> = check(element)
            .filterNot { it.info.rule.id in disabledRules }
            .filter { it.location.element?.let { gotElement -> gotElement == element || element.isAncestor(gotElement) } ?: false }.toSet()

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(element: PsiElement): Set<Typo>
}
