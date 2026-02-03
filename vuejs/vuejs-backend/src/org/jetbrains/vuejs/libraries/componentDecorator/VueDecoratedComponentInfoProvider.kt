// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.libraries.componentDecorator

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSFunction
import com.intellij.lang.javascript.psi.JSFunctionItem
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSParameterTypeDecorator
import com.intellij.lang.javascript.psi.JSRecordType.PropertySignature
import com.intellij.lang.javascript.psi.JSRecordType.TypeMember
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner
import com.intellij.lang.javascript.psi.ecmal4.JSClass
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.lang.javascript.psi.types.JSParameterTypeDecoratorImpl
import com.intellij.lang.javascript.psi.types.evaluable.JSUnwrapPromiseType
import com.intellij.lang.javascript.psi.types.primitives.JSVoidType
import com.intellij.lang.javascript.psi.types.primitives.TypeScriptNeverType
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.parentOfType
import com.intellij.util.asSafely
import org.jetbrains.vuejs.codeInsight.findDecorator
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.codeInsight.getDecoratorArgument
import org.jetbrains.vuejs.codeInsight.getRequiredFromPropOptions
import org.jetbrains.vuejs.codeInsight.getTextIfLiteral
import org.jetbrains.vuejs.model.EMIT_CALL_UPDATE_PREFIX
import org.jetbrains.vuejs.model.VueComputedProperty
import org.jetbrains.vuejs.model.VueDataProperty
import org.jetbrains.vuejs.model.VueEmitCall
import org.jetbrains.vuejs.model.VueImplicitElement
import org.jetbrains.vuejs.model.VueInputProperty
import org.jetbrains.vuejs.model.VueMethod
import org.jetbrains.vuejs.model.VueMixin
import org.jetbrains.vuejs.model.VueModelDirectiveProperties
import org.jetbrains.vuejs.model.VueModelManager
import org.jetbrains.vuejs.model.VueProperty
import org.jetbrains.vuejs.model.VueSymbol
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider
import org.jetbrains.vuejs.model.source.VueContainerInfoProvider.VueContainerInfo
import org.jetbrains.vuejs.types.optionalIf
import java.util.Locale

private const val PROP_DEC = "Prop"
private const val PROP_SYNC_DEC = "PropSync"
private const val MODEL_DEC = "Model"
private const val EMIT_DEC = "Emit"

private val DECS = setOf(
  PROP_DEC,
  PROP_SYNC_DEC,
  MODEL_DEC,
  EMIT_DEC,
)

private fun getNameFromDecorator(decorator: ES6Decorator): String? {
  return getDecoratorArgument(decorator, 0)
    ?.let { getTextIfLiteral(it) }
}

fun isVueComponentDecoratorName(name: String): Boolean =
  name in DECS

class VueDecoratedComponentInfoProvider : VueContainerInfoProvider.VueDecoratedContainerInfoProvider(::VueDecoratedComponentInfo) {

  private class VueDecoratedComponentInfo(clazz: JSClass) : VueContainerInfo {
    override val mixins: List<VueMixin>
    override val extends: List<VueMixin>
    override val data: List<VueDataProperty>
    override val computed: List<VueComputedProperty>
    override val methods: List<VueMethod>
    override val emits: List<VueEmitCall>
    override val props: List<VueInputProperty>
    override val model: VueModelDirectiveProperties?

    init {
      val mixins = mutableListOf<VueMixin>()
      val extends = mutableListOf<VueMixin>()
      val data = mutableListOf<VueDataProperty>()
      val computed = mutableListOf<VueComputedProperty>()
      val methods = mutableListOf<VueMethod>()
      val emits = mutableListOf<VueEmitCall>()
      val props = mutableListOf<VueInputProperty>()
      var model: VueModelDirectiveProperties? = null

      clazz.jsType
        .asRecordType()
        .typeMembers
        .forEach { member ->
          if (member !is PropertySignature) return@forEach
          val decorator = findDecorator(member, DECS)
          when (decorator?.decoratorName) {
            PROP_DEC -> {
              props.add(VueDecoratedInputProperty(member.memberName, member, decorator, 0))
            }
            PROP_SYNC_DEC -> {
              computed.add(VueDecoratedComputedProperty(clazz, member.memberName, member, decorator, 1))
              getNameFromDecorator(decorator)?.let { name ->
                props.add(VueDecoratedInputProperty(name, member, decorator, 1))
                emits.add(VueDecoratedPropSyncEmitCall("$EMIT_CALL_UPDATE_PREFIX$name", decorator, member))
              }
            }
            MODEL_DEC -> if (model === null) {
              val name = getNameFromDecorator(decorator)
              model = VueModelDirectiveProperties(member.memberName, name)
              props.add(VueDecoratedInputProperty(member.memberName, member, decorator, 1))
            }
            EMIT_DEC -> if (member.memberSource.singleElement is JSFunction) {
              emits.add(VueDecoratedPropertyEmitCall(
                clazz, getNameFromDecorator(decorator) ?: fromAsset(member.memberName), member)
              )
              methods.add(VueDecoratedPropertyMethod(clazz, member.memberName, member))
            }
            else -> {
              val source = member.memberSource.singleElement
              if (source is JSFunction) {
                if (source.isGetProperty || source.isSetProperty)
                  member.memberSource.allSourceElements.mapNotNullTo(computed) {
                    if (it is JSFunctionItem)
                      VueDecoratedComputedProperty(clazz, member.memberName, member, null, 0, it)
                    else
                      null
                  }
                else
                  methods.add(VueDecoratedPropertyMethod(clazz, member.memberName, member))
              }
              else if (source is JSAttributeListOwner) {
                data.add(VueDecoratedDataProperty(clazz, member))
              }
            }
          }
        }

      clazz.extendsList?.members?.forEach { extendItem ->
        if (extendItem.referenceText == null) {
          val call = extendItem.expression as? JSCallExpression
          if ((call?.methodExpression as? JSReferenceExpression)?.referenceName?.lowercase(Locale.US) == "mixins") {
            call.arguments.mapNotNullTo(mixins) { arg ->
              (arg as? JSReferenceExpression)?.resolve()
                ?.let { VueModelManager.getMixin(it) }
            }
          }
        }
        else {
          extendItem.classes.mapNotNullTo(mixins) {
            VueModelManager.getMixin(it)
          }
        }
      }

      this.mixins = mixins
      this.extends = extends
      this.data = data
      this.computed = computed
      this.methods = methods
      this.emits = emits
      this.props = props
      this.model = model
    }

