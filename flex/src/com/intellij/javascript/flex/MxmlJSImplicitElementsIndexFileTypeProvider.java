// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.flex;

import com.intellij.lang.javascript.index.JSImplicitElementsIndexFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;

import java.util.Collections;
import java.util.List;

final class MxmlJSImplicitElementsIndexFileTypeProvider implements JSImplicitElementsIndexFileTypeProvider {
  @Override
  public List<FileType> getFileTypes() {
    return Collections.singletonList(FlexApplicationComponent.MXML);
  }
}
