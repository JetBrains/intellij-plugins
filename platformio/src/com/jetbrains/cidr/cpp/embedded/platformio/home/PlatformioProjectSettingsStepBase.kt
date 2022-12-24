package com.jetbrains.cidr.cpp.embedded.platformio.home

import com.intellij.ide.util.projectWizard.AbstractNewProjectStep
import com.intellij.ide.util.projectWizard.ProjectSettingsStepBase
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.SystemInfo
import com.intellij.platform.DirectoryProjectGenerator
import com.intellij.util.ConcurrencyUtil
import com.jetbrains.cidr.cpp.embedded.platformio.PlatformioConfigurable
import com.jetbrains.cidr.cpp.embedded.platformio.project.BoardInfo
import java.io.File
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class PlatformioProjectSettingsStepBase(projectGenerator: DirectoryProjectGenerator<Ref<BoardInfo?>>,
                                                 callback: AbstractNewProjectStep.AbstractCallback<Ref<BoardInfo?>>) :
  ProjectSettingsStepBase<Ref<BoardInfo?>>(projectGenerator, callback) {

  override fun isDumbAware(): Boolean = true

  enum class Presense { UNKNOWN, YES, NO }

  protected val platformioPresent = AtomicProperty(Presense.UNKNOWN)

  private val platformioWatcher: ScheduledExecutorService = ConcurrencyUtil.newSingleScheduledThreadExecutor("PlatformIO watcher")

  protected fun startPlatformioWatcher() {
    platformioWatcher.scheduleWithFixedDelay(
      {
        var pioExePath = PlatformioConfigurable.pioExePath()
        if (SystemInfo.isWindows) {
          pioExePath = "$pioExePath.exe"
        }
        val value = if (File(pioExePath).exists()) Presense.YES else Presense.NO
        if (platformioPresent.get() != value) {
          platformioPresent.set(value)
        }
      }, 0, 500, TimeUnit.MILLISECONDS
    )
  }

  override fun dispose() {
    platformioWatcher.shutdown()
    super.dispose()
  }
}