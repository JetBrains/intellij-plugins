package tanvd.grazi.ide.language

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection.Companion.EP_NAME

interface LanguageSupport {
    companion object {
        val all: Set<LanguageSupport>
            get() = EP_NAME.extensionList.toSet()
    }

    data class Result(val typo: Typo, val element: PsiElement)

    /** Don't forget to use ProgressManager.checkCancelled() */
    fun extract(file: PsiFile): List<Result>?
}
