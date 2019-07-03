package tanvd.grazi.ide.language.rust

import com.intellij.psi.PsiElement
import org.rust.lang.core.psi.RsLitExpr
import org.rust.lang.core.psi.ext.stubKind
import org.rust.lang.core.stubs.RsStubLiteralKind
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class RsStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement): Boolean {
        return element is RsLitExpr && element.stubKind is RsStubLiteralKind.String
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is RsLitExpr) { "Got not RsLitExpr in a RsStringSupport" }

        return GrammarChecker.ignoringQuotes.check(element)
    }
}
