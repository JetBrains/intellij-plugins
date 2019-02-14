/*
 * Copyright (c) 2019, the Dart project authors.  Please see the AUTHORS file
 * for details. All rights reserved. Use of this source code is governed by a
 * BSD-style license that can be found in the LICENSE file.
 *
 * This file has been automatically generated.  Please do not edit it manually.
 * To regenerate the file, use the script "pkg/analysis_server/tool/spec/generate_files".
 */
package org.dartlang.analysis.server.protocol;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A result of requesting runtime completion.
 *
 * @coverage dart.server.generated.types
 */
public class GetCompletionDetailsResult {
  @NotNull public final String completion;
  @Nullable public final SourceChange change;

  public GetCompletionDetailsResult(String completion, SourceChange change) {
    this.completion = completion;
    this.change = change;
  }
}
