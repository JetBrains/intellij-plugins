package tanvd.grazi.spellcheck

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.PreferrableNameSuggestionProvider
import tanvd.grazi.utils.Text
import tanvd.grazi.utils.withOffset

class SpellCheckRenameSuggestions : PreferrableNameSuggestionProvider() {
    var active: Boolean = false

    override fun shouldCheckOthers() = !active

    override fun getSuggestedNames(element: PsiElement, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        if (!active || nameSuggestionContext == null) return null

        val text: String = if (element is PsiNamedElement) {
            element.name
        } else {
            element.text
        } ?: return null

        GraziSpellchecker.getTypos(element).forEach { typo ->
            typo.fixes.filterNot { Text.containsBlank(it) }.forEach {
                val indexInName = typo.location.element!!.text.indexOf(text)
                result.add(text.replaceRange(typo.location.range.withOffset(-indexInName), it))
            }
        }

        return SuggestedNameInfo.NULL_INFO
    }
}
