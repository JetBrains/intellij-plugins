// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import org.angular2.codeInsight.metadata.AngularDirectiveMetadata;
import org.angular2.codeInsight.metadata.AngularPipeMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2EntitiesProvider {

  @Nullable
  public static Angular2Component getComponent(TypeScriptClass cls) {
    Angular2Directive directive = AngularDirectiveMetadata.create(cls);
    return directive instanceof Angular2Component ? (Angular2Component)directive : null;
  }

  public static Angular2Component getComponent(JSImplicitElement element) {
    Angular2Directive directive = AngularDirectiveMetadata.create(element);
    return directive instanceof Angular2Component ? (Angular2Component)directive : null;
  }

  @NotNull
  public static Angular2Directive getDirective(JSImplicitElement directive) {
    return AngularDirectiveMetadata.create(directive);
  }

  @NotNull
  public static Angular2Pipe getPipe(@NotNull JSImplicitElement declaration) {
    return AngularPipeMetadata.create(declaration);
  }
}
