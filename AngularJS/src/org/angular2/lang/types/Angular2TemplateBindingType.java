// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.util.ProcessingContext;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TemplateBindingType extends Angular2BaseType<Angular2TemplateBindings> {

  private final String myKey;

  public Angular2TemplateBindingType(@NotNull Angular2TemplateBindings attribute, @NotNull String key) {
    super(attribute);
    myKey = key;
  }

  protected Angular2TemplateBindingType(@NotNull JSTypeSource source, @NotNull String key) {
    super(source);
    myKey = key;
  }

  @Override
  protected void validateSourceElement(@NotNull Angular2TemplateBindings element) {
    // validated by casting
  }

  @Override
  protected @Nullable String getTypeOfText() {
    return "*" + getSourceElement().getTemplateName() + "#" + myKey;
  }

  @Override
  protected @NotNull JSType copyWithNewSource(@NotNull JSTypeSource source) {
    return new Angular2TemplateBindingType(source, myKey);
  }

  @Override
  protected boolean isEquivalentToWithSameClass(@NotNull JSType type, @Nullable ProcessingContext context, boolean allowResolve) {
    return super.isEquivalentToWithSameClass(type, context, allowResolve)
           && myKey.equals(((Angular2TemplateBindingType)type).myKey);
  }

  @Override
  protected int resolvedHashCodeImpl() {
    return super.hashCode() * 31 + myKey.hashCode();
  }

  @Override
  protected @Nullable JSType resolveType() {
    return BindingsTypeResolver.get(getSourceElement()).resolveDirectiveInputType(myKey);
  }
}