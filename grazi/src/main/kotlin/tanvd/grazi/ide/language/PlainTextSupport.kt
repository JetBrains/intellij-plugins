package tanvd.grazi.ide.language


import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.grammar.SanitizingGrammarChecker

class PlainTextSupport : LanguageSupport {
    override fun isSupport(file: PsiFile): Boolean {
        return file is PsiPlainTextFile
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        return SanitizingGrammarChecker.default.check(PsiTreeUtil.collectElementsOfType(file, PsiPlainText::class.java).toList()).toList()
    }
}
