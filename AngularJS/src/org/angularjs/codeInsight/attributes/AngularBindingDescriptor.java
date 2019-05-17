// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * @deprecated Kept for compatibility with NativeScript
 * To be removed in 2019.2
 */
@Deprecated
public class AngularBindingDescriptor extends Angular2AttributeDescriptor {
  public AngularBindingDescriptor(@NotNull PsiElement element,
                                  @NotNull String attributeName) {
    super(null, attributeName, AttributePriority.NORMAL, Collections.singletonList(element), true);
  }
}
