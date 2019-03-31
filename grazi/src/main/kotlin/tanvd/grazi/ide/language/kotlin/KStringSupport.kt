package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.ide.language.LanguageSupport

class KStringSupport : LanguageSupport {
    override fun isSupport(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val strings = PsiTreeUtil.collectElementsOfType(file, KtStringTemplateEntry::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (str in strings) {
            result += SanitizingGrammarChecker.default.check(PsiTreeUtil.collectElementsOfType(str, KtLiteralStringTemplateEntry::class.java).toList())

            ProgressManager.checkCanceled()
        }

        return result
    }
}
