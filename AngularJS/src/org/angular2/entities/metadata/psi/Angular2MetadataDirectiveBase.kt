// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi

import com.intellij.lang.javascript.psi.JSParameter
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider.Result
import org.angular2.entities.*
import org.angular2.entities.metadata.stubs.Angular2MetadataDirectiveStubBase

abstract class Angular2MetadataDirectiveBase<Stub : Angular2MetadataDirectiveStubBase<*>>(element: Stub)
  : Angular2MetadataDeclaration<Stub>(element), Angular2Directive {

  private val myExportAsList = NotNullLazyValue.lazy {
    val exportAsString = stub.exportAs
    if (exportAsString == null)
      emptyList()
    else
      StringUtil.split(exportAsString, ",")
  }
  private val mySelector = NotNullLazyValue.lazy<Angular2DirectiveSelector> { Angular2DirectiveSelectorImpl(this, stub.selector, null) }
  private val myAttributes = NotNullLazyValue.lazy { this.buildAttributes() }

  override val selector: Angular2DirectiveSelector
    get() = mySelector.value

  override val exportAsList: List<String>
    get() = myExportAsList.value

  override val attributes: Collection<Angular2DirectiveAttribute>
    get() = myAttributes.value

  override fun resolveMappings(prop: String): Result<Map<String, String>> {
    val propertyStub = stub.getDecoratorFieldValueStub(prop)
                       ?: return Result.create(emptyMap(), this)
    val result = HashMap<String, String>()
    val cacheDependencies = HashSet<PsiElement>()
    collectReferencedElements(propertyStub.psi, { element ->
      if (element is Angular2MetadataString) {
        val p = Angular2EntityUtils.parsePropertyMapping(element.value)
        result.putIfAbsent(p.first, p.second)
      }
    }, cacheDependencies)
    return Result.create(result, cacheDependencies)
  }

  private fun buildAttributes(): Collection<Angular2DirectiveAttribute> {
    return stub.attributes.map { (name, index) -> Angular2MetadataDirectiveAttribute(this, index, name) }
  }

  fun getConstructorParameter(index: Int): JSParameter? {
    val cls = typeScriptClass
    if (cls == null || index < 0) {
      return null
    }
    val constructors = cls.constructors
    val ctor = if (constructors.size == 1)
      constructors[0]
    else
      constructors.find { it.isOverloadImplementation }
    if (ctor == null) {
      return null
    }
    val parameters = ctor.parameterVariables
    return if (index < parameters.size) parameters[index] else null
  }
}
