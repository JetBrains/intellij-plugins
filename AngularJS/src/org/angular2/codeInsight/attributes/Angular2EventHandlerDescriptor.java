// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.html.parser.Angular2AttributeNameParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class Angular2EventHandlerDescriptor extends Angular2AttributeDescriptor {

  public Angular2EventHandlerDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull List<PsiElement> elements) {
    super(attributeName, isInTemplateTag, elements);
  }

  public Angular2EventHandlerDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull AttributePriority priority,
                                        @NotNull List<PsiElement> elements) {
    super(attributeName, isInTemplateTag, priority, elements);
  }

  public Angular2EventHandlerDescriptor(@NotNull String attributeName,
                                        boolean isInTemplateTag,
                                        @NotNull AttributePriority priority,
                                        @NotNull Collection<Angular2Directive> sourceDirectives,
                                        @NotNull List<PsiElement> elements) {
    super(attributeName, isInTemplateTag, priority, sourceDirectives, elements);
  }

  public Angular2EventHandlerDescriptor(@NotNull String attributeName,
                                        @NotNull Angular2AttributeNameParser.AttributeInfo info,
                                        @Nullable Collection<Angular2Directive> sourceDirectives,
                                        @NotNull List<PsiElement> elements) {
    super(attributeName, info, sourceDirectives, elements);
  }
}
