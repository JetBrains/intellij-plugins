// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.find.usages.api.SearchTarget
import com.intellij.find.usages.api.UsageHandler
import com.intellij.model.Pointer
import com.intellij.navigation.TargetPresentation
import com.intellij.webSymbols.WebSymbolOrigin
import org.jetbrains.vuejs.model.VueScopeElement

abstract class VueScopeElementSymbol<T : VueScopeElement>(name: String, item: T) :
  VueDocumentedItemSymbol<T>(name, item), SearchTarget {

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbol<T>>

  override val origin: WebSymbolOrigin =
    VueScopeElementOrigin(item)

  override val usageHandler: UsageHandler
    get() = UsageHandler.createEmptyUsageHandler(presentation.presentableText)

  override fun presentation(): TargetPresentation {
    return presentation
  }
}