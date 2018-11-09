// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.psi.stubs.StubIndexKey;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.jetbrains.annotations.NotNull;

public class Angular2MetadataEntityClassNameIndex extends Angular2IndexBase<Angular2MetadataEntity> {

  public static final StubIndexKey<String, Angular2MetadataEntity> KEY =
    StubIndexKey.createIndexKey("angular2.metadata.entityClassName.index");

  @NotNull
  @Override
  public StubIndexKey<String, Angular2MetadataEntity> getKey() {
    return KEY;
  }
}