    private abstract class VueDecoratedNamedSymbol<T : TypeMember>(
      override val name: String,
      protected val member: T,
    ) : VueSymbol, PsiSourcedPolySymbol {
      override val source: PsiElement?
        get() = member.memberSource.singleElement

      abstract override fun createPointer(): Pointer<out VueDecoratedNamedSymbol<T>>

      override fun equals(other: Any?): Boolean =
        other === this
        || other is VueDecoratedNamedSymbol<*>
        && other.javaClass == javaClass
        && other.name == name
        && other.member.memberSource.singleElement == member.memberSource.singleElement

      override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + member.memberSource.singleElement.hashCode()
        return result
      }
    }

    private abstract class VueDecoratedProperty(
      name: String,
      member: PropertySignature,
    ) : VueDecoratedNamedSymbol<PropertySignature>(name, member),
        VueProperty, PsiSourcedPolySymbol {
      override val type: JSType?
        get() = member.jsType

      abstract override fun createPointer(): Pointer<out VueDecoratedProperty>
    }

    private class VueDecoratedInputProperty(
      name: String,
      member: PropertySignature,
      private val decorator: ES6Decorator,
      private val decoratorArgumentIndex: Int,
    ) : VueDecoratedProperty(name, member),
        VueInputProperty {

      override val required: Boolean =
        getRequiredFromPropOptions(getDecoratorArgument(decorator, decoratorArgumentIndex))

      override val type: JSType =
        VueDecoratedComponentPropType(member, decorator, decoratorArgumentIndex)
          .optionalIf(!required)

      override fun equals(other: Any?): Boolean =
        other === this
        || other is VueDecoratedInputProperty
        && other.name == name
        && other.decorator == decorator
        && other.decoratorArgumentIndex == decoratorArgumentIndex

      override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + decorator.hashCode()
        result = 31 * result + decoratorArgumentIndex
        return result
      }

      override fun createPointer(): Pointer<VueDecoratedInputProperty> {
        val name = name
        val memberName = member.memberName
        val decoratorPtr = decorator.createSmartPointer()
        val decoratorArgumentIndex = decoratorArgumentIndex
        return Pointer {
          val decorator = decoratorPtr.dereference() ?: return@Pointer null
          val clazz = decorator.parentOfType<JSClass>() ?: return@Pointer null
          clazz.jsType.asRecordType()
            .findPropertySignature(memberName)
            ?.let {
              VueDecoratedInputProperty(name, it, decorator, decoratorArgumentIndex)
            }
        }
      }
    }

    private class VueDecoratedComputedProperty(
      private val clazz: JSClass,
      name: String,
      member: PropertySignature,
      private val decorator: ES6Decorator?,
      private val decoratorArgumentIndex: Int,
      private val provider: PsiElement = member.memberSource.singleElement!!,
    ) : VueDecoratedProperty(name, member),
        VueComputedProperty {

      override val type: JSType =
        VueDecoratedComponentPropType(member, decorator, decoratorArgumentIndex)

      override val source: PsiElement =
        VueImplicitElement(
          name = name,
          jsType = type,
          provider = provider,
          kind = JSImplicitElement.Type.Property,
          equivalentToProvider = true,
        )

      override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
        when (property) {
          PolySymbol.PROP_READ_WRITE_ACCESS -> property.tryCast(
            if (provider is JSFunctionItem) {
              when {
                provider.isSetProperty -> ReadWriteAccessDetector.Access.Write
                provider.isGetProperty -> ReadWriteAccessDetector.Access.Read
                else -> null
              }
            }
            else null
          )
          else -> super<VueDecoratedProperty>.get(property)
        }

      override fun equals(other: Any?): Boolean =
        other === this
        || other is VueDecoratedComputedProperty
        && other.clazz == clazz
        && other.name == name
        && other.decorator == decorator
        && other.decoratorArgumentIndex == decoratorArgumentIndex
        && other.provider == provider

      override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (decorator?.hashCode() ?: 0)
        result = 31 * result + decoratorArgumentIndex
        result = 31 * result + provider.hashCode()
        return result
      }

      override fun createPointer(): Pointer<VueDecoratedComputedProperty> {
        val clazzPtr = clazz.createSmartPointer()
        val memberName = member.memberName
        val name = name
        val decoratorPtr = decorator?.createSmartPointer()
        val decoratorArgumentIndex = decoratorArgumentIndex
        val providerPtr = provider.createSmartPointer()
        return Pointer {
          val clazz = clazzPtr.dereference() ?: return@Pointer null
          val decorator = decoratorPtr?.let { it.dereference() ?: return@Pointer null }
          val provider = providerPtr.dereference() ?: return@Pointer null
          clazz.jsType.asRecordType()
            .findPropertySignature(memberName)
            ?.let { VueDecoratedComputedProperty(clazz, name, it, decorator, decoratorArgumentIndex, provider) }
        }

      }
    }

    private class VueDecoratedDataProperty(
      private val clazz: JSClass,
      member: PropertySignature,
    ) : VueDecoratedProperty(member.memberName, member),
        VueDataProperty {

      override fun createPointer(): Pointer<VueDecoratedDataProperty> {
        val clazzPtr = clazz.createSmartPointer()
        val memberName = member.memberName
        return Pointer {
          val clazz = clazzPtr.dereference() ?: return@Pointer null
          clazz.jsType.asRecordType()
            .findPropertySignature(memberName)
            ?.let { VueDecoratedDataProperty(clazz, it) }
        }
      }

    }

    private class VueDecoratedPropertyEmitCall(
      private val clazz: JSClass,
      name: String,
      member: PropertySignature,
    ) : VueDecoratedNamedSymbol<PropertySignature>(name, member),
        VueEmitCall {

      override val params: List<JSParameterTypeDecorator> = run {
        val functionType = member.jsType?.asSafely<JSFunctionType>() ?: return@run super.params
        val returnType = functionType.returnType
        if (returnType != null && returnType !is JSVoidType && returnType !is TypeScriptNeverType) {
          buildList {
            add(JSParameterTypeDecoratorImpl("arg", JSUnwrapPromiseType(returnType, returnType.source), false, false, true))
            addAll(functionType.parameters)
          }
        }
        else {
          functionType.parameters
        }
      }

      override val hasStrictSignature: Boolean = member.jsType is JSFunctionType

      override fun createPointer(): Pointer<VueDecoratedPropertyEmitCall> {
        val clazzPtr = clazz.createSmartPointer()
        val name = name
        val memberName = member.memberName
        return Pointer {
          val clazz = clazzPtr.dereference() ?: return@Pointer null
          clazz.jsType.asRecordType()
            .findPropertySignature(memberName)
            ?.let { VueDecoratedPropertyEmitCall(clazz, name, it) }
        }
      }
    }

    private class VueDecoratedPropSyncEmitCall(
      name: String,
      private val decorator: ES6Decorator,
      member: PropertySignature,
    ) : VueDecoratedNamedSymbol<PropertySignature>(name, member),
        VueEmitCall {

      override val params: List<JSParameterTypeDecorator> = listOf(
        JSParameterTypeDecoratorImpl("arg", member.jsType ?: VueDecoratedComponentPropType(member, decorator, 1), false, false, true)
      )

      override val hasStrictSignature: Boolean = true

      override fun createPointer(): Pointer<VueDecoratedPropSyncEmitCall> {
        val decoratorPtr = decorator.createSmartPointer()
        val name = name
        val memberName = member.memberName
        return Pointer {
          val decorator = decoratorPtr.dereference() ?: return@Pointer null
          decorator.parentOfType<JSClass>()
            ?.jsType
            ?.asRecordType()
            ?.findPropertySignature(memberName)
            ?.let { VueDecoratedPropSyncEmitCall(name, decorator, it) }
        }
      }
    }

    private class VueDecoratedPropertyMethod(
      private val clazz: JSClass,
      name: String,
      member: PropertySignature,
    ) : VueDecoratedProperty(name, member),
        VueMethod {

      override fun createPointer(): Pointer<VueDecoratedPropertyMethod> {
        val clazzPtr = clazz.createSmartPointer()
        val memberName = member.memberName
        val name = name
        return Pointer {
          val clazz = clazzPtr.dereference() ?: return@Pointer null
          clazz.jsType.asRecordType()
            .findPropertySignature(memberName)
            ?.let { VueDecoratedPropertyMethod(clazz, name, it) }
        }
      }

    }
  }
}