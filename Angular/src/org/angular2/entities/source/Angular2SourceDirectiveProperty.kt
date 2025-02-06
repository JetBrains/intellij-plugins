// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.source

import com.intellij.javascript.webSymbols.apiStatus
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.guard.JSTypeGuardUtil
import com.intellij.lang.javascript.psi.util.JSStubBasedPsiTreeUtil.isStubBased
import com.intellij.model.Pointer
import com.intellij.navigation.SymbolNavigationService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.util.applyIf
import com.intellij.util.asSafely
import com.intellij.webSymbols.PsiSourcedWebSymbol
import com.intellij.webSymbols.WebSymbolApiStatus
import com.intellij.webSymbols.WebSymbolQualifiedKind
import com.intellij.webSymbols.utils.WebSymbolDeclaredInPsi
import com.intellij.webSymbols.utils.coalesceApiStatus
import com.intellij.webSymbols.utils.coalesceWith
import org.angular2.Angular2DecoratorUtil
import org.angular2.entities.Angular2ClassBasedDirectiveProperty
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2SourceDirective.Companion.getPropertySources
import org.angular2.lang.expr.service.tcb.R3Identifiers
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.web.NG_DIRECTIVE_OUTPUTS
import java.util.*

abstract class Angular2SourceDirectiveProperty(
  override val owner: TypeScriptClass,
  protected val signature: JSRecordType.PropertySignature,
  override val qualifiedKind: WebSymbolQualifiedKind,
  override val name: String,
  override val required: Boolean,
  val declarationSource: PsiElement?,
) : Angular2ClassBasedDirectiveProperty {

  companion object {
    fun create(
      owner: TypeScriptClass,
      signature: JSRecordType.PropertySignature,
      qualifiedKind: WebSymbolQualifiedKind,
      info: Angular2PropertyInfo,
    ): Angular2SourceDirectiveProperty =
      if (info.declarationRange == null || info.declaringElement == null)
        Angular2SourceFieldDirectiveProperty(
          owner, signature, qualifiedKind, info.name, info.required, info.declarationSource?.takeIf { isStubBased(it) }
        )
      else
        Angular2SourceMappedDirectiveProperty(
          owner, signature, qualifiedKind, info.name, info.required, info.declarationSource?.takeIf { isStubBased(it) },
          info.declaringElement, info.declarationRange
        )
  }

  override val fieldName: String
    get() = signature.memberName

  val transformParameterType: JSType?
    get() = objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP)
      ?.jsType
      ?.asRecordType(owner)
      ?.callSignatures
      ?.firstNotNullOfOrNull { signature -> signature.functionType.parameters.takeIf { it.size > 0 }?.get(0) }
      ?.inferredType

  override val isCoerced: Boolean
    get() = super.isCoerced || objectInitializer?.findProperty(Angular2DecoratorUtil.TRANSFORM_PROP) != null

  override val isSignalProperty: Boolean
    get() = signature.jsType
      ?.asRecordType()
      ?.findPropertySignature(R3Identifiers.InputSignalBrandWriteType.name) != null

  override val type: JSType?
    get() = if (qualifiedKind == NG_DIRECTIVE_OUTPUTS)
      typeFromSignal ?: super.type
    else
      super.type

  override val rawJsType: JSType?
    get() = typeFromSignal
            ?: transformParameterType
            ?: withTypeEvaluationLocation(owner) {
              signature.setterJSType ?: signature.jsType
            }?.applyIf(signature.isOptional) {
              JSTypeGuardUtil.wrapWithUndefined(this, this.getSource())!!
            }

  override val virtualProperty: Boolean
    get() = false

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

  abstract override fun createPointer(): Pointer<out Angular2SourceDirectiveProperty>

  private val objectInitializer: JSObjectLiteralExpression?
    get() = declarationSource as? JSObjectLiteralExpression
            ?: (declarationSource as? JSLiteralExpression)
              ?.context?.asSafely<JSProperty>()
              ?.context?.asSafely<JSObjectLiteralExpression>()

  val typeFromSignal: JSType?
    get() = withTypeEvaluationLocation(owner) {
      signature.jsType
        ?.asRecordType()
        ?.findPropertySignature(R3Identifiers.InputSignalBrandWriteType.name)
        ?.jsTypeWithOptionality
    }

  private class Angular2SourceFieldDirectiveProperty(
    owner: TypeScriptClass,
    signature: JSRecordType.PropertySignature,
    qualifiedKind: WebSymbolQualifiedKind,
    name: String,
    required: Boolean,
    declarationSource: PsiElement?,
  ) : Angular2SourceDirectiveProperty(owner, signature, qualifiedKind, name, required, declarationSource), PsiSourcedWebSymbol {
    override val sourceElement: PsiElement
      get() = sources[0]

    override val source: PsiElement
      get() = sourceElement

    override fun getNavigationTargets(project: Project): Collection<NavigationTarget> {
      val sns = SymbolNavigationService.getInstance()
      return sources.map { s -> sns.psiElementNavigationTarget(s) }
    }

    override fun createPointer(): Pointer<Angular2SourceFieldDirectiveProperty> {
      val sourcePtr = owner.createSmartPointer()
      val propertyName = signature.memberName
      val name = this.name
      val qualifiedKind = this.qualifiedKind
      val required = this.required
      val declarationSourcePtr = declarationSource?.createSmartPointer()
      return Pointer {
        val source = sourcePtr.dereference()
                     ?: return@Pointer null
        val declarationSource = declarationSourcePtr?.let { it.dereference() ?: return@Pointer null }
        val propertySignature = Angular2TypeUtils
                                  .buildTypeFromClass(source)
                                  .findPropertySignature(propertyName)
                                ?: return@Pointer null
        Angular2SourceFieldDirectiveProperty(source, propertySignature, qualifiedKind, name, required, declarationSource)
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is Angular2SourceFieldDirectiveProperty
      && super.equals(other)

    override fun hashCode(): Int =
      super.hashCode()

  }

  private class Angular2SourceMappedDirectiveProperty(
    owner: TypeScriptClass,
    signature: JSRecordType.PropertySignature,
    qualifiedKind: WebSymbolQualifiedKind,
    name: String,
    required: Boolean,
    declarationSource: PsiElement?,
    override val sourceElement: PsiElement,
    override val textRangeInSourceElement: TextRange,
  ) : Angular2SourceDirectiveProperty(owner, signature, qualifiedKind, name, required, declarationSource), WebSymbolDeclaredInPsi {

    override fun createPointer(): Pointer<Angular2SourceMappedDirectiveProperty> {
      val ownerPtr = owner.createSmartPointer()
      val sourceElementPtr = sourceElement.createSmartPointer()
      val propertyName = signature.memberName
      val name = name
      val qualifiedKind = qualifiedKind
      val required = required
      val declarationSourcePtr = declarationSource?.createSmartPointer()
      val textRangeInSourceElement = textRangeInSourceElement
      return Pointer {
        val owner = ownerPtr.dereference()
                    ?: return@Pointer null
        val sourceElement = sourceElementPtr.dereference()
                            ?: return@Pointer null
        val declarationSource = declarationSourcePtr?.let { it.dereference() ?: return@Pointer null }
        val propertySignature = Angular2TypeUtils
                                  .buildTypeFromClass(owner)
                                  .findPropertySignature(propertyName)
                                ?: return@Pointer null
        Angular2SourceMappedDirectiveProperty(owner, propertySignature, qualifiedKind, name, required, declarationSource,
                                              sourceElement, textRangeInSourceElement)
      }
    }

    override fun equals(other: Any?): Boolean =
      other === this ||
      other is Angular2SourceMappedDirectiveProperty
      && super.equals(other)
      && other.sourceElement == sourceElement
      && other.textRangeInSourceElement == textRangeInSourceElement

    override fun hashCode(): Int =
      Objects.hash(super.hashCode(), sourceElement, textRangeInSourceElement)

  }

}
