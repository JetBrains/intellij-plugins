// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.codeInsight.Angular2Processor;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angular2.lang.selector.Angular2SelectorMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.angular2.entities.Angular2EntitiesProvider.findElementDirectivesCandidates;

public class Angular2ApplicableDirectivesProvider {

  private final NotNullLazyValue<List<Angular2Directive>> myDirectiveCandidates;
  private final List<Angular2Directive> myMatchedDirectives;

  public Angular2ApplicableDirectivesProvider(@NotNull XmlTag xmlTag) {
    this(xmlTag, false);
  }

  public Angular2ApplicableDirectivesProvider(@NotNull XmlTag xmlTag, boolean onlyMatchingTagName) {
    Set<Angular2Directive> directiveCandidates = new HashSet<>(
      findElementDirectivesCandidates(xmlTag.getProject(), xmlTag.getName()));
    if (!onlyMatchingTagName) {
      directiveCandidates.addAll(findElementDirectivesCandidates(xmlTag.getProject(), ""));
    }

    Angular2SelectorMatcher<Angular2Directive> matcher = new Angular2SelectorMatcher<>();
    directiveCandidates.forEach(d -> matcher.addSelectables(d.getSelector().getSimpleSelectors(), d));
    myDirectiveCandidates = NotNullLazyValue.createValue(() -> ContainerUtil.newArrayList(directiveCandidates));

    boolean isTemplateTag = Angular2Processor.isTemplateTag(xmlTag.getName());
    myMatchedDirectives = new ArrayList<>();
    Angular2DirectiveSimpleSelector tagInfo = Angular2DirectiveSimpleSelector.createElementCssSelector(xmlTag);
    matcher.match(tagInfo, (selector, directive) -> {
      if (!directive.isTemplate() || isTemplateTag) {
        myMatchedDirectives.add(directive);
      }
    });
  }

  public List<Angular2Directive> getCandidates() {
    return myDirectiveCandidates.getValue();
  }

  public List<Angular2Directive> getMatched() {
    return myMatchedDirectives;
  }
}
