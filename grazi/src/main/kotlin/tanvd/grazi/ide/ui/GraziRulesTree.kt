package tanvd.grazi.ide.ui

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
import org.languagetool.rules.Rule
import org.picocontainer.Disposable
import tanvd.grazi.GraziConfig
import tanvd.grazi.language.Lang
import tanvd.grazi.language.LangTool
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.Comparator

private data class RuleWithLang(val rule: Rule, val lang: Lang, val enabled: Boolean, var enabledInTree: Boolean) {
    val category: Category = rule.category
}

private typealias RulesMap = List<Pair<Lang, List<Pair<Category, List<RuleWithLang>>>>>

private fun LangTool.allRulesWithLangs(): RulesMap {
    val result = TreeMap<Lang, SortedMap<Category, MutableList<RuleWithLang>>>()
    GraziConfig.state.enabledLanguages.forEach { lang ->
        val categories = TreeMap<Category, MutableList<RuleWithLang>>(Comparator.comparing(Category::getName))

        with(get(lang)) {
            val activeRules = allActiveRules.toSet()
            val (enabled, disabled) = allRules.distinctBy { it.id }.partition { it in activeRules }
            enabled.forEach { categories.getOrPut(it.category, ::LinkedList).add(RuleWithLang(it, lang, enabled = true, enabledInTree = true)) }
            disabled.forEach { categories.getOrPut(it.category, ::LinkedList).add(RuleWithLang(it, lang, enabled = false, enabledInTree = false)) }

            if (categories.isNotEmpty()) result[lang] = categories
        }
    }

    return result.map { (lang, categories) -> lang to categories.map { (category, rules) -> category to rules }.toList() }.toList()
}

class GraziRulesTree(selectionListener: (meta: Any) -> Unit) : Disposable {
    private val state = HashMap<String, RuleWithLang>()
    val panel: JPanel

    private val tree: CheckboxTree = CheckboxTree(object : CheckboxTree.CheckboxTreeCellRenderer(true) {

        override fun customizeRenderer(tree: JTree?, node: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
            if (node !is CheckedTreeNode) return

            val background = UIUtil.getTreeBackground(selected, true)
            val text = getNodeText(node)
            val attrs = if (node.userObject is RuleWithLang) REGULAR_ATTRIBUTES else REGULAR_BOLD_ATTRIBUTES
            UIUtil.changeBackGround(this, background)

            SearchUtil.appendFragments(
                    filter?.filter, // it can be null
                    text,
                    attrs.style,
                    attrs.fgColor,
                    background,
                    textRenderer
            )
        }
    }, CheckedTreeNode())

    private val filter = object : FilterComponent("GRAZI_RULES_FILTER", 10) {
        val expansionMonitor = TreeExpansionMonitor.install(tree)

        override fun filter() {
            if (!filter.isNullOrBlank()) expansionMonitor.freeze()

            filter(filter)

            val expandedPaths = TreeUtil.collectExpandedPaths(tree)
            (tree.model as DefaultTreeModel).reload()
            TreeUtil.restoreExpandedPaths(tree, expandedPaths)

            SwingUtilities.invokeLater {
                tree.setSelectionRow(0)
            }

            TreeUtil.expandAll(tree)
            if (filter.isNullOrBlank()) {
                TreeUtil.collapseAll(tree, 0)
                expansionMonitor.restore()
            }
        }
    }

    init {
        tree.selectionModel.addTreeSelectionListener { e ->
            when (val meta = (e?.path?.lastPathComponent as DefaultMutableTreeNode).userObject) {
                is RuleWithLang -> selectionListener(meta.rule)
                is Category -> selectionListener(meta)
                is Lang -> selectionListener(meta)
            }
        }

        tree.addCheckboxTreeListener(object : CheckboxTreeListener {
            override fun mouseDoubleClicked(node: CheckedTreeNode) {
                // do nothing
            }

            override fun beforeNodeStateChanged(node: CheckedTreeNode) {
                // do nothing
            }

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

        panel = panel {
            panel(constraint = BorderLayout.NORTH) {
                border = JBUI.Borders.emptyBottom(2)

                with(DefaultActionGroup()) {
                    val actionManager = CommonActionsManager.getInstance()
                    val treeExpander = DefaultTreeExpander(tree)
                    add(actionManager.createExpandAllAction(treeExpander, tree))
                    add(actionManager.createCollapseAllAction(treeExpander, tree))

                    add(ActionManager.getInstance().createActionToolbar("GraziRulesTree", this, true).component, BorderLayout.WEST)
                }

                add(filter, BorderLayout.CENTER)
            }

            panel(constraint = BorderLayout.CENTER) {
                add(ScrollPaneFactory.createScrollPane(tree))
            }
        }

        filter.reset()
    }

    private fun getNodeText(node: CheckedTreeNode): String {
        return when (val meta = node.userObject) {
            is RuleWithLang -> meta.rule.description
            is Category -> meta.name
            is Lang -> meta.displayName
            else -> ""
        }
    }

    private fun visit(node: CheckedTreeNode, visitor: (node: CheckedTreeNode) -> Unit) {
        node.children().iterator().forEach {
            visitor(it as CheckedTreeNode)
        }
    }

    fun apply(config: GraziConfig.State) {
        state.values.forEach { rule ->
            if (rule.enabledInTree) {
                LangTool[rule.lang].enableRule(rule.rule.id)
                config.userDisabledRules.remove(rule.rule.id)
                config.userEnabledRules.add(rule.rule.id)
            } else {
                LangTool[rule.lang].disableRule(rule.rule.id)
                config.userEnabledRules.remove(rule.rule.id)
                config.userDisabledRules.add(rule.rule.id)
            }
        }

        state.clear()
    }

    private fun resetCheckMark(root: CheckedTreeNode): Boolean {
        val meta = root.userObject
        if (meta is RuleWithLang) {
            root.isChecked = when (val rule = state[meta.rule.id]) {
                is RuleWithLang -> rule.enabledInTree
                else -> !LangTool[meta.lang].disabledRules.contains(meta.rule.id)
            }
        } else {
            root.isChecked = false
            visit(root) { if (resetCheckMark(it)) root.isChecked = true }
        }

        return root.isChecked
    }

    fun filter(filterString: String?) {
        if (!filterString.isNullOrBlank()) {
            reset(LangTool.allRulesWithLangs().asSequence().map { (lang, categories) ->
                lang to categories.map { (category, rules) ->
                    category to rules.filter { it.rule.description.contains(filterString, true) }
                }.filter { it.second.isNotEmpty() }
            }.filter { it.second.isNotEmpty() }.toList())
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

        resetCheckMark(root)
        model.setRoot(root)
        model.nodeChanged(root)
        TreeUtil.expandAll(tree)
        tree.setSelectionRow(0)
    }

    fun isModified(): Boolean {
        return state.isNotEmpty()
    }

    fun setFilter(filterString: String?) {
        filter.filter = filterString
    }

    fun getFilter(): String {
        return filter.filter
    }

    override fun dispose() {
        filter.dispose()
    }
}
