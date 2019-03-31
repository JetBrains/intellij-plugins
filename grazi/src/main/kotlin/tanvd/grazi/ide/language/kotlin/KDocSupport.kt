package tanvd.grazi.ide.language.kotlin

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import org.jetbrains.kotlin.psi.KtFile
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.ide.language.LanguageSupport


class KDocSupport : LanguageSupport {
    override fun isSupport(file: PsiFile): Boolean {
        return file is KtFile
    }

    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val docs = PsiTreeUtil.collectElementsOfType(file, KDoc::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (doc in docs) {
            result += SanitizingGrammarChecker.default.check(PsiTreeUtil.collectElementsOfType(doc, KDocSection::class.java).toList())

            ProgressManager.checkCanceled()
        }

        return result
    }
}
