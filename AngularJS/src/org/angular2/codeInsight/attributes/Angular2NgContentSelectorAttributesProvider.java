// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2CodeInsightUtils;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2DirectiveSelector.SimpleSelectorWithPsi;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class Angular2NgContentSelectorAttributesProvider implements Angular2AttributesProvider {
  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    getAvailableNgContentAttrSelectorsStream(tag, completionResultsConsumer.getScope())
      .map(selector -> new NgContentSelectorBasedAttributeDescriptor(tag, selector))
      .forEach(completionResultsConsumer::addDescriptor);
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    String attrName = StringUtil.toLowerCase(attributeName);
    return getAvailableNgContentAttrSelectorsStream(tag, new Angular2DeclarationsScope(tag))
      .filter(selector -> StringUtil.toLowerCase(selector.getName()).equals(attrName))
      .map(selector -> new NgContentSelectorBasedAttributeDescriptor(tag, selector))
      .findAny()
      .orElse(null);
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    return Collections.emptyList();
  }

  private static StreamEx<Angular2DirectiveSelectorPsiElement> getAvailableNgContentAttrSelectorsStream(@NotNull XmlTag tag,
                                                                                                        @NotNull Angular2DeclarationsScope scope) {
    String tagName = StringUtil.toLowerCase(tag.getName());
    return Angular2CodeInsightUtils.getAvailableNgContentSelectorsStream(tag, scope)
      .filter(selector -> selector.getElement() == null
                          || StringUtil.toLowerCase(selector.getElement().getName()).equals(tagName))
      .flatMap(selector -> StreamEx.of(selector.getNotSelectors())
        .map(SimpleSelectorWithPsi::getAttributes)
        .append(selector.getAttributes()))
      .flatMap(Collection::stream);
  }

  private static class NgContentSelectorBasedAttributeDescriptor extends Angular2AttributeDescriptor {

    protected NgContentSelectorBasedAttributeDescriptor(@NotNull XmlTag xmlTag,
                                                        @NotNull Angular2DirectiveSelectorPsiElement selector) {
      super(xmlTag, selector.getName(), AttributePriority.HIGH, Collections.singleton(selector), true);
    }
  }
}
