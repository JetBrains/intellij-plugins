package tanvd.grazi.ide.language.xml

import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlText
import com.intellij.psi.xml.XmlToken
import com.intellij.psi.xml.XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport


class XmlSupport : LanguageSupport() {
    override fun isRelevant(element: PsiElement) = element is XmlText || (element is XmlToken && element.tokenType == XML_ATTRIBUTE_VALUE_TOKEN)

    override fun check(element: PsiElement): Set<Typo> {
        require(isRelevant(element)) { "Got non XmlText or XML_ATTRIBUTE_VALUE_TOKEN in XmlSupport" }
        return GrammarChecker.default.check(element).filterNot { typo -> typo.location.isAtStart() || typo.location.isAtEnd() }.toSet()
    }
}
