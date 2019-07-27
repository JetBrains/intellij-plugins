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
                moveToLine(1)
                typeText("// text with eror")

                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            settings { } // FIXME workaround for check highlights in git dialog

            gitEditor {
                moveToLine(1)
                typeText("text with eror")

                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(!isSelected)
                    click()
                }
            }

            waitAMoment()

            editor {

                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
            }

            gitEditor {

                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(isSelected)
                    click()
                }
            }

            waitAMoment()

            editor {
                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }

            gitEditor {
                waitUntilErrorAnalysisFinishes()
                requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
            }
        }
    }
}
