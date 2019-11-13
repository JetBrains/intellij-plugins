// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptField;
import org.angular2.entities.Angular2Declaration;
import org.jetbrains.annotations.NotNull;

public abstract class Angular2IvyDeclaration extends Angular2IvyEntity implements Angular2Declaration {
  public Angular2IvyDeclaration(@NotNull TypeScriptField defField) {
    super(defField);
  }
}
