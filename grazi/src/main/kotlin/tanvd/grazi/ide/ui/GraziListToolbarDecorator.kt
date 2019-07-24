package tanvd.grazi.ide.ui

import com.intellij.ui.CommonActionsPanel
import com.intellij.ui.RowsDnDSupport
import com.intellij.ui.ToolbarDecorator
import com.intellij.util.ui.EditableModel
import tanvd.grazi.language.Lang
import javax.swing.JList

class GraziListToolbarDecorator(val list: JList<Any>) : ToolbarDecorator() {

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
