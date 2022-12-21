// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.JSTypeOwner;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2DirectiveAttribute;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.openapi.util.NullableLazyValue.lazyNullable;
import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static com.intellij.util.ObjectUtils.doIfNotNull;
import static com.intellij.util.ObjectUtils.notNull;

public class Angular2MetadataDirectiveAttribute implements Angular2DirectiveAttribute {

  private final Angular2MetadataDirectiveBase<?> myOwner;
  private final int myIndex;
  private final String myName;

  private final NullableLazyValue<JSParameter> myParameter;

  Angular2MetadataDirectiveAttribute(@NotNull Angular2MetadataDirectiveBase<?> owner,
                                     int index, @NotNull String name) {
    myOwner = owner;
    myIndex = index;
    myName = name;
    myParameter = lazyNullable(() -> myOwner.getConstructorParameter(myIndex));
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getType() {
    return doIfNotNull(myParameter.getValue(), JSTypeOwner::getJSType);
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return notNull(myParameter.getValue(), myOwner::getSourceElement);
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2MetadataDirectiveAttribute attribute = (Angular2MetadataDirectiveAttribute)o;
    return myIndex == attribute.myIndex && myOwner.equals(attribute.myOwner) && myName.equals(attribute.myName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myOwner, myIndex, myName);
  }

  @NotNull
  @Override
  public Pointer<Angular2MetadataDirectiveAttribute> createPointer() {
    var owner = createSmartPointer(myOwner);
    var index = myIndex;
    var name = myName;
    return () -> {
      var newOwner = owner.dereference();
      return newOwner != null ? new Angular2MetadataDirectiveAttribute(newOwner, index, name) : null;
    };
  }
}
