// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.extapi.psi.StubBasedPsiElementBase
import com.intellij.lang.javascript.JSStubElementTypes
import com.intellij.lang.javascript.psi.ecma6.*
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.lang.javascript.psi.stubs.JSVarStatementStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptClassStub
import com.intellij.lang.javascript.psi.stubs.TypeScriptFieldStub
import com.intellij.lang.typescript.TypeScriptStubElementTypes
import com.intellij.model.Pointer
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.asSafely
import org.angular2.Angular2DecoratorUtil.ALIAS_PROP
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.DIRECTIVE_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.Angular2DecoratorUtil.IMPORTS_PROP
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.Angular2DecoratorUtil.REQUIRED_PROP
import org.angular2.entities.source.Angular2PropertyInfo
import org.jetbrains.annotations.NonNls
import java.util.*
import java.util.function.BiConsumer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

abstract class Angular2IvySymbolDef private constructor(private val myFieldOrStub: Any) {

  val field: TypeScriptField
    get() = if (myFieldOrStub is TypeScriptFieldStub) myFieldOrStub.psi as TypeScriptField
    else myFieldOrStub as TypeScriptField

  protected abstract val defTypeNames: List<String>

  val contextClass: TypeScriptClass?
    get() = PsiTreeUtil.getContextOfType(this.field, TypeScriptClass::class.java)

  abstract class Entity protected constructor(fieldOrStub: Any) : Angular2IvySymbolDef(fieldOrStub) {

    abstract val isStandalone: Boolean

    abstract fun createEntity(): Angular2IvyEntity<*>
  }

  class Module internal constructor(fieldStubOrPsi: Any) : Entity(fieldStubOrPsi) {

    override val isStandalone: Boolean
      get() = false

    override val defTypeNames: List<String>
      get() = TYPE_MODULE_DEFS

    fun getTypesList(property: String): List<TypeScriptTypeofType> {
      val index: Int
      when (property) {
        DECLARATIONS_PROP -> index = 1
        IMPORTS_PROP -> index = 2
        EXPORTS_PROP -> index = 3
        else -> return emptyList()
      }
      return processTupleArgument(index, TypeScriptTypeofType::class, { it }, false)!!
    }

    override fun createEntity(): Angular2IvyModule {
      return Angular2IvyModule(this)
    }

    fun createPointer(): Pointer<out Module> {
      val fieldPtr = field.createSmartPointer()
      return Pointer {
        fieldPtr.dereference()?.let { Module(field) }
      }
    }
  }

  open class Directive internal constructor(fieldStubOrPsi: Any) : Entity(fieldStubOrPsi) {

    override val isStandalone: Boolean
      get() = getBooleanParam(7)

    val selector: String?
      get() = getStringGenericParam(1)

    val selectorElement: TypeScriptStringLiteralType?
      get() = getDefFieldArgument(1) as? TypeScriptStringLiteralType

    val exportAsList: List<String>
      get() = processTupleArgument(2, TypeScriptStringLiteralType::class,
                                   { it.innerText }, false)!!

    val hostDirectives: List<HostDirectiveDef>
      get() = processTupleArgument(8, TypeScriptObjectType::class,
                                   { createHostDirectiveDef(it) }, false)!!

    override val defTypeNames: List<String>
      get() = TYPE_DIRECTIVE_DEFS

    open fun createPointer(): Pointer<out Directive> {
      val fieldPtr = field.createSmartPointer()
      return Pointer {
        fieldPtr.dereference()?.let { Directive(field) }
      }
    }

    override fun createEntity(): Angular2IvyDirective {
      return Angular2IvyDirective(this)
    }

