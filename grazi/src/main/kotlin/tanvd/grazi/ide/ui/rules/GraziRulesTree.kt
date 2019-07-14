package tanvd.grazi.ide.ui.rules

import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.ide.ui.search.SearchUtil
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.packageDependencies.ui.TreeExpansionMonitor
import com.intellij.ui.*
import com.intellij.ui.SimpleTextAttributes.REGULAR_ATTRIBUTES
import com.intellij.ui.SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.tree.TreeUtil
import org.languagetool.rules.Category
import org.picocontainer.Disposable
import tanvd.grazi.ide.ui.panel
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import java.awt.BorderLayout
import java.util.*
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class GraziRulesTree(selectionListener: (meta: Any) -> Unit) : Disposable {
    private val state = HashMap<String, RuleWithLang>()

    private val tree: CheckboxTree by lazy {
        CheckboxTree(object : CheckboxTree.CheckboxTreeCellRenderer(true) {
            override fun customizeRenderer(tree: JTree?, node: Any?, selected: Boolean, expanded: Boolean,
                                           leaf: Boolean, row: Int, hasFocus: Boolean) {
                if (node !is CheckedTreeNode) return

                val background = UIUtil.getTreeBackground(selected, true)
                UIUtil.changeBackGround(this, background)

                SearchUtil.appendFragments(_filter?.filter, node.nodeText, node.attrs.style, node.attrs.fgColor, background, textRenderer)
            }
        }, CheckedTreeNode()).apply {
            selectionModel.addTreeSelectionListener { e ->
                when (val meta = (e?.path?.lastPathComponent as DefaultMutableTreeNode).userObject) {
                    is RuleWithLang -> selectionListener(meta.rule)
                    is Category -> selectionListener(meta)
                    is Lang -> selectionListener(meta)
                }
            }

            addCheckboxTreeListener(object : CheckboxTreeAdapter() {
                override fun nodeStateChanged(node: CheckedTreeNode) {
                    val meta = node.userObject
                    if (meta is RuleWithLang) {
                        meta.enabledInTree = node.isChecked
                        if (meta.enabled == meta.enabledInTree) {
                            state.remove(meta.rule.id)
                        } else {
                            state[meta.rule.id] = meta
                        }
                    }
                }
            })
        }
    }

    private val _filter: FilterComponent? by lazy {
        object : FilterComponent("GRAZI_RULES_FILTER", 10) {
            private val expansionMonitor by lazy { TreeExpansionMonitor.install(tree) }

            override fun filter() {
                if (!filter.isNullOrBlank()) expansionMonitor.freeze()

                filterTree(filter)

                (tree.model as DefaultTreeModel).reload()
                TreeUtil.restoreExpandedPaths(tree, TreeUtil.collectExpandedPaths(tree))

                SwingUtilities.invokeLater { tree.setSelectionRow(0) }

                TreeUtil.collapseAll(tree, 1)
                if (filter.isNullOrBlank()) {
                    TreeUtil.collapseAll(tree, 0)
                    expansionMonitor.restore()
                }
            }
        }
    }

    val panel by lazy {
        panel {
            panel(constraint = BorderLayout.NORTH) {
                border = JBUI.Borders.emptyBottom(2)

                with(DefaultActionGroup()) {
                    val actionManager = CommonActionsManager.getInstance()
                    val treeExpander = DefaultTreeExpander(tree)
                    add(actionManager.createExpandAllAction(treeExpander, tree))
                    add(actionManager.createCollapseAllAction(treeExpander, tree))

                    add(ActionManager.getInstance().createActionToolbar("GraziRulesTree", this, true).component, BorderLayout.WEST)
                }

                add(_filter, BorderLayout.CENTER)
            }

            panel(constraint = BorderLayout.CENTER) {
                add(ScrollPaneFactory.createScrollPane(tree))
            }
        }
    }

    var filter: String?
        get() = _filter?.filter
        set(value) {
            _filter?.filter = value
        }

    val isModified: Boolean
        get() = state.isNotEmpty()

    init {
        _filter?.reset()
    }

    data class TreeState(val enabled: Set<String>, val disabled: Set<String>)

    fun state(): TreeState {
        val (enabled, disabled) = state.values.partition { it.enabledInTree }
        return TreeState(enabled.map { it.rule.id }.toSet(), disabled.map { it.rule.id }.toSet())
    }


    /** Will filter tree representation in UI */
    fun filterTree(filterString: String?) {
        if (!filterString.isNullOrBlank()) {
            reset(LangTool.allRulesWithLangs().asSequence().map { (lang, categories) ->
                lang to categories.map { (category, rules) ->
                    category to rules.filter { it.rule.description.contains(filterString, true) }
                }.toMap().filterValues { it.isNotEmpty() }
            }.toMap().filterValues { it.isNotEmpty() })
        } else {
            reset(LangTool.allRulesWithLangs())
        }
    }

    fun reset() {
        state.clear()
        reset(LangTool.allRulesWithLangs())
    }

    private fun reset(rules: RulesMap) {
        val root = CheckedTreeNode()
        val model = tree.model as DefaultTreeModel

        rules.forEach { (lang, categories) ->
            val langNode = CheckedTreeNode(lang)
            model.insertNodeInto(langNode, root, root.childCount)
            categories.forEach { (category, rules) ->
                val categoryNode = CheckedTreeNode(category)
                model.insertNodeInto(categoryNode, langNode, langNode.childCount)
                rules.forEach { rule ->
                    model.insertNodeInto(CheckedTreeNode(rule), categoryNode, categoryNode.childCount)
                }
            }
        }

        with(root) {
            resetMark()
            model.setRoot(this)
            model.nodeChanged(this)
        }

        TreeUtil.collapseAll(tree, 1)
        tree.setSelectionRow(0)
    }

    private fun CheckedTreeNode.resetMark(): Boolean {
        val meta = userObject
        if (meta is RuleWithLang) {
            isChecked = when (val rule = state[meta.rule.id]) {
                is RuleWithLang -> rule.enabledInTree
                else -> !LangTool[meta.lang].disabledRules.contains(meta.rule.id)
            }
        } else {
            isChecked = false
            for (child in children()) {
                if (child is CheckedTreeNode && child.resetMark()) {
                    isChecked = true
                }
            }
        }

        return isChecked
    }

    override fun dispose() {
        _filter?.dispose()
    }

    private val CheckedTreeNode.nodeText: String
        get() = when (val meta = userObject) {
            is RuleWithLang -> meta.rule.description
            is Category -> meta.name
            is Lang -> meta.displayName
            else -> ""
        }

    private val CheckedTreeNode.attrs: SimpleTextAttributes
        get() = if (userObject is RuleWithLang) REGULAR_ATTRIBUTES else REGULAR_BOLD_ATTRIBUTES
}
