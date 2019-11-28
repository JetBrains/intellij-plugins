package training.ui.welcomeScreen.recentProjects

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.wm.IdeFrame
import com.intellij.openapi.wm.WelcomeScreen
import com.intellij.openapi.wm.impl.welcomeScreen.FlatWelcomeFrame
import com.intellij.ui.components.panels.NonOpaquePanel
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.accessibility.AccessibleContextAccessor
import javax.swing.JPanel

class IFTFlatWelcomeFrame : FlatWelcomeFrame(), IdeFrame, Disposable, AccessibleContextAccessor {

    private val originalWelcomeScreen: JPanel
        get() {
            return UIUtil.findComponentsOfType(component, JPanel::class.java).first { it is WelcomeScreen }
        }
    private val originalCenterWelcomeScreen: NonOpaquePanel
        get() {
            return UIUtil.uiChildren(originalWelcomeScreen).filterIsInstance(NonOpaquePanel::class.java).first()
        }
    private val originalRecentProjectsPanel: JPanel?
        get() {
            return UIUtil.uiChildren(originalWelcomeScreen).filterNot { it == originalCenterWelcomeScreen }.firstOrNull() as JPanel?
        }

    init {
        if (showCustomWelcomeScreen)
            replaceRecentProjectsPanel()
    }

    private fun replaceRecentProjectsPanel() {
        getRootPane().preferredSize = JBUI.size(MAX_DEFAULT_WIDTH, height)
        val groupsPanel = GroupsPanel(ApplicationManager.getApplication()).customizeActions()
        val recentProjectPanel = UIUtil.uiChildren(originalRecentProjectsPanel).first()
        originalRecentProjectsPanel?.remove(recentProjectPanel)
        originalRecentProjectsPanel?.add(groupsPanel)
        repaint()
    }
}

internal val showCustomWelcomeScreen
    get() = Registry.`is`("ideFeaturesTrainer.welcomeScreen.tutorialsTree")
