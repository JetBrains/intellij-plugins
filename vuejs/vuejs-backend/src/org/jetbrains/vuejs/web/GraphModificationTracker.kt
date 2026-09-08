// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.typescript.tsconfig.TypeScriptConfigService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker

internal fun optimizedGraphModificationTracker(
  project: Project,
): ModificationTracker =
  project.service<TypeScriptConfigService>().graphModificationTracker
