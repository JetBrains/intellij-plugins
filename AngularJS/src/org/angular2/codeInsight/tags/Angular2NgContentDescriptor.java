// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.tags;

import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlAttributeDescriptor;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2NgContentDescriptor extends Angular2TagDescriptor {

  @NonNls public static final String ATTR_SELECT = "select";

  public Angular2NgContentDescriptor(@NotNull XmlTag tag) {
    super(tag);
  }

  @Override
  @NotNull
  public XmlAttributeDescriptor[] getAttributesDescriptors(@Nullable XmlTag context) {
    return context != null
           ? new XmlAttributeDescriptor[]{Angular2AttributeDescriptor.create(context, ATTR_SELECT)}
           : XmlAttributeDescriptor.EMPTY;
  }

  @Override
  public boolean allowContributions() {
    return false;
  }
}
