// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.stubs.StubIndexKey;
import org.angular2.entities.metadata.psi.Angular2MetadataNodeModule;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataNodeModuleIndex extends Angular2IndexBase<Angular2MetadataNodeModule> {

  public static final StubIndexKey<String, Angular2MetadataNodeModule> KEY = StubIndexKey.createIndexKey("angular2.metadata.node.index");

  @Override
  public @NotNull StubIndexKey<String, Angular2MetadataNodeModule> getKey() {
    return KEY;
  }
}

