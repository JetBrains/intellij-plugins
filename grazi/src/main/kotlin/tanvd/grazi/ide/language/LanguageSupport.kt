package tanvd.grazi.ide.language

import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection.Companion.EP_NAME

abstract class LanguageSupport(private val disabledRules: Set<String> = emptySet()) {
    companion object {
        val all: Set<LanguageSupport>
            get() = EP_NAME.extensionList.toSet()
    }

    open fun isSupported(file: PsiFile): Boolean = true

    fun getFixes(file: PsiFile): Set<Typo> = check(file).filterNot { it.info.rule.id in disabledRules }.toSet()

    /** Don't forget to use ProgressManager.checkCancelled() */
    protected abstract fun check(file: PsiFile): Set<Typo>
}
