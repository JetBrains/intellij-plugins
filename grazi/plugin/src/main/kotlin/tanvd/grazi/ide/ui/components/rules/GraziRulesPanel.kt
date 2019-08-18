package tanvd.grazi.ide.ui.components.rules

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.ui.ScrollPaneFactory
import com.intellij.util.ui.JBUI
import org.picocontainer.Disposable
import tanvd.grazi.ide.ui.components.dsl.actionGroup
import tanvd.grazi.ide.ui.components.dsl.panel
import tanvd.grazi.language.Lang
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.tree.DefaultMutableTreeNode

class GraziRulesPanel(onSelectionChanged: (meta: Any) -> Unit) : Disposable {
    private val tree: GraziRulesTree = GraziRulesTree(GraziCheckboxTreeCellRenderer { filter?.filter ?: "" })
    private val filter: GraziFilterComponent = GraziFilterComponent(tree, "GRAZI_RULES_FILTER", "GRAZI_RULES_SEARCH")

    init {
        tree.selectionModel.addTreeSelectionListener { event ->
            val meta = (event?.path?.lastPathComponent as DefaultMutableTreeNode).userObject
            if (meta != null) onSelectionChanged(meta)
        }
    }

    val panel by lazy {
        panel {
            panel(constraint = BorderLayout.NORTH) {
                border = JBUI.Borders.emptyBottom(2)

                actionGroup {
                    val actionManager = CommonActionsManager.getInstance()
                    val treeExpander = DefaultTreeExpander(tree)
                    add(actionManager.createExpandAllAction(treeExpander, tree))
                    add(actionManager.createCollapseAllAction(treeExpander, tree))

                    add(ActionManager.getInstance().createActionToolbar("GraziRulesPanel", this, true).component, BorderLayout.WEST)
                }

                add(filter as Component, BorderLayout.CENTER)
            }

            panel(constraint = BorderLayout.CENTER) {
                add(ScrollPaneFactory.createScrollPane(tree))
            }
        }
    }

    val isModified: Boolean
        get() = tree.isModified

    fun state() = tree.state()

    fun addLang(lang: Lang) {
        tree.addLang(lang)
        update()
    }

    fun removeLang(lang: Lang) {
        tree.removeLang(lang)
        update()
    }

    fun update() {
        filter.filter()
        if (tree.isSelectionEmpty) tree.setSelectionRow(0)
    }

    fun reset() {
        tree.clearState()
        update()
    }

    fun filter(str: String) {
        filter.filter = str
        filter.filter()
    }

    override fun dispose() {
        filter.dispose()
    }
}
