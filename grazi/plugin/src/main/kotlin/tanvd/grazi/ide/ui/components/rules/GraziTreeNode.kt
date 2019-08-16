package tanvd.grazi.ide.ui.components.rules

import com.intellij.ui.CheckedTreeNode
import com.intellij.ui.SimpleTextAttributes
import tanvd.grazi.language.Lang
import java.util.HashMap

@Suppress("EqualsOrHashCode")
class GraziTreeNode(userObject: Any? = null) : CheckedTreeNode(userObject) {
    val nodeText: String
        get() = when (val meta = userObject) {
            is RuleWithLang -> meta.rule.description //+ " " + meta.rule.id
            is ComparableCategory -> meta.name //+ " " + meta.category.id
            is Lang -> meta.displayName
            else -> ""
        }

    val attrs: SimpleTextAttributes
        get() = if (userObject is RuleWithLang) SimpleTextAttributes.REGULAR_ATTRIBUTES else SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES

    fun resetMark(state: HashMap<String, RuleWithLang>): Boolean {
        val meta = userObject
        if (meta is RuleWithLang) {
            isChecked = when (val rule = state[meta.rule.id]) {
                is RuleWithLang -> rule.enabledInTree
                else -> meta.enabled
            }
        } else {
            isChecked = false
            for (child in children()) {
                if (child is GraziTreeNode && child.resetMark(state)) {
                    isChecked = true
                }
            }
        }

        return isChecked
    }

    override fun equals(other: Any?): Boolean {
        if (other is GraziTreeNode) {
            return userObject == other.userObject
        }

        return super.equals(other)
    }
}
