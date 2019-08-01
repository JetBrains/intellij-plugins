package tanvd.grazi.ide.ui.components

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.*
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.JBUI
import tanvd.grazi.GraziConfig
import tanvd.grazi.ide.ui.components.dsl.msg
import tanvd.grazi.ide.ui.components.dsl.padding
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.*

class GraziAddDeleteListPanel(private val onLanguageAdded: (lang: Lang) -> Unit, private val onLanguageRemoved: (lang: Lang) -> Unit) :
        AddDeleteListPanel<Lang>(null, GraziConfig.get().enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName))) {
    private val decorator: ToolbarDecorator =
            GraziListToolbarDecorator(myList as JList<Any>)
                    .setAddAction { addElement(findItemToAdd()) }
                    .setToolbarPosition(ActionToolbarPosition.BOTTOM)
                    .setRemoveAction {
                        myList.selectedValuesList.forEach(onLanguageRemoved)
                        ListUtil.removeSelectedItems(myList as JList<Lang>)
                    }

    init {
        emptyText.text = msg("grazi.ui.settings.language.empty.text")
        layout = BorderLayout()
        add(decorator.createPanel(), BorderLayout.CENTER)
    }

    override fun initPanel() {
        // do nothing
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
            onLanguageAdded(itemToAdd)
            myList.clearSelection()
            myList.setSelectedValue(itemToAdd, true)
        }
    }

    override fun findItemToAdd(): Lang? {
        val langsInList = listItems.map { it as Lang }.toSet()
        val menu = JBPopupFactory.getInstance()
                .createListPopup(GraziListPopupStep(msg("grazi.ui.settings.language.dialog.title"), Lang.sortedValues().filter { it !in langsInList }, this, ::addElement))
        decorator.actionsPanel?.getAnActionButton(CommonActionsPanel.Buttons.ADD)?.preferredPopupPoint?.let(menu::show)
        return null
    }

    fun reset(settings: GraziConfig) {
        val model = myList.model as DefaultListModel<Lang>
        model.elements().asSequence().forEach(onLanguageRemoved)
        model.clear()
        settings.state.enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName)).forEach(::addElement)
    }

    private class GraziListToolbarDecorator(val list: JList<Any>) : ToolbarDecorator() {
        init {
            myRemoveActionEnabled = true
            myAddActionEnabled = true

            list.addListSelectionListener { updateButtons() }
            list.addPropertyChangeListener("enabled") { updateButtons() }
        }

        public override fun updateButtons() {
            actionsPanel?.let {
                it.setEnabled(CommonActionsPanel.Buttons.ADD, list.isEnabled && list.model.size < Lang.values().size)
                it.setEnabled(CommonActionsPanel.Buttons.REMOVE, !list.isSelectionEmpty)
                updateExtraElementActions(!list.isSelectionEmpty)
            }
        }

        override fun setVisibleRowCount(rowCount: Int) = this.also { list.visibleRowCount = rowCount }

        override fun getComponent() = list

        override fun installDnDSupport() = RowsDnDSupport.install(list, list.model as EditableModel)

        override fun isModelEditable() = true
    }
}