    fun readPropertyMappings(kind: String): Map<String, Angular2PropertyInfo> =
      when (kind) {
        INPUTS_PROP -> processObjectArgument(3, TypeScriptType::class) { type, defaultName ->
          when (type) {
            is TypeScriptStringLiteralType -> type.innerText?.let { Angular2PropertyInfo(it, false, type) }
            is TypeScriptObjectType -> {
              val nameType = type.typeMembers
                .firstNotNullOfOrNull { member -> (member as? TypeScriptPropertySignature)?.takeIf { it.name == ALIAS_PROP }?.typeDeclaration }
                ?.asSafely<TypeScriptStringLiteralType>()
              val name = nameType?.innerText
              val required = type.typeMembers
                               .firstNotNullOfOrNull { member -> (member as? TypeScriptPropertySignature)?.takeIf { it.name == REQUIRED_PROP }?.typeDeclaration }
                               ?.asSafely<TypeScriptBooleanLiteralType>()
                               ?.value ?: false
              Angular2PropertyInfo(name ?: defaultName, required, nameType, if (name != null) TextRange(1, 1 + name.length) else null)
            }
            else -> null
          }
        }
        OUTPUTS_PROP -> processObjectArgument(4, TypeScriptStringLiteralType::class) { type, _ ->
          type.innerText?.let { Angular2PropertyInfo(it, false, type) }
        }
        else -> emptyMap()
      }

  }

  class Component internal constructor(fieldStubOrPsi: Any) : Directive(fieldStubOrPsi) {

    /**
     * Returns null if the type doesn't contain the argument and logic should fall back to metadata.json
     */
    val ngContentSelectors: Collection<TypeScriptStringLiteralType>?
      get() = processTupleArgument(6, TypeScriptStringLiteralType::class, { it }, true)

    override val defTypeNames: List<String>
      get() = TYPE_COMPONENT_DEFS

    override fun createPointer(): Pointer<Component> {
      val fieldPtr = field.createSmartPointer()
      return Pointer {
        fieldPtr.dereference()?.let { Component(it) }
      }
    }

    override fun createEntity(): Angular2IvyDirective {
      return Angular2IvyComponent(this)
    }
  }

  class Pipe internal constructor(fieldStubOrPsi: Any) : Entity(fieldStubOrPsi) {

    override val isStandalone: Boolean
      get() = getBooleanParam(2)

    val name: String?
      get() = getStringGenericParam(1)

    override val defTypeNames: List<String>
      get() = TYPE_PIPE_DEFS

    override fun createEntity(): Angular2IvyPipe {
      return Angular2IvyPipe(this)
    }

    fun createPointer(): Pointer<out Pipe> {
      val fieldPtr = field.createSmartPointer()
      return Pointer {
        fieldPtr.dereference()?.let { Pipe(field) }
      }
    }
  }

  class Factory internal constructor(fieldStubOrPsi: Any) : Angular2IvySymbolDef(fieldStubOrPsi) {

    override val defTypeNames: List<String>
      get() = TYPE_FACTORY_DEFS

    /**
     * Returns null if the type doesn't contain the argument and logic should fall back to metadata.json
     */
    val attributeNames: Map<String, JSTypeDeclaration>?
      get() {
        val result = HashMap<String, JSTypeDeclaration>()
        if (
          !processConstructorArguments("attribute", TypeScriptStringLiteralType::class) { name, type ->
            name.innerText?.let { result[it] = type }
          }
        ) {
          return null
        }
        return result
      }

    private fun <T : TypeScriptType> processConstructorArguments(kind: String, valueClass: KClass<T>,
                                                                 consumer: BiConsumer<T, TypeScriptType>): Boolean {
      val declaration = getDefFieldArgument(1) ?: return false
      val cls = contextClass
      if (declaration !is TypeScriptTupleType || cls == null) {
        return true
      }

      val constructor = cls.constructors.find { !it.isOverloadImplementation }
                        ?: // TODO support annotations in super constructors
                        return true

      val params = constructor.parameters
      val paramsDecoratorsInfo = declaration.elements
      if (params.size != paramsDecoratorsInfo.size) {
        return true
      }
      for (i in params.indices) {
        val info = paramsDecoratorsInfo[i] as? TypeScriptObjectType
        if (info != null) {
          val kindInfo = info.typeMembers.find { member -> kind == member.name } as? TypeScriptPropertySignature
                         ?: continue
          val value = valueClass.safeCast(kindInfo.typeDeclaration)
          if (value != null) {
            consumer.accept(value, params[i].typeElement as? TypeScriptType ?: continue)
          }
        }
      }
      return true
    }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val entityDef = other as Angular2IvySymbolDef?
    return field == entityDef!!.field
  }

