package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase
import kotlin.test.assertEquals


@RunWithIde(CommunityIde::class)
class NativeLanguageGuiTest : GraziGuiTestBase() {
    @Test
    fun `test native language combobox`() {
        settings {
            actionButton("Add").click()
            popupMenu("Russian").clickSearchedItem()

            waitAMoment()

            with(combobox("Native language:")) {
                val lang = "English (US)"
                assertEquals(lang, selectedItem())

                with(jTree(lang)) {
                    assert(path(lang, "False friends").hasPath())
                    assert(!path(lang, "Омонимы").hasPath())
                }

                selectItem("Russian")
                button("Apply").clickWhenEnabled()

                with(jTree(lang)) {
                    assert(!path(lang, "False friends").hasPath())
                    assert(path(lang, "Омонимы").hasPath())
                }

                selectItem("English (US)")
                actionButton("Remove").click()
            }
        }
    }
}
