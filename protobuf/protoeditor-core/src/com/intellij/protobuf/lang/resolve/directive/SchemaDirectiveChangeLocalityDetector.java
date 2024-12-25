/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.lang.resolve.directive;

import com.intellij.codeInsight.daemon.ChangeLocalityDetector;
import com.intellij.protobuf.lang.PbTextLanguage;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link ChangeLocalityDetector} for text format files that causes the entire file to be
 * re-highlighted when a comment is updated.
 *
 * <p>The default highlighting behavior for a comment is to re-highlight only that comment. Because
 * we use comments to denote schema location, changing a comment should result in re-highlighting
 * the entire file (just as changing any non-comment, non-whitespace PSI would).
 */
public class SchemaDirectiveChangeLocalityDetector implements ChangeLocalityDetector {
  @Override
  public @Nullable PsiElement getChangeHighlightingDirtyScopeFor(@NotNull PsiElement changedElement) {
    if (changedElement instanceof PsiComment && PbTextLanguage.INSTANCE.is(changedElement.getLanguage())) {
      return changedElement.getContainingFile();
    }

    return null;
  }
}
