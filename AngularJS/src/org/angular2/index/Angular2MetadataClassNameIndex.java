// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.stubs.StubIndexKey;
import org.angular2.entities.metadata.psi.Angular2MetadataClassBase;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataClassNameIndex extends Angular2IndexBase<Angular2MetadataClassBase> {

  public static final StubIndexKey<String, Angular2MetadataClassBase> KEY =
    StubIndexKey.createIndexKey("angular2.metadata.className.index");

  @Override
  public @NotNull StubIndexKey<String, Angular2MetadataClassBase> getKey() {
    return KEY;
  }
}
