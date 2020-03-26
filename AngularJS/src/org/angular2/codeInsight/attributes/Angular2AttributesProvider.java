// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface Angular2AttributesProvider {

  ExtensionPointName<Angular2AttributesProvider> ANGULAR_ATTRIBUTES_PROVIDER_EP =
    ExtensionPointName.create("org.angular2.attributesProvider");

  void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                   @NotNull XmlTag tag,
                                   @NotNull String attributeName);

  @Nullable
  Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                            @NotNull String attributeName,
                                            @NotNull Angular2AttributeNameParser.AttributeInfo info);

  @NotNull
  Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor);

  interface CompletionResultsConsumer {

    Angular2DeclarationsScope getScope();

    void addDescriptors(@NotNull List<? extends Angular2AttributeDescriptor> descriptors);

    default void addDescriptor(@NotNull Angular2AttributeDescriptor descriptor) {
      addDescriptors(Collections.singletonList(descriptor));
    }

    void addAbbreviation(@NotNull List<String> lookupNames,
                         @NotNull AttributePriority priority,
                         @Nullable String hidePrefix,
                         @Nullable String suffix);

    default void addAbbreviation(@NotNull String lookupName,
                                 @NotNull AttributePriority priority,
                                 @Nullable String hidePrefix,
                                 @Nullable String suffix) {
      addAbbreviation(Collections.singletonList(lookupName), priority, hidePrefix, suffix);
    }
  }
}
