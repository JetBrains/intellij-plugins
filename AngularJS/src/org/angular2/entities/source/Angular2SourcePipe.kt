// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.source;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.Angular2Pipe;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Angular2SourcePipe extends Angular2SourceDeclaration implements Angular2Pipe {

  private final String myName;

  public Angular2SourcePipe(@NotNull ES6Decorator decorator, @NotNull JSImplicitElement implicitElement) {
    super(decorator, implicitElement);
    this.myName = implicitElement.getName();
  }

  @Override
  public @NotNull String getName() {
    return myName;
  }

  @Override
  public @NotNull Collection<? extends TypeScriptFunction> getTransformMethods() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      Angular2EntityUtils.getPipeTransformMethods(myClass), getClassModificationDependencies())
    );
  }
}
