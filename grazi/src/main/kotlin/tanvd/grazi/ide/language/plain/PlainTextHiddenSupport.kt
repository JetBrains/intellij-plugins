package tanvd.grazi.ide.language.plain

import com.intellij.psi.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class PlainTextHiddenSupport : LanguageSupport() {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiPlainTextFile && file.name.startsWith(".")
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        addAll(SanitizingGrammarChecker.default.check(file.filterFor<PsiPlainText>()).filter {
            it.info.rule.isDictionaryBasedSpellingRule
        })
    }
}
