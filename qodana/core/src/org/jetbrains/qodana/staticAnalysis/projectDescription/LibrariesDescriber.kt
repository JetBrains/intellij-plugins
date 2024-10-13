// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.projectDescription

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.roots.impl.libraries.LibraryEx
import com.intellij.openapi.roots.libraries.LibraryProperties

class LibrariesDescriber : QodanaProjectDescriber {
  override val id: String = "Libraries"

  override suspend fun description(project: Project): LibrariesDescriptionList {
    val macroManager = PathMacroManager.getInstance(project)
    val entries = mutableListOf<LibraryOrderEntry>()

    ProjectRootManager.getInstance(project).orderEntries().librariesOnly().forEach { entry ->
      (entry as? LibraryOrderEntry)?.also { entries.add(it) }
      true
    }
    return LibrariesDescriptionList(entries.distinctBy { it.libraryName }.map { LibraryDescription(it, macroManager) })
  }

  class LibrariesDescriptionList(val libraries: List<LibraryDescription>)

  @Suppress("unused")
  class LibraryDescription(libraryOrderEntry: LibraryOrderEntry, macroManager: PathMacroManager) {
    val name: String? = libraryOrderEntry.libraryName
    val roots: List<String> = libraryOrderEntry.getRootUrls(OrderRootType.CLASSES).map { macroManager.collapsePath(it) }
    val libraryKind = (libraryOrderEntry as? LibraryEx)?.kind?.kindId
    val properties: LibraryProperties<*>? = (libraryOrderEntry.library as? LibraryEx)?.properties
  }
}