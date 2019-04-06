package tanvd.grazi.ide.language.python

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyStringLiteralExpression
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor


class PDocSupport : LanguageSupport {
    companion object {
        private val disabledRules = setOf("PUNCTUATION_PARAGRAPH_END")
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is PyFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val strLiterals = file.filterFor<PyStringLiteralExpression>()
        for (strElements in strLiterals.filter { it.isDocString }.map { it.stringElements }) {
            addAll(PUtils.python.check(strElements).filter { it.info.rule.id !in disabledRules })

            ProgressManager.checkCanceled()
        }
    }
}
