package tanvd.grazi.ide.language


import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.tree.java.IJavaDocElementType
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.MarkdownFileType
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownHeaderImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownListItemImpl
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraphImpl
import tanvd.grazi.grammar.GrammarEngineService
import tanvd.grazi.ide.GraziInspection
import tanvd.grazi.model.TextBlock
import tanvd.grazi.model.Typo

class MarkdownSupport : LanguageSupport {
    companion object {
        class MarkdownParagraphImplWrapper(private val token: PsiElement) : LeafPsiElement(IJavaDocElementType("MARKDOWN_TEXT_ELEMENT"), "") {
            private val newLine = Regex("(\\n|\\n\\r)")
            private val trueText = token.text.replace(newLine, " ")


            fun getFixes(manager: InspectionManager, isOnTheFly: Boolean, ext: LanguageSupport): MutableList<ProblemDescriptor> {
                val grammarEngineService = GrammarEngineService.getInstance()
                val fixesForText = grammarEngineService.getFixes(trueText)

                val filteredFixes = fixesForText.filter{
                    (token is MarkdownListItemImpl).not() || it.range.first != 0 || it.category != Typo.Category.CASING
                }

                return filteredFixes.map {
                    GraziInspection.typoToProblemDescriptors(it, TextBlock(token, token.text), manager, isOnTheFly, ext)
                }.toMutableList()
            }
        }
    }

    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        val newFile = PsiFileFactory.getInstance(textBlock.element.project).createFileFromText("a.md", MarkdownFileType.INSTANCE, newText) as MarkdownFile
        val neededClass = when (textBlock.element) {
            is MarkdownListItemImpl  -> MarkdownListItemImpl::class.java
            is MarkdownParagraphImpl -> MarkdownParagraphImpl::class.java
            else                     -> MarkdownHeaderImpl::class.java
        }
        textBlock.element.replace(PsiTreeUtil.collectElementsOfType(newFile, neededClass).single())
    }

    override fun extract(file: PsiFile): List<TextBlock>? {
        val markdownFile = file as? MarkdownFile ?: return null
        return collectParagraphs(markdownFile).map {
            TextBlock(it, it.text)
        }
    }

    private fun collectParagraphs(markdownFile: MarkdownFile): MutableCollection<MarkdownParagraphImplWrapper> {
        val paragraphs = PsiTreeUtil.collectElementsOfType(markdownFile, MarkdownParagraphImpl::class.java)
        val headers = PsiTreeUtil.collectElementsOfType(markdownFile, MarkdownHeaderImpl::class.java)
//        val listItems = PsiTreeUtil.collectElementsOfType(markdownFile, MarkdownListItemImpl::class.java)
//
//        paragraphs = paragraphs.filter{ paragraph ->
//            (listItems.any {
//                it.text.endsWith(paragraph.text)
//            }).not()
//        }

        return (paragraphs + headers).map { MarkdownParagraphImplWrapper(it) }.toMutableList()
    }
}
