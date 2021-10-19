// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.model.Pointer;
import org.angular2.entities.Angular2DirectiveProperty;
import org.angular2.entities.Angular2EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.intellij.refactoring.suggested.UtilsKt.createSmartPointer;

public class Angular2SourceDirectiveVirtualProperty implements Angular2DirectiveProperty {

  private final TypeScriptClass myOwner;
  private final String myName;
  private final String myKind;

  public Angular2SourceDirectiveVirtualProperty(@NotNull TypeScriptClass owner,
                                                @NotNull String bindingName,
                                                @NotNull String kind) {
    myOwner = owner;
    myName = bindingName;
    myKind = kind;
  }

  @NotNull
  @Override
  public String getKind() {
    return myKind;
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @Nullable JSType getRawJsType() {
    return null;
  }

  @Override
  public boolean isVirtual() {
    return true;
  }

  @Override
  public @NotNull JSElement getSourceElement() {
    return myOwner;
  }

  @Override
  public String toString() {
    return Angular2EntityUtils.toString(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Angular2SourceDirectiveVirtualProperty property = (Angular2SourceDirectiveVirtualProperty)o;
    return myOwner.equals(property.myOwner) && myName.equals(property.myName) && myKind.equals(property.myKind);
  }

  @Override
  public int hashCode() {
    return Objects.hash(myOwner, myName, myKind);
  }

  @NotNull
  @Override
  public Pointer<Angular2SourceDirectiveVirtualProperty> createPointer() {
    var name = myName;
    var kind = myKind;
    var owner = createSmartPointer(myOwner);
    return () -> {
      var newOwner = owner.getElement();
      return newOwner != null ? new Angular2SourceDirectiveVirtualProperty(newOwner, name, kind) : null;
    };
  }
}
