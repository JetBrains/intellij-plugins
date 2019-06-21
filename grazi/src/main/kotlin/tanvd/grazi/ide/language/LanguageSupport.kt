package tanvd.grazi.ide.language

import com.intellij.lang.Language
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection

abstract class LanguageSupport(private val disabledRules: Set<String> = emptySet()) {
    companion object {
        private val LANGUAGE_SUPPORT_EP_NAME = ExtensionPointName.create<LanguageSupport>("tanvd.grazi.languageSupport")

        val all: Set<LanguageSupport>
            get() = LANGUAGE_SUPPORT_EP_NAME.extensionList.toSet()
    }

    open fun isSupported(language: Language): Boolean = true

    open fun isRelevant(element: PsiElement): Boolean = true

    fun getFixes(element: PsiElement): Set<Typo> = check(element)
            .filterNot { it.info.rule.id in disabledRules }
            .filter { it.location.element?.let { gotElement -> gotElement == element || element.isAncestor(gotElement) } ?: false }.toSet()

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(element: PsiElement): Set<Typo>
}