  override fun hashCode(): Int {
    return Objects.hash(field)
  }

  protected fun getDefFieldArgument(index: Int): JSTypeDeclaration? {
    val stub = if (myFieldOrStub is TypeScriptFieldStub)
      myFieldOrStub
    else
      (myFieldOrStub as? StubBasedPsiElementBase<*>)?.stub
    return if (stub != null) {
      getDefFieldArgumentStubbed(stub as TypeScriptFieldStub, index, defTypeNames)
    }
    else getDefFieldArgumentPsi(myFieldOrStub as TypeScriptField, index, defTypeNames)
  }

  protected fun getStringGenericParam(index: Int): String? {
    val declaration = getDefFieldArgument(index)
    return if (declaration is TypeScriptStringLiteralType) {
      declaration.innerText
    }
    else null
  }

  protected fun getBooleanParam(index: Int): Boolean {
    val type = getDefFieldArgument(index)
    return type is TypeScriptBooleanLiteralType && type.value
  }

  @OptIn(ExperimentalContracts::class)
  protected fun <T : TypeScriptType, R> processTupleArgument(index: Int,
                                                             itemsClass: KClass<T>,
                                                             itemMapper: (T) -> R?,
                                                             nullIfNotFound: Boolean): List<R>? {
    contract {
      returnsNotNull() implies (!nullIfNotFound)
    }
    val declaration = getDefFieldArgument(index)
                      ?: return if (nullIfNotFound) null else emptyList()
    return if (declaration !is TypeScriptTupleType) {
      emptyList()
    }
    else declaration.elements
      .filterIsInstance(itemsClass.java)
      .mapNotNull(itemMapper)
  }

  protected fun <T : JSTypeDeclaration, R> processObjectArgument(index: Int,
                                                                 valueClass: KClass<T>,
                                                                 valueMapper: (T, String) -> R?): Map<String, R> =
    processObjectArgument(getDefFieldArgument(index) as? TypeScriptObjectType, valueClass, valueMapper)

  data class HostDirectiveDef(val directive: TypeScriptTypeofType,
                              val inputs: Map<String, Angular2PropertyInfo>,
                              val outputs: Map<String, Angular2PropertyInfo>)

