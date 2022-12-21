// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.stubs.StubIndexKey;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataModuleIndex extends Angular2IndexBase<Angular2MetadataModule> {

  public static final StubIndexKey<String, Angular2MetadataModule> KEY =
    StubIndexKey.createIndexKey("angular2.metadata.module.index");

  @Override
  public @NotNull StubIndexKey<String, Angular2MetadataModule> getKey() {
    return KEY;
  }
}
