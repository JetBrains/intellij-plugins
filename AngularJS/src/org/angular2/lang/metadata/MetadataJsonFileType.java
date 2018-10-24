// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.json.JsonFileType;
import org.jetbrains.annotations.NotNull;

public class MetadataJsonFileType extends JsonFileType {

  public static final MetadataJsonFileType INSTANCE = new MetadataJsonFileType();

  protected MetadataJsonFileType() {
    super(MetadataJsonLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public String getName() {
    return "Metadata JSON";
  }

  @NotNull
  @Override
  public String getDescription() {
    return "Provides additional metadata information for a compiled TypeScript file";
  }

  @NotNull
  @Override
  public String getDefaultExtension() {
    return "json";
  }
}
