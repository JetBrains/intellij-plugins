// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.psi.impl.source.html.dtd.HtmlAttributeDescriptorImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.ObjectUtils.notNull;
import static com.intellij.util.containers.ContainerUtil.*;
import static java.util.Arrays.asList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority.NONE;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.EVENT_ATTR_PREFIX;
import static org.angular2.lang.html.parser.Angular2AttributeType.PROPERTY_BINDING;

public class Angular2BoundHtmlAttributesProvider implements Angular2AttributesProvider {

  private static final String CANONICAL_PREFIX_BASE = PROPERTY_BINDING.getCanonicalPrefix();

  @NonNls private static final String BASE_PREFIX = "attr.";
  private static final String SHORT_PREFIX = "[" + BASE_PREFIX;
  private static final String CANONICAL_PREFIX = CANONICAL_PREFIX_BASE + BASE_PREFIX;

  private static final List<String> PREFIXES = immutableList(SHORT_PREFIX, CANONICAL_PREFIX, BASE_PREFIX);

  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    String prefix = find(PREFIXES, attributeName::startsWith);
    if (prefix != null) {
      boolean isCanonical = CANONICAL_PREFIX.equals(prefix);
      completionResultsConsumer.addDescriptors(mapNotNull(
        Angular2AttributeDescriptorsProvider.getDefaultAttributeDescriptors(tag), descr -> {
          if (!descr.getName().startsWith(EVENT_ATTR_PREFIX)) {
            return new Angular2BoundHtmlAttributeDescriptor(tag, descr, isCanonical);
          }
          return null;
        }));
    }
    else if (attributeName.startsWith(CANONICAL_PREFIX_BASE)) {
      completionResultsConsumer.addAbbreviation(CANONICAL_PREFIX, NONE, CANONICAL_PREFIX_BASE, null);
    }
    else {
      completionResultsConsumer.addAbbreviation(newArrayList(SHORT_PREFIX, BASE_PREFIX), NONE, null, "]");
    }
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    if (info.type == PROPERTY_BINDING
        && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.ATTRIBUTE) {
      String name = info.name;
      XmlAttributeDescriptor descriptor = find(Angular2AttributeDescriptorsProvider.getDefaultAttributeDescriptors(tag),
                                               attr -> name.equalsIgnoreCase(attr.getName()));
      return descriptor != null ? new Angular2BoundHtmlAttributeDescriptor(tag, descriptor, info.isCanonical) : null;
    }
    return null;
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    if (descriptor instanceof Angular2AttributeDescriptor) {
      Angular2AttributeNameParser.AttributeInfo info = ((Angular2AttributeDescriptor)descriptor).getInfo();
      if (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
          && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.ATTRIBUTE) {
        String attrName = info.name;
        return Angular2AttributeNameVariantsBuilder.forTypes(
          BASE_PREFIX + attrName, true, true,
          PROPERTY_BINDING);
      }
    }
    else if (descriptor instanceof HtmlAttributeDescriptorImpl) {
      String attrName = descriptor.getName();
      if (!attrName.startsWith(EVENT_ATTR_PREFIX)) {
        return Angular2AttributeNameVariantsBuilder.forTypes(
          BASE_PREFIX + attrName, true, true,
          PROPERTY_BINDING);
      }
    }
    return Collections.emptyList();
  }

  private static class Angular2BoundHtmlAttributeDescriptor extends Angular2AttributeDescriptor {

    protected Angular2BoundHtmlAttributeDescriptor(@NotNull XmlTag xmlTag,
                                                   @NotNull XmlAttributeDescriptor originalDescriptor,
                                                   boolean canonical) {
      super(xmlTag,
            notNull(PROPERTY_BINDING.buildName(BASE_PREFIX + originalDescriptor.getName(), canonical),
                    () -> PROPERTY_BINDING.buildName(BASE_PREFIX + originalDescriptor.getName())),
            originalDescriptor.getDeclarations(),
            true);
    }

    @Override
    public XmlAttributeDescriptor cloneWithName(String attributeName) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected LookupElementInfo buildElementInfo(@NotNull PrefixMatcher prefixMatcher) {
      return new LookupElementInfo(getName(), asList(pair(SHORT_PREFIX, "]"), pair(CANONICAL_PREFIX, "")), null);
    }

    @Override
    protected boolean shouldInsertHandlerRemoveLeftover() {
      return true;
    }
  }
}
