// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.query.polySymbolScope
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.createSmartPointer
import org.jetbrains.astro.polySymbols.ASTRO_COMPONENTS
import org.jetbrains.astro.polySymbols.AstroProximity
import org.jetbrains.astro.polySymbols.AstroProximityProperty
import org.jetbrains.astro.polySymbols.UI_FRAMEWORK_COMPONENT_PROPS

class AstroComponent(private val file: PsiFile) :
  ComponentPolySymbol,
  PolySymbolScope by polySymbolScope(
    {
      provides(UI_FRAMEWORK_COMPONENT_PROPS)
      +AstroComponentWildcardAttribute
    }) {

  override val source: PsiElement
    get() = file

  override val kind: PolySymbolKind
    get() = ASTRO_COMPONENTS

  override val name: String
    get() = StringUtil.capitalize(FileUtil.getNameWithoutExtension(file.name))

  @PolySymbol.Property(AstroProximityProperty::class)
  val astroProximity: AstroProximity
    get() = AstroProximity.OUT_OF_SCOPE

  override fun createPointer(): Pointer<AstroComponent> {
    val filePtr = file.createSmartPointer()
    return Pointer {
      filePtr.dereference()?.let { AstroComponent(it) }
    }
  }

  override fun computeNavigationElement(project: Project): PsiElement = source
}