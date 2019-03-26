package tanvd.grazi.ide

import com.intellij.openapi.util.TextRange
import com.intellij.psi.*

interface GraziLanguageSupport {
    fun replace(textBlock: TextBlock, range: TextRange, replacement: String)

    fun extract(file: PsiFile): List<TextBlock>? = null

    fun extract(cls: PsiClass): List<TextBlock>? = null

    fun extract(field: PsiField): List<TextBlock>? = null

    fun extract(method: PsiMethod): List<TextBlock>? = null
}
