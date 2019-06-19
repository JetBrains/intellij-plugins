package tanvd.grazi.ide.language

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlText
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.*
import tanvd.kex.*

class XmlSupport : LanguageSupport() {
    companion object {
        val xmlChecker = SanitizingGrammarChecker(
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

    override fun isSupported(file: PsiFile): Boolean {
        return file is XmlFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val xmlTexts = file.filterFor<XmlText>()
        addAll(xmlChecker.check(xmlTexts))
    }
}
