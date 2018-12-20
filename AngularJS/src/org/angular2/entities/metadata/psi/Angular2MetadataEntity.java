// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.metadata.psi;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.psi.PsiElement;
import org.angular2.entities.Angular2Entity;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.metadata.stubs.Angular2MetadataEntityStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.notNull;

public class Angular2MetadataEntity<Stub extends Angular2MetadataEntityStub<?>> extends Angular2MetadataClassBase<Stub> implements
                                                                                                                        Angular2Entity {

  public Angular2MetadataEntity(@NotNull Stub element) {
    super(element);
  }

  @NotNull
  @Override
  public PsiElement getNavigableElement() {
    return getSourceElement();
  }

  @NotNull
  @Override
  public PsiElement getSourceElement() {
    return notNull(getTypeScriptClass(), this);
  }

  @Nullable
  @Override
  public ES6Decorator getDecorator() {
    return null;
  }


  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