  @Suppress("NonAsciiCharacters")
  companion object {
    @JvmStatic
    fun get(typeScriptClass: TypeScriptClass, allowAbstractClass: Boolean): Entity? {
      return getSymbolDef(typeScriptClass, allowAbstractClass) { fieldName, fieldPsiOrStub ->
        createEntityDef(fieldName, fieldPsiOrStub)
      }
    }

    @JvmStatic
    fun getFactory(typeScriptClass: TypeScriptClass): Factory? {
      return getSymbolDef(typeScriptClass, true) { fieldName, fieldPsiOrStub ->
        createFactoryDef(fieldName, fieldPsiOrStub)
      }
    }

    @JvmStatic
    fun get(stub: TypeScriptClassStub, allowAbstractClass: Boolean): Entity? {
      return getSymbolDefStubbed(stub, allowAbstractClass) { fieldName, fieldPsiOrStub ->
        createEntityDef(fieldName, fieldPsiOrStub)
      }
    }

    @JvmStatic
    fun get(field: TypeScriptField, allowAbstractClass: Boolean): Entity? {
      return getSymbolDef(field, allowAbstractClass) { fieldName, fieldPsiOrStub ->
        createEntityDef(fieldName, fieldPsiOrStub)
      }
    }

    @NonNls
    private const val FIELD_DIRECTIVE_DEF = "ɵdir"

    @NonNls
    private const val FIELD_MODULE_DEF = "ɵmod"

    @NonNls
    private const val FIELD_PIPE_DEF = "ɵpipe"

    @NonNls
    private const val FIELD_COMPONENT_DEF = "ɵcmp"

    @NonNls
    private const val FIELD_FACTORY_DEF = "ɵfac"

    /* NG 9-11: *Def(WithMeta), NG 12+: *Declaration */
    @NonNls
    private val TYPE_DIRECTIVE_DEFS = listOf("ɵɵDirectiveDefWithMeta", "ɵɵDirectiveDeclaration")

    @NonNls
    private val TYPE_MODULE_DEFS = listOf("ɵɵNgModuleDefWithMeta", "ɵɵNgModuleDeclaration")

    @NonNls
    private val TYPE_PIPE_DEFS = listOf("ɵɵPipeDefWithMeta", "ɵɵPipeDeclaration")

    @NonNls
    private val TYPE_COMPONENT_DEFS = listOf("ɵɵComponentDefWithMeta", "ɵɵComponentDeclaration")

    @NonNls
    private val TYPE_FACTORY_DEFS = listOf("ɵɵFactoryDef", "ɵɵFactoryDeclaration")

    private fun isAbstractClass(tsClass: TypeScriptClass): Boolean {
      return tsClass.attributeList?.hasModifier(JSAttributeList.ModifierType.ABSTRACT) ?: false
    }

    private fun <T : Angular2IvySymbolDef> getSymbolDefStubbed(jsClassStub: TypeScriptClassStub,
                                                               allowAbstractClasses: Boolean,
                                                               symbolFactory: (String, Any) -> T?): T? {
      val clsAttrs = jsClassStub.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST)
      if (clsAttrs == null || !allowAbstractClasses && clsAttrs.hasModifier(JSAttributeList.ModifierType.ABSTRACT)) {
        return null
      }
      for (classChild in jsClassStub.childrenStubs) {
        if (classChild !is JSVarStatementStub) {
          continue
        }
        val attrs = classChild.findChildStubByType(JSStubElementTypes.ATTRIBUTE_LIST)
        if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
          continue
        }
        val fieldStub = classChild.findChildStubByType(TypeScriptStubElementTypes.TYPESCRIPT_FIELD)
        if (fieldStub !is TypeScriptFieldStub) {
          continue
        }
        val entityDefKind = fieldStub.name?.let { symbolFactory(it, fieldStub) }
        if (entityDefKind != null) {
          return entityDefKind
        }
      }
      return null
    }

    private fun <T : Angular2IvySymbolDef> findSymbolDefFieldPsi(jsClass: TypeScriptClass,
                                                                 allowAbstractClass: Boolean,
                                                                 symbolFactory: (String, Any) -> T?): T? {
      for (field in jsClass.fields) {
        if (field !is TypeScriptField) {
          continue
        }
        val entityDefKind = getSymbolDef(field, allowAbstractClass, symbolFactory)
        if (entityDefKind != null) {
          return entityDefKind
        }
      }
      return null
    }

    private fun <T : Angular2IvySymbolDef> getSymbolDef(typeScriptClass: TypeScriptClass,
                                                        allowAbstractClass: Boolean,
                                                        symbolFactory: (String, Any) -> T?): T? {
      if (!allowAbstractClass && isAbstractClass(typeScriptClass)) {
        return null
      }
      val stub = (typeScriptClass as? StubBasedPsiElementBase<*>)?.stub
      return if (stub is TypeScriptClassStub) {
        getSymbolDefStubbed(stub, allowAbstractClass, symbolFactory)
      }
      else findSymbolDefFieldPsi(typeScriptClass, allowAbstractClass, symbolFactory)
    }

    private fun <T : Angular2IvySymbolDef> getSymbolDef(field: TypeScriptField,
                                                        allowAbstractClass: Boolean,
                                                        symbolFactory: (String, Any) -> T?): T? {
      val attrs = field.attributeList
      if (attrs == null || !attrs.hasModifier(JSAttributeList.ModifierType.STATIC)) {
        return null
      }
      val tsClass = PsiTreeUtil.getContextOfType(field, TypeScriptClass::class.java)
      return if (tsClass == null || !allowAbstractClass && isAbstractClass(tsClass)) {
        null
      }
      else field.name?.let { symbolFactory(it, field) }
    }

    private fun createEntityDef(fieldName: String?, fieldPsiOrStub: Any): Entity? =
      when (fieldName) {
        FIELD_COMPONENT_DEF -> Component(fieldPsiOrStub)
        FIELD_DIRECTIVE_DEF -> Directive(fieldPsiOrStub)
        FIELD_MODULE_DEF -> Module(fieldPsiOrStub)
        FIELD_PIPE_DEF -> Pipe(fieldPsiOrStub)
        else -> null
      }

    private fun createFactoryDef(fieldName: String?, fieldPsiOrStub: Any): Factory? {
      return if (fieldName != null && fieldName == FIELD_FACTORY_DEF) Factory(fieldPsiOrStub) else null
    }

    private fun getDefFieldArgumentStubbed(field: TypeScriptFieldStub,
                                           index: Int,
                                           typeNames: List<String>): JSTypeDeclaration? {
      val type = field.findChildStubByType(TypeScriptStubElementTypes.SINGLE_TYPE)
      val qualifiedName = type?.qualifiedTypeName ?: return null
      if (typeNames.any { name -> qualifiedName.endsWith(name) }) {
        val typeArguments = type.findChildStubByType(TypeScriptStubElementTypes.TYPE_ARGUMENT_LIST)
        if (typeArguments != null) {
          val declarations = typeArguments.childrenStubs
          if (index < declarations.size) {
            return declarations[index].psi as? JSTypeDeclaration
          }
        }
      }
      return null
    }

    private fun getDefFieldArgumentPsi(field: TypeScriptField,
                                       index: Int,
                                       typeNames: List<String>): JSTypeDeclaration? {
      val type = PsiTreeUtil.getChildOfType(field, TypeScriptSingleType::class.java)
      val qualifiedName = type?.qualifiedTypeName ?: return null
      if (typeNames.any { name -> qualifiedName.endsWith(name) }) {
        val declarations = type.typeArguments
        if (index < declarations.size) {
          return declarations[index]
        }
      }
      return null
    }

    private fun <T : JSTypeDeclaration, R> processObjectArgument(`object`: TypeScriptObjectType?,
                                                                 valueClass: KClass<T>,
                                                                 valueMapper: (T, String) -> R?): Map<String, R> {
      if (`object` == null) return emptyMap()
      val result = LinkedHashMap<String, R>()
      for (child in `object`.typeMembers) {
        val prop = child as? TypeScriptPropertySignature
        val propName = prop?.name
        if (propName != null) {
          valueClass.safeCast(prop.typeDeclaration)
            ?.let { valueMapper(it, propName) }
            ?.let { value -> result[propName] = value }
        }
      }
      return result
    }

    private fun createHostDirectiveDef(obj: TypeScriptObjectType): HostDirectiveDef? {
      val members = obj.typeMembers.filterIsInstance<TypeScriptPropertySignature>()
      val directive = members.find { it.name == DIRECTIVE_PROP }?.typeDeclaration?.asSafely<TypeScriptTypeofType>()
                      ?: return null
      val inputs = members.find { it.name == INPUTS_PROP }?.typeDeclaration?.asSafely<TypeScriptObjectType>()
      val outputs = members.find { it.name == OUTPUTS_PROP }?.typeDeclaration?.asSafely<TypeScriptObjectType>()
      return HostDirectiveDef(
        directive,
        processObjectArgument(inputs, TypeScriptStringLiteralType::class) { type, _ ->
          type.innerText?.let { Angular2PropertyInfo(it, false, type) }
        },
        processObjectArgument(outputs, TypeScriptStringLiteralType::class) { type, _ ->
          type.innerText?.let { Angular2PropertyInfo(it, false, type) }
        }
      )
    }
  }
}
