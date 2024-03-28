// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import org.angular2.codeInsight.documentation.Angular2ElementDocumentationTarget
import org.angular2.web.Angular2Symbol
import org.angular2.web.NG_DIRECTIVE_EXPORTS_AS
import java.util.*

class Angular2DirectiveExportAs(
  override val name: @NlsSafe String,
  val directive: Angular2Directive,
  override val sourceElement: PsiElement = directive.sourceElement,
  override val textRangeInSourceElement: TextRange? = null,
) : Angular2Symbol, Angular2Element, WebSymbolDeclaredInPsi {

  override val psiContext: PsiElement
    get() = sourceElement

  override val priority: WebSymbol.Priority
    get() = WebSymbol.Priority.NORMAL

  override val project: Project
    get() = sourceElement.project

  override val qualifiedKind: WebSymbolQualifiedKind
    get() = NG_DIRECTIVE_EXPORTS_AS

  override val type: Any?
    get() = directive.entityJsType

  override fun createPointer(): Pointer<out Angular2DirectiveExportAs> {
    val name = name
    val directivePtr = directive.createPointer()
    val sourceElementPtr = sourceElement.createSmartPointer()
    val textRangeInSourceElement = textRangeInSourceElement
    return Pointer {
      val directive = directivePtr.dereference() ?: return@Pointer null
      val sourceElement = sourceElementPtr.dereference() ?: return@Pointer null
      Angular2DirectiveExportAs(name, directive, sourceElement, textRangeInSourceElement)
    }
  }

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget =
    Angular2ElementDocumentationTarget.create(name, location, this.directive)
    ?: super<Angular2Symbol>.getDocumentationTarget(location)

  override val presentation: TargetPresentation
    get() {
      return TargetPresentation.builder(name)
        .icon(icon)
        .locationText(directive.entitySource?.containingFile?.name)
        .containerText(directive.getName())
        .presentation()
    }

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is Angular2DirectiveExportAs
     && other.name == name
     && other.directive == directive
     && other.sourceElement == sourceElement
     && other.textRangeInSourceElement == textRangeInSourceElement
    )

  override fun hashCode(): Int =
    Objects.hash(name, directive)
}