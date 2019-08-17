package tanvd.grazi.ide.language.java

import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport

class JStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiLiteralExpressionImpl && (element.literalElementType == JavaTokenType.STRING_LITERAL && element.innerText != null)
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PsiLiteralExpressionImpl) { "Got non PsiLiteralExpressionImpl in JStringSupport" }

        return when (element.literalElementType) {
            JavaTokenType.STRING_LITERAL -> GrammarChecker.default.check(element).toSet()
            else -> emptySet()
        }
    }
}
