package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test
import kotlin.test.assertEquals


@RunWithIde(CommunityIde::class)
class NativeLanguageGuiTest : GraziGuiTestCase() {
    @Test
    fun `test native language combobox`() {
        settings {
            with(combobox("Native language:")) {
                val lang = "English (US)"
                assertEquals(lang, selectedItem())
                assertEquals(langs, listItems())

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

                selectItem(lang)
            }
        }
    }
}
