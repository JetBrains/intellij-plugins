// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.documentation.mdn.MdnSymbolDocumentation
import com.intellij.documentation.mdn.getDomEventDocumentation
import com.intellij.javascript.web.codeInsight.html.WebSymbolsHtmlAdditionalContextProvider
import com.intellij.javascript.web.symbols.*
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Namespace.HTML
import com.intellij.javascript.web.symbols.WebSymbolsContainer.Namespace.JS
import com.intellij.javascript.web.types.WebJSTypesUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.util.containers.Stack
import org.angular2.Angular2Framework
import org.angular2.codeInsight.attributes.DomElementSchemaRegistry
import org.angular2.lang.html.parser.Angular2AttributeNameParser
import org.angular2.lang.types.Angular2TypeUtils
import org.angular2.web.Angular2PsiSourcedSymbol
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2WebSymbolsAdditionalContextProvider.Companion.EVENT_ATTR_PREFIX
import java.util.*

class StandardPropertyAndEventsContainer(private val templateFile: PsiFile) : WebSymbolsContainer {

  override fun getModificationCount(): Long = templateFile.project.psiModificationCount

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == HTML && kind == WebSymbol.KIND_HTML_ELEMENTS && name != null) {
      listOf(HtmlElementStandardPropertyAndEventsExtension(templateFile, "", name))
    }
    else emptyList()

  override fun createPointer(): Pointer<StandardPropertyAndEventsContainer> {
    val templateFile = this.templateFile.createSmartPointer()
    return Pointer {
      templateFile.dereference()?.let { StandardPropertyAndEventsContainer(it) }
    }
  }

  override fun equals(other: Any?): Boolean =
    other is StandardPropertyAndEventsContainer
    && other.templateFile == templateFile

  override fun hashCode(): Int =
    templateFile.hashCode()

  private class HtmlElementStandardPropertyAndEventsExtension(
    templateFile: PsiFile, tagNamespace: String, tagName: String)
    : WebSymbolsContainerWithCache<PsiFile, Pair<String, String>>(Angular2Framework.ID, templateFile.project,
                                                                  templateFile, Pair(tagNamespace, tagName)), WebSymbol {

    override fun provides(namespace: WebSymbolsContainer.Namespace, kind: String): Boolean =
      (namespace == JS && kind == WebSymbol.KIND_JS_PROPERTIES)
      || (namespace == HTML && kind == WebSymbol.KIND_HTML_EVENTS)

    override val name: String
      get() = key.second

    override val extension: Boolean
      get() = true

    override val origin: WebSymbolsContainer.Origin
      get() = WebSymbolsContainer.OriginData(Angular2Framework.ID, null)

    override val namespace: WebSymbolsContainer.Namespace
      get() = HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ELEMENTS

    override fun getModificationCount(): Long =
      project.psiModificationCount

    override fun createPointer(): Pointer<HtmlElementStandardPropertyAndEventsExtension> {
      val templateFile = this.dataHolder.createSmartPointer()
      val tagName = this.key.first
      val tagNamespace = this.key.second
      return Pointer {
        templateFile.dereference()?.let { HtmlElementStandardPropertyAndEventsExtension(it, tagNamespace, tagName) }
      }
    }

    override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      val templateFile = dataHolder
      val tagNamespace = key.first
      val tagName = key.second

      val typeSource = Angular2TypeUtils.createJSTypeSourceForXmlElement(templateFile)
      val tagClass = WebJSTypesUtil.getHtmlElementClassType(typeSource, tagName)
      val elementEventMap = Angular2TypeUtils.getElementEventMap(typeSource)?.asRecordType()

      val allowedElementProperties = DomElementSchemaRegistry.getElementProperties(tagNamespace, tagName).toMutableSet()
      val eventNames = DomElementSchemaRegistry.getElementEvents(tagNamespace, tagName).toMutableSet()
      elementEventMap?.propertyNames?.forEach { name -> eventNames.add(name) }

      fun addStandardProperty(name: String, project: Project, source: TypeScriptPropertySignature?) {
        propToAttrName[name]?.let { consumer(Angular2StandardProperty(it, project, source)) }
        consumer(Angular2StandardProperty(name, project, source))
      }

      if (tagClass != null) {
        for (property in tagClass.asRecordType().properties) {
          val propertyDeclaration = property.memberSource.singleElement
          if (propertyDeclaration is TypeScriptPropertySignature) {
            if (propertyDeclaration.attributeList?.hasModifier(JSAttributeList.ModifierType.READONLY) == true) {
              continue
            }
            cacheDependencies.add(propertyDeclaration.containingFile)
            val name: String
            if (property.memberName.startsWith(EVENT_ATTR_PREFIX)) {
              val eventName = property.memberName.substring(2)
              eventNames.remove(eventName)
              consumer(Angular2StandardEvent(eventName,
                                             propertyDeclaration.project, propertyDeclaration,
                                             elementEventMap?.findPropertySignature(eventName)
                                               ?.memberSource?.singleElement as? TypeScriptPropertySignature))
            }
            else {
              name = property.memberName
              if (!allowedElementProperties.remove(name)) {
                continue
              }
              addStandardProperty(name, propertyDeclaration.project, propertyDeclaration)
            }
          }
        }
      }
      for (name in allowedElementProperties) {
        addStandardProperty(name, templateFile.project, null)
      }
      for (eventName in eventNames) {
        consumer(Angular2StandardEvent(eventName,
                                       templateFile.project,
                                       null,
                                       elementEventMap?.findPropertySignature(eventName)
                                         ?.memberSource?.singleElement as? TypeScriptPropertySignature))
      }
      if (cacheDependencies.isEmpty()) {
        cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      }
    }

  }

  companion object {
    private val propToAttrName = Angular2AttributeNameParser.ATTR_TO_PROP_MAPPING
      .asSequence().associate { Pair(it.value, it.key) }
  }

  private class Angular2StandardProperty(override val name: String,
                                         override val project: Project,
                                         override val source: TypeScriptPropertySignature?) : Angular2PsiSourcedSymbol {


    override fun createPointer(): Pointer<Angular2StandardProperty> {
      val name = name
      val project = project
      val source = source?.createSmartPointer()
      return Pointer {
        val newSource = source?.let {
          it.dereference() ?: return@Pointer null
        }
        Angular2StandardProperty(name, project, newSource)
      }
    }

    override val jsType: JSType?
      get() = source?.jsType

    override val namespace: WebSymbolsContainer.Namespace
      get() = JS

    override val kind: SymbolKind
      get() = WebSymbol.KIND_JS_PROPERTIES

    override fun equals(other: Any?): Boolean =
      other === this
      || other is Angular2StandardProperty
      && other.name == name
      && other.project == project
      && other.source == source

    override fun hashCode(): Int =
      Objects.hash(name, project, source)

  }

  private class Angular2StandardEvent(override val name: String,
                                      override val project: Project,
                                      private val mainSource: TypeScriptPropertySignature?,
                                      private val mapSource: TypeScriptPropertySignature?)
    : WebSymbolsHtmlAdditionalContextProvider.StandardHtmlSymbol(), Angular2PsiSourcedSymbol {

    override val source: PsiElement?
      get() = mainSource ?: mapSource

    override fun getMdnDocumentation(): MdnSymbolDocumentation? =
      getDomEventDocumentation(name)

    override fun createPointer(): Pointer<Angular2StandardEvent> {
      val name = this.name
      val project = this.project
      val mainSourcePtr = mainSource?.createSmartPointer()
      val mapSourcePtr = mapSource?.createSmartPointer()
      return Pointer {
        val mainSource = mainSourcePtr?.let {
          it.dereference() ?: return@Pointer null
        }
        val mapSource = mapSourcePtr?.let {
          it.dereference() ?: return@Pointer null
        }
        Angular2StandardEvent(name, project, mainSource, mapSource)
      }
    }

    override val jsType: JSType?
      get() = Angular2TypeUtils.extractEventVariableType(mainSource?.jsType) ?: mapSource?.jsType

    override val namespace: WebSymbolsContainer.Namespace
      get() = HTML

    override val priority: WebSymbol.Priority
      get() = WebSymbol.Priority.NORMAL

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_EVENTS

    override fun equals(other: Any?): Boolean =
      other === this
      || other is Angular2StandardEvent
      && other.name == name
      && other.project == project
      && other.mainSource == mainSource
      && other.mapSource == mapSource

    override fun hashCode(): Int =
      Objects.hash(name, project, mainSource, mapSource)

  }

}