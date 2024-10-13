// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.*
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes
import org.jetbrains.jps.model.java.JavaResourceRootType
import org.jetbrains.jps.model.java.JavaSourceRootType

class ModulesDescriber : QodanaProjectDescriber {
  override val id: String = "Modules"

  override suspend fun description(project: Project): ModulesDescriptionList {
    val macroManager = PathMacroManager.getInstance(project)
    val modules = ModuleManager.getInstance(project).modules

    return ModulesDescriptionList(modules.map { ModulesDescription(it, macroManager) })
  }

  class ModulesDescriptionList(val modules: List<ModulesDescription>)

  @Suppress("unused")
  class ModulesDescription(module: Module, macroManager: PathMacroManager) {
    val name: String = module.name

    val contentEntries: List<ContentEntryDescription> =
      ModuleRootManager.getInstance(module).contentEntries.map { ContentEntryDescription(it, macroManager) }

    val orderEntries: List<OrderEntryDescription> =
      ModuleRootManager.getInstance(module).orderEntries.map { OrderEntryDescription(it) }
  }

  class OrderEntryDescription(orderEntry: OrderEntry) {
    val name = if (orderEntry is JdkOrderEntry) {
      orderEntry.jdkName
    }
    else {
      orderEntry.presentableName
    }

    val type: String = orderEntry.type()

    private fun OrderEntry.type(): String {
      return when (this) {
        is JdkOrderEntry -> "SDK"
        is LibraryOrderEntry -> "Library"
        is ModuleSourceOrderEntry -> "Own sources"
        is ModuleOrderEntry -> "Module"
        else -> "unknown"
      }
    }
  }


  @Suppress("unused")
  class ContentEntryDescription(contentEntry: ContentEntry, macroManager: PathMacroManager) {
    val path: String = macroManager.collapsePath(contentEntry.url)
    val excludePatterns: List<String> = contentEntry.excludePatterns
    val excludeFolders: List<SourceFolderDescription> =
      contentEntry.excludeFolders.map { SourceFolderDescription(macroManager.collapsePath(it.url), "Exclude", "") }
    val sourceFolders: List<SourceFolderDescription> =
      contentEntry.sourceFolders.map { SourceFolderDescription(macroManager.collapsePath(it.url), it.type(), it.packagePrefix) }


  }

  @Suppress("unused")
  class SourceFolderDescription(val path: String, val type: String, val packagePrefix: String)
}

fun SourceFolder.type(): String {
  return when (rootType) {
    JavaResourceRootType.RESOURCE -> "Resource"
    JavaResourceRootType.TEST_RESOURCE -> "TestResource"
    JavaSourceRootType.TEST_SOURCE -> "TestSource"
    JavaSourceRootType.SOURCE -> {
      if (jpsElement.getProperties(JavaModuleSourceRootTypes.SOURCES)?.isForGeneratedSources == true) "GeneratedSource"
      else "Source"
    }
    else -> "Source"
  }
}