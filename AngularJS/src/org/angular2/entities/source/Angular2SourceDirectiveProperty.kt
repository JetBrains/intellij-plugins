// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.psi.JSElementBase
import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.model.Pointer
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.utils.coalesceApiStatus
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2SourceDirective.Companion.getPropertySources
import java.util.*

class Angular2SourceDirectiveProperty(override val owner: TypeScriptClass,
                                      private val signature: JSRecordType.PropertySignature,
                                      override val kind: String,
                                      override val name: String,
                                      override val required: Boolean) : Angular2DirectiveProperty {

  override val rawJsType: JSType?
    get() = signature.jsType

  override val virtualProperty: Boolean
    get() = false

  override val sourceElement: PsiElement
    get() = sources[0]

  override val apiStatus: WebSymbolApiStatus
    get() = coalesceApiStatus(sources) { (it as? JSElementBase)?.apiStatus }.coalesceWith(owner.apiStatus)

  val sources: List<PsiElement>
    get() {
      val sources = getPropertySources(signature.memberSource.singleElement)
      val decorated = sources.filter { s -> s.attributeList?.decorators?.isNotEmpty() ?: false }
      return when {
        !decorated.isEmpty() -> decorated
        !sources.isEmpty() -> sources
        else -> listOf<PsiElement>(owner)
      }
    }

  override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
    val sns = SymbolNavigationService.getInstance()
    return sources.map { s -> sns.psiElementNavigationTarget(s) }
  }

  override fun toString(): String {
    return Angular2EntityUtils.toString(this)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val property = other as Angular2SourceDirectiveProperty?
    return (owner == property!!.owner
            && signature.memberName == property.signature.memberName
            && name == property.name
            && kind == property.kind
            && required == property.required
           )
  }

  override fun hashCode(): Int {
    return Objects.hash(owner, signature.memberName, name, kind, required)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveProperty> {
    val sourcePtr = owner.createSmartPointer()
    val propertyName = signature.memberName
    val name = this.name
    val kind = this.kind
    val required = this.required
    return Pointer {
      val source = sourcePtr.dereference()
                   ?: return@Pointer null
      val propertySignature = TypeScriptTypeParser
                                .buildTypeFromClass(source, false)
                                .findPropertySignature(propertyName)
                              ?: return@Pointer null
      Angular2SourceDirectiveProperty(source, propertySignature, kind, name, required)
    }
  }

}
