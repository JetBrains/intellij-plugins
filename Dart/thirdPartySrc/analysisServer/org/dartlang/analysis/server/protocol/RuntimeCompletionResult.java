/*
 * Copyright (c) 2018, the Dart project authors.  Please see the AUTHORS file
 * for details. All rights reserved. Use of this source code is governed by a
 * BSD-style license that can be found in the LICENSE file.
 *
 * This file has been automatically generated.  Please do not edit it manually.
 * To regenerate the file, use the script "pkg/analysis_server/tool/spec/generate_files".
 */
package org.dartlang.analysis.server.protocol;

import java.util.List;

/**
 * A result of requesting runtime completion.
 *
 * @coverage dart.server.generated.types
 */
public class RuntimeCompletionResult {
  public final List<CompletionSuggestion> suggestions;
  public final List<RuntimeCompletionExpression> expressions;

  public RuntimeCompletionResult(List<CompletionSuggestion> suggestions,
                                 List<RuntimeCompletionExpression> expressions) {
    this.suggestions = suggestions;
    this.expressions = expressions;
  }
}
