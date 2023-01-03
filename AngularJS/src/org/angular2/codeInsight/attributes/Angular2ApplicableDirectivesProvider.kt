// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.containers.ContainerUtil
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates
import org.angular2.lang.expr.psi.Angular2TemplateBindings
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector
import org.angular2.lang.selector.Angular2SelectorMatcher
import org.angular2.web.Angular2WebSymbolsQueryConfigurator.Companion.ELEMENT_NG_TEMPLATE

/**
 * @see Angular2EntitiesProvider
 */
class Angular2ApplicableDirectivesProvider private constructor(project: Project,
                                                               tagName: String,
                                                               onlyMatchingTagName: Boolean,
                                                               cssSelector: Angular2DirectiveSimpleSelector) {

  private val myDirectiveCandidates: NotNullLazyValue<List<Angular2Directive>>
  val matched: List<Angular2Directive>

  val candidates: List<Angular2Directive>
    get() = myDirectiveCandidates.value

  @JvmOverloads
  constructor(xmlTag: XmlTag, onlyMatchingTagName: Boolean = false)
    : this(xmlTag.project, xmlTag.localName, onlyMatchingTagName,
           Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag))

  constructor(bindings: Angular2TemplateBindings)
    : this(bindings.project, ELEMENT_NG_TEMPLATE, false,
           Angular2DirectiveSimpleSelector.createTemplateBindingsCssSelector(bindings))

  init {
    val directiveCandidates = HashSet(findElementDirectivesCandidates(project, tagName))
    if (!onlyMatchingTagName) {
      directiveCandidates.addAll(findElementDirectivesCandidates(project, ""))
    }

    val matcher = Angular2SelectorMatcher<Angular2Directive>()
    directiveCandidates.forEach { d -> matcher.addSelectables(d.selector.simpleSelectors, d) }
    myDirectiveCandidates = NotNullLazyValue.createValue { ArrayList(directiveCandidates) }

    val isTemplateTag = isTemplateTag(tagName)
    val matchedDirectives = HashSet<Angular2Directive>()
    matcher.match(cssSelector) { _, directive ->
      if (directive != null && (directive.directiveKind.isRegular || isTemplateTag)) {
        matchedDirectives.add(directive)
      }
    }
    matched = ContainerUtil.sorted(matchedDirectives, Comparator.comparing { it.getName() })
  }
}
