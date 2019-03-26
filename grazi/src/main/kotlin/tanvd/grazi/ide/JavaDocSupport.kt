package tanvd.grazi.ide


import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.javadoc.PsiDocCommentImpl
import com.intellij.psi.util.PsiTreeUtil

class JavaDocSupport : GraziLanguageSupport {
    override fun extract(cls: PsiClass): List<TextBlock>? {
        return (PsiTreeUtil.collectElementsOfType(cls, PsiDocCommentImpl::class.java)).map {
            TextBlock(it, it.text)
        }
    }

    override fun extract(field: PsiField): List<TextBlock>? {
        return (PsiTreeUtil.collectElementsOfType(field, PsiDocCommentImpl::class.java)).map {
            TextBlock(it, it.text)
        }
    }

    override fun extract(method: PsiMethod): List<TextBlock>? {
        return (PsiTreeUtil.collectElementsOfType(method, PsiDocCommentImpl::class.java)).map {
            TextBlock(it, it.text)
        }
    }

    override fun replace(textBlock: TextBlock, range: TextRange, replacement: String) {
        val newText = range.replace(textBlock.element.text, replacement)
        val newDocComment = PsiDocCommentImpl(newText)
        textBlock.element.replace(PsiTreeUtil.collectElementsOfType(newDocComment, PsiDocCommentImpl::class.java).single())
    }

}
