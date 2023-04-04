// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.lang.javascript.psi.JSRecordType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.types.TypeScriptTypeParser
import com.intellij.lang.javascript.psi.util.JSClassUtils
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.Pair.pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.CachedValuesManager
import com.intellij.util.containers.Stack
import org.angular2.Angular2DecoratorUtil.INPUTS_PROP
import org.angular2.Angular2DecoratorUtil.OUTPUTS_PROP
import org.angular2.codeInsight.Angular2LibrariesHacks
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperties
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.metadata.stubs.Angular2MetadataClassStubBase
import org.angular2.lang.Angular2Bundle
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_OUTPUTS

abstract class Angular2MetadataClassBase<Stub : Angular2MetadataClassStubBase<*>>(element: Stub)
  : Angular2MetadataElement<Stub>(element) {

  val typeScriptClass: TypeScriptClass?
    get() = classAndDependencies.first

  val extendedClass: Angular2MetadataClassBase<out Angular2MetadataClassStubBase<*>>?
    get() {
      val refStub = stub.extendsReference
      return refStub?.psi?.resolve() as? Angular2MetadataClassBase<*>
    }

  val sourceElement: PsiElement
    get() = typeScriptClass ?: this

  val bindings: Angular2DirectiveProperties
    get() = CachedValuesManager.getCachedValue(this, CachedValueProvider { propertiesNoCache })

  private val classAndDependencies: Pair<TypeScriptClass?, Collection<Any>>
    get() = CachedValuesManager.getCachedValue(this) {
      ProgressManager.checkCanceled()
      val className = stub.className
      val nodeModule = nodeModule
      val fileAndClass = if (className != null && nodeModule != null)
        nodeModule.locateFileAndMember(className, TypeScriptClass::class.java)
      else
        Pair.create<PsiFile?, TypeScriptClass?>(null, null)
      val dependencies = HashSet<Any>()
      dependencies.add(containingFile)
      if (fileAndClass.second != null) {
        JSClassUtils.processClassesInHierarchy(fileAndClass.second, true) { aClass, _, _ ->
          dependencies.add(aClass.containingFile)
          true
        }
      }
      else if (fileAndClass.first != null) {
        dependencies.add(fileAndClass.first)
      }
      Result.create(Pair.create(fileAndClass.second, dependencies), dependencies)
    }

  private val propertiesNoCache: Result<Angular2DirectiveProperties>
    get() {
      val mappings = allMappings
      val inputs = collectProperties(mappings.value.first, KIND_NG_DIRECTIVE_INPUTS)
      val outputs = collectProperties(mappings.value.second, KIND_NG_DIRECTIVE_OUTPUTS)
      return Result.create(Angular2DirectiveProperties(inputs, outputs),
                           *mappings.dependencyItems)
    }

  private val allMappings: Result<Pair<Map<String, String>, Map<String, String>>>
    get() {
      val inputs = HashMap<String, String>()
      val outputs = HashMap<String, String>()
      val classes = Stack<Angular2MetadataClassBase<out Angular2MetadataClassStubBase<*>>>()
      var current: Angular2MetadataClassBase<out Angular2MetadataClassStubBase<*>>? = this
      while (current != null) {
        classes.push(current)
        current = current.extendedClass
      }
      if (this is Angular2Directive) {
        outputs.putAll(Angular2LibrariesHacks.hackIonicComponentOutputs(this))
      }
      while (!classes.isEmpty()) {
        current = classes.pop()
        inputs.putAll(current!!.stub.inputMappings)
        outputs.putAll(current.stub.outputMappings)
      }
      val cacheDependencies = HashSet<Any>()

      fun collectAdditionalMappings(map: MutableMap<String, String>, prop: String) {
        val mappings = resolveMappings(prop)
        map.putAll(mappings.value)
        cacheDependencies.addAll(mappings.dependencyItems)
      }
      collectAdditionalMappings(inputs, INPUTS_PROP)
      collectAdditionalMappings(outputs, OUTPUTS_PROP)
      return Result.create(pair(inputs, outputs), cacheDependencies)
    }

  override fun getName(): String =
    getCachedClassBasedValue { cls ->
      cls?.name ?: stub.memberName ?: Angular2Bundle.message("angular.description.unnamed")
    }

  protected fun <T> getCachedClassBasedValue(provider: (TypeScriptClass?) -> T): T {
    return CachedValuesManager.getCachedValue(
      this,
      CachedValuesManager.getManager(project).getKeyForClass(provider.javaClass)
    ) {
      val dependencies = classAndDependencies
      Result.create(provider(dependencies.first), dependencies.second)
    }
  }

  fun getPropertySignature(fieldName: String): JSRecordType.PropertySignature? {
    return typeScriptClass?.let { cls ->
      TypeScriptTypeParser.buildTypeFromClass(cls, false)
        .findPropertySignature(fieldName)
    }
  }

  private fun collectProperties(mappings: Map<String, String>, kind: String): List<Angular2DirectiveProperty> {
    val result = ArrayList<Angular2DirectiveProperty>()
    mappings.forEach { (fieldName: String, bindingName: String) ->
      result.add(Angular2MetadataDirectiveProperty(
        this, fieldName, bindingName, kind))
    }
    return result
  }

  protected open fun resolveMappings(prop: String): Result<Map<String, String>> {
    return Result.create(emptyMap(), this)
  }
}
