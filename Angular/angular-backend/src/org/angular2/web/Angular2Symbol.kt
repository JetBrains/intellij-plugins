// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_ICON
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport.Companion.PROP_TYPE_SUPPORT
import icons.AngularIcons
import org.angular2.Angular2Framework
import javax.swing.Icon

interface Angular2Symbol : HtmlFrameworkSymbol {

  override val framework: FrameworkId
    get() = Angular2Framework.ID

  override val icon: Icon?
    get() = AngularIcons.Angular2

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_DOC_HIDE_ICON -> property.tryCast(true)
      PROP_TYPE_SUPPORT -> property.tryCast(TypeScriptSymbolTypeSupport.default)
      else -> super.get(property)
    }

  override fun createPointer(): Pointer<out Angular2Symbol>

}
