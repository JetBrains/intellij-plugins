// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.lang.metadata;

import com.intellij.lang.DependentLanguage;
import com.intellij.lang.Language;

public final class MetadataJsonLanguage extends Language implements DependentLanguage {

  public static final MetadataJsonLanguage INSTANCE = new MetadataJsonLanguage();

  private MetadataJsonLanguage() {
    super("Metadata JSON", "application/json");
  }
}
