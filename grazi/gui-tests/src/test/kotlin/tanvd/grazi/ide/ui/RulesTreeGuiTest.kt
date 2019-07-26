package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.fest.swing.timing.Timeout
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase
import javax.swing.JPanel


@RunWithIde(CommunityIde::class)
class RulesTreeGuiTest : GraziGuiTestBase() {
    @Test
    fun `test rules checkbox tree`() {
        settings("Cancel") {
            actionButton("Add").click()
            popupMenu("Russian").clickSearchedItem()

            val enPath = arrayOf("English (US)", "British English phrases", "master's dissertation (thesis)")
            val ruPath = arrayOf("Russian", "Грамматика", "Глагол и причастие")

            val tree = jTree("English (US)")
            assert(tree.path(*enPath).hasPath())
            tree.path(*enPath).clickPath()

            val linkPanel: JPanel = findComponentWithTimeout(Timeout.timeout(5)) { it.name == "GRAZI_LINK_PANEL" }
            assert(linkPanel.isVisible)

            val search = rulesSearchField()

            search.setText("грамматика")
            assert(!tree.path(*enPath).hasPath())
            search.deleteText()
            assert(tree.path(*enPath).hasPath())
            assert(tree.path(*enPath).isPathSelected())

            search.setText("грамматика")
            checkboxTree(*ruPath).clickCheckbox()
            assert(!linkPanel.isVisible)
            search.deleteText()
            assert(!checkboxTree(*ruPath, timeout = Timeout.timeout(5)).isSelected())
        }
    }

    @Test
    fun `test default disabled rules`() {
        settings("Cancel") {
            actionButton("Add").click()
            popupMenu("Russian").clickSearchedItem()

            assert(!checkboxTree("English (US)", "Capitalization", "Checks that a sentence starts with an uppercase letter").isSelected())
            assert(!checkboxTree("English (US)", "Creative Writing", "Creative Writing: E-Prime: all 'to be' forms").isSelected())
            assert(!checkboxTree("English (US)", "Text Analysis", "Readability: Too difficult text").isSelected())
            assert(checkboxTree("English (US)", "Grammar", "'kind/type/sort of a/an'").isSelected())

            assert(!checkboxTree("Russian", "Miscellaneous", "Проверка на использование тире вместо дефиса (то есть «из — за» вместо «из-за»).").isSelected())
            assert(!checkboxTree("Russian", "Стиль", "Благозвучность").isSelected())
            assert(checkboxTree("Russian", "Грамматика", "Глагол и причастие").isSelected())
        }
    }
}
