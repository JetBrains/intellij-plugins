package tanvd.grazi.ide.language

import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection.Companion.EP_NAME

interface LanguageSupport {
    companion object {
        val all: Set<LanguageSupport>
            get() = EP_NAME.extensionList.toSet()
    }

    fun isSupported(file: PsiFile): Boolean = true

    /** Don't forget to use ProgressManager.checkCancelled() */
    fun check(file: PsiFile): Set<Typo>
}
