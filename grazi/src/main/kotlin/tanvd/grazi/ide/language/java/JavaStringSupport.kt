package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class JavaStringSupport : LanguageSupport {


    override fun isSupport(file: PsiFile): Boolean {
        return file is JavaCodeFragment
    }


    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val literalExpressions = PsiTreeUtil.collectElementsOfType(file, PsiLiteralExpressionImpl::class.java)

        val literalStrings = literalExpressions.filter { it.literalElementType == JavaTokenType.STRING_LITERAL }
        val rawStrings = literalExpressions.filter { it.literalElementType == JavaTokenType.RAW_STRING_LITERAL }

        val result = ArrayList<LanguageSupport.Result>()
        for (str in literalStrings) {
            result += SanitizingGrammarChecker.default.check(literalStrings) { it.innerText ?: "" }.toList()

            ProgressManager.checkCanceled()
        }

        for (str in rawStrings) {
            result += SanitizingGrammarChecker.default.check(literalStrings) { it.rawString ?: "" }.toList()

            ProgressManager.checkCanceled()
        }

        return result
    }
}
