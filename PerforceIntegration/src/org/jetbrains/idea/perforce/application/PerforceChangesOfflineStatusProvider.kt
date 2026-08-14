// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.perforce.application

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeListManagerStatusProvider
import com.intellij.ui.HyperlinkLabel
import com.intellij.ui.JBColor
import org.jetbrains.idea.perforce.PerforceBundle
import javax.swing.JComponent
import javax.swing.event.HyperlinkEvent

internal class PerforceChangesOfflineStatusProvider : ChangeListManagerStatusProvider {
  override fun getStatusComponent(project: Project): JComponent? {
    val settings = PerforceVcs.getInstance(project).settings
    if (settings.ENABLED) return null

    return HyperlinkLabel().apply {
      setForeground(JBColor.RED)
      setHyperlinkText(PerforceBundle.message("connection.offline") + ' ', PerforceBundle.message("connection.go.online"), "")
      addHyperlinkListener { e ->
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
          if (!settings.ENABLED) {
            settings.enable()
          }
        }
      }
    }
  }
}
