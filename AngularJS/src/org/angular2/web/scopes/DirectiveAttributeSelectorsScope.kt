// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.javascript.webSymbols.types.TypeScriptSymbolTypeSupport
import com.intellij.model.Pointer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.containers.Stack
import com.intellij.webSymbols.*
import com.intellij.webSymbols.query.WebSymbolsNameMatchQueryParams
import com.intellij.webSymbols.utils.psiModificationCount
import org.angular2.Angular2Framework
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2StructuralDirectiveSymbol
import org.angular2.web.Angular2Symbol
import org.angular2.web.Angular2WebSymbolsQueryConfigurator
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.KIND_NG_DIRECTIVE_INPUTS

class DirectiveAttributeSelectorsScope(val project: Project) : WebSymbolsScope {

  override fun createPointer(): Pointer<out WebSymbolsScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getSymbols(namespace: SymbolNamespace,
                          kind: SymbolKind,
                          name: String?,
                          params: WebSymbolsNameMatchQueryParams,
                          scope: Stack<WebSymbolsScope>): List<WebSymbolsScope> =
    if (namespace == WebSymbol.NAMESPACE_HTML && kind == WebSymbol.KIND_HTML_ELEMENTS && name != null) {
      listOf(HtmlAttributeDirectiveAttributeSelectorsExtension(project, name))
    }
    else emptyList()

  override fun equals(other: Any?): Boolean =
    other is DirectiveAttributeSelectorsScope
    && other.project == project

  override fun hashCode(): Int =
    project.hashCode()

  class HtmlAttributeDirectiveAttributeSelectorsExtension(project: Project,
                                                          tagName: String)
    : WebSymbolsScopeWithCache<Project, String>(Angular2Framework.ID, project, project, tagName), WebSymbol {

    override fun provides(namespace: SymbolNamespace, kind: SymbolKind): Boolean =
      namespace == WebSymbol.NAMESPACE_JS

    override val name: String
      get() = key

    override val extension: Boolean
      get() = true

    override val origin: WebSymbolOrigin
      get() = WebSymbolOrigin.create(Angular2Framework.ID, typeSupport = TypeScriptSymbolTypeSupport())

    override val namespace: SymbolNamespace
      get() = WebSymbol.NAMESPACE_HTML

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

      fun processStructuralDirective(candidate: Angular2Directive) {
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

      for (candidate in findElementDirectivesCandidates(project, tagName)
        .asSequence().plus(findElementDirectivesCandidates(project, ""))) {

        ProgressManager.checkCanceled()

        fillNamesAndProperties(inputs, candidate.inputs)
        val kind = candidate.directiveKind

        if (!isTemplateTag && kind.isStructural) {
          processStructuralDirective(candidate)
        }
        if (isTemplateTag || kind.isRegular) {
          fillNamesAndProperties(outputs, candidate.outputs)
          fillNamesAndProperties(inOuts, candidate.inOuts)
          fillNamesAndProperties(attributes, candidate.attributes)
          for (selector in candidate.selector.simpleSelectorsWithPsi) {
            for (attr in selector.attributes) {
              val attrName = attr.name
              var addSelector = true
              sequenceOf(inOuts, inputs, attributes, outputs)
                .mapNotNull { it[attrName] }
                .forEach {
                  consumer(Angular2DirectiveSymbolWrapper.create(candidate, it))
                  addSelector = addSelector && (it !is Angular2DirectiveProperty || it.virtualProperty || (it.kind == KIND_NG_DIRECTIVE_INPUTS && !it.required))
                }
              if (addSelector) {
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
      if (!isTemplateTag) {
        for (candidate in findElementDirectivesCandidates(project, Angular2WebSymbolsQueryConfigurator.ELEMENT_NG_TEMPLATE)) {
          if (candidate.directiveKind.isStructural) {
            processStructuralDirective(candidate)
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