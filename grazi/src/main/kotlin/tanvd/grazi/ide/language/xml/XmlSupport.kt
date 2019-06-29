package tanvd.grazi.ide.language.xml

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlText
import org.apache.xmlbeans.XmlLanguage
import tanvd.grazi.grammar.GrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.*
import tanvd.kex.ifTrue
import tanvd.kex.orTrue

class XmlSupport : LanguageSupport() {
    companion object {
        val xmlChecker = GrammarChecker(
                ignore = listOf({ str, cur ->
                    str.lastOrNull()?.let { blankOrNewLineCharRegex.matches(it) }.orTrue() && blankOrNewLineCharRegex.matches(cur)
                }),
                replace = listOf({ _, cur ->
                    newLineCharRegex.matches(cur).ifTrue { ' ' }
                }),
                ignoreToken = listOf({ str ->
                    str.all { !it.isLetter() && it !in punctuationChars }
                }))
    }

    override fun isSupported(language: Language): Boolean {
        return language is XmlLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is XmlFile
    }

    override fun check(element: PsiElement): Set<Typo> {
        val xmlTexts = element.filterFor<XmlText>()
        return xmlChecker.check(xmlTexts)
    }
}
