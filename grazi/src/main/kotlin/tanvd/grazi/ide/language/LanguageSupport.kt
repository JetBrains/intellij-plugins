package tanvd.grazi.ide.language

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import tanvd.grazi.model.Typo

interface LanguageSupport {
    data class Result(val typo: Typo, val element: PsiElement)

    /** Don't forget to use ProgressManager.checkCancelled() */
    fun extract(file: PsiFile): List<Result>?
}
