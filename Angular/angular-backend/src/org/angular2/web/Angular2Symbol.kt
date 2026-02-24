// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbol.DocHideIconProperty
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import com.intellij.polySymbols.js.types.TypeScriptSymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport
import com.intellij.polySymbols.utils.PolySymbolTypeSupport.TypeSupportProperty
import icons.AngularIcons
import org.angular2.Angular2Framework
import javax.swing.Icon

interface Angular2Symbol : HtmlFrameworkSymbol {

  override val framework: FrameworkId
    get() = Angular2Framework.ID

  override val icon: Icon?
    get() = AngularIcons.Angular2

  @PolySymbol.Property(DocHideIconProperty::class)
  val docHideIcon: Boolean
    get() = true

  @PolySymbol.Property(TypeSupportProperty::class)
  val typeSupport: PolySymbolTypeSupport
    get() = TypeScriptSymbolTypeSupport.default

  override fun createPointer(): Pointer<out Angular2Symbol>

}
