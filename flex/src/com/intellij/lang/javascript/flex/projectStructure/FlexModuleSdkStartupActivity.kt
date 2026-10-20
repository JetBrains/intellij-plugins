// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure

import com.intellij.lang.javascript.flex.projectStructure.model.impl.isModuleSdkSyncNeeded
import com.intellij.lang.javascript.flex.projectStructure.model.impl.syncAllModuleSdks
import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.application.readAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * Makes the module SDK of every Flex module follow its active build configuration. An `.iml` file from an older version can still name
 * the removed composite SDK `__CompositeFlexSdk__`, and the SDK table can change while the project is closed.
 */
internal class FlexModuleSdkStartupActivity : ProjectActivity {
  override suspend fun execute(project: Project) {
    if (!readAction { isModuleSdkSyncNeeded(project) }) return
    edtWriteAction { syncAllModuleSdks(project) }
  }
}
