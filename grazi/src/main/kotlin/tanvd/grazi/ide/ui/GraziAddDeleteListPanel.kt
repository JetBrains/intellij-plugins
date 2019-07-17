package tanvd.grazi.ide.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.AddDeleteListPanel
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import java.awt.Component
import javax.swing.*

class GraziAddDeleteListPanel : AddDeleteListPanel<Lang>(null, GraziConfig.get().enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName))) {
    private val cbLanguage = ComboBox<Lang>()

    init {
        emptyText.text = msg("grazi.ui.settings.language.empty.text")
    }

    override fun getListCellRenderer(): ListCellRenderer<*> = object : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JComponent
            component.border = padding(JBUI.insets(5))
            return component
        }
    }

    override fun addElement(itemToAdd: Lang?) {
        if (itemToAdd != null) {
            val position = -(myListModel.elements().toList().binarySearch(itemToAdd, Comparator.comparing(Lang::displayName)) + 1)
            myListModel.add(position, itemToAdd)
            myList.setSelectedValue(itemToAdd, true)
        }
    }

    override fun findItemToAdd(): Lang? {
        val langsInList = listItems.toSet()
        cbLanguage.removeAllItems()
        Lang.sortedValues.filter { it !in langsInList }.forEach { cbLanguage.addItem(it) }


        val dialog = DialogBuilder(this)
                .title(msg("grazi.ui.settings.language.dialog.title"))
                .centerPanel(
                        if (cbLanguage.itemCount == 0) JBLabel(msg("grazi.ui.settings.language.dialog.empty.text"))
                        else wrap(cbLanguage, msg("grazi.ui.settings.language.dialog.comment.text"), msg("grazi.ui.settings.language.dialog.label.text"))
                )

        return when (dialog.show()) {
            DialogWrapper.OK_EXIT_CODE -> (cbLanguage.selectedItem as Lang)
            else -> null
        }
    }

    fun reset(settings: GraziConfig) {
        val model = myList.model as DefaultListModel<Lang>
        model.clear()
        settings.state.enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName)).forEach {
            model.addElement(it)
        }
    }
}
