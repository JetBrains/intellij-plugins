// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.css;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributesProvider;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.find;
import static com.intellij.util.containers.ContainerUtil.newArrayList;
import static java.util.Collections.unmodifiableList;
import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority.NONE;
import static org.angular2.lang.html.parser.Angular2AttributeType.PROPERTY_BINDING;

public class Angular2CssBoundAttributeProvider implements Angular2AttributesProvider {

  private static final String CANONICAL_PREFIX_BASE = PROPERTY_BINDING.getCanonicalPrefix();

  @NonNls private static final String BASE_PREFIX = "class.";
  public static final String SHORT_PREFIX = "[" + BASE_PREFIX;
  private static final String CANONICAL_PREFIX = CANONICAL_PREFIX_BASE + BASE_PREFIX;

  private static final List<String> PREFIXES = unmodifiableList(newArrayList(SHORT_PREFIX, CANONICAL_PREFIX, BASE_PREFIX));

  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    if (find(PREFIXES, attributeName::startsWith) == null) {
      if (attributeName.startsWith(CANONICAL_PREFIX_BASE)) {
        completionResultsConsumer.addAbbreviation(CANONICAL_PREFIX, NONE, CANONICAL_PREFIX_BASE, null);
      }
      else {
        completionResultsConsumer.addAbbreviation(newArrayList(SHORT_PREFIX, BASE_PREFIX), NONE, null, "]");
      }
    }
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    return null;
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    return Collections.emptyList();
  }
}
