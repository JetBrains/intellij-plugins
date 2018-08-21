// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.json.JsonFileType;
import com.intellij.lang.javascript.index.JSImplicitElementsIndexFileTypeProvider;
import com.intellij.openapi.fileTypes.FileType;

public class Angular2ImplicitlyElementsFileTypeProvider implements JSImplicitElementsIndexFileTypeProvider {
  @Override
  public FileType[] getFileTypes() {
    return new FileType[] {JsonFileType.INSTANCE};
  }
}
