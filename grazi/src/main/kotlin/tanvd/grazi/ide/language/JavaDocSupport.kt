package tanvd.grazi.ide.language


import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.PsiElementFactoryImpl
import com.intellij.psi.impl.PsiManagerEx
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl
import com.intellij.psi.util.PsiTreeUtil
import tanvd.grazi.model.TextBlock

class JavaDocSupport : LanguageSupport {
    override fun extract(file: PsiFile): List<TextBlock>? {
        return PsiTreeUtil.collectElementsOfType(file, PsiDocCommentImpl::class.java).map {
            TextBlock(it, it.text)
        }
    }

    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val elemFactory = PsiElementFactoryImpl(PsiManagerEx.getInstanceEx(textBlock.element.project))
        val newText = range.replace(textBlock.element.text, replacement)
        val newDocComment = elemFactory.createDocCommentFromText(newText, textBlock.element)
        textBlock.element.replace(PsiTreeUtil.collectElementsOfType(newDocComment, PsiDocCommentImpl::class.java).single())
    }

}
