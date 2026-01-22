// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSPsiNamedElementBase
import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.codeInsight.resolveIfImportSpecifier

class VueLocallyDefinedComponent
private constructor(
  override val defaultName: String?,
  override val delegate: VueComponent,
  source: PsiElement,
  override val vueProximity: VueModelVisitor.Proximity = VueModelVisitor.Proximity.LOCAL,
) : VueDelegatedContainer<VueComponent>(),
    VueComponent, PsiSourcedPolySymbol {

  companion object {
    fun create(delegate: VueComponent, source: JSLiteralExpression): VueLocallyDefinedComponent {
      return VueLocallyDefinedComponent(getTextIfLiteral(source), delegate, source)
    }

    fun create(delegate: VueComponent, source: JSPsiNamedElementBase): VueLocallyDefinedComponent {
      return VueLocallyDefinedComponent(source.name, delegate, source)
    }
  }

  private val mySource = source

  override val name: String
    get() = defaultName ?: "<unnamed>"

  override fun withNameAndProximity(name: String, proximity: VueModelVisitor.Proximity): VueComponent =
    VueLocallyDefinedComponent(name, delegate, mySource, proximity)

  override val source: PsiElement
    get() = mySource

  override val nameElement: PsiElement
    get() = if (mySource is JSPsiNamedElementBase) componentSource else mySource

  override val componentSource: PsiElement by lazy(LazyThreadSafetyMode.PUBLICATION) {
    if (mySource is JSPsiNamedElementBase)
      mySource.resolveIfImportSpecifier()
    else
      (delegate.componentSource ?: mySource)
  }

  override val rawSource: PsiElement
    get() = mySource as? JSPsiNamedElementBase ?: delegate.rawSource ?: mySource

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(
      this, location,
      PolySymbolDocumentationProvider<VueLocallyDefinedComponent> { symbol, location ->
        val myDoc = PolySymbolDocumentation.create(symbol, location) {
          library = getLibraryNameForDocumentationOf(symbol.source)
        }
        symbol.delegate
          .getDocumentationTarget(location)
          .asSafely<PolySymbolDocumentationTarget>()
          ?.documentation
          ?.withLibrary(myDoc.library)
          ?.withName(myDoc.name)
          ?.withDefinition(myDoc.definition)
          ?.withIcon(myDoc.icon)
        ?: myDoc
      })

  override val typeParameters: List<TypeScriptTypeParameter>
    get() = delegate.typeParameters

  override fun createPointer(): Pointer<VueLocallyDefinedComponent> {
    val defaultName = this.defaultName
    val vueProximity = this.vueProximity
    val delegate = this.delegate.createPointer()
    val source = this.mySource.createSmartPointer()
    return Pointer {
      val newDelegate = delegate.dereference() ?: return@Pointer null
      val newSource = source.dereference() ?: return@Pointer null
      VueLocallyDefinedComponent(defaultName, newDelegate, newSource, vueProximity)
    }
  }

  override fun equals(other: Any?): Boolean =
    other is VueLocallyDefinedComponent
    && other.name == name
    && other.delegate == delegate
    && other.mySource == mySource

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + delegate.hashCode()
    result = 31 * result + mySource.hashCode()
    return result
  }
}