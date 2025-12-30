// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.polySymbols.search.PsiSourcedPolySymbol

interface Angular2PsiSourcedSymbol : Angular2Symbol, PsiSourcedPolySymbol {

  val project: Project

  override fun createPointer(): Pointer<out Angular2PsiSourcedSymbol>
}
