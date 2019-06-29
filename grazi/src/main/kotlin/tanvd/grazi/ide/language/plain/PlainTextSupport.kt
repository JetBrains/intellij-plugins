package tanvd.grazi.ide.language.plain


import com.intellij.lang.Language
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.psi.*
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor

class PlainTextSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is PlainTextLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiPlainTextFile && element.name.endsWith(".txt")
    }

    override fun check(element: PsiElement): Set<Typo> {
        return GrammarChecker.default.check(element.filterFor<PsiPlainText>())
    }
}
