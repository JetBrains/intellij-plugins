package com.intellij.lang.javascript.linter.tslint.highlight;


import com.google.gson.JsonElement;
import com.intellij.lang.javascript.linter.tslint.config.TsLintConfiguration;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;

public class TsLintFixInfo {

  @Nullable
  public static TsLintFixInfo createTsLintFixInfo(@Nullable JsonElement element) {
    if (element == null) return null;

    try {
      return JSLanguageServiceQueue.GSON.fromJson(element, TsLintFixInfo.class);
    }
    catch (Exception e) {
      LOG.debug(e.getMessage(), e);
    }

    return null;
  }

  private static final Logger LOG = Logger.getInstance(TsLintConfiguration.LOG_CATEGORY);

  public static class TsLintFixReplacements {
    public int innerStart; //0-based for tslint 4
    public int innerLength;
    public String innerText;
  }

  public String innerRuleName;
  public TsLintFixReplacements[] innerReplacements;
}
