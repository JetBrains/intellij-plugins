// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.model.Pointer

interface Angular2PsiSourcedSymbol : Angular2Symbol, PsiSourcedPolySymbol {
  override fun createPointer(): Pointer<out Angular2PsiSourcedSymbol>
}
