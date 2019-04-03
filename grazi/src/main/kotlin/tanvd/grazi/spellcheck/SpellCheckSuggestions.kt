package tanvd.grazi.spellcheck

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.codeStyle.SuggestedNameInfo
import com.intellij.refactoring.rename.PreferrableNameSuggestionProvider

class SpellCheckSuggestions : PreferrableNameSuggestionProvider() {
    var active: Boolean = false


    override fun shouldCheckOthers(): Boolean {
        return !active
    }

    override fun getSuggestedNames(element: PsiElement, nameSuggestionContext: PsiElement?, result: MutableSet<String>): SuggestedNameInfo? {
        if (!active || nameSuggestionContext == null) {
            return null
        }
        var text: String? = nameSuggestionContext.text
        if (nameSuggestionContext is PsiNamedElement) {
            text = (element as PsiNamedElement).name
        }
        if (text == null) {
            return null
        }

        SpellChecker.check(text, element.project).forEach { typo ->
            typo.fix?.forEach {
                result.add(text.replaceRange(typo.location.range, it))
            }
        }

        return SuggestedNameInfo.NULL_INFO
    }
}
