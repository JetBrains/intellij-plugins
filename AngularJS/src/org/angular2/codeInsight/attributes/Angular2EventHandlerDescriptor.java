// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight.attributes;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Angular2EventHandlerDescriptor extends Angular2AttributeDescriptor {

  public Angular2EventHandlerDescriptor(@NotNull String attributeName,
                                        @NotNull String bindingName,
                                        @NotNull List<PsiElement> elements) {
    super(attributeName, bindingName, elements);
  }

}
