package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.ide.impl.ProjectUtil
import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.SystemInfo
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ConcurrencyUtil
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.NamedColorUtil
import com.jetbrains.cidr.cpp.embedded.platformio.ClionEmbeddedPlatformioBundle
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable
import com.jetbrains.cidr.cpp.embedded.platformio.project.BoardInfo
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenInstallGuide
import com.jetbrains.cidr.cpp.embedded.platformio.ui.OpenSettings
import java.awt.event.ActionListener
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.JPanel
import javax.swing.SwingUtilities

class PlatformioProjectSettingsDialogStep(projectGenerator: DirectoryProjectGenerator<Ref<BoardInfo?>>,
                                          callback: AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo?>>) :
  PlatformioProjectSettingsStepBase(projectGenerator, callback) {

  override fun checkValid(): Boolean = true

  override fun createPanel(): JPanel {
    myLazyGeneratorPeer = createLazyPeer()
    startPlatformioWatcher()
    platformioPresent.afterChange {
      SwingUtilities.invokeLater {
        actionButton.isEnabled = platformioPresent.get() == Presense.YES
      }
    }

    myCreateButton = createActionButton()
    myCreateButton.addActionListener(ActionListener {

      val platformioHomeDialog = PlatformioHomeDialog(null, myCreateButton)
      if (platformioHomeDialog.showAndGet()) {
        val dialog = DialogWrapper.findInstance(myCreateButton)
        dialog?.close(OK_EXIT_CODE)
        var location = platformioHomeDialog.getProjectLocationToOpen()
        if (location == null) location = platformioHomeDialog.getDocumentLocationToOpen()
        if (location != null) {
          ProjectUtil.openOrImport(Paths.get(location))
        }
      }
    })
    myCreateButton.isEnabled = false
    return panel {
      row {
        @Suppress("DialogTitleCapitalization")
        label(ClionEmbeddedPlatformioBundle.message("label.checking.platformio"))
      }.visibleIf(platformioPresent.transform { it == Presense.UNKNOWN })
      row {
        panel {
          row {
            label(ClionEmbeddedPlatformioBundle.message("label.platformio.detected"))
              .applyToComponent { icon = AllIcons.General.InspectionsOK }
          }
          indent {
            row {
              @Suppress("DialogTitleCapitalization")
              text(ClionEmbeddedPlatformioBundle.message("label.start.platformio.home"))
            }
            row {
              label(ClionEmbeddedPlatformioBundle.message("label.ide.will.run.platformio.script"))
                .applyToComponent {
                  icon = AllIcons.General.Warning
                  font = JBFont.small()
                  foreground = NamedColorUtil.getInactiveTextColor()
                }
            }
          }
        }
      }.visibleIf(platformioPresent.transform { it == Presense.YES })
      row {
        panel {
          row {
            label(ClionEmbeddedPlatformioBundle.message("label.platformio.not.found"))
              .applyToComponent { icon = AllIcons.General.Error }
          }
          indent {
            row {
              text(
                ClionEmbeddedPlatformioBundle.message("label.follow.href.official.guide.or.provide.path.in.platformio.settings",
                                                      OpenInstallGuide.URL))
              {
                if (it.url != null) {
                  BrowserUtil.browse(it.url)
                }
                else {
                  OpenSettings(null).run()
                }
              }
            }
          }
        }
      }.visibleIf(platformioPresent.transform { it == Presense.NO })
    }
  }
}