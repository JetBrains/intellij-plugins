// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.model.Pointer
import com.intellij.navigation.NavigationTarget
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.createSmartPointer
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2SourceDirective.Companion.getPropertySources
import java.util.*

class Angular2SourceDirectiveProperty(private val mySource: TypeScriptClass,
                                      private val mySignature: JSRecordType.PropertySignature,
                                      override val kind: String,
                                      override val name: String) : Angular2DirectiveProperty {

  override val rawJsType: JSType?
    get() = mySignature.jsType

  override val virtual: Boolean
    get() = false

  override val sourceElement: PsiElement
    get() = sources[0]

  val sources: List<PsiElement>
    get() {
      val sources = getPropertySources(mySignature.memberSource.singleElement)
      val decorated = sources.filter { s -> s.attributeList?.decorators?.isNotEmpty() ?: false }
      return when {
        !decorated.isEmpty() -> decorated
        !sources.isEmpty() -> sources
        else -> listOf<PsiElement>(mySource)
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
    return (mySource == property!!.mySource
            && mySignature.memberName == property.mySignature.memberName
            && name == property.name
            && kind == property.kind)
  }

  override fun hashCode(): Int {
    return Objects.hash(mySource, mySignature.memberName, name, kind)
  }

  override fun createPointer(): Pointer<Angular2SourceDirectiveProperty> {
    val sourcePtr = mySource.createSmartPointer()
    val propertyName = mySignature.memberName
    val name = this.name
    val kind = this.kind
    return Pointer {
      val source = sourcePtr.dereference()
                   ?: return@Pointer null
      val propertySignature = TypeScriptTypeParser
                                .buildTypeFromClass(source, false)
                                .findPropertySignature(propertyName)
                              ?: return@Pointer null
      Angular2SourceDirectiveProperty(source, propertySignature, kind, name)
    }
  }
}
