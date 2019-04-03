package tanvd.grazi.ide.language.java


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.javadoc.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class JDocSupport : LanguageSupport {
    companion object {
        val tagsIgnoredCategories = listOf(Typo.Category.CASING)
    }

    private fun isTag(token: PsiDocToken) = token.parent is PsiDocTag
    //JDocSupport should ignore code fragments
    private fun isApplicableTag(token: PsiDocToken) = isTag(token) && ((token.parent as PsiDocTag).nameElement.text != "@code")

    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    override fun check(file: PsiFile) = buildSet<Typo> {
        val docs = file.filterFor<PsiDocComment>()

        for (doc in docs) {
            val allDocTokens = doc.filterFor<PsiDocToken> { it.tokenType == JavaDocTokenType.DOC_COMMENT_DATA }
            addAll(SanitizingGrammarChecker.default.check(allDocTokens.filterNot { isTag(it) }))

            addAll(SanitizingGrammarChecker.default.check(allDocTokens.filter { isApplicableTag(it) })
                    .filter { it.info.category !in tagsIgnoredCategories })

            ProgressManager.checkCanceled()
        }
    }
}
