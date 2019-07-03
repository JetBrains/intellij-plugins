package tanvd.grazi.ide.language.plain


import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPlainText
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class PlainTextSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiPlainText && element.containingFile.name.endsWith(".txt")
    }

    override fun check(element: PsiElement): Set<Typo> {
        return GrammarChecker.default.check(element)
    }
}
