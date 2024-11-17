// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager

class SdksDescriber : QodanaProjectDescriber {
  override val id: String = "SDKs"

  override suspend fun description(project: Project): SdkDescriptionList {
    val macroManager = PathMacroManager.getInstance(project)
    val entries = mutableListOf<JdkOrderEntry>()

    ProjectRootManager.getInstance(project).orderEntries().sdkOnly().forEach { entry ->
      (entry as? JdkOrderEntry)?.also { entries.add(it) }
      true
    }
    return SdkDescriptionList(entries.distinctBy { it.jdkName }.map { SdkDescription(it, macroManager) })
  }

  @Suppress("unused")
  class SdkDescriptionList(val sdks: List<SdkDescription>)

  @Suppress("unused")
  class SdkDescription(sdkEntry: JdkOrderEntry, macroManager: PathMacroManager) {
    val name: String? = sdkEntry.jdkName
    val roots: List<String> = sdkEntry.getRootUrls(OrderRootType.CLASSES).map { macroManager.collapsePath(it) }
    val sdkType = sdkEntry.jdk?.sdkType?.name
    val sdkVersion = sdkEntry.jdk?.versionString
  }
}