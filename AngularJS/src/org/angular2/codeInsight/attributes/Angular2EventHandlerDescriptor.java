// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.xml.XmlTag;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2EventHandlerDescriptor extends Angular2AttributeDescriptor {

  public Angular2EventHandlerDescriptor(@NotNull XmlTag tag,
                                        @NotNull String attributeName,
                                        @NotNull List<?> sources,
                                        boolean implied) {
    super(tag, attributeName, sources, implied);
  }

  public Angular2EventHandlerDescriptor(@Nullable XmlTag tag,
                                        @NotNull String attributeName,
                                        @NotNull AttributePriority priority,
                                        @NotNull List<?> sources,
                                        boolean implied) {
    super(tag, attributeName, priority, sources, implied);
  }

  public Angular2EventHandlerDescriptor(@Nullable XmlTag tag,
                                        @NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @NotNull List<?> sources,
                                        boolean implied) {
    super(tag, attributeName, info, sources, implied);
  }
}
