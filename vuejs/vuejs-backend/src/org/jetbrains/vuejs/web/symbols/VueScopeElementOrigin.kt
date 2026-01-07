// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.util.asSafely
import org.jetbrains.vuejs.model.VueLibrary
import org.jetbrains.vuejs.model.VueScopeElement

class VueScopeElementOrigin(private val item: VueScopeElement) : PolySymbolOrigin {

  private val info: Pair<String?, String?>? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    item.parents
      .takeIf { it.size == 1 }
      ?.get(0)
      ?.asSafely<VueLibrary>()
      ?.let { Pair(it.moduleName, it.moduleVersion) }
    ?: item.source
      ?.containingFile
      ?.virtualFile
      ?.let { PackageJsonUtil.findUpPackageJson(it) }
      ?.takeIf { NodeModuleUtil.hasNodeModulesDirInPath(it, null) }
      ?.let { PackageJsonData.getOrCreate(it) }
      ?.let { Pair(it.name, it.version?.rawVersion) }
  }

  override val library: String?
    get() = info?.first

  override val version: String?
    get() = info?.second
}