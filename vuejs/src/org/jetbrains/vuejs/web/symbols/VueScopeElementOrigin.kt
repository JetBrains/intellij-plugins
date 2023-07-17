// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.util.asSafely
import com.intellij.webSymbols.FrameworkId
import com.intellij.webSymbols.WebSymbolOrigin
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.VueScopeElement
import org.jetbrains.vuejs.web.VueFramework

class VueScopeElementOrigin(private val item: VueScopeElement) : WebSymbolOrigin {

  private val info: Pair<String?, String?>? by lazy(LazyThreadSafetyMode.PUBLICATION) {
    item.parents
      .takeIf { it.size == 1 }
      ?.get(0)
      ?.asSafely<VuePlugin>()
      ?.let { Pair(it.moduleName, it.moduleVersion) }
    ?: item.source
      ?.containingFile
      ?.virtualFile
      ?.let { PackageJsonUtil.findUpPackageJson(it) }
      ?.takeIf { NodeModuleUtil.hasNodeModulesDirInPath(it, null) }
      ?.let { PackageJsonData.getOrCreate(it) }
      ?.let { Pair(it.name, it.version?.rawVersion) }
  }

  override val framework: FrameworkId
    get() = VueFramework.ID

  override val library: String?
    get() = info?.first

  override val version: String?
    get() = info?.second
}