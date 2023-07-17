// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities

import com.intellij.openapi.util.UserDataHolder
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.util.SmartList
import org.angular2.entities.ivy.Angular2IvyDirective
import org.angular2.entities.source.Angular2SourceDirective

class Angular2HostDirectivesResolver(private val directive: Angular2Directive) {

  val hostDirectives: Collection<Angular2HostDirective> get() = resolveHostDirectives().hostDirectives

  val exportAs: Map<String, Angular2DirectiveExportAs> get() = resolveHostDirectives().exportAs

  val hostDirectivesFullyResolved: Boolean get() = resolveHostDirectives().hostDirectivesFullyResolved

  private fun resolveHostDirectives(): CollectedResults =
    CachedValuesManager.getManager(directive.sourceElement.project).getCachedValue(directive as UserDataHolder) {
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
    get() =
      when (this) {
        is Angular2IvyDirective -> directHostDirectivesSet
        is Angular2SourceDirective -> directHostDirectivesSet
        else -> Angular2ResolvedSymbolsSet.createResult(emptySet<Angular2HostDirective>(), true, emptySet<Any>()).value
      }

  private val Angular2Directive.directExportAs
    get() =
      when (this) {
        is Angular2IvyDirective -> directExportAs
        is Angular2SourceDirective -> directExportAs
        else -> exportAs
      }

  private data class CollectedResults(
    val hostDirectives: Collection<Angular2HostDirective>,
    val hostDirectivesFullyResolved: Boolean,
    val exportAs: Map<String, Angular2DirectiveExportAs>,
  )

}