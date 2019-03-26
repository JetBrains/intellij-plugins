package tanvd.grazi.ide


import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.source.html.HtmlDocumentImpl
import com.intellij.psi.impl.source.html.HtmlFileImpl
import com.intellij.psi.util.PsiTreeUtil

class HtmlSupport : GraziLanguageSupport {
    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        val newFile = PsiFileFactory.getInstance(textBlock.element.project).createFileFromText("a.html", HtmlFileType.INSTANCE, newText) as HtmlFileImpl
        textBlock.element.replace(collectParagraphs(newFile).single())
    }

    override fun extract(file: PsiFile): List<TextBlock>? {
        val htmlFile = file as? HtmlFileImpl ?: return null
        return collectParagraphs(htmlFile).map {
            TextBlock(it, it.text)
        }
    }

    private fun collectParagraphs(htmlFile: HtmlFileImpl): MutableCollection<HtmlDocumentImpl> =
            PsiTreeUtil.collectElementsOfType(htmlFile, HtmlDocumentImpl::class.java)
}
