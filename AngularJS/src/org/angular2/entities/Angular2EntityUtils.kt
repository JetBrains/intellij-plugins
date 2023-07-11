// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.psi.JSElement
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunctionSignature
import com.intellij.openapi.util.text.StringUtil
import org.angular2.entities.ivy.Angular2IvyEntity
import org.angular2.entities.metadata.psi.Angular2MetadataEntity
import org.angular2.entities.source.Angular2SourceEntity
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector.ParseException
import org.jetbrains.annotations.NonNls

object Angular2EntityUtils {

  @NonNls
  const val ELEMENT_REF = "ElementRef"

  @NonNls
  const val TEMPLATE_REF = "TemplateRef"

  @NonNls
  const val VIEW_CONTAINER_REF = "ViewContainerRef"

  const val NG_ACCEPT_INPUT_TYPE_PREFIX = "ngAcceptInputType_"

  private const val INDEX_ELEMENT_NAME_PREFIX = ">"
  const val anyElementDirectiveIndexName = "E"
  private const val INDEX_ATTRIBUTE_NAME_PREFIX = "="

  @JvmStatic
  fun getPipeTransformMembers(cls: TypeScriptClass): Collection<JSElement> =
    cls.jsType
      .asRecordType()
      .properties
      .asSequence()
      .filter { Angular2EntitiesProvider.TRANSFORM_METHOD == it.memberName }
      .flatMap { it.memberSource.allSourceElements }
      .filterIsInstance<JSElement>()
      .toList()
      .let { list ->
        list.filter { it !is TypeScriptFunctionSignature }
          .ifEmpty { list }
      }

  @JvmStatic
  fun parsePropertyMapping(property: String): Pair<String, String> {
    val ind = property.indexOf(':')
    return if (ind > 0) {
      Pair(property.substring(0, ind).trim { it <= ' ' }, property.substring(ind + 1).trim { it <= ' ' })
    }
    else Pair(property.trim { it <= ' ' }, property.trim { it <= ' ' })
  }

  @JvmStatic
  fun getElementDirectiveIndexName(elementName: String): String {
    return INDEX_ELEMENT_NAME_PREFIX + elementName
  }

  @JvmStatic
  fun isElementDirectiveIndexName(elementName: String): Boolean {
    return elementName.startsWith(INDEX_ELEMENT_NAME_PREFIX)
  }

  @JvmStatic
  fun getElementName(elementDirectiveIndexName: String): String {
    require(isElementDirectiveIndexName(elementDirectiveIndexName))
    return elementDirectiveIndexName.substring(1)
  }

  @JvmStatic
  fun getAttributeDirectiveIndexName(attributeName: String): String {
    return INDEX_ATTRIBUTE_NAME_PREFIX + attributeName
  }

  @JvmStatic
  fun isAttributeDirectiveIndexName(attributeName: String): Boolean {
    return attributeName.startsWith(INDEX_ATTRIBUTE_NAME_PREFIX)
  }

  @JvmStatic
  fun getAttributeName(attributeIndexName: String): String {
    require(isAttributeDirectiveIndexName(attributeIndexName))
    return attributeIndexName.substring(1)
  }

  @JvmStatic
  fun <T : Angular2Module> defaultChooseModule(modules: Collection<T>): T? {
    return modules.minByOrNull { it.getName() }
  }

  @JvmStatic
  fun getDirectiveIndexNames(selector: String): Set<String> {
    val selectors: List<Angular2DirectiveSimpleSelector>
    try {
      selectors = Angular2DirectiveSimpleSelector.parse(selector)
    }
    catch (e: ParseException) {
      return emptySet()
    }

    val result = HashSet<String>()
    val indexSelector = { sel: Angular2DirectiveSimpleSelector ->
      val elementName = sel.elementName
      if (StringUtil.isEmpty(elementName) || "*" == elementName) {
        result.add(INDEX_ELEMENT_NAME_PREFIX)
      }
      else {
        result.add(INDEX_ELEMENT_NAME_PREFIX + elementName!!)
        result.add(anyElementDirectiveIndexName)
      }
      for (attrName in sel.attrNames) {
        result.add(INDEX_ATTRIBUTE_NAME_PREFIX + attrName)
      }
    }
    for (sel in selectors) {
      indexSelector(sel)
      sel.notSelectors.forEach(indexSelector)
    }
    return result
  }

