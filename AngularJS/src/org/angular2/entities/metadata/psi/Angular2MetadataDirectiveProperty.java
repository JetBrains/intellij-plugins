// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.psi.PsiElement;
import org.angular2.codeInsight.Angular2LibrariesHacks;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2MetadataDirectiveProperty implements Angular2DirectiveProperty {

  private final JSRecordType.PropertySignature signature;
  private final PsiElement source;
  private final String name;

  Angular2MetadataDirectiveProperty(@Nullable JSRecordType.PropertySignature signature, @NotNull PsiElement source,
                                    @NotNull String name) {
    this.signature = signature;
    this.source = source;
    this.name = name;
  }

  @NotNull
  @Override
  public String getName() {
    return name;
  }

  @Nullable
  @Override
  public JSType getType() {
    return signature != null ? Angular2LibrariesHacks.hackQueryListTypeInNgForOf(signature.getJSType(), this)
                             : null;
  }

  @Override
  public boolean isVirtual() {
    return signature == null;
  }

  @NotNull
  @Override
  public PsiElement getSourceElement() {
    return source;
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
