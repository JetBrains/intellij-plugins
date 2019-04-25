package tanvd.grazi.ide.language.java


import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.*
import com.intellij.psi.javadoc.*
import tanvd.grazi.grammar.SanitizingGrammarChecker
import tanvd.grazi.grammar.Typo
import tanvd.grazi.ide.language.LanguageSupport
import tanvd.grazi.utils.buildSet
import tanvd.grazi.utils.filterFor

class JDocSupport : LanguageSupport() {
    companion object {
        val tagsIgnoredCategories = listOf(Typo.Category.CASING)

        private fun isTag(token: PsiDocToken) = token.parent is PsiDocTag
        private fun isCodeTag(token: PsiDocToken) = isTag(token) && ((token.parent as PsiDocTag).nameElement.text == "@code")
    }

    override fun isSupported(file: PsiFile): Boolean {
        return file is PsiJavaFile
    }

    /**
     * Checks:
     * * Body lines -- lines, which are a DOC_COMMENT_DATA, and their parent is not PsiDocTag
     * * Tag lines -- lines, which are a DOC_COMMENT_DATA, and their parent is PsiDocTag
     *
     * Note: Tag lines ignores casing.
     */
    override fun check(file: PsiFile) = buildSet<Typo> {
        for (doc in file.filterFor<PsiDocComment>()) {
            val allDocTokens = doc.filterFor<PsiDocToken> { it.tokenType == JavaDocTokenType.DOC_COMMENT_DATA }

            addAll(SanitizingGrammarChecker.default.check(allDocTokens.filterNot { isTag(it) }))
            addAll(SanitizingGrammarChecker.default.check(allDocTokens.filter { isTag(it) && !isCodeTag(it) })
                    .filter { it.info.category !in tagsIgnoredCategories })

            ProgressManager.checkCanceled()
        }
    }
}
