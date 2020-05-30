// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities.ivy;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.psi.util.CachedValueProvider;
import org.angular2.entities.Angular2EntityUtils;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static com.intellij.util.ObjectUtils.notNull;

public class Angular2IvyPipe extends Angular2IvyDeclaration<Angular2IvySymbolDef.Pipe> implements Angular2Pipe {

  public Angular2IvyPipe(@NotNull Angular2IvySymbolDef.Pipe entityDef) {
    super(entityDef);
  }

  @Override
  public @NotNull String getName() {
    return notNull(myEntityDef.getName(), () -> Angular2Bundle.message("angular.description.unnamed"));
  }

  @Override
  public @NotNull Collection<? extends TypeScriptFunction> getTransformMethods() {
    return getCachedValue(() -> CachedValueProvider.Result.create(
      Angular2EntityUtils.getPipeTransformMethods(myClass), getClassModificationDependencies())
    );
  }
}
