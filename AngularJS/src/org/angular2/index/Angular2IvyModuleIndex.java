// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

public class Angular2IvyModuleIndex extends Angular2IndexBase<TypeScriptClass> {

  public static final StubIndexKey<String, TypeScriptClass> KEY =
    StubIndexKey.createIndexKey("angular2.ivy.module.index");

  @Override
  public @NotNull StubIndexKey<String, TypeScriptClass> getKey() {
    return KEY;
  }
}
