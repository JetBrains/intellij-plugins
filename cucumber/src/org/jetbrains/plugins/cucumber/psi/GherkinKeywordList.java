package org.jetbrains.plugins.cucumber.psi;

import com.intellij.psi.tree.IElementType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.*;

/**
* @author yole, Roman.Chernyatchik
*/
public class GherkinKeywordList {
  // maps custom language keyword to base (English) keyword
  private final Map<String, String> myKeyword2BaseNameTable = new THashMap<>();
  private final Set<String> mySpaceAfterKeywords = new THashSet<>();
  private final GherkinKeywordTable myKeywordsTable = new GherkinKeywordTable();

  public GherkinKeywordList() {
  }

  public GherkinKeywordList(HashMap<Object, Object> hashMap) {
    for (Map.Entry e : hashMap.entrySet()) {
      String key = e.getKey().toString();
      if (!key.equals("name") && !key.equals("native") && !key.equals("encoding")) {
        List values = (List)e.getValue();
        String[] translatedKeywords = (String[])values.toArray(new String[0]);

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

  private static String capitalizeAndFixSpace(String s) {
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
