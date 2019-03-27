package tanvd.grazi.ide

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.codeInsight.daemon.impl.SeveritiesProvider
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color


class GraziSeveritiesProvider : SeveritiesProvider() {
    override fun getSeveritiesHighlightInfoTypes(): List<HighlightInfoType> {
        return arrayListOf(HighlightInfoType.HighlightInfoTypeImpl(GRAZI_TYPO,
                TextAttributesKey.createTextAttributesKey("Grazi.Typo", TextAttributes().apply {
                    effectType = EffectType.WAVE_UNDERSCORE
                    effectColor = Color.ORANGE
                })))
    }

    companion object {
        val GRAZI_TYPO = HighlightSeverity("Grazi.Typo", 10)
    }
}
