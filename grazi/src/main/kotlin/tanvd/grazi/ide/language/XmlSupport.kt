package tanvd.grazi.ide.language

import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlText
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class XmlSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return file is XmlFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val xmlTexts = file.filterFor<XmlText>()
        addAll(SanitizingGrammarChecker.default.check(xmlTexts))
    }
}
