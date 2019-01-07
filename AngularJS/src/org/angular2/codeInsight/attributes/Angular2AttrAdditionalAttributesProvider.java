// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.xml.XmlTag;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.lang.html.parser.Angular2AttributeType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static com.intellij.util.containers.ContainerUtil.newArrayList;

public class Angular2AttrAdditionalAttributesProvider implements Angular2AdditionalAttributesProvider {

  public static final String PREFIX = "attr.";

  private static final List<String> PREFIXES = newArrayList("[" + PREFIX, "bind-" + PREFIX);
  private static final List<String> PREFIXES_COMPLETION = newArrayList("[" + PREFIX, "bind-" + PREFIX, PREFIX);

  @Override
  public List<String> getPrefixes(boolean forCompletion) {
    return forCompletion ? PREFIXES_COMPLETION : PREFIXES;
  }

  @Override
  public List<Angular2AttributeDescriptor> getDescriptors(XmlTag tag, boolean forCompletion) {
    return ContainerUtil.mapNotNull(Angular2AttributeDescriptorsProvider.getDefaultAttributeDescriptors(tag), descr -> {
      if (!descr.getName().startsWith("on")) {
        return new Angular2BoundHtmlAttributeDescriptor(descr);
      }
      return null;
    });
  }

  private static class Angular2BoundHtmlAttributeDescriptor extends Angular2AttributeDescriptor {

    protected Angular2BoundHtmlAttributeDescriptor(@NotNull XmlAttributeDescriptor originalDescriptor) {
      super(Angular2AttributeType.PROPERTY_BINDING.buildName(PREFIX + originalDescriptor.getName()), false,
            Collections.singletonList(originalDescriptor.getDeclaration()));
    }
  }
}
