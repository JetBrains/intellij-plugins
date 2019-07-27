package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.checkbox
import com.intellij.testGuiFramework.impl.waitAMoment
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test
import tanvd.grazi.GraziGuiTestBase


@RunWithIde(CommunityIde::class)
class SpellcheckGuiTest : GraziGuiTestBase() {
    @Test
    fun `test spellcheck checkbox`() {
        simpleProject {
            waitAMoment()

            openTestFile()
            enableGit()

            editor {
                waitAMoment()
                moveToLine(1)
                typeText("// text with eror")

                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            settings { } // FIXME workaround for check highlights in git dialog

            gitEditor {
                waitAMoment()
                moveToLine(1)
                typeText("text with eror")

                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(!isSelected)
                    click()
                }
            }

            editor {
                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
            }

            gitEditor {
                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(isSelected)
                    click()
                }
            }

            editor {
                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            gitEditor {
                waitAMoment()
                waitForCodeAnalysisHighlightCount(HighlightSeverity.INFORMATION, 1)
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }
        }
    }
}
