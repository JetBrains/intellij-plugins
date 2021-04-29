// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;


public interface GherkinKeywordProvider {
  Collection<String> getAllKeywords(String language);
  IElementType getTokenType(String language, String keyword);
  String getBaseKeyword(String language, String keyword);
  boolean isSpaceRequiredAfterKeyword(String language, String keyword);
  boolean isStepKeyword(String keyword);
  @NotNull
  GherkinKeywordTable getKeywordsTable(@Nullable String language);
}
