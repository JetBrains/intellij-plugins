// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.webtypes

import com.intellij.lang.javascript.documentation.JSMarkdownUtil
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.types.JSCompositeTypeImpl
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.util.CachedValueProvider.Result
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.ATTR_DIRECTIVE_PREFIX
import org.jetbrains.vuejs.codeInsight.BOOLEAN_TYPE
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.webtypes.json.Html
import org.jetbrains.vuejs.model.webtypes.json.Source
import org.jetbrains.vuejs.model.webtypes.json.WebTypes
import java.util.*
import java.util.regex.PatternSyntaxException

open class VueWebTypesEntitiesContainer(project: Project, packageJson: VirtualFile?,
                                        webTypes: WebTypes, owner: VueEntitiesContainer) : VueEntitiesContainer {

  override val source: PsiElement? = null
  override val parents: List<VueEntitiesContainer> = emptyList()
  final override val components: Map<String, VueComponent>
  final override val directives: Map<String, VueDirective>
  final override val filters: Map<String, VueFilter>

  override val mixins: List<VueMixin>
    get() = Collections.emptyList()

  init {
    assert(webTypes.framework == WebTypes.Framework.VUE)

    val typeProvider: (Any?) -> JSType? =
      if (webTypes.contributions?.html?.typesSyntax == Html.TypesSyntax.TYPESCRIPT)
        TypeScriptTypeProvider()::getType
      else
        { _: Any? -> null }
    val descriptionRenderer: (String) -> String? =
      when (webTypes.contributions?.html?.descriptionMarkup) {
        Html.DescriptionMarkup.HTML -> { doc -> doc }
        Html.DescriptionMarkup.MARKDOWN -> { doc -> JSMarkdownUtil.toHtml(doc, false) }
        else -> { doc -> "<p>" + StringUtil.escapeXmlEntities(doc).replace(EOL_PATTERN, "<br>") }
      }

    val packageJsonFile = packageJson?.let { PsiManager.getInstance(project).findFile(it) }
    val sourceSymbolResolver = packageJsonFile
      ?.let { WebTypesSourceSymbolResolver(it, webTypes.name ?: "unknown") }

    val support = object : WebTypesContext {
      override val project: Project = project
      override val pluginName: String? = webTypes.name
      override val parent: VueEntitiesContainer = owner
      override val packageJsonFile: PsiFile? = packageJsonFile

      override fun getType(webTypesType: Any?): JSType? = typeProvider(webTypesType)

      override fun resolveSourceSymbol(source: Source): Result<PsiElement?> =
        sourceSymbolResolver?.resolve(source) ?: Result.create(null as PsiElement?, PsiModificationTracker.NEVER_CHANGED)

      override fun renderDescription(description: String): String? = descriptionRenderer(description)
    }

    components = webTypes.contributions
                   ?.html
                   ?.tags
                   ?.asSequence()
                   ?.filter { it.name != null }
                   ?.flatMap { tag ->
                     tag.aliases
                       .asSequence()
                       .plus(tag.name!!)
                       .map { Pair(it, VueWebTypesComponent(tag, support)) }
                   }
                   ?.toMap()
                 ?: Collections.emptyMap()
    directives = webTypes.contributions
                   ?.html
                   ?.attributes
                   ?.asSequence()
                   ?.filter { it.name != null }
                   ?.flatMap { attribute ->
                     attribute.aliases
                       .asSequence()
                       .plus(attribute.name!!)
                       .filter { it.startsWith(ATTR_DIRECTIVE_PREFIX) }
                       .map { Pair(it.substring(2), VueWebTypesDirective(attribute, support)) }
                   }
                   ?.toMap()
                 ?: Collections.emptyMap()
    filters = webTypes.contributions
                ?.html
                ?.vueFilters
                ?.asSequence()
                ?.filter { it.name != null }
                ?.flatMap { filter ->
                  filter.aliases
                    .asSequence()
                    .plus(filter.name!!)
                    .map { Pair(it, VueWebTypesFilter(filter, support)) }
                }
                ?.toMap()
              ?: Collections.emptyMap()
  }

  private class TypeScriptTypeProvider {
    fun getType(type: Any?): JSType? {
      if (type is String) {
        return getType(type)
      }
      else if (type is List<*>) {
        val result = type.mapNotNull { t -> if (t is String) getType(t) else null }
        if (result.isNotEmpty()) {
          return JSCompositeTypeImpl.getCommonType(result, JSTypeSource.EXPLICITLY_DECLARED, false)
        }
      }
      return null
    }

    private fun getType(type: String): JSType? {
      // TODO support other types here
      if (type == "boolean" || type == "Boolean") {
        return BOOLEAN_TYPE
      }
      return null
    }
  }

  internal interface WebTypesContext {
    val pluginName: String?
    val project: Project
    val parent: VueEntitiesContainer
    val packageJsonFile: PsiFile?

    fun getType(webTypesType: Any?): JSType?
    fun resolveSourceSymbol(source: Source): Result<PsiElement?>
    fun renderDescription(description: String): String?

    fun createPattern(pattern: Any?): Regex? {
      return try {
        when (pattern) {
          is String -> Regex(pattern)
          is Map<*, *> -> {
            val regex = pattern["regex"] as? String ?: return null
            val ignoreCase = pattern["case-sensitive"] as? Boolean == false
            if (ignoreCase) {
              Regex(regex, RegexOption.IGNORE_CASE)
            }
            else {
              Regex(regex)
            }
          }
          else -> null
        }
      }
      catch (exception: PatternSyntaxException) {
        LOG.warn(exception)
        null
      }
    }
  }

  companion object {
    private val LOG = Logger.getInstance(VueWebTypesEntitiesContainer::class.java)
    private val EOL_PATTERN: Regex = Regex("\n|\r\n")
  }

}
