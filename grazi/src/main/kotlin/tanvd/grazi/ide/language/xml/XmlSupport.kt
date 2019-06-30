package tanvd.grazi.ide.language.xml

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlText
import org.apache.xmlbeans.XmlLanguage
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor

class XmlSupport : LanguageSupport() {
    override fun isSupported(language: Language): Boolean {
        return language is XmlLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is XmlFile
    }

    override fun check(element: PsiElement): Set<Typo> {
        val xmlTexts = element.filterFor<XmlText>()
        return GrammarChecker.default.check(xmlTexts)
    }
}
