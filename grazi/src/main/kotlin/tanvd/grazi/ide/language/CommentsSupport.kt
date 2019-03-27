package tanvd.grazi.ide.language


import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.PsiCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.model.TextBlock

class CommentsSupport : LanguageSupport {
    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        (textBlock.element as? PsiCommentImpl)?.updateText(newText)
    }

    override fun extract(file: PsiFile): List<TextBlock>? {
        return collectParagraphs(file).map {
            TextBlock(it, it.text)
        }
    }

    private fun collectParagraphs(file: PsiFile): MutableCollection<PsiCommentImpl> {
        return PsiTreeUtil.collectElementsOfType(file, PsiCommentImpl::class.java)
    }
}
