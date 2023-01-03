// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2Interpolation;
import org.angular2.lang.html.parser.Angular2AttributeNameParser.PropertyBindingInfo;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Angular2HtmlPropertyBindingBase extends Angular2HtmlBoundAttributeImpl {

  public Angular2HtmlPropertyBindingBase(@NotNull Angular2ElementType type) {
    super(type);
  }

  public final @NotNull String getPropertyName() {
    return getAttributeInfo().name;
  }

  public final @NotNull PropertyBindingType getBindingType() {
    return ((PropertyBindingInfo)getAttributeInfo()).bindingType;
  }

  public final @Nullable Angular2Binding getBinding() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Binding.class));
  }

  public final @NotNull Angular2Interpolation @NotNull [] getInterpolations() {
    return PsiTreeUtil.findChildrenOfType(this, Angular2Interpolation.class)
      .toArray(new Angular2Interpolation[0]);
  }
}
