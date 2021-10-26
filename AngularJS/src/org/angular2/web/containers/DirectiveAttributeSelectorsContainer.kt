// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.containers

import com.intellij.javascript.web.symbols.*
import com.intellij.model.Pointer
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import org.angular2.Angular2Framework
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2StructuralDirectiveSymbol
import org.angular2.web.Angular2Symbol

class DirectiveAttributeSelectorsContainer(val project: Project) : WebSymbolsContainer {

  override fun createPointer(): Pointer<out WebSymbolsContainer> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getSymbols(namespace: WebSymbolsContainer.Namespace?,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          context: Stack<WebSymbolsContainer>): List<WebSymbolsContainer> =
    if (namespace == WebSymbolsContainer.Namespace.HTML && kind == WebSymbol.KIND_HTML_ELEMENTS && name != null) {
      listOf(HtmlAttributeDirectiveAttributeSelectorsExtension(project, name))
    }
    else emptyList()

  override fun equals(other: Any?): Boolean =
    other is DirectiveAttributeSelectorsContainer
    && other.project == project

  override fun hashCode(): Int =
    project.hashCode()

  class HtmlAttributeDirectiveAttributeSelectorsExtension(project: Project,
                                                          tagName: String)
    : WebSymbolsContainerWithCache<Project, String>(Angular2Framework.ID, project, project, tagName), WebSymbol {

    override fun provides(namespace: WebSymbolsContainer.Namespace, kind: String): Boolean =
      namespace == WebSymbolsContainer.Namespace.JS

    override val name: String
      get() = key

    override val extension: Boolean
      get() = true

    override val origin: WebSymbolsContainer.Origin
      get() = WebSymbolsContainer.OriginData(Angular2Framework.ID, null)

    override val namespace: WebSymbolsContainer.Namespace
      get() = WebSymbolsContainer.Namespace.HTML

    override val kind: SymbolKind
      get() = WebSymbol.KIND_HTML_ELEMENTS

    override fun getModificationCount(): Long =
      project.psiModificationCount

    override fun createPointer(): Pointer<HtmlAttributeDirectiveAttributeSelectorsExtension> =
      Pointer.hardPointer(this)

    override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)

      val tagName = key
      val isTemplateTag = isTemplateTag(tagName)
      val inputs = HashMap<String, Angular2Symbol>()
      val outputs = HashMap<String, Angular2Symbol>()
      val inOuts = HashMap<String, Angular2Symbol>()
      val attributes = HashMap<String, Angular2Symbol>()

      for (candidate in findElementDirectivesCandidates(project, tagName)
        .asSequence().plus(findElementDirectivesCandidates(project, ""))) {

        fillNamesAndProperties(inputs, candidate.inputs)
        val kind = candidate.directiveKind

        if (!isTemplateTag && kind.isStructural) {
          val selectors = candidate.selector.simpleSelectorsWithPsi
          for (selector in selectors) {
            val attributeCandidates = selector.attributes
            if (attributeCandidates.size == 1) {
              consumer(createAngular2StructuralDirectiveSymbol(candidate, inputs, attributeCandidates[0]))
            }
            else {
              CANDIDATES_LOOP@ for (attr in attributeCandidates) {
                val attrName = attr.name
                for (input in inputs.keys) {
                  if (!input.startsWith(attrName)) {
                    break@CANDIDATES_LOOP
                  }
                }
                consumer(createAngular2StructuralDirectiveSymbol(candidate, inputs, attr))
              }
            }
          }
        }
        if (isTemplateTag || kind.isRegular) {
          fillNamesAndProperties(outputs, candidate.outputs)
          fillNamesAndProperties(inOuts, candidate.inOuts)
          fillNamesAndProperties(attributes, candidate.attributes)
          for (selector in candidate.selector.simpleSelectorsWithPsi) {
            for (attr in selector.attributes) {
              val attrName = attr.name
              var added = false
              sequenceOf(inOuts, inputs, attributes, outputs)
                .mapNotNull { it[attrName] }
                .forEach {
                  consumer(Angular2DirectiveSymbolWrapper.create(candidate, it))
                  added = true
                }
              if (!added) {
                consumer(Angular2DirectiveSymbolWrapper.create(candidate, attr))
              }
            }
            for (notSelector in selector.notSelectors) {
              for (attr in notSelector.attributes) {
                consumer(Angular2DirectiveSymbolWrapper.create(candidate, attr))
              }
            }
          }
        }
      }
    }

    companion object {
      private fun fillNamesAndProperties(map: MutableMap<String, Angular2Symbol>,
                                         propertiesCollection: Collection<Angular2Symbol>) {
        map.clear()
        for (item in propertiesCollection) {
          map[item.name] = item
        }
      }

      private fun createAngular2StructuralDirectiveSymbol(directive: Angular2Directive,
                                                          inputs: HashMap<String, Angular2Symbol>,
                                                          selector: Angular2DirectiveSelectorSymbol): Angular2Symbol =
        inputs[selector.name]?.let { Angular2StructuralDirectiveSymbol.create(directive, it, true) }
        ?: Angular2StructuralDirectiveSymbol.create(directive, selector, inputs.any { it.key.startsWith(selector.name) })
    }

  }

}