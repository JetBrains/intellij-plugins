// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.stubs.StubIndexKey;
import org.angular2.entities.metadata.psi.Angular2MetadataFunction;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataFunctionIndex extends Angular2IndexBase<Angular2MetadataFunction> {

  public static final StubIndexKey<String, Angular2MetadataFunction> KEY =
    StubIndexKey.createIndexKey("angular2.metadata.function.index");

  @Override
  public @NotNull StubIndexKey<String, Angular2MetadataFunction> getKey() {
    return KEY;
  }
}
