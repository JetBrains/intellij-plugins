// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.osgi.jps.build;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Reporter {
  void progress(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String message);

  void warning(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String message, @Nullable Throwable t, @Nullable String sourcePath, int lineNum);

  void error(@NotNull @Nls(capitalization = Nls.Capitalization.Sentence) String message, @Nullable Throwable t, @Nullable String sourcePath, int lineNum);

  boolean isDebugEnabled();

  void debug(@NotNull String message);

  String setReportSource(String source);
}
