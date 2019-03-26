package tanvd.grazi.ide.language


import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import tanvd.grazi.model.TextBlock

class PlainTextSupport : LanguageSupport {
    override fun extract(file: PsiFile): List<TextBlock>? {
        val plainText = file.children.firstOrNull() as? PsiPlainText ?: return null
        return listOf(TextBlock(plainText, plainText.text))
    }

    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        val newFile = PsiFileFactory.getInstance(textBlock.element.project).createFileFromText("a.txt", PlainTextFileType.INSTANCE, newText)
        textBlock.element.replace(newFile.children[0])
    }
}
