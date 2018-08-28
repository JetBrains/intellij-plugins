// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.html.psi.impl;

import com.intellij.openapi.util.Pair;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.lang.expr.psi.Angular2Binding;
import org.angular2.lang.expr.psi.Angular2Interpolation;
import org.angular2.lang.html.parser.Angular2HtmlElementTypes.Angular2ElementType;
import org.angular2.lang.html.psi.PropertyBindingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.util.Pair.pair;
import static org.angular2.lang.html.parser.Angular2HtmlParsing.normalizeAttributeName;

public abstract class Angular2HtmlPropertyBindingBase extends Angular2HtmlBoundAttributeImpl {

  public Angular2HtmlPropertyBindingBase(@NotNull Angular2ElementType type) {
    super(type);
  }

  @NotNull
  public final String getPropertyName() {
    return getNameAndType().first;
  }

  @NotNull
  public final PropertyBindingType getBindingType() {
    return getNameAndType().second;
  }

  protected abstract Pair<String, String> getDelimiters();

  protected abstract String getPrefix();

  protected abstract boolean supportsEvents();

  protected final Pair<String, PropertyBindingType> getNameAndType() {
    String name = normalizeAttributeName(getName());
    if (name.startsWith(getDelimiters().first) && name.endsWith(getDelimiters().second)) {
      name = name.substring(getDelimiters().first.length(), name.length() - getDelimiters().second.length());
      if (supportsEvents() && name.startsWith("@")) {
        return pair(name.substring(1), PropertyBindingType.ANIMATION);
      }
    }
    else if (name.startsWith(getPrefix())) {
      name = name.substring(getPrefix().length());
      if (supportsEvents() && name.startsWith("animate-")) {
        return pair(name.substring(8), PropertyBindingType.ANIMATION);
      }
    }
    else if (supportsEvents() && name.startsWith("@")) {
      return pair(name.substring(1), PropertyBindingType.ANIMATION);
    }
    if (name.startsWith("attr.")) {
      return pair(name.substring(5), PropertyBindingType.ATTRIBUTE);
    }
    if (name.startsWith("class.")) {
      return pair(name.substring(6), PropertyBindingType.CLASS);
    }
    if (name.startsWith("style.")) {
      return pair(name.substring(6), PropertyBindingType.STYLE);
    }
    return pair(name, PropertyBindingType.PROPERTY);
  }

  @Nullable
  public final Angular2Binding getBinding() {
    return ContainerUtil.getFirstItem(PsiTreeUtil.findChildrenOfType(this, Angular2Binding.class));
  }

  @NotNull
  public final Angular2Interpolation[] getInterpolations() {
    return PsiTreeUtil.findChildrenOfType(this, Angular2Interpolation.class)
      .toArray(new Angular2Interpolation[0]);
  }

}
