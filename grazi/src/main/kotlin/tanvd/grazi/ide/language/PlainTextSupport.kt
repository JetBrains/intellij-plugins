package tanvd.grazi.ide.language


import com.intellij.psi.PsiFile
import com.intellij.psi.PsiPlainText
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.ide.language.utils.CustomTokensChecker

class PlainTextSupport : LanguageSupport {
    override fun extract(file: PsiFile): List<LanguageSupport.Result>? {
        return CustomTokensChecker.default.check(PsiTreeUtil.collectElementsOfType(file, PsiPlainText::class.java).toList())
    }
}
