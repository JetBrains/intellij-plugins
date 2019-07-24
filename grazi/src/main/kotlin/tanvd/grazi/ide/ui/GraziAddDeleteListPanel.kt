package tanvd.grazi.ide.ui

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.ui.JBPopupMenu
import com.intellij.ui.*
import com.intellij.util.ui.EditableModel
import com.intellij.util.ui.JBUI
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.*

class GraziAddDeleteListPanel(private val onLanguageAdded: (lang: Lang) -> Unit, private val onLanguageRemoved: (lang: Lang) -> Unit) :
        AddDeleteListPanel<Lang>(null, GraziConfig.get().enabledLanguages.sortedWith(Comparator.comparing(Lang::displayName))) {
    private val decorator: ToolbarDecorator =
            GraziListToolbarDecorator(myList as JList<Any>)
                    .setAddAction { addElement(findItemToAdd()) }
                    .setToolbarPosition(ActionToolbarPosition.BOTTOM)
                    .setRemoveAction {
                        myList.selectedValuesList.forEach(onLanguageRemoved)
                        ListUtil.removeSelectedItems<Lang>(myList as JList<Lang>)
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
        val menu = JBPopupMenu(msg("grazi.ui.settings.language.dialog.title"))
        val langsInList = listItems.toSet()
        Lang.sortedValues.filter { it !in langsInList }.forEach {
            menu.add(object : AbstractAction(it.displayName) {
                override fun actionPerformed(event: ActionEvent?) {
                    addElement(it)
                }
            })
        }

        decorator.actionsPanel?.getAnActionButton(CommonActionsPanel.Buttons.ADD)?.preferredPopupPoint?.let {
            menu.show(it.component, it.point.x, it.point.y)
        } ?: run {
            menu.show(this, width - insets.right, insets.top)
        }

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
