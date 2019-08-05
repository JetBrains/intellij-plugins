package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.util.step
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase
import kotlin.test.assertEquals


class NativeLanguageGuiTest : GraziGuiTestBase() {
    @Test
    fun `test native language combobox`() {
        project {
            settings {
                actionButton("Add").click()
                popupMenu("Russian").clickSearchedItem()

                waitADecentMoment()

                with(combobox("Native language:")) {
                    val lang = "English (US)"

                    step("Check English native language work") {
                        assertEquals(lang, selectedItem())

                        with(jTree(lang)) {
                            assert(path(lang, "False friends").hasPath())
                            assert(!path(lang, "Омонимы").hasPath())
                        }
                    }

                    step("Enable Russian native language and check settings") {
                        selectItem("Russian")
                        button("Apply").clickWhenEnabled()

                        with(jTree(lang)) {
                            assert(!path(lang, "False friends").hasPath())
                            assert(path(lang, "Омонимы").hasPath())
                        }
                    }

                    selectItem("English (US)")
                }

                jList("English (US)").selectItems("Russian")
                actionButton("Remove").click()
            }
        }
    }
}
