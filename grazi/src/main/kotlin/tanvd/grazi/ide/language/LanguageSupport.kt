package tanvd.grazi.ide.language

import com.intellij.lang.LanguageExtension
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.isInjectedFragment

abstract class LanguageSupport(private val disabledRules: Set<String> = emptySet()) : LanguageExtensionPoint<LanguageSupport>() {
    companion object : LanguageExtension<LanguageSupport>("tanvd.grazi.languageSupport")

    open fun isRelevant(element: PsiElement): Boolean = true

    fun getTypos(element: PsiElement): Set<Typo> = check(element)
            .asSequence()
            .filterNot { it.info.rule.id in disabledRules }
            .filter {
                it.location.element?.let { gotElement ->
                    !gotElement.isInjectedFragment() && (gotElement == element || element.isAncestor(gotElement))
                } ?: false
            }.toSet()

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(element: PsiElement): Set<Typo>
}
