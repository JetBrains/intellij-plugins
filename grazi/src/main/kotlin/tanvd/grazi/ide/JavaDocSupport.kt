package tanvd.grazi.ide


import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import org.intellij.plugins.markdown.lang.MarkdownFileType
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownParagraphImpl

import com.intellij.psi.PsiDocCommentBase
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiJavaDocumentedElement
import com.intellij.psi.javadoc.PsiDocComment

class JavaDocSupport : GraziLanguageSupport {
    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
//        val newText = range.replace(textBlock.element.text, replacement)
//        val newFile = PsiFileFactory.getInstance(textBlock.element.project).createFileFromText("a.md", MarkdownFileType.INSTANCE, newText) as MarkdownFile
//        textBlock.element.replace(collectParagraphs(newFile).single())
    }

    override fun extract(file: PsiFile): List<TextBlock>? {
        val markdownFile = file as? MarkdownFile ?: return null
        return collectParagraphs(markdownFile).map {
            TextBlock(it, it.text)
        }
    }

    private fun collectParagraphs(markdownFile: MarkdownFile): MutableCollection<MarkdownParagraphImpl> =
            PsiTreeUtil.collectElementsOfType(markdownFile, MarkdownParagraphImpl::class.java)
}
