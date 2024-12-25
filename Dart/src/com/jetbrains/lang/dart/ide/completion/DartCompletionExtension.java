// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartCompletionExtension {
  private static final ExtensionPointName<DartCompletionExtension> EP_NAME = ExtensionPointName.create("Dart.completionExtension");

  static @NotNull List<DartCompletionExtension> getExtensions() {
    return EP_NAME.getExtensionList();
  }

  /**
   * Implementations may use {@link DartServerCompletionContributor#createLookupElement(Project, CompletionSuggestion)} as a base for the
   * returned {@link LookupElementBuilder};
   * if <code>null</code> is returned then the Dart plugin defaults to
   * {@link DartServerCompletionContributor#createLookupElement(Project, CompletionSuggestion, String, DartServerCompletionContributor.SuggestionDetailsInsertHandlerBase)}
   */
  public @Nullable LookupElementBuilder createLookupElement(final @NotNull Project project, final @NotNull CompletionSuggestion suggestion) {
    return null;
  }
}
