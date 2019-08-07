package tanvd.grazi.ide.ui.components

import com.intellij.openapi.util.Comparing
import com.intellij.ui.JBColor
import com.intellij.ui.popup.list.ListPopupImpl
import com.intellij.ui.popup.list.PopupListElementRenderer
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Color
import javax.swing.*

class GraziPopupListElementRenderer(list: ListPopupImpl) : PopupListElementRenderer<Lang>(list) {
    private lateinit var size: JLabel

    override fun createItemComponent(): JComponent {
        val panel = JPanel(BorderLayout())
        createLabel()
        panel.add(myTextLabel, BorderLayout.CENTER)
        size = JLabel().apply {
            border = JBUI.Borders.emptyLeft(5)
            foreground = JBColor.GRAY
        }
        panel.add(size, BorderLayout.EAST)
        return layoutComponent(panel)
    }

    override fun customizeComponent(list: JList<out Lang>, lang: Lang?, isSelected: Boolean) {
        val step = myPopup.listStep as GraziListPopupStep
        val isSelectable = step.isSelectable(lang)
        myTextLabel.isEnabled = isSelectable

        val bg = step.getBackgroundFor(lang)
        val fg = step.getForegroundFor(lang)

        if (!isSelected && fg != null) myTextLabel.foreground = fg
        if (!isSelected && bg != null) UIUtil.setBackgroundRecursively(myComponent, bg)
        if (bg != null && mySeparatorComponent.isVisible && myCurrentIndex > 0) {
            val prev = list.model.getElementAt(myCurrentIndex - 1)
            if (Comparing.equal(bg, step.getBackgroundFor(prev))) myRendererComponent.background = bg
        }

        myTextLabel.displayedMnemonicIndex = -1
        myNextStepLabel.isVisible = false

        setSelected(myComponent, isSelected && isSelectable)
        setSelected(myTextLabel, isSelected && isSelectable)
        setSelected(size, isSelected && isSelectable)

        size.text = if (lang?.jLanguage != null) "" else lang?.descriptor?.size ?: ""
        size.foreground = if (!isSelected) Color.GRAY else myTextLabel.foreground
    }
}
