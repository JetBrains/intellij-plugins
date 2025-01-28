// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.ContainerUtil
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.codeInsight.template.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.lang.selector.Angular2SelectorMatcher
import org.angular2.web.ELEMENT_NG_TEMPLATE

/**
 * @see Angular2EntitiesProvider
 */
class Angular2ApplicableDirectivesProvider internal constructor(
  project: Project,
  file: PsiFile,
  tagName: String,
  onlyMatchingTagName: Boolean,
  cssSelector: Angular2DirectiveSimpleSelector,
  scope: Angular2DeclarationsScope?,
) {

  private val myDirectiveCandidates: NotNullLazyValue<List<Angular2Directive>>
  val matched: List<Angular2Directive>

  val candidates: List<Angular2Directive>
    get() = myDirectiveCandidates.value

  @JvmOverloads
  constructor(xmlTag: XmlTag, onlyMatchingTagName: Boolean = false, scope: Angular2DeclarationsScope? = null)
    : this(xmlTag.project, xmlTag.containingFile, xmlTag.localName, onlyMatchingTagName,
           Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag), scope)

  constructor(bindings: Angular2TemplateBindings, scope: Angular2DeclarationsScope? = null)
    : this(bindings.project, bindings.containingFile, ELEMENT_NG_TEMPLATE, false,
           Angular2DirectiveSimpleSelector.createTemplateBindingsCssSelector(bindings), scope)

  init {
    val directiveCandidates = HashSet<Angular2Directive>()
    val filter = if (scope != null) { it: Angular2Directive -> scope.contains(it) } else { _ -> true }
    findElementDirectivesCandidates(project, tagName)
      .filterTo(directiveCandidates, filter)
    if (!onlyMatchingTagName && tagName != "") {
      findElementDirectivesCandidates(project, "")
        .filterTo(directiveCandidates, filter)
    }

    val matcher = Angular2SelectorMatcher<Angular2Directive>()
    directiveCandidates.forEach { d -> matcher.addSelectables(d.selector.simpleSelectors, d) }
    myDirectiveCandidates = NotNullLazyValue.createValue { ArrayList(directiveCandidates) }

    val isTemplateTag = isTemplateTag(tagName)
    val matchedDirectives = HashSet<Angular2Directive>()
    JSTypeEvaluationLocationProvider.withTypeEvaluationLocation(file) {
      matcher.match(cssSelector) { _, directive ->
        if (directive != null && (isTemplateTag || directive.directiveKind.isRegular)) {
          matchedDirectives.add(directive)
        }
      }
    }
    matched = ContainerUtil.sorted(matchedDirectives, Comparator.comparing { it.getName() })
  }
}
