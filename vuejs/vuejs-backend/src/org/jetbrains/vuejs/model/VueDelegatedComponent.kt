// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model

import com.intellij.lang.javascript.psi.ecma6.TypeScriptTypeParameter
import com.intellij.model.Pointer
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.psi.PsiElement
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.getLibraryNameForDocumentationOf

abstract class VueDelegatedComponent<T : VueComponent> : VueDelegatedContainer<T>(), VueNamedComponent {

  final override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    PolySymbolDocumentationTarget.create(
      this, location,
      PolySymbolDocumentationProvider<VueDelegatedComponent<*>> { symbol, location ->
        val myDoc = PolySymbolDocumentation.create(symbol, location) {
          library = getLibraryNameForDocumentationOf(symbol.source)
        }
        (symbol.delegate as? VueNamedComponent)
          ?.getDocumentationTarget(location)
          ?.asSafely<PolySymbolDocumentationTarget>()
          ?.documentation
          ?.withLibrary(myDoc.library)
          ?.withName(myDoc.name)
          ?.withDefinition(myDoc.definition)
          ?.withIcon(myDoc.icon)
        ?: myDoc
      })

  final override val typeParameters: List<TypeScriptTypeParameter>
    get() = delegate?.typeParameters ?: emptyList()

  override val elementToImport: PsiElement?
    get() = delegate?.elementToImport

  override fun equals(other: Any?): Boolean =
    other === this ||
    other is VueDelegatedComponent<*>
    && other.javaClass == javaClass
    && other.name == name
    && other.delegate == delegate

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + delegate.hashCode()
    return result
  }

  abstract override fun createPointer(): Pointer<out VueDelegatedComponent<T>>
}