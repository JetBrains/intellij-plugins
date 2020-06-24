// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.codeInsight.completion.PrefixMatcher;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.AttributeInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.intellij.openapi.util.Pair.pair;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority.NONE;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.I18N_ATTR;
import static org.angular2.lang.html.parser.Angular2AttributeType.I18N;
import static org.angular2.lang.html.parser.Angular2AttributeType.REGULAR;

public class Angular2I18nAttributesProvider implements Angular2AttributesProvider {

  @NonNls private static final String PREFIX = "i18n-";

  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    if (attributeName.startsWith(PREFIX)) {
      completionResultsConsumer.addDescriptors(mapNotNull(
        tag.getAttributes(), attr -> {
          AttributeInfo info = Angular2AttributeNameParser.parse(attr.getName(), tag);
          if (isI18nCandidate(info)) {
            return new Angular2I18nAttributeDescriptor(tag, attr.getName(), attr);
          }
          return null;
        }));
    }
    else {
      Set<String> candidates = new HashSet<>();
      Set<String> provided = new HashSet<>();
      for (XmlAttribute attribute : tag.getAttributes()) {
        AttributeInfo info = Angular2AttributeNameParser.parse(attribute.getName(), tag);
        if (isI18nCandidate(info)
            && !info.name.equals(attributeName + CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED)) {
          candidates.add(attribute.getName());
        }
        else if (info.type == I18N) {
          provided.add(info.name);
        }
      }
      candidates.removeAll(provided);
      if (!candidates.isEmpty()) {
        completionResultsConsumer.addAbbreviation(PREFIX, NONE, null, null);
      }
    }
  }

  public static boolean isI18nCandidate(@NotNull AttributeInfo info) {
    return info.type == REGULAR && !info.name.equals(I18N_ATTR);
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull AttributeInfo info) {
    if (info.type == I18N) {
      return new Angular2I18nAttributeDescriptor(tag, info.name, tag.getAttribute(info.name));
    }
    return null;
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    return emptyList();
  }

  private static class Angular2I18nAttributeDescriptor extends Angular2AttributeDescriptor {

    protected Angular2I18nAttributeDescriptor(@NotNull XmlTag xmlTag,
                                              @NotNull String originalAttributeName,
                                              @Nullable XmlAttribute originalAttribute) {
      super(xmlTag,
            I18N.buildName(originalAttributeName),
            Optional.ofNullable(originalAttribute)
              .map(XmlAttribute::getDescriptor)
              .map(XmlAttributeDescriptor::getDeclarations)
              .map(collection -> collection.isEmpty() ? null : collection)
              .orElseGet(() -> originalAttribute != null ? singletonList(originalAttribute) : emptyList()),
            true);
    }

    @Override
    protected LookupElementInfo buildElementInfo(@NotNull PrefixMatcher prefixMatcher) {
      return new LookupElementInfo(getName(), singletonList(pair(PREFIX, "")), null);
    }

    @Override
    public XmlAttributeDescriptor cloneWithName(String attributeName) {
      throw new UnsupportedOperationException();
    }

    @Override
    protected boolean shouldInsertHandlerRemoveLeftover() {
      return false;
    }
  }
}
