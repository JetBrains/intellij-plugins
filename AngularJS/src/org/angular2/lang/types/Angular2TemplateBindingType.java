// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.util.ProcessingContext;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TemplateBindingType extends Angular2BaseType<Angular2TemplateBindings> {

  private final String myKey;

  public Angular2TemplateBindingType(@NotNull Angular2TemplateBindings attribute, @NotNull String key) {
    super(attribute, Angular2TemplateBindings.class);
    myKey = key;
  }

  protected Angular2TemplateBindingType(@NotNull JSTypeSource source, @NotNull String key) {
    super(source, Angular2TemplateBindings.class);
    myKey = key;
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
  protected int hashCodeImpl() {
    return super.hashCodeImpl() * 31 + myKey.hashCode();
  }

  @Override
  protected @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context) {
    return BindingsTypeResolver.get(getSourceElement()).resolveDirectiveInputType(myKey);
  }
}