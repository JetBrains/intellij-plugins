package tanvd.grazi.ide.language.java

import com.intellij.lang.Language
import com.intellij.lang.java.JavaLanguage
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.withOffset

class JStringSupport : LanguageSupport(GraziBundle.langConfig("global.literal_string.disabled")) {
    override fun isSupported(language: Language): Boolean {
        return language is JavaLanguage
    }

    override fun isRelevant(element: PsiElement): Boolean {
        return element is PsiLiteralExpressionImpl &&
                ((element.literalElementType == JavaTokenType.STRING_LITERAL && element.innerText != null) ||
                        (element.literalElementType == JavaTokenType.RAW_STRING_LITERAL && element.rawString != null))
    }

    override fun check(element: PsiElement): Set<Typo> {
        require(element is PsiLiteralExpressionImpl) { "Got non literal PsiElement in JStringSupport" }
        return when (element.literalElementType) {
            JavaTokenType.STRING_LITERAL -> {
                SanitizingGrammarChecker.default.check(element) { it.innerText!! }.map { typo ->
                    val typoElement = typo.location.pointer?.element!!
                    val indexStart = typoElement.text.indexOf((typoElement as PsiLiteralExpressionImpl).innerText!!)
                    typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexStart)))
                }
            }
            JavaTokenType.RAW_STRING_LITERAL -> {
                SanitizingGrammarChecker.default.check(element) { it.rawString!! }.map { typo ->
                    val typoElement = typo.location.pointer?.element!!
                    val indexStart = typoElement.text.indexOf((typoElement as PsiLiteralExpressionImpl).rawString!!)
                    typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexStart)))
                }
            }
            else -> {
                error("Got non literal JavaTokenType in JStringSupport")
            }
        }.toSet()
    }
}
