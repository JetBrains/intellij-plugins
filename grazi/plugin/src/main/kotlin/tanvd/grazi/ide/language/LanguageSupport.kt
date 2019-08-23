package tanvd.grazi.ide.language

import com.intellij.lang.LanguageExtension
import com.intellij.lang.LanguageExtensionPoint
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil.isAncestor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.isInjectedFragment

abstract class LanguageSupport(private val disabledRules: Set<String> = emptySet()) : LanguageExtensionPoint<LanguageSupport>() {
    companion object : LanguageExtension<LanguageSupport>("tanvd.grazi.languageSupport")

    abstract fun isRelevant(element: PsiElement): Boolean

    fun getTypos(element: PsiElement): Set<Typo> {
        require(isRelevant(element)) { "Got not relevant element in LanguageSupport" }
        return check(element)
                .asSequence()
                .filterNot { it.info.rule.id in disabledRules }
                .filter {
                    it.location.element?.let { gotElement -> isAncestor(element, gotElement, false) } ?: false
                }.toSet()
    }

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(element: PsiElement): Set<Typo>
}
