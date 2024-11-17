// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.staticAnalysis.inspections.targets

import com.intellij.openapi.components.PathMacroManager
import com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.qodana.staticAnalysis.scopes.QodanaAnalysisScope

interface TestTarget {
  val name: String
  val inspections: List<String>
  val threshold: Int
  fun contains(inspection: String, url: String): Boolean
}

class SingleScopeTestTarget(override val name: String,
                            override val inspections: List<String>,
                            override val threshold: Int,
                            private val scope: QodanaAnalysisScope,
                            private val macroManager: PathMacroManager
) : TestTarget {

  override fun contains(inspection: String, url: String): Boolean {
    val virtualFile = VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPath(url)) ?: return false
    return inspections.contains(inspection) && scope.contains(virtualFile)
  }
}

@Suppress("unused")
class MultiScopeTestTarget(override val name: String,
                           private val tools: Map<String, QodanaAnalysisScope>,
                           override val threshold: Int,
                           private val macroManager: PathMacroManager) : TestTarget {
  override val inspections = tools.keys.toList()

  override fun contains(inspection: String, url: String): Boolean {
    val virtualFile = VirtualFileManager.getInstance().findFileByUrl(macroManager.expandPath(url)) ?: return false
    val scope = tools[inspection] ?: return false
    return scope.contains(virtualFile)
  }
}