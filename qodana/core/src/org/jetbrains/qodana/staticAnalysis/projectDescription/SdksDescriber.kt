// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.JdkOrderEntry
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.OrderEntry
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.module.ModuleManager

class SdksDescriber : QodanaProjectDescriber {
  override val id: String = "SDKs"

  override suspend fun description(project: Project): SdkDescriptionList {
    val macroManager = PathMacroManager.getInstance(project)
    val entries = mutableListOf<JdkOrderEntry>()

    ProjectRootManager.getInstance(project).orderEntries().sdkOnly().forEach { entry ->
      // An inherited entry of a project without a project SDK names no SDK, so it has nothing to describe.
      (entry as? JdkOrderEntry)?.takeIf { it.jdkName != null }?.also { entries.add(it) }
      true
    }
    val sdks = entries.distinctBy { it.jdkName }.map { SdkDescription(it, macroManager) }
    return SdkDescriptionList(sdks + sdkLibraries(project, macroManager))
  }

  /** The library entries that stand for an SDK, see [QodanaSdkLibraryProvider]. */
  private fun sdkLibraries(project: Project, macroManager: PathMacroManager): List<SdkDescription> {
    val providers = QodanaSdkLibraryProvider.EP_NAME.extensionList
    if (providers.isEmpty()) return emptyList()
    return ModuleManager.getInstance(project).modules.asSequence()
      .flatMap { ModuleRootManager.getInstance(it).orderEntries.asSequence() }
      .filterIsInstance<LibraryOrderEntry>()
      .mapNotNull { entry -> providers.firstNotNullOfOrNull { it.sdkOf(entry) }?.let { entry to it } }
      .distinctBy { (entry, _) -> entry.libraryName }
      .map { (entry, sdk) ->
        SdkDescription(
          name = entry.libraryName,
          roots = entry.getRootUrls(OrderRootType.CLASSES).map { macroManager.collapsePathNonNull(it) },
          sdkType = sdk.type,
          sdkVersion = sdk.version,
        )
      }
      .toList()
  }

  @Suppress("unused")
  class SdkDescriptionList(val sdks: List<SdkDescription>)

  @Suppress("unused")
  class SdkDescription(val name: String?, val roots: List<String>, val sdkType: String?, val sdkVersion: String?) {
    constructor(sdkEntry: JdkOrderEntry, macroManager: PathMacroManager) : this(
      name = sdkEntry.jdkName,
      roots = sdkEntry.getRootUrls(OrderRootType.CLASSES).map { macroManager.collapsePathNonNull(it) },
      sdkType = sdkEntry.jdk?.sdkType?.name,
      sdkVersion = sdkEntry.jdk?.versionString,
    )
  }
}

/**
 * Tells [SdksDescriber] that a library order entry stands for an SDK. A language whose modules name their SDK by a library
 * entry, such as the interpreter library of a Python module, reports that library as an SDK through it.
 */
interface QodanaSdkLibraryProvider {
  /** The SDK that [entry] stands for, or `null` when it is a plain library. */
  fun sdkOf(entry: OrderEntry): SdkLibrary?

  /** The type and the version of the SDK of a library entry, as [SdksDescriber] reports them. */
  data class SdkLibrary(val type: String, val version: String?)

  companion object {
    val EP_NAME: ExtensionPointName<QodanaSdkLibraryProvider> = ExtensionPointName("org.intellij.qodana.sdkLibraryProvider")
  }
}