// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import com.intellij.openapi.util.NlsSafe;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class OsgiBuildException extends Exception {
  private final String mySourcePath;

  public OsgiBuildException(@Nls(capitalization = Nls.Capitalization.Sentence) String message) {
    this(message, null, null);
  }

  public OsgiBuildException(@Nls(capitalization = Nls.Capitalization.Sentence) String message,
                            @Nullable Throwable cause,
                            @Nullable String sourcePath) {
    super(message, cause);
    mySourcePath = sourcePath;
  }

  public @NlsSafe @Nullable String getSourcePath() {
    return mySourcePath;
  }
}
