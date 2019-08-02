package tanvd.grazi.ide.ui.components

import com.intellij.openapi.project.guessCurrentProject
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangDownloader
import tanvd.kex.ifTrue
import javax.swing.JComponent


class GraziListPopupStep(title: String, elements: List<Lang>, private val panel: JComponent, val onResult: (Lang) -> Unit) : BaseListPopupStep<Lang>(title, elements) {
    override fun onChosen(selectedValue: Lang?, finalChoice: Boolean): PopupStep<*>? = selectedValue?.let { lang ->
        doFinalStep {
            with(LangDownloader) {
                lang.downloadLanguage(guessCurrentProject(panel)).ifTrue { onResult(lang) }
            }
        }
    } ?: PopupStep.FINAL_CHOICE
}
