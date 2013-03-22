package org.jetbrains.plugins.cucumber.java.psi;

import com.google.common.base.Splitter;
import com.intellij.util.containers.HashMap;
import org.jetbrains.plugins.cucumber.psi.GherkinElementType;
import org.jetbrains.plugins.cucumber.psi.GherkinKeywordTable;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import java.util.Map;

public class GherkinI18nJsonToKeywordTable {
  private static final Map<String, GherkinElementType> keyToTokenTypes = new HashMap<String, GherkinElementType>();

  static{
    keyToTokenTypes.put("background", GherkinTokenTypes.BACKGROUND_KEYWORD);
    keyToTokenTypes.put("examples", GherkinTokenTypes.EXAMPLES_KEYWORD);
    keyToTokenTypes.put("feature", GherkinTokenTypes.FEATURE_KEYWORD);
    keyToTokenTypes.put("scenario", GherkinTokenTypes.SCENARIO_KEYWORD);
    keyToTokenTypes.put("scenario_outline", GherkinTokenTypes.SCENARIO_OUTLINE_KEYWORD);
    keyToTokenTypes.put("given", GherkinTokenTypes.STEP_KEYWORD);
    keyToTokenTypes.put("when", GherkinTokenTypes.STEP_KEYWORD);
    keyToTokenTypes.put("then", GherkinTokenTypes.STEP_KEYWORD);
    keyToTokenTypes.put("and", GherkinTokenTypes.STEP_KEYWORD);
    keyToTokenTypes.put("but", GherkinTokenTypes.STEP_KEYWORD);
  }

  public GherkinKeywordTable convert(Map<String, String> raw) {
    GherkinKeywordTable table = new GherkinKeywordTable();
    populateTableFrom(table, raw);
    return table;
  }

  private void populateTableFrom(GherkinKeywordTable table, Map<String, String> raw) {
    for (Map.Entry<String, String> entry : raw.entrySet()) {
      if (mappingExistsFor(entry.getKey())) {
        addKeywordsToTable(table, entry);
      }
    }
  }

  private boolean mappingExistsFor(String key) {
    return keyToTokenTypes.containsKey(key);
  }

  private void addKeywordsToTable(GherkinKeywordTable table, Map.Entry<String, String> entry) {
    for (String keyword : Splitter.on('|').split(entry.getValue())) {
      GherkinElementType type = keyToTokenTypes.get(entry.getKey());
      if (!table.tableContainsKeyword(type, keyword)) {
        table.put(type, keyword);
      }
    }
  }
}