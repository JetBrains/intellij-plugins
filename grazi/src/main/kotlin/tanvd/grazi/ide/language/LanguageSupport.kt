package tanvd.grazi.ide.language

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import tanvd.grazi.model.TextBlock

interface LanguageSupport {
    fun replace(textBlock: TextBlock, range: TextRange, replacement: String)

    fun extract(file: PsiFile): List<TextBlock>? = null

    fun extract(cls: PsiClass): List<TextBlock>? = null

    fun extract(field: PsiField): List<TextBlock>? = null

    fun extract(method: PsiMethod): List<TextBlock>? = null
}
