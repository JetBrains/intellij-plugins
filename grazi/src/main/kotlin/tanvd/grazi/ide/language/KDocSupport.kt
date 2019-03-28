package tanvd.grazi.ide.language

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.kdoc.psi.api.KDoc
import org.jetbrains.kotlin.kdoc.psi.impl.KDocSection
import tanvd.grazi.ide.language.utils.CustomTokensChecker


class KDocSupport : LanguageSupport {
    override fun extract(file: PsiFile): List<LanguageSupport.Result> {
        val docs = PsiTreeUtil.collectElementsOfType(file, KDoc::class.java)

        val result = ArrayList<LanguageSupport.Result>()
        for (doc in docs) {
            result += CustomTokensChecker.default.check(PsiTreeUtil.collectElementsOfType(doc, KDocSection::class.java).toList())
        }

        return result
    }
}
