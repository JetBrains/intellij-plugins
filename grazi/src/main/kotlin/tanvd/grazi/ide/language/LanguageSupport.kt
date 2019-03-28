package tanvd.grazi.ide.language

import com.intellij.openapi.extensions.Extensions
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.GraziInspection.Companion.EP_NAME

interface LanguageSupport {
    companion object {
        val all: Set<LanguageSupport>
            get() = Extensions.getExtensions(EP_NAME).toSet()
    }

    data class Result(val typo: Typo, val element: PsiElement)

    /** Don't forget to use ProgressManager.checkCancelled() */
    fun extract(file: PsiFile): List<Result>?
}
