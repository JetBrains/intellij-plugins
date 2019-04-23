// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.index;

import com.intellij.lang.typescript.library.TypeScriptIndexableFileNamesProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import static org.angular2.entities.metadata.Angular2MetadataFileType.METADATA_SUFFIX;

public class Angular2IndexableFileNamesProvider implements TypeScriptIndexableFileNamesProvider {
  @NotNull
  @Override
  public List<String> getIndexableExtensions() {
    return Collections.singletonList(METADATA_SUFFIX);
  }
}
