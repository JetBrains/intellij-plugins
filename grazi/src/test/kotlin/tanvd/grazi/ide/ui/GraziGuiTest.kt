package tanvd.grazi.ide.ui

import com.intellij.testGuiFramework.framework.RunWithIde
import com.intellij.testGuiFramework.impl.GuiTestCase
import com.intellij.testGuiFramework.impl.button
import com.intellij.testGuiFramework.impl.jTree
import com.intellij.testGuiFramework.impl.waitAMoment
import com.intellij.testGuiFramework.launcher.ide.CommunityIde
import org.junit.Test


@RunWithIde(CommunityIde::class)
class GraziGuiTest : GuiTestCase() {

    @Test
    fun test() {
        simpleProject {
            waitAMoment()
            openIdeSettings()
            settingsDialog {
                jTree("Tools", "Grazi").clickPath()
                button("OK").click()
            }
        }
    }

}
