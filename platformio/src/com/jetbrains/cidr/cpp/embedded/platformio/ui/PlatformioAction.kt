package com.jetbrains.cidr.cpp.embedded.platformio.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioService
import com.jetbrains.cidr.cpp.embedded.platformio.project.PlatformioWorkspace
import icons.ClionEmbeddedPlatformioIcons
import org.jetbrains.annotations.Nls
import java.util.function.Supplier
import javax.swing.Icon

abstract class PlatformioAction(text: Supplier<@Nls String?>,
                                toolTip: Supplier<@Nls String?>,
                                icon: Icon? = ClionEmbeddedPlatformioIcons.Platformio)
  : PlatformioActionBase(text, toolTip, icon) {

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.project?.service<PlatformioWorkspace>()?.isInitialized ?: false
  }
}

class PlatformioMonitorAction : PlatformioAction(ClionEmbeddedPlatformioBundle.messagePointer("task.monitor"), { "pio device monitor" },
                                                 pioIcon(AllIcons.Nodes.Console)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformed(e, false, true, false, "device", "monitor")
}

class PlatformioPkgUpdateAction : PlatformioAction(ClionEmbeddedPlatformioBundle.messagePointer("platformio.update"), { "pio pkg update" },
                                                   pioIcon(AllIcons.Actions.Download)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformed(e, true, false, false, "pkg", "update")
}

class PlatformioCheckAction : PlatformioAction(ClionEmbeddedPlatformioBundle.messagePointer("task.check"), { "pio check" },
                                               pioIcon(AllIcons.Actions.ProjectWideAnalysisOn)) {
  override fun actionPerformed(e: AnActionEvent) =
    actionPerformed(e, false, true, true, "check")
}

open class PlatformioTargetAction(val target: String,
                                  @Nls text: () -> String,
                                  toolTip: Supplier<@Nls String?>,
                                  icon: Icon? = ClionEmbeddedPlatformioIcons.Platformio)
  : PlatformioAction(text, toolTip, icon) {

  override fun displayTextInToolbar(): Boolean = true

  override fun actionPerformed(e: AnActionEvent) {
    actionPerformed(e, false, true, true, "run", "-t", target)
  }

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible =
      e.project?.service<PlatformioService>()?.visibleActions?.contains(target) ?: false
  }
}

object PlatformioUploadMonitorAction : PlatformioTargetAction(target = "upload-monitor",
                                                             text = { ClionEmbeddedPlatformioBundle.message("action.upload.n.monitor") },
                                                             toolTip = { "pio run -t upload -t monitor" },
                                                             icon = ClionEmbeddedPlatformioIcons.Platformio) {

  override fun actionPerformed(e: AnActionEvent) {
    super.actionPerformed(e, false, true, true, "run", "-t", "upload", "-t", "monitor")
  }
}
