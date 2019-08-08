package tanvd.grazi

import com.intellij.testGuiFramework.fixtures.*
import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.*
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import com.intellij.ui.SearchTextField
import org.fest.swing.exception.WaitTimedOutError
import org.fest.swing.fixture.JTextComponentFixture
import org.fest.swing.timing.Pause
import org.fest.swing.timing.Timeout
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

abstract class GraziGuiTestBase : GuiTestCase(false) {
    protected val ciTimeout = Timeout.timeout(3, TimeUnit.MINUTES)

    companion object {
        private val logger = LoggerFactory.getLogger(GraziGuiTestBase::class.java)
    }

    private fun waitForFirstIndexing() {
        ideFrame {
            val secondToWaitIndexing = 10
            try {
                waitForStartingIndexing(secondToWaitIndexing)
            } catch (timedOutError: WaitTimedOutError) {
                logger.warn("Waiting for indexing has been exceeded $secondToWaitIndexing seconds")
            }
            waitForBackgroundTasksToFinish()
        }
    }

    fun GraziGuiTestBase.project(body: IdeFrameFixture.() -> Unit) {
        simpleProject {
            waitForFirstIndexing()
            body(this)
        }
    }

    fun settings(button: String = "OK", body: JDialogFixture.() -> Unit) {
        project {
            settings(button, body)
        }
    }

    fun IdeFrameFixture.settings(button: String = "OK", body: JDialogFixture.() -> Unit) {
        openIdeSettings().apply {
            settingsDialog(ciTimeout) {
                jTree("Tools", "Grazi", timeout = ciTimeout).clickPath()
                body()
                button(button, timeout = ciTimeout).click()
            }
        }
    }

    fun JDialogFixture.rulesSearchField(): JTextComponentFixture {
        val field: SearchTextField = findComponentWithTimeout(ciTimeout) { it.name == "GRAZI_RULES_SEARCH" }
        return JTextComponentFixture(robot(), field.textEditor)
    }


    fun IdeFrameFixture.openTestFile() {
        projectView {
            path("SimpleProject", "src", "Main.md").doubleClick()
            waitForBackgroundTasksToFinish()
        }
    }

    fun IdeFrameFixture.gitEditor(body: FileEditorFixture.() -> Unit) {
        invokeMainMenu("CheckinProject")
        waitAMoment()

        dialog("Commit Changes") {
            editor {
                body()
            }

            button("Cancel").click()
        }
    }

    fun GraziGuiTestBase.waitADecentMoment() {
        Pause.pause(10, TimeUnit.SECONDS)
        waitAMoment()
    }

    fun IdeFrameFixture.enableGit() {
        invokeMainMenu("Start.Use.Vcs")
        waitAMoment()

        dialog("Enable Version Control Integration") {
            combobox("Select a version control system to associate with the project root:").selectItem("Git")
            button("OK").click()
        }
    }
}

