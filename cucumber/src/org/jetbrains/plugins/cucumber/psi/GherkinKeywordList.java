package org.jetbrains.plugins.cucumber.psi;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
* @author yole, Roman.Chernyatchik
*/
public class GherkinKeywordList {
  // maps custom language keyword to base (English) keyword
  private Map<String, String> myKeyword2BaseNameTable = new THashMap<>();
  private Set<String> myKeywordsWithNoSpaceAfter = new THashSet<>();
  private GherkinKeywordTable myKeywordsTable = new GherkinKeywordTable();

  public GherkinKeywordList() {
  }

  public GherkinKeywordList(HashMap<Object, Object> hashMap) {
    Boolean forceSpaceAfterKeyword = null;

    for (Map.Entry e : hashMap.entrySet()) {
      String key = e.getKey().toString();
      String value = e.getValue().toString();

      if (key.equals("space_after_keyword")) {
        forceSpaceAfterKeyword = Boolean.valueOf(value);
      }
      else if (!key.equals("name") && !key.equals("native") && !key.equals("encoding")) {
        final String[] keywords = value.split("\\|");
        final String baseKeyword = StringUtil.toTitleCase(key.replace("_", " "));
        final IElementType type = getTokenTypeByBaseKeyword(baseKeyword);

        for (String keyword : keywords) {
          if (keyword.endsWith("<")) {
            keyword = keyword.substring(0, keyword.length()-1);
            myKeywordsWithNoSpaceAfter.add(keyword);
          }
          myKeyword2BaseNameTable.put(keyword, baseKeyword);
          myKeywordsTable.put(type, keyword);
        }
      }
      if (forceSpaceAfterKeyword != null) {
        if (forceSpaceAfterKeyword.booleanValue()) {
          myKeywordsWithNoSpaceAfter.clear();
        }
        else {
          myKeywordsWithNoSpaceAfter.addAll(myKeyword2BaseNameTable.keySet());
        }
      }
    }
  }


  public Collection<String> getAllKeywords() {
    return myKeyword2BaseNameTable.keySet();
  }

  public GherkinKeywordTable getKeywordsTable() {
    return myKeywordsTable;
  }

  public boolean isSpaceAfterKeyword(String keyword) {
    return !myKeywordsWithNoSpaceAfter.contains(keyword);
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
