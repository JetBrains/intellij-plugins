package tanvd.grazi.ide.language.java

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.tree.IElementType
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor
import tanvd.grazi.utils.withOffset
import tanvd.kex.buildSet

class JStringSupport : LanguageSupport(GraziBundle.langConfigSet("global.literal_string.disabled")) {
    companion object {
        private fun Collection<PsiLiteralExpressionImpl>.filterForType(type: IElementType) = filter { it.literalElementType == type }
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val literals = file.filterFor<PsiLiteralExpressionImpl>()

        for (str in literals.filterForType(JavaTokenType.STRING_LITERAL).filter { it.innerText != null }) {
            val typos = SanitizingGrammarChecker.default.check(str) { it.innerText!! }.map { typo ->
                val element = typo.location.pointer?.element!!
                val indexStart = element.text.indexOf((element as PsiLiteralExpressionImpl).innerText!!)
                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexStart)))
            }
            addAll(typos)
            ProgressManager.checkCanceled()
        }

        for (str in literals.filterForType(JavaTokenType.RAW_STRING_LITERAL).filter { it.rawString != null }) {
            val typos = SanitizingGrammarChecker.default.check(str) { it.rawString!! }.map { typo ->
                val element = typo.location.pointer?.element!!
                val indexStart = element.text.indexOf((element as PsiLiteralExpressionImpl).rawString!!)
                typo.copy(location = typo.location.copy(range = typo.location.range.withOffset(indexStart)))
            }
            addAll(typos)
            ProgressManager.checkCanceled()
        }
    }

}
