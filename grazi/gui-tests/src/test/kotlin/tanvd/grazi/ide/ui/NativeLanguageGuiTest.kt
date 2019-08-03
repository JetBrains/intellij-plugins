package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import com.intellij.testGuiFramework.util.Key
import com.intellij.testGuiFramework.util.step
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase
import kotlin.test.assertEquals


class NativeLanguageGuiTest : GraziGuiTestBase() {
    @Test
    fun `test native language combobox`() {
        project {
            settings {
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
                }
            }

            step("Check that Russian native language produces mistakes in an editor") {
                openTestFile()

                editor {
                    typeText("I love a baton")
                    shortcut(Key.ENTER)

                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "baton &rarr; loafЗначение омонимов: baton")
                }
            }

            step("Check that English native language does not produce equal mistake") {
                settings {
                    combobox("Native language:").selectItem("English (US)", ciTimeout)
                }

                editor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 0)
                    requireCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 0)
                }
            }
        }
    }
}
