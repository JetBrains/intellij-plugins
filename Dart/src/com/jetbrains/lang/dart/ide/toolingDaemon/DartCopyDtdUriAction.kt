// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.toolingDaemon

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ide.CopyPasteManager

class DartCopyDtdUriAction : AnAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = e.project?.let { DartToolingDaemonService.getInstance(it).webSocketReady } ?: false
  }

  override fun actionPerformed(e: AnActionEvent) {
    e.project
      ?.let { project -> DartToolingDaemonService.getInstance(project).uri }
      ?.let { uri -> CopyPasteManager.copyTextToClipboard(uri) }
  }
}
