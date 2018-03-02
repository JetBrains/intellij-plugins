// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartCompletionExtension {
  private static final ExtensionPointName<DartCompletionExtension> EP_NAME = ExtensionPointName.create("Dart.completionExtension");

  @NotNull
  static DartCompletionExtension[] getExtensions() {
    return EP_NAME.getExtensions();
  }

  /**
   * Implementations may use {@link DartServerCompletionContributor#createLookupElement(Project, CompletionSuggestion)} as a base for the
   * returned {@link LookupElementBuilder};
   * if <code>null</code> is returned then the Dart plugin defaults to {@link DartServerCompletionContributor#createLookupElement(Project, CompletionSuggestion)}
   */
  @Nullable
  public LookupElementBuilder createLookupElement(@NotNull final Project project, @NotNull final CompletionSuggestion suggestion) {
    return null;
  }
}
