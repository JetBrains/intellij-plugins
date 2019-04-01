package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.tree.IElementType
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class JStringSupport : LanguageSupport {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val literals = file.filterFor<PsiLiteralExpressionImpl>()

        val literalStrings = literals.filter { it.literalElementType == JavaTokenType.STRING_LITERAL }
        val rawStrings = literals.filter { it.literalElementType == JavaTokenType.RAW_STRING_LITERAL }

        for (str in literals.filterForType(JavaTokenType.STRING_LITERAL)) {
            addAll(SanitizingGrammarChecker.default.check(literalStrings) { it.innerText ?: "" })
            ProgressManager.checkCanceled()
        }

        for (str in rawStrings.filterForType(JavaTokenType.RAW_STRING_LITERAL)) {
            addAll(SanitizingGrammarChecker.default.check(literalStrings) { it.rawString ?: "" })

            ProgressManager.checkCanceled()
        }
    }

    private fun Collection<PsiLiteralExpressionImpl>.filterForType(type: IElementType) = filter { it.literalElementType == type }
}
