// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2SelectorMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static org.angular2.codeInsight.tags.Angular2TagDescriptorsProvider.NG_TEMPLATE;
import static org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.isTemplateTag;
import static org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates;

public class Angular2ApplicableDirectivesProvider {

  private final NotNullLazyValue<List<Angular2Directive>> myDirectiveCandidates;
  private final List<Angular2Directive> myMatchedDirectives;

  public Angular2ApplicableDirectivesProvider(@NotNull XmlTag xmlTag) {
    this(xmlTag, false);
  }

  public Angular2ApplicableDirectivesProvider(@NotNull XmlTag xmlTag, boolean onlyMatchingTagName) {
    this(xmlTag.getProject(), xmlTag.getLocalName(), onlyMatchingTagName,
         Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag));
  }

  public Angular2ApplicableDirectivesProvider(@NotNull Angular2TemplateBindings bindings) {
    this(bindings.getProject(), NG_TEMPLATE, false,
         Angular2DirectiveSimpleSelector.createTemplateBindingsCssSelector(bindings));
  }

  private Angular2ApplicableDirectivesProvider(@NotNull Project project,
                                               @NotNull String tagName,
                                               boolean onlyMatchingTagName,
                                               @NotNull Angular2DirectiveSimpleSelector cssSelector) {
    Set<Angular2Directive> directiveCandidates = new HashSet<>(
      findElementDirectivesCandidates(project, tagName));
    if (!onlyMatchingTagName) {
      directiveCandidates.addAll(findElementDirectivesCandidates(project, ""));
    }

    Angular2SelectorMatcher<Angular2Directive> matcher = new Angular2SelectorMatcher<>();
    directiveCandidates.forEach(d -> matcher.addSelectables(d.getSelector().getSimpleSelectors(), d));
    myDirectiveCandidates = NotNullLazyValue.createValue(() -> new ArrayList<>(directiveCandidates));

    boolean isTemplateTag = isTemplateTag(tagName);
    Set<Angular2Directive> matchedDirectives = new HashSet<>();
    matcher.match(cssSelector, (selector, directive) -> {
      if (directive.getDirectiveKind().isRegular() || isTemplateTag) {
        matchedDirectives.add(directive);
      }
    });
    myMatchedDirectives = ContainerUtil.sorted(matchedDirectives,
                                               Comparator.comparing(Angular2Directive::getName));
  }

  public List<Angular2Directive> getCandidates() {
    return myDirectiveCandidates.getValue();
  }

  public List<Angular2Directive> getMatched() {
    return myMatchedDirectives;
  }
}
