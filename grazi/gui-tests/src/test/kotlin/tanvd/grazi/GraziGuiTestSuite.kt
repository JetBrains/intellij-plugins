package tanvd.grazi

import com.intellij.testGuiFramework.fixtures.IdeFrameFixture
import com.intellij.testGuiFramework.fixtures.JDialogFixture
import com.intellij.testGuiFramework.framework.GuiTestSuite
import com.intellij.testGuiFramework.framework.GuiTestSuiteRunner
import com.intellij.testGuiFramework.impl.*
import com.intellij.ui.SearchTextField
import org.fest.swing.fixture.JTextComponentFixture
import org.fest.swing.timing.Timeout
import org.junit.runner.RunWith
import org.junit.runners.Suite

open class GraziGuiTestCase : GuiTestCase() {
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
}

