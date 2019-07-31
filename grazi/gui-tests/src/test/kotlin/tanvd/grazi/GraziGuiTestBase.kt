package tanvd.grazi

import com.intellij.testGuiFramework.fixtures.FileEditorFixture
import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.fixtures.JDialogFixture
import com.intellij.testGuiFramework.impl.*
import com.intellij.ui.SearchTextField
import org.fest.swing.fixture.JTextComponentFixture
import org.fest.swing.timing.Timeout

abstract class GraziGuiTestBase : GuiTestCase() {
    companion object {
        val langs = listOf("Chinese", "Dutch", "English (Canadian)", "English (GB)", "English (US)",
                "French", "German (Austria)", "German (Germany)", "Greek", "Italian", "Japanese", "Persian", "Polish",
                "Portuguese (Brazil)", "Portuguese (Portugal)", "Romanian", "Russian", "Slovak", "Spanish", "Ukrainian")
    }

    fun settings(button: String = "OK", body: JDialogFixture.() -> Unit) {
        simpleProject {
            settings(button, body)
        }
    }

    fun IdeFrameFixture.settings(button: String = "OK", body: JDialogFixture.() -> Unit) {
        waitAMoment()
        openIdeSettings()
        settingsDialog {
            jTree("Tools", "Grazi").clickPath()
            body()
            button(button).click()
        }
    }

    fun JDialogFixture.rulesSearchField(): JTextComponentFixture {
        val field: SearchTextField = findComponentWithTimeout(Timeout.timeout(5)) { it.name == "GRAZI_RULES_SEARCH" }
        return JTextComponentFixture(robot(), field.textEditor)
    }

    fun IdeFrameFixture.enableGit() {
        invokeMainMenu("Start.Use.Vcs")
        dialog("Enable Version Control Integration") {
            combobox("Select a version control system to associate with the project root:").selectItem("Git")
            button("OK").click()
        }
    }

    fun IdeFrameFixture.openTestFile() {
        projectView {
            path("SimpleProject", "src", "Main.md").doubleClick()
        }
    }

    fun IdeFrameFixture.gitEditor(body: FileEditorFixture.() -> Unit) {
        invokeMainMenu("CheckinProject")
        dialog("Commit Changes") {
            editor {
                body()
            }

            button("Cancel").click()
        }
    }
}

