// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.javascript.web.symbols.FrameworkId
import com.intellij.javascript.web.symbols.WebSymbolsContainer
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.model.Pointer
import com.intellij.navigation.TargetPresentation
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.model.VuePlugin
import org.jetbrains.vuejs.model.VueScopeElement
import org.jetbrains.vuejs.web.VueFramework

abstract class VueScopeElementSymbol<T : VueDocumentedItem>(matchedName: String, item: T) :
  VueDocumentedItemSymbol<T>(matchedName, item), SearchTarget {

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbol<T>>

  override val origin: WebSymbolsContainer.Origin =
    object : WebSymbolsContainer.Origin {

      private val info: Pair<String?, String?>? by lazy(LazyThreadSafetyMode.NONE) {
        (item as VueScopeElement).parents
          .takeIf { it.size == 1 }
          ?.get(0)
          ?.asSafely<VuePlugin>()
          ?.let { Pair(it.moduleName, it.moduleVersion) }
        ?: item.source
          ?.containingFile
          ?.virtualFile
          ?.let { PackageJsonUtil.findUpPackageJson(it) }
          ?.takeIf { NodeModuleUtil.isFromNodeModules(item.source!!.project, it) }
          ?.let { PackageJsonData.getOrCreate(it) }
          ?.let { Pair(it.name, it.version?.rawVersion) }
      }

      override val framework: FrameworkId
        get() = VueFramework.ID

      override val packageName: String?
        get() = info?.first

      override val version: String?
        get() = info?.second
    }

  override val usageHandler: UsageHandler<*>
    get() = UsageHandler.createEmptyUsageHandler(presentation.presentableText)

  override val presentation: TargetPresentation
    get() = super.presentation

}