// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2SourceDirectiveVirtualProperty implements Angular2DirectiveProperty {

  private final TypeScriptClass myOwner;
  private final String myName;

  public Angular2SourceDirectiveVirtualProperty(@NotNull TypeScriptClass owner, @NotNull String bindingName) {
    myOwner = owner;
    myName = bindingName;
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @Nullable
  @Override
  public JSType getType() {
    return null;
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @NotNull
  @Override
  public JSElement getSourceElement() {
    return myOwner;
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }
}
