package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.util.NlsSafe
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import icons.ClionEmbeddedPlatformioIcons
import org.jetbrains.annotations.Nls
import javax.swing.Icon

abstract class PlatformioAction(text: () -> @Nls String,
                                toolTip: () -> @Nls String?,
                                icon: Icon? = ClionEmbeddedPlatformioIcons.LogoPlatformIO)
  : PlatformioActionBase(text, toolTip, icon) {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.project?.service<PlatformioWorkspace>()?.isInitialized == true
  }
}

class PlatformioMonitorAction : PlatformioAction({ ClionEmbeddedPlatformioBundle.message("task.monitor") }, { "pio device monitor" },
                                                 pioIcon(AllIcons.Nodes.Console)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformedKillAlreadyRunning(e, false, true, false, "device", "monitor")
}

class PlatformioPkgUpdateAction : PlatformioAction({ ClionEmbeddedPlatformioBundle.message("platformio.update") }, { "pio pkg update" },
                                                   pioIcon(AllIcons.Actions.Download)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformed(e, true, false, false, "pkg", "update")
}

class PlatformioCheckAction : PlatformioAction({ ClionEmbeddedPlatformioBundle.message("task.check") }, { "pio check" },
                                               pioIcon(AllIcons.Actions.ProjectWideAnalysisOn)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformed(e, false, true, true, "check")
}

open class PlatformioTargetAction(val target: String,
                                  @Nls text: () -> String,
                                  @Nls toolTip: () -> String?,
                                  icon: Icon? = ClionEmbeddedPlatformioIcons.LogoPlatformIO)
  : PlatformioAction(text, toolTip, icon) {

  init {
    templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
  }

  override fun actionPerformed(e: AnActionEvent) {
    actionPerformed(e, false, true, true, "run", "-t", target)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible =
      e.project?.service<PlatformioService>()?.isTargetActive(target) == true
  }
}

@NlsSafe
private const val UPLOAD_COMMAND = "pio run -t upload"
object PlatformioUploadAction : PlatformioTargetAction(target = "upload",
                                                       text = { ClionEmbeddedPlatformioBundle.message("action.upload") },
                                                       toolTip = { UPLOAD_COMMAND },
                                                       icon = ClionEmbeddedPlatformioIcons.LogoPlatformIO)

@NlsSafe
private const val UPLOAD_MONITOR_COMMAND = "pio run -t upload -t monitor"

object PlatformioUploadMonitorAction : PlatformioTargetAction(target = "upload-monitor",
                                                              text = { ClionEmbeddedPlatformioBundle.message("action.upload.n.monitor") },
                                                              toolTip = { UPLOAD_MONITOR_COMMAND },
                                                              icon = ClionEmbeddedPlatformioIcons.LogoPlatformIO) {

  override fun actionPerformed(e: AnActionEvent) {
    super.actionPerformedKillAlreadyRunning(e, false, true, true, "run", "-t", "upload", "-t", "monitor")
  }

}
