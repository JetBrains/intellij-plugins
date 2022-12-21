// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.model.Pointer;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static com.intellij.openapi.util.NullableLazyValue.lazyNullable;
import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;
import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2MetadataDirectiveProperty implements Angular2DirectiveProperty {

  private final String myFieldName;
  private final String myName;
  private final String myKind;
  private final Angular2MetadataClassBase<?> myOwner;

  private final NullableLazyValue<JSRecordType.PropertySignature> mySignature;

  Angular2MetadataDirectiveProperty(@NotNull Angular2MetadataClassBase<?> owner,
                                    @NotNull String fieldName,
                                    @NotNull String name,
                                    @NotNull String kind) {
    this.myFieldName = fieldName;
    this.myName = name;
    this.myKind = kind;
    this.myOwner = owner;
    this.mySignature = lazyNullable(() -> myOwner.getPropertySignature(myFieldName));
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @NotNull
  @Override
  public String getKind() {
    return myKind;
  }

  @NotNull
  @Override
  public Pointer<Angular2MetadataDirectiveProperty> createPointer() {
    var owner = createSmartPointer(myOwner);
    var name = myName;
    var fieldName = myFieldName;
    var kind = myKind;
    return () -> {
      var newOwner = owner.dereference();
      return newOwner != null
             ? new Angular2MetadataDirectiveProperty(newOwner, fieldName, name, kind)
             : null;
    };
  }

  @Override
  public @Nullable JSType getRawJsType() {
    return doIfNotNull(mySignature.getValue(),
                       signature -> Angular2LibrariesHacks.hackQueryListTypeInNgForOf(signature.getJSType(), this));
  }

  @Override
  public boolean isVirtual() {
    return mySignature.getValue() == null;
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return Optional.ofNullable(mySignature.getValue())
      .map(sig -> sig.getMemberSource().getSingleElement())
      .orElse(myOwner.getSourceElement());
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2MetadataDirectiveProperty property = (Angular2MetadataDirectiveProperty)o;
    return myFieldName.equals(property.myFieldName) &&
           myName.equals(property.myName) &&
           myKind.equals(property.myKind) &&
           myOwner.equals(property.myOwner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myFieldName, myName, myKind, myOwner);
  }
}
