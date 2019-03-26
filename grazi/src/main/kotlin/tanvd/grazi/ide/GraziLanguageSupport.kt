package tanvd.grazi.ide

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod

interface GraziLanguageSupport {
    fun replace(textBlock: TextBlock, range: TextRange, replacement: String)

    fun extract(file: PsiFile): List<TextBlock>? {
        return null
    }

    fun extract(cls: PsiClass): List<TextBlock>? {
        return null
    }

    fun extract(field: PsiField): List<TextBlock>? {
        return null
    }

    fun extract(method: PsiMethod): List<TextBlock>? {
        return null
    }
}
