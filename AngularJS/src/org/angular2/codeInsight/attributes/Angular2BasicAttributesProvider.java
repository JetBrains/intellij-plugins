// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.impl.source.html.dtd.HtmlAttributeDescriptorImpl;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor.AttributePriority;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static org.angular2.codeInsight.attributes.Angular2AttributeDescriptorsProvider.EVENT_ATTR_PREFIX;

public class Angular2BasicAttributesProvider implements Angular2AttributesProvider {
  @Override
  public void contributeCompletionResults(@NotNull CompletionResultsConsumer completionResultsConsumer,
                                          @NotNull XmlTag tag,
                                          @NotNull String attributeName) {
    completionResultsConsumer.addDescriptors(ContainerUtil.mapNotNull(
      Objects.requireNonNull(tag.getDescriptor()).getAttributesDescriptors(tag),
      descr -> ObjectUtils.tryCast(descr, Angular2AttributeDescriptor.class)));

    ContainerUtil.mapNotNull(Angular2AttributeType.values(), Angular2AttributeType::getCanonicalPrefix).forEach(
      prefix -> {
        if (!attributeName.startsWith(prefix)) completionResultsConsumer.addAbbreviation(prefix, AttributePriority.NONE, null, null);
      }
    );

    completionResultsConsumer.addAbbreviation("#", AttributePriority.NONE, null, null);
  }

  @Override
  public @Nullable Angular2AttributeDescriptor getDescriptor(@NotNull XmlTag tag,
                                                             @NotNull String attributeName,
                                                             @NotNull Angular2AttributeNameParser.AttributeInfo info) {
    return Angular2AttributeDescriptor.create(tag, attributeName);
  }

  @Override
  public @NotNull Collection<String> getRelatedAttributes(@NotNull XmlAttributeDescriptor descriptor) {
    if (descriptor instanceof Angular2AttributeDescriptor) {
      Angular2AttributeNameParser.AttributeInfo info = ((Angular2AttributeDescriptor)descriptor).getInfo();
      if (info.type == Angular2AttributeType.BANANA_BOX_BINDING
          || info.type == Angular2AttributeType.REGULAR
          || (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
              && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.PROPERTY)
      ) {
        return Angular2AttributeNameVariantsBuilder.forTypes(
          info.name, true, true,
          Angular2AttributeType.REGULAR,
          Angular2AttributeType.PROPERTY_BINDING,
          Angular2AttributeType.BANANA_BOX_BINDING);
      }
      else if (info instanceof Angular2AttributeNameParser.PropertyBindingInfo
               && ((Angular2AttributeNameParser.PropertyBindingInfo)info).bindingType == PropertyBindingType.ATTRIBUTE) {
        String attrName = info.name;
        return Collections.singletonList(attrName);
      }
      return Angular2AttributeNameVariantsBuilder.forTypes(
        info.getFullName(), true, true,
        info.type);
    }
    else if (descriptor instanceof HtmlAttributeDescriptorImpl) {
      String attrName = descriptor.getName();
      if (!attrName.startsWith(EVENT_ATTR_PREFIX)) {
        return Collections.singletonList(attrName);
      }
    }
    return Collections.emptyList();
  }
}
