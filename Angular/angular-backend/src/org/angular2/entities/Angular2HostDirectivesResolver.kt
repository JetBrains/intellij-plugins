// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.lang.javascript.psi.JSObjectLiteralExpression
import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import com.intellij.util.asSafely
import org.angular2.entities.source.Angular2SourceHostDirectiveWithMappings
import org.angular2.entities.source.Angular2SourceHostDirectiveWithoutMappings
import org.angular2.entities.source.Angular2SourceSymbolCollectorBase

class Angular2HostDirectivesResolver(private val directive: Angular2DirectiveWithHostDirectives) {

  val hostDirectives: Collection<Angular2HostDirective> get() = resolveHostDirectives().hostDirectives

  val exportAs: Map<String, Angular2DirectiveExportAs> get() = resolveHostDirectives().exportAs

  val hostDirectivesFullyResolved: Boolean get() = resolveHostDirectives().hostDirectivesFullyResolved

  private fun resolveHostDirectives(): CollectedResults =
    CachedValuesManager.getManager(directive.sourceElement.project).getCachedValue(directive as UserDataHolder) {
      JSTypeEvaluationLocationProvider.assertLocationIsSet()
      CachedValueProvider.Result.create(collectAll(), PsiModificationTracker.MODIFICATION_COUNT)
    }

  private fun collectAll(): CollectedResults {
    val hostDirectives = SmartList<Angular2HostDirective>()
    val exportAs = mutableMapOf<String, Angular2DirectiveExportAs>()
    val visited = mutableSetOf<Angular2Directive>()
    var isFullyResolved = true
    fun visit(directive: Angular2Directive) {
      if (visited.add(directive)) {
        val resolvedDirectives = directive.directHostDirectivesSet
        isFullyResolved = isFullyResolved && resolvedDirectives.isFullyResolved
        exportAs.putAll(directive.directExportAs)
        resolvedDirectives.symbols.forEach { hostDirective ->
          hostDirectives.add(hostDirective)
          hostDirective.directive?.let { visit(it) }
        }
        visited.remove(directive)
      }
    }
    visit(this.directive)
    return CollectedResults(hostDirectives, isFullyResolved, exportAs)
  }

  private val Angular2Directive.directHostDirectivesSet
    get() = asSafely<Angular2DirectiveWithHostDirectives>()?.directHostDirectivesSet
            ?: Angular2ResolvedSymbolsSet.createResult(emptySet<Angular2HostDirective>(), true, emptySet<Any>()).value

  private val Angular2Directive.directExportAs
    get() = asSafely<Angular2DirectiveWithHostDirectives>()?.directExportAs ?: exportAs

  private data class CollectedResults(
    val hostDirectives: Collection<Angular2HostDirective>,
    val hostDirectivesFullyResolved: Boolean,
    val exportAs: Map<String, Angular2DirectiveExportAs>,
  )

  interface Angular2DirectiveWithHostDirectives : Angular2Directive {
    val directExportAs: Map<String, Angular2DirectiveExportAs>
    val directHostDirectivesSet: Angular2ResolvedSymbolsSet<Angular2HostDirective>
  }

  class HostDirectivesCollector(source: PsiElement)
    : Angular2SourceSymbolCollectorBase<Angular2Directive, Angular2ResolvedSymbolsSet<Angular2HostDirective>>(
    Angular2Directive::class.java, source) {

    private val result = mutableSetOf<Angular2HostDirective>()

    override fun createResult(isFullyResolved: Boolean, dependencies: Set<PsiElement>)
      : CachedValueProvider.Result<Angular2ResolvedSymbolsSet<Angular2HostDirective>> =
      Angular2ResolvedSymbolsSet.createResult(result, isFullyResolved, dependencies)

    override fun processAnyElement(node: PsiElement) {
      if (node is JSObjectLiteralExpression)
        result.add(Angular2SourceHostDirectiveWithMappings(node))
      else
        super.processAnyElement(node)
    }

    override fun processAcceptableEntity(entity: Angular2Directive) {
      result.add(Angular2SourceHostDirectiveWithoutMappings(entity))
    }
  }

}