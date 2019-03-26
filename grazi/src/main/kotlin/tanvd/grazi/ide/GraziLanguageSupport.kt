package tanvd.grazi.ide

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile

interface GraziLanguageSupport {
    fun extract(file: PsiFile): List<TextBlock>?
    fun replace(textBlock: TextBlock, range: TextRange, replacement: String)
}
