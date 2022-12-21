// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.ivy

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptStringLiteralType
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.model.Pointer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS
import com.intellij.psi.util.CachedValueProvider.Result.create
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.Angular2DecoratorUtil
import org.angular2.codeInsight.Angular2LibrariesHacks.hackIonicComponentOutputs
import org.angular2.entities.*
import org.angular2.entities.metadata.Angular2MetadataUtil.getMetadataEntity
import org.angular2.entities.source.Angular2SourceDirective.Companion.getDirectiveKindNoCache
import org.angular2.entities.source.Angular2SourceDirectiveProperty
import org.angular2.entities.source.Angular2SourceDirectiveVirtualProperty
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

open class Angular2IvyDirective(entityDef: Angular2IvySymbolDef.Directive) : Angular2IvyDeclaration<Angular2IvySymbolDef.Directive>(
  entityDef), Angular2Directive {

  override val selector: Angular2DirectiveSelector
    get() = getLazyValue(IVY_SELECTOR) {
      myEntityDef.selectorElement
        ?.let { createSelectorFromStringLiteralType(it) }
      ?: Angular2DirectiveSelectorImpl(myEntityDef.field, null, null)
    }

  override val exportAsList: List<String>
    get() = getLazyValue(IVY_EXPORT_AS) { myEntityDef.exportAsList }

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = getAttributes(myEntityDef)

  override val directiveKind: Angular2DirectiveKind
    get() = getCachedValue {
      create(
        getDirectiveKindNoCache(typeScriptClass), classModificationDependencies)
    }

  override val bindings: Angular2DirectiveProperties
    get() = getCachedValue {
      create(propertiesNoCache, classModificationDependencies)
    }

  private val propertiesNoCache: Angular2DirectiveProperties
    get() {
      val inputs = LinkedHashMap<String, Angular2DirectiveProperty>()
      val outputs = LinkedHashMap<String, Angular2DirectiveProperty>()

      val inputMap = LinkedHashMap<String, String>()
      val outputMap = LinkedHashMap<String, String>()

      val clazz = typeScriptClass

      JSClassUtils.processClassesInHierarchy(clazz, false) { aClass, _, _ ->
        if (aClass is TypeScriptClass) {
          val entityDef = Angular2IvySymbolDef.get(aClass, true)
          if (entityDef is Angular2IvySymbolDef.Directive) {
            readMappingsInto(entityDef, Angular2DecoratorUtil.INPUTS_PROP, inputMap)
            readMappingsInto(entityDef, Angular2DecoratorUtil.OUTPUTS_PROP, outputMap)
          }
        }
        true
      }

      TypeScriptTypeParser
        .buildTypeFromClass(clazz, false)
        .properties
        .forEach { prop ->
          if (prop.memberSource.singleElement != null) {
            processProperty(clazz, prop, inputMap, KIND_NG_DIRECTIVE_INPUTS, inputs)
            processProperty(clazz, prop, outputMap, KIND_NG_DIRECTIVE_OUTPUTS, outputs)
          }
        }

      hackIonicComponentOutputs(this, outputMap)

      inputMap.values.forEach { input -> inputs[input] = Angular2SourceDirectiveVirtualProperty(clazz, input, KIND_NG_DIRECTIVE_INPUTS) }
      outputMap.values.forEach { output ->
        outputs[output] = Angular2SourceDirectiveVirtualProperty(clazz, output, KIND_NG_DIRECTIVE_OUTPUTS)
      }

      return Angular2DirectiveProperties(inputs.values, outputs.values)
    }

  override fun createPointer(): Pointer<out Angular2Directive> {
    val entityDef = myEntityDef.createPointer()
    return Pointer {
      entityDef.dereference()?.let { Angular2IvyDirective(it) }
    }
  }

  protected fun createSelectorFromStringLiteralType(type: TypeScriptStringLiteralType): Angular2DirectiveSelector {
    return Angular2DirectiveSelectorImpl(type, type.innerText, 1)
  }

  companion object {

    private val IVY_SELECTOR = Key<Angular2DirectiveSelector>("ng.ivy.selector")
    private val IVY_EXPORT_AS = Key<List<String>>("ng.ivy.export-as")

    private fun getAttributes(entityDef: Angular2IvySymbolDef.Directive): Collection<Angular2DirectiveAttribute> {
      return CachedValuesManager.getCachedValue(entityDef.field) {
        val cls = entityDef.contextClass
        if (cls == null) {
          return@getCachedValue create(emptyList(), entityDef.field)
        }

        // find class with constructor
        val dependencies = HashSet<Any>()
        dependencies.add(cls)
        var constructor = cls.constructors.find { !it.isOverloadImplementation }
        if (constructor == null) {
          JSClassUtils.processClassesInHierarchy(cls, false) { aClass, _, _ ->
            dependencies.add(aClass)
            if (aClass is TypeScriptClass) {
              constructor = aClass.constructors.find { !it.isOverloadImplementation }
            }
            constructor == null
          }
          if (constructor == null) {
            return@getCachedValue create(emptyList(), *dependencies.toTypedArray())
          }
        }

        val constructorClass = PsiTreeUtil.getContextOfType(constructor, TypeScriptClass::class.java)
        if (constructorClass != null) {
          val attributeNames = Angular2IvySymbolDef.getFactory(constructorClass)?.attributeNames
          if (attributeNames != null) {
            return@getCachedValue create(
              attributeNames.entries.map { entry -> Angular2IvyDirectiveAttribute(entry.key, entry.value) },
              *dependencies.toTypedArray())
          }
        }

        // Try to fallback to metadata JSON information - Angular 9.0.x case
        val metadataDirective = getMetadataDirective(cls)
        if (metadataDirective == null) {
          return@getCachedValue create(emptyList(), cls, VFS_STRUCTURE_MODIFICATIONS)
        }
        create(metadataDirective.attributes, cls, metadataDirective)
      }
    }

    @JvmStatic
    protected fun getMetadataDirective(clazz: TypeScriptClass): Angular2Directive? {
      return CachedValuesManager.getCachedValue(clazz) {
        val metadataDirective = getMetadataEntity(clazz) as? Angular2Directive
        if (metadataDirective != null) {
          return@getCachedValue create(metadataDirective, clazz, metadataDirective)
        }
        create(null, clazz, VFS_STRUCTURE_MODIFICATIONS)
      }
    }

    private fun readMappingsInto(directiveDef: Angular2IvySymbolDef.Directive, field: String, target: MutableMap<String, String>) {
      directiveDef.readPropertyMappings(field)
        .forEach { (key, value) -> target.putIfAbsent(key, value) }
    }

    private fun processProperty(clazz: TypeScriptClass,
                                property: JSRecordType.PropertySignature,
                                mappings: MutableMap<String, String>,
                                kind: String,
                                result: MutableMap<String, Angular2DirectiveProperty>) {
      val bindingName = mappings.remove(property.memberName)
      if (bindingName != null) {
        result.putIfAbsent(bindingName, Angular2SourceDirectiveProperty(clazz, property, kind, bindingName))
      }
    }
  }
}