  @JvmStatic
  fun toString(element: Angular2Element): String {
    val sourceKind: String
    when (element) {
      is Angular2SourceEntity -> sourceKind = "source"
      is Angular2MetadataEntity<*> -> sourceKind = "metadata"
      is Angular2IvyEntity<*> -> sourceKind = "ivy"
      else -> sourceKind = "unknown"
    }
    if (element is Angular2Directive) {
      val result = StringBuilder()
      result.append(element.getName())
        .append(" <")
        .append(sourceKind)
        .append(' ')
      if (element.isComponent) {
        result.append("component")
      }
      else {
        val kind = element.directiveKind
        if (kind.isStructural) {
          if (kind.isRegular) {
            result.append("directive/")
          }
          result.append("template")
        }
        else {
          result.append("directive")
        }
      }
      result.append(">")
        .append(": selector=")
        .append(element.selector.text)
      if (element is Angular2Component && !element.ngContentSelectors.isEmpty()) {
        result.append("; ngContentSelectors=")
        result.append(element.ngContentSelectors)
      }
      if (!element.exportAs.isEmpty()) {
        result.append("; exportAs=")
          .append(StringUtil.join(element.exportAs.mapNotNull { (key, value) -> key.takeIf { value === element } }, ","))
      }
      result.append("; inputs=")
        .append(element.inputs.toString())
        .append("; outputs=")
        .append(element.outputs.toString())
        .append("; inOuts=")
        .append(element.inOuts.toString())

      if (!element.attributes.isEmpty()) {
        result.append("; attributes=")
          .append(element.attributes)
      }
      if (!element.hostDirectives.isEmpty()) {
        result.append("; hostDirectives=")
          .append(element.hostDirectives.mapNotNull { it.directive }.render())
      }

      return result.toString()
    }
    else return when (element) {
      is Angular2Pipe -> element.getName() + " <" + sourceKind + " pipe>"
      is Angular2DirectiveProperty -> element.name + (if (element.required) " <required>" else "")
      is Angular2DirectiveAttribute -> element.name
      is Angular2Module -> element.getName() +
                           " <" + sourceKind + " module>: " +
                           "imports=[" + element.imports.render() + "]; " +
                           "declarations=[" + element.declarations.render() + "]; " +
                           "exports=[" + element.exports.render() + "]; " +
                           "scopeFullyResolved=" + element.isScopeFullyResolved + "; " +
                           "exportsFullyResolved=" + element.areExportsFullyResolved()
      else -> element.javaClass.name + "@" + Integer.toHexString(element.hashCode())
    }
  }

  @JvmStatic
  fun <T : Angular2Entity> renderEntityList(entities: Collection<T>): String {
    val result = StringBuilder()
    var i = -1
    for (entity in entities) {
      if (++i > 0) {
        if (i == entities.size - 1) {
          result.append(' ')
          result.append(Angular2Bundle.message("angular.description.and-separator"))
          result.append(' ')
        }
        else {
          result.append(", ")
        }
      }
      result.append(getEntityClassName(entity))
      if (entity is Angular2Pipe) {
        result.append(" (")
        result.append(entity.getName())
        result.append(")")
      }
      else if (entity is Angular2Directive) {
        result.append(" (")
        result.append((entity as Angular2Directive).selector.text)
        result.append(')')
      }
    }
    return result.toString()
  }

  @JvmStatic
  fun getEntityClassName(entity: Angular2Entity): String {
    return if (entity is Angular2Pipe) {
      entity.typeScriptClass?.name ?: Angular2Bundle.message("angular.description.unknown-class")
    }
    else entity.getName()
  }

  @JvmStatic
  fun getEntityClassName(decorator: ES6Decorator): String {
    val entity = Angular2EntitiesProvider.getEntity(decorator) ?: return Angular2Bundle.message("angular.description.unknown-class")
    return getEntityClassName(entity)
  }

  @JvmStatic
  fun isImportableEntity(entity: Angular2Entity): Boolean {
    return entity is Angular2Module || entity is Angular2Declaration && entity.isStandalone
  }

  /**
   * Calls `moduleConsumer` for each NgModule, ignoring Components, Directives & Pipes.
   *
   * @see .forEachEntity
   */
  @JvmStatic
  fun forEachModule(entities: Iterable<Angular2Entity>,
                    moduleConsumer: (Angular2Module) -> Unit) {
    forEachEntity(entities, moduleConsumer) {}
  }

  /**
   * Calls `moduleConsumer` for each NgModule, or `declarationConsumer` for each Component, Directive, or Pipe.
   *
   * @see .forEachModule
   */
  @JvmStatic
  fun forEachEntity(entities: Iterable<Angular2Entity>,
                    moduleConsumer: (Angular2Module) -> Unit,
                    declarationConsumer: (Angular2Declaration) -> Unit) {
    entities.forEach { entity ->
      when (entity) {
        is Angular2Module -> moduleConsumer(entity)
        is Angular2Declaration -> declarationConsumer(entity)
      }
    }
  }

  fun jsTypeFromAcceptInputType(clz: TypeScriptClass?, name: String): JSType? =
    clz
      ?.staticJSType
      ?.asRecordType()
      ?.findPropertySignature(NG_ACCEPT_INPUT_TYPE_PREFIX + name)
      ?.jsType

  private fun Collection<Angular2Entity>.render(): String =
    this.asSequence().map { it.getName() }.sorted().joinToString(", ")
}
