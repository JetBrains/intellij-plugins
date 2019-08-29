package tanvd.grazi.ide.ui.components.langlist

import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import tanvd.grazi.language.Lang
import tanvd.kex.ifTrue

class GraziListPopupStep(title: String, downloadedLangs: List<Lang>, private val otherLangs: List<Lang>,
                         private val download: (Lang) -> Boolean, val onResult: (Lang) -> Unit) : BaseListPopupStep<Lang>(title, downloadedLangs + otherLangs) {
    override fun getSeparatorAbove(value: Lang?) = when (value) {
        otherLangs.firstOrNull() -> ListSeparator()
        else -> null
    }

    override fun onChosen(selectedValue: Lang?, finalChoice: Boolean): PopupStep<*>? = selectedValue?.let { lang ->
        doFinalStep { download(lang).ifTrue { onResult(lang) } }
    } ?: PopupStep.FINAL_CHOICE
}
