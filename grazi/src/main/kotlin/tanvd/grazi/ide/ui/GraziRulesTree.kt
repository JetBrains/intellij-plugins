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
import kotlin.collections.HashSet

private data class RuleWithLang(val rule: Rule, val lang: Lang, val enabled: Boolean, var state: Boolean) {
    val category: Category
        get() = rule.category
}

private typealias RulesMap = SortedMap<Lang, SortedMap<Category, MutableList<RuleWithLang>>>

private fun LangTool.allRulesWithLangs(): RulesMap {
    val result = TreeMap<Lang, SortedMap<Category, MutableList<RuleWithLang>>>()
    GraziConfig.state.enabledLanguages.forEach { lang ->
        val categories = TreeMap<Category, MutableList<RuleWithLang>>(Comparator<Category> { o1, o2 -> o1!!.name.compareTo(o2!!.name) })
        val activeRules = this[lang].allActiveRules.toHashSet()
        val usedRules = HashSet<String>() // prevent subrules with same ids

        this[lang].allRules.forEach {rule ->
            if (rule.id !in usedRules) {
                usedRules.add(rule.id)
                if (rule in activeRules) {
                    categories.getOrPut(rule.category, ::LinkedList).add(RuleWithLang(rule, lang, enabled = true, state = true))
                } else {
                    categories.getOrPut(rule.category, ::LinkedList).add(RuleWithLang(rule, lang, enabled = false, state = false))
                }
            }
        }

        if (categories.isNotEmpty()) result[lang] = categories
    }

    return result
}

class GraziRulesTree(selectionListener: (meta: Any) -> Unit) : Disposable {
    private val state = HashMap<String, RuleWithLang>()
    val panel = JPanel(BorderLayout())

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
                    meta.state = node.isChecked
                    if (meta.enabled == meta.state) {
                        state.remove(meta.rule.id)
                    } else {
                        state[meta.rule.id] = meta
                    }
                }
            }
        })

        val scrollPane = ScrollPaneFactory.createScrollPane(tree)
        val filterPanel = JPanel(BorderLayout())
        filterPanel.add(filter, BorderLayout.CENTER)
        filterPanel.border = JBUI.Borders.emptyBottom(2)

        val group = DefaultActionGroup()
        val actionManager = CommonActionsManager.getInstance()
        val treeExpander = DefaultTreeExpander(tree)
        group.add(actionManager.createExpandAllAction(treeExpander, tree))
        group.add(actionManager.createCollapseAllAction(treeExpander, tree))
        filterPanel.add(ActionManager.getInstance().createActionToolbar("GraziRulesTree", group, true).component, BorderLayout.WEST)

        panel.add(filterPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)

        filter.reset()
    }

    private fun getNodeText(node: CheckedTreeNode): String {
        return when (val meta = node.userObject) {
            is RuleWithLang -> meta.rule.description
            is Category -> meta.name
            is Lang -> meta.displayName
            else -> "???"
        }
    }

    private fun visit(node: CheckedTreeNode, visitor: (node: CheckedTreeNode) -> Unit) {
        node.children().iterator().forEach {
            visitor(it as CheckedTreeNode)
        }
    }

    fun apply() {
        state.values.forEach { rule ->
            if (rule.state) {
                LangTool[rule.lang].enableRule(rule.rule.id)
                GraziConfig.state.userDisabledRules.remove(rule.rule.id)
                GraziConfig.state.userEnabledRules.add(rule.rule.id)
            } else {
                LangTool[rule.lang].disableRule(rule.rule.id)
                GraziConfig.state.userEnabledRules.remove(rule.rule.id)
                GraziConfig.state.userDisabledRules.add(rule.rule.id)
            }
        }

        state.clear()
    }

    private fun resetCheckMark(root: CheckedTreeNode): Boolean {
        val meta = root.userObject
        if (meta is RuleWithLang) {
            root.isChecked = when(val rule = state[meta.rule.id]) {
                is RuleWithLang -> rule.state
                else -> !LangTool[meta.lang].disabledRules.contains(meta.rule.id)
            }
        } else {
            root.isChecked = false
            visit(root) { if (resetCheckMark(it)) root.isChecked = true }
        }

        return root.isChecked
    }

    fun filter(filterString: String?) {

        val filteredRules = LangTool.allRulesWithLangs()
        if (!filterString.isNullOrBlank()) {
            val iterator = filteredRules.iterator()
            while (iterator.hasNext()) {
                val (lang, categories) = iterator.next()
                if (!lang.displayName.contains(filterString, true)) {
                    val categoryIterator = categories.iterator()
                    while (categoryIterator.hasNext()) {
                        val (category, rules) = categoryIterator.next()
                        if (!category.name.contains(filterString, true)) {
                            val ruleIterator = rules.listIterator()
                            while (ruleIterator.hasNext()) {
                                val rule = ruleIterator.next()
                                if (!rule.rule.description.contains(filterString, true)) ruleIterator.remove()
                            }
                        }

                        if (rules.isEmpty()) categoryIterator.remove()
                    }
                }

                if (categories.isEmpty()) iterator.remove()
            }
        }

        reset(filteredRules)
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
