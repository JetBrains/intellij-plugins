package tanvd.grazi.ide.ui

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test


@RunWithIde(CommunityIde::class)
class SpellcheckGuiTest : GraziGuiTestCase() {
    @Test
    fun `test spellcheck checkbox`() {
        simpleProject {
            waitAMoment()

            ideFrame {
                projectView {
                    path("SimpleProject", "src", "Main.kt").doubleClick()
                }

                editor {
                    moveToLine(1)
                    typeText("// text with eror")
                    waitAMoment()

                    requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                }
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(!isSelected)
                    click()
                }
            }

            ideFrame {
                editor {
                    waitAMoment()
                    requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                }
            }

            ideFrame {
                invokeMainMenu("Start.Use.Vcs")
                dialog("Enable Version Control Integration") {
                    combobox("Select a version control system to associate with the project root:").selectItem("Git")
                    button("OK").click()
                }

                invokeMainMenu("CheckinProject")
                dialog("Commit Changes") {
                    editor {
                        moveToLine(1)
                        typeText("text with eror")
                        waitAMoment()

                        requireHighlights(HighlightSeverity.INFORMATION, "Possible spelling mistake")
                    }

                    button("Cancel").click()
                }
            }

            settings {
                with(checkbox("Enable Grazi spellchecker")) {
                    assert(isSelected)
                    click()
                }
            }

            waitAMoment()
            ideFrame {
                invokeMainMenu("CheckinProject")
                dialog("Commit Changes") {
                    editor {
                        requireHighlights(HighlightSeverity.INFORMATION, "Typo: In word 'eror'")
                    }

                    button("Cancel").click()
                }
            }
        }
    }
}
