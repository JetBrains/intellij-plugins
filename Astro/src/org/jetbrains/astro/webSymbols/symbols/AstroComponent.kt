// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.webSymbols.symbols

import com.intellij.model.Pointer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.*
import com.intellij.webSymbols.WebSymbol.Companion.KIND_HTML_ELEMENTS
import com.intellij.webSymbols.WebSymbol.Companion.NAMESPACE_HTML
import com.intellij.webSymbols.utils.psiModificationCount
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.webSymbols.AstroProximity
import org.jetbrains.astro.webSymbols.AstroQueryConfigurator

class AstroComponent(file: PsiFile) : PsiSourcedWebSymbol, WebSymbolsScopeWithCache<PsiFile, Unit>(AstroFramework.ID, file.project, file,
                                                                                                   Unit) {

  override val source: PsiElement
    get() = dataHolder

  override val origin: WebSymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val namespace: SymbolNamespace
    get() = NAMESPACE_HTML

  override val kind: SymbolKind
    get() = KIND_HTML_ELEMENTS

  override val name: String
    get() = StringUtil.capitalize(FileUtil.getNameWithoutExtension(dataHolder.name))

  override val properties: Map<String, Any>
    get() = mapOf(Pair(AstroQueryConfigurator.PROP_ASTRO_PROXIMITY, AstroProximity.OUT_OF_SCOPE))

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(dataHolder)
  }

  override fun getModificationCount(): Long =
    project.psiModificationCount

  override fun createPointer(): Pointer<AstroComponent> {
    val filePtr = dataHolder.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { AstroComponent(it) }
    }
  }

}