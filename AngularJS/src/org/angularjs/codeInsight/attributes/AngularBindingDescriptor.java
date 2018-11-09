// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angularjs.codeInsight.attributes;

import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.attributes.Angular2AttributeDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

/**
 * @deprecated Kept for compatibility with NativeScript
 */
@Deprecated
public class AngularBindingDescriptor extends Angular2AttributeDescriptor {
  public AngularBindingDescriptor(@NotNull PsiElement element,
                                  @NotNull String attributeName) {
    super(attributeName, null, Collections.singletonList(element));
  }
}
