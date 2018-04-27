package com.intellij.lang.javascript.linter.tslint.highlight;


import com.google.gson.JsonElement;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TsLintFixInfo {

  @Nullable
  public static TsLintFixInfo createTsLintFixInfo(@Nullable JsonElement element) {
    if (element == null) return null;

    try {
      if (element.isJsonArray()) {
        List<TsLintFixReplacements> replacements =
          ContainerUtil.mapNotNull(element.getAsJsonArray(), el -> JSLanguageServiceQueue.GSON.fromJson(el, TsLintFixReplacements.class));

        TsLintFixInfo info = new TsLintFixInfo();
        info.innerReplacements = replacements.toArray(new TsLintFixReplacements[0]);
        return info;
      }
      else {
        if (element.getAsJsonObject().has("innerReplacements")) {
          //tslint < 5 compatibility
          return JSLanguageServiceQueue.GSON.fromJson(element, TsLintFixInfo.class);
        }
        else {
          TsLintFixReplacements replacement = JSLanguageServiceQueue.GSON.fromJson(element, TsLintFixReplacements.class);

          TsLintFixInfo info = new TsLintFixInfo();
          info.innerReplacements = new TsLintFixReplacements[]{replacement};
          return info;
        }
      }
    }
    catch (Exception e) {
      TslintUtil.LOG.debug(e.getMessage(), e);
    }

    return null;
  }

  public static class TsLintFixReplacements {
    public int innerStart; //0-based for tslint 4
    public int innerLength;
    public String innerText;
  }

  @Nullable
  public String innerRuleName;
  public TsLintFixReplacements[] innerReplacements;
}
