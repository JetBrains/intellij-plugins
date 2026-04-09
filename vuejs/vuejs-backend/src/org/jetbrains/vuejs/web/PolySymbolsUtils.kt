// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.web

import com.intellij.javascript.nodejs.library.node_modules.NodeModulesDirectoryManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.html.elements.HtmlElementSymbolDescriptor
import com.intellij.psi.stubs.StubIndex
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.model.VueModelVisitor
import org.jetbrains.vuejs.web.symbols.VueComponentWithProximity
import org.jetbrains.vuejs.web.symbols.VueDirectiveWithProximity

fun HtmlElementSymbolDescriptor.getModel(): VueModelDirectiveProperties =
  runListSymbolsQuery(VUE_MODEL, true).firstOrNull()
    ?.let {
      VueModelDirectiveProperties(prop = it[VueModelPropProperty],
                                  event = it[VueModelEventProperty])
    }
  ?: VueModelDirectiveProperties()

fun VueModelVisitor.Proximity.asPolySymbolPriority(): PolySymbol.Priority =
  when (this) {
    VueModelVisitor.Proximity.LOCAL -> PolySymbol.Priority.HIGHEST
    VueModelVisitor.Proximity.APP -> PolySymbol.Priority.HIGH
    VueModelVisitor.Proximity.LIBRARY, VueModelVisitor.Proximity.GLOBAL -> PolySymbol.Priority.NORMAL
    VueModelVisitor.Proximity.OUT_OF_SCOPE -> PolySymbol.Priority.LOW
  }

fun PolySymbol.unwrapVueSymbolWithProximity(): PolySymbol =
  (this as? VueComponentWithProximity)?.delegate
  ?: (this as? VueDirectiveWithProximity)?.delegate
  ?: this

internal fun isVueComponentQuery(name: String): Boolean {
  return name.getOrNull(0)?.isUpperCase() == true || name.contains('-') || name == "slot"
}

internal fun getVueSymbolsCacheDependencies(project: Project, withPsiModTracker: Boolean = true): Set<Any> =
  setOfNotNull(
    PsiModificationTracker.MODIFICATION_COUNT.takeIf { withPsiModTracker },
    VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
    DumbService.getInstance(project).modificationTracker,
    StubIndex.getInstance().getStubIndexModificationTracker(project),
    NodeModulesDirectoryManager.getInstance(project).nodeModulesDirChangeTracker,
  )