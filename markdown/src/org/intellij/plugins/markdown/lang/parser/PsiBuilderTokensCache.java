/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.plugins.markdown.lang.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.ContainerUtil;
import org.intellij.markdown.parser.TokensCache;
import org.intellij.plugins.markdown.lang.MarkdownElementType;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class PsiBuilderTokensCache extends TokensCache {
  @NotNull
  private final List<TokenInfo> cachedTokens;
  @NotNull
  private final List<TokenInfo> filteredTokens;
  @NotNull
  private final PsiBuilder builder;

  public PsiBuilderTokensCache(@NotNull PsiBuilder builder) {
    this.builder = builder;
    cachedTokens = ContainerUtil.newArrayList();
    filteredTokens = ContainerUtil.newArrayList();

    cacheTokens();
    verify();
  }

  private static boolean isWhitespace(@NotNull IElementType type) {
    return type == MarkdownTokenTypes.WHITE_SPACE;
  }

  private void cacheTokens() {
    PsiBuilder.Marker startMarker = builder.mark();

    for (int i = 0; builder.rawLookup(i) != null; ++i) {
      cachedTokens.add(new TokenInfo(MarkdownElementType.markdownType(builder.rawLookup(i)),
              builder.rawTokenTypeStart(i),
              builder.rawTokenTypeStart(i + 1),
              i,
              -1));
    }

    int listIndex = 0;
    int filteredIndex = 0;
    while (builder.getTokenType() != null) {
      while (builder.getCurrentOffset() > cachedTokens.get(listIndex).getTokenStart()) {
        listIndex++;
      }
      assert builder.getCurrentOffset() == cachedTokens.get(listIndex).getTokenStart();
      cachedTokens.get(listIndex).setNormIndex(filteredIndex);

      if (!isWhitespace(builder.getTokenType())) {
        filteredTokens.add(cachedTokens.get(listIndex));
        filteredIndex++;
      }

      builder.advanceLexer();
    }

    startMarker.rollbackTo();
  }

  @NotNull
  @Override
  public List<TokenInfo> getCachedTokens() {
    return cachedTokens;
  }

  @NotNull
  @Override
  public List<TokenInfo> getFilteredTokens() {
    return filteredTokens;
  }

  @NotNull
  @Override
  public CharSequence getOriginalText() {
    return builder.getOriginalText();
  }
}
