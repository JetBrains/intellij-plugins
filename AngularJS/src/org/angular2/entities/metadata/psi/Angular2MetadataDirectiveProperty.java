// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.openapi.util.NotNullComputable;
import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

import static com.intellij.util.ObjectUtils.doIfNotNull;

public class Angular2MetadataDirectiveProperty implements Angular2DirectiveProperty {

  private final Supplier<JSRecordType.PropertySignature> mySignatureSupplier;
  private final NotNullComputable<? extends PsiElement> mySourceSupplier;
  private final String myName;

  Angular2MetadataDirectiveProperty(@NotNull Supplier<JSRecordType.PropertySignature> signatureSupplier,
                                    @NotNull NotNullComputable<? extends PsiElement> sourceSupplier,
                                    @NotNull String name) {
    this.mySignatureSupplier = signatureSupplier;
    this.mySourceSupplier = sourceSupplier;
    this.myName = name;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getType() {
    return doIfNotNull(mySignatureSupplier.get(),
                       signature -> Angular2LibrariesHacks.hackQueryListTypeInNgForOf(signature.getJSType(), this));
  }

  @Override
  public boolean isVirtual() {
    return mySignatureSupplier.get() == null;
  }

  @Override
  public @NotNull PsiElement getSourceElement() {
    return Optional.ofNullable(mySignatureSupplier.get())
      .map(sig -> sig.getMemberSource().getSingleElement())
      .orElse(mySourceSupplier.compute());
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
