package tanvd.grazi.ide.language.python

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.jetbrains.python.psi.*
import tanvd.grazi.GraziBundle
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.filterFor
import tanvd.kex.buildSet


class PStringSupport : LanguageSupport(GraziBundle.langConfigSet("global.literal_string.disabled")) {
    override fun isSupported(file: PsiFile): Boolean {
        return file is PyFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val strLiterals = file.filterFor<PyStringLiteralExpression>()
        for (strElements in strLiterals.filter { !it.isDocString }.map { it.stringElements }) {
            addAll(PUtils.python.check(strElements, indexBasedIgnore = { token, index ->
                when (token) {
                    is PyFormattedStringElement -> token.literalPartRanges.all { index !in it }
                    is PyPlainStringElement -> false
                    else -> false
                }
            }))
            ProgressManager.checkCanceled()
        }
    }
}
