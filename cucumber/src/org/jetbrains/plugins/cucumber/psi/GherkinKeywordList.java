// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public final class GherkinKeywordList {
  // i18n.json file contains list of keywords and some meta-information about the language. At the moment it's three attributes below.
  private static final Collection<String> GHERKIN_LANGUAGE_META_ATTRIBUTES = Arrays.asList("name", "native", "encoding");

  // maps custom language keyword to base (English) keyword
  private final Map<String, String> myKeyword2BaseNameTable = new HashMap<>();
  private final Set<String> mySpaceAfterKeywords = new HashSet<>();
  private final GherkinKeywordTable myKeywordsTable = new GherkinKeywordTable();

  public GherkinKeywordList() {
  }

  public GherkinKeywordList(@NotNull Map<String, Object> map) {
    for (Map.Entry<String, Object> e : map.entrySet()) {
      String key = e.getKey();
      if (!GHERKIN_LANGUAGE_META_ATTRIBUTES.contains(key)) {
        @SuppressWarnings("unchecked")
        List<String> values = (List<String>)e.getValue();
        String[] translatedKeywords = ArrayUtil.toStringArray(values);

        String keyword = capitalizeAndFixSpace(key);
        IElementType type = getTokenTypeByBaseKeyword(keyword);

        for (String translatedKeyword : translatedKeywords) {
          if (translatedKeyword.endsWith(" ")) {
            translatedKeyword = translatedKeyword.substring(0, translatedKeyword.length() - 1);
            mySpaceAfterKeywords.add(translatedKeyword);
          }
          myKeyword2BaseNameTable.put(translatedKeyword, keyword);
          myKeywordsTable.put(type, translatedKeyword);
        }
      }
    }
  }

  private static @NotNull String capitalizeAndFixSpace(@NotNull String s) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (i == 0) {
        c = Character.toUpperCase(c);
      }
      if (Character.isUpperCase(c) && i > 0) {
        result.append(' ');
      }
      result.append(c);
    }
    return result.toString();
  }

  public Collection<String> getAllKeywords() {
    return myKeyword2BaseNameTable.keySet();
  }

  public GherkinKeywordTable getKeywordsTable() {
    return myKeywordsTable;
  }

  public boolean isSpaceAfterKeyword(String keyword) {
    return mySpaceAfterKeywords.contains(keyword);
  }

  public IElementType getTokenType(String keyword) {
    return getTokenTypeByBaseKeyword(getBaseKeyword(keyword));
  }

  private static IElementType getTokenTypeByBaseKeyword(String baseKeyword) {
    return PlainGherkinKeywordProvider.DEFAULT_KEYWORDS.get(baseKeyword);
  }

  public String getBaseKeyword(String keyword) {
    return myKeyword2BaseNameTable.get(keyword);
  }
}
