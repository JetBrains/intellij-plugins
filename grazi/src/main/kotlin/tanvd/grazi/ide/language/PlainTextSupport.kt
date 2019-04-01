package tanvd.grazi.ide.language


import com.intellij.psi.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class PlainTextSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiPlainTextFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val plainTexts = file.filterFor<PsiPlainText>()
        addAll(SanitizingGrammarChecker.default.check(plainTexts))
    }
}
