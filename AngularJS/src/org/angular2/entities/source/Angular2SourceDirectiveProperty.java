// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSRecordType;
import com.intellij.lang.javascript.psi.JSType;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Angular2SourceDirectiveProperty implements Angular2DirectiveProperty {

  private final JSRecordType.PropertySignature mySignature;
  private final String myName;

  public Angular2SourceDirectiveProperty(@NotNull JSRecordType.PropertySignature signature, @NotNull String bindingName) {
    mySignature = signature;
    myName = bindingName;
    assert mySignature.getMemberSource().getSingleElement() != null;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getType() {
    return mySignature.getJSType();
  }

  @Override
  public boolean isVirtual() {
    return false;
  }

  @Override
  public @NotNull JSElement getSourceElement() {
    return Objects.requireNonNull((JSElement)mySignature.getMemberSource().getSingleElement());
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
