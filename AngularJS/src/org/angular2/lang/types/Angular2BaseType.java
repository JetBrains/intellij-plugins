// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.types;

import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeSubstitutionContext;
import com.intellij.lang.javascript.psi.JSTypeTextBuilder;
import com.intellij.lang.javascript.psi.JSTypeWithIncompleteSubstitution;
import com.intellij.lang.javascript.psi.types.JSCodeBasedType;
import com.intellij.lang.javascript.psi.types.JSSimpleTypeBaseImpl;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Angular2BaseType<T extends PsiElement> extends JSSimpleTypeBaseImpl
  implements JSCodeBasedType,                                        JSTypeWithIncompleteSubstitution {

  protected Angular2BaseType(@NotNull T source, Class<T> sourceClass) {
    this(JSTypeSourceFactory.createTypeSource(source, true), sourceClass);
  }

  protected Angular2BaseType(@NotNull JSTypeSource source, Class<T> sourceClass) {
    super(source);
    PsiElement sourceElement = source.getSourceElement();
    assert sourceElement != null;
    sourceClass.cast(sourceElement);
  }

  protected abstract @Nullable String getTypeOfText();

  protected abstract @Nullable JSType resolveType(@NotNull JSTypeSubstitutionContext context);

  @Override
  public @NotNull T getSourceElement() {
    //noinspection unchecked
    return (T)Objects.requireNonNull(super.getSourceElement());
  }

  @Override
  protected @Nullable JSType substituteImpl(@NotNull JSTypeSubstitutionContext context) {
    JSType type = resolveType(context);
    if (type != null) {
      context.add(type);
    }
    return type;
  }

  @Override
  public @NotNull JSType substituteCompletely() {
    return this.substitute().substitute();
  }

  @Override
  protected boolean isEquivalentToWithSameClass(@NotNull JSType type, @Nullable ProcessingContext context, boolean allowResolve) {
    return type.getClass() == this.getClass()
           && Objects.equals(type.getSourceElement(), getSourceElement());
  }

  @Override
  protected int resolvedHashCodeImpl() {
    return Objects.hash(getClass(), getSourceElement());
  }

  @Override
  protected void buildTypeTextImpl(@NotNull TypeTextFormat format, @NotNull JSTypeTextBuilder builder) {
    if (format == TypeTextFormat.SIMPLE) {
      builder.append("ngtypeof#" + getTypeOfText());
      return;
    }
    substitute().buildTypeText(format, builder);
  }
}