package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.checkbox
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import com.intellij.testGuiFramework.util.Key
import com.intellij.testGuiFramework.util.step
import org.junit.Ignore
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase


class SpellcheckGuiTest : GraziGuiTestBase() {
    @Test
    fun `test spellcheck checkbox in editor`() {
        project {
            openTestFile()

            step("Check that Typo works by default and Grazi spellchecker not") {
                editor {
                    typeText("text with eror")
                    shortcut(Key.ENTER)

                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }

            step("Enabled Grazi spellchecker") {
                settings {
                    with(checkbox("Enable Grazi spellchecker")) {
                        assert(!isSelected)
                        click()
                    }
                }
            }

            waitADecentMoment()

            step("Check that Grazi spellchecker enabled and Typo is disabled") {
                editor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                }
            }

            step("Disable Grazi spellchecker") {
                settings {
                    with(checkbox("Enable Grazi spellchecker")) {
                        assert(isSelected)
                        click()
                    }
                }
            }

            waitADecentMoment()

            step("Check that Typo works and Grazi spellchecker not") {
                editor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }
        }
    }

    @Test
    @Ignore
    fun `test spellcheck checkbox in git`() {
        project {
            enableGit()

            step("Check that Typo works by default and Grazi spellchecker not") {
                settings { } // FIXME workaround for check highlights in git dialog
                settings { }

                gitEditor {
                    typeText("text with eror")
                    shortcut(Key.ENTER)

                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }

            step("Enabled Grazi spellchecker") {
                settings {
                    with(checkbox("Enable Grazi spellchecker")) {
                        assert(!isSelected)
                        click()
                    }
                }
            }

            waitADecentMoment()

            step("Check that Grazi spellchecker enabled and Typo is disabled") {
                gitEditor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                }
            }

            step("Disable Grazi spellchecker") {
                settings {
                    with(checkbox("Enable Grazi spellchecker")) {
                        assert(isSelected)
                        click()
                    }
                }
            }

            waitADecentMoment()

            step("Check that Typo works and Grazi spellchecker not") {
                gitEditor {
                    waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }
        }
    }
}
