// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.grazie.ide.notification

import com.intellij.grazie.GrazieConfig
import com.intellij.grazie.GraziePlugin
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

open class GrazieNotificationComponent : StartupActivity.Background {
  override fun runActivity(project: Project) {
    GrazieConfig.update {
      if (!GraziePlugin.isBundled) {
        when {
          it.lastSeenVersion == null -> Notification.showInstallationMessage(project)
          GraziePlugin.version != it.lastSeenVersion -> Notification.showUpdateMessage(project)
        }
      }

      if (it.hasMissedLanguages()) Notification.showLanguagesMessage(project)

      it.update(lastSeenVersion = GraziePlugin.version)
    }
  }
}
