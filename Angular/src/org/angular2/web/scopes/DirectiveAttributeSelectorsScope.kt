// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.model.Pointer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.PolySymbolQualifiedName
import com.intellij.polySymbols.completion.PolySymbolCodeCompletionItem
import com.intellij.polySymbols.html.HTML_ELEMENTS
import com.intellij.polySymbols.query.PolySymbolCodeCompletionQueryParams
import com.intellij.polySymbols.query.PolySymbolNameMatchQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.utils.MappedPolySymbol
import com.intellij.polySymbols.utils.PolySymbolScopeWithCache
import com.intellij.polySymbols.utils.qualifiedName
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.asSafely
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2DirectiveProperty
import org.angular2.entities.Angular2DirectiveSelectorSymbol
import org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates
import org.angular2.web.*

class DirectiveAttributeSelectorsScope(val file: PsiFile) : PolySymbolScope {

  override fun createPointer(): Pointer<out PolySymbolScope> =
    Pointer.hardPointer(this)

  override fun getModificationCount(): Long = 0

  override fun getMatchingSymbols(
    qualifiedName: PolySymbolQualifiedName,
    params: PolySymbolNameMatchQueryParams,
    stack: PolySymbolQueryStack,
  ): List<PolySymbol> =
    if (qualifiedName.matches(HTML_ELEMENTS)) {
      listOf(HtmlAttributeDirectiveAttributeSelectorsExtension(file, qualifiedName.name))
    }
    else emptyList()

  override fun equals(other: Any?): Boolean =
    other is DirectiveAttributeSelectorsScope
    && other.file == file

  override fun hashCode(): Int =
    file.hashCode()

  class HtmlAttributeDirectiveAttributeSelectorsExtension(
    file: PsiFile,
    tagName: String,
  ) : PolySymbolScopeWithCache<PsiFile, String>(file.project, file, tagName), PolySymbol {

    override fun provides(kind: PolySymbolKind): Boolean =
      kind in providedKinds

    override val name: String
      get() = key

    override val extension: Boolean
      get() = true

    override val origin: PolySymbolOrigin
      get() = Angular2SymbolOrigin.empty

    override val kind: PolySymbolKind
      get() = HTML_ELEMENTS

    override fun getModificationCount(): Long =
      PsiModificationTracker.getInstance(project).modificationCount

    override fun createPointer(): Pointer<HtmlAttributeDirectiveAttributeSelectorsExtension> =
      Pointer.hardPointer(this)

    override fun initialize(consumer: (PolySymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
      cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
      withTypeEvaluationLocation(dataHolder) {
        initializeWithTypeLocation(consumer)
      }
    }

    override fun getCodeCompletions(
      qualifiedName: PolySymbolQualifiedName,
      params: PolySymbolCodeCompletionQueryParams,
      stack: PolySymbolQueryStack,
    ): List<PolySymbolCodeCompletionItem> =
      withTypeEvaluationLocation(dataHolder) {
        super<PolySymbolScopeWithCache>.getCodeCompletions(qualifiedName, params, stack)
          .filter { it.symbol.asSafely<Angular2StructuralDirectiveSymbol>()?.directive?.directiveKind?.isStructural != false }
      }

    private fun initializeWithTypeLocation(consumer: (PolySymbol) -> Unit) {
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
            consumer(createAngular2StructuralDirectiveSymbol(candidate, inputs, attributeCandidates[0], dataHolder))
          }
          else {
            CANDIDATES_LOOP@ for (attr in attributeCandidates) {
              val attrName = attr.name
              for (input in inputs.keys) {
                if (!input.startsWith(attrName)) {
                  break@CANDIDATES_LOOP
                }
              }
              consumer(createAngular2StructuralDirectiveSymbol(candidate, inputs, attr, dataHolder))
            }
          }
        }
      }

      for (candidate in findElementDirectivesCandidates(project, tagName)
        .asSequence().plus(findElementDirectivesCandidates(project, ""))) {

        ProgressManager.checkCanceled()

        fillNamesAndProperties(inputs, candidate.inputs)
        val kind = candidate.directiveKind

        if (!isTemplateTag) {
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
                  consumer(Angular2DirectiveSymbolWrapper.create(candidate, it, dataHolder))
                  addSelector =
                    addSelector && (it !is Angular2DirectiveProperty || it.virtualProperty || (it.kind == NG_DIRECTIVE_INPUTS && !it.required))
                }
              if (addSelector) {
                consumer(Angular2DirectiveSymbolWrapper.create(candidate, attr, dataHolder))
                if (kind.isStructural && isTemplateTag && !inputs.containsKey(attrName)) {
                  // Add fake input
                  consumer(MappedPolySymbol.create(NG_DIRECTIVE_INPUTS, attrName, attr.origin, attr.qualifiedName))
                }
              }
            }
            for (notSelector in selector.notSelectors) {
              for (attr in notSelector.attributes) {
                consumer(Angular2DirectiveSymbolWrapper.create(candidate, attr, dataHolder))
              }
            }
          }
        }
      }
      if (!isTemplateTag) {
        for (candidate in findElementDirectivesCandidates(project, ELEMENT_NG_TEMPLATE)) {
          processStructuralDirective(candidate)
        }
      }
    }

    companion object {

      val providedKinds: Set<PolySymbolKind> = setOf(
        NG_DIRECTIVE_ELEMENT_SELECTORS, NG_DIRECTIVE_ATTRIBUTE_SELECTORS, NG_STRUCTURAL_DIRECTIVES,
        NG_DIRECTIVE_INPUTS, NG_DIRECTIVE_OUTPUTS, NG_DIRECTIVE_IN_OUTS, NG_DIRECTIVE_ATTRIBUTES,
      )

      private fun fillNamesAndProperties(
        map: MutableMap<String, Angular2Symbol>,
        propertiesCollection: Collection<Angular2Symbol>,
      ) {
        map.clear()
        for (item in propertiesCollection) {
          map[item.name] = item
        }
      }

      private fun createAngular2StructuralDirectiveSymbol(
        directive: Angular2Directive,
        inputs: HashMap<String, Angular2Symbol>,
        selector: Angular2DirectiveSelectorSymbol,
        location: PsiFile,
      ): Angular2Symbol =
        inputs[selector.name]?.let { Angular2StructuralDirectiveSymbol.create(directive, it, true, location) }
        ?: Angular2StructuralDirectiveSymbol.create(directive, selector, inputs.any { it.key.startsWith(selector.name) }, location)
    }

  }

}