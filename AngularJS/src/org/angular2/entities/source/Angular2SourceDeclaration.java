// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Angular2SourceDeclaration extends Angular2SourceEntity implements Angular2Declaration {

  public Angular2SourceDeclaration(@NotNull ES6Decorator source) {
    super(source);
  }

  @Nullable
  @Override
  public Angular2Module getModule() {
    throw new UnsupportedOperationException();
  }

}
