// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.JsonLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.Nullable;

public class MetadataJsonLanguage extends JsonLanguage {

  public static final MetadataJsonLanguage INSTANCE = new MetadataJsonLanguage();

  protected MetadataJsonLanguage() {
    super("Metadata JSON", "application/json");
  }

  @Nullable
  @Override
  public LanguageFileType getAssociatedFileType() {
    return MetadataJsonFileType.INSTANCE;
  }


}
