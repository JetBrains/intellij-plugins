// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.model.Pointer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.*
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.AstroProximity
import org.jetbrains.astro.polySymbols.PROP_ASTRO_PROXIMITY
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_PROPS

class AstroComponent(file: PsiFile)
  : PsiSourcedPolySymbol, PolySymbolsScopeWithCache<PsiFile, Unit>(AstroFramework.ID, file.project, file, Unit) {

  override val source: PsiElement
    get() = dataHolder

  override val origin: PolySymbolOrigin
    get() = AstroProjectSymbolOrigin

  override val qualifiedKind: PolySymbolQualifiedKind
    get() = ASTRO_COMPONENTS

  override val name: String
    get() = StringUtil.capitalize(FileUtil.getNameWithoutExtension(dataHolder.name))

  override val properties: Map<String, Any>
    get() = mapOf(Pair(PROP_ASTRO_PROXIMITY, AstroProximity.OUT_OF_SCOPE))

  override fun provides(qualifiedKind: PolySymbolQualifiedKind): Boolean =
    qualifiedKind == UI_FRAMEWORK_COMPONENT_PROPS

  override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    consumer(AstroComponentWildcardAttribute)
    cacheDependencies.add(dataHolder)
  }

  override fun getModificationCount(): Long =
    PsiModificationTracker.getInstance(project).modificationCount

  override fun createPointer(): Pointer<AstroComponent> {
    val filePtr = dataHolder.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { AstroComponent(it) }
    }
  }
}