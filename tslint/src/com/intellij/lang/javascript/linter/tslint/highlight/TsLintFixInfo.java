// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.linter.tslint.highlight;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.lang.javascript.service.JSLanguageServiceQueue;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TsLintFixInfo {

  public static @Nullable TsLintFixInfo createTsLintFixInfo(@Nullable JsonElement element) {
    if (element == null) return null;

    try {
      if (element.isJsonArray()) {
        Gson gson = JSLanguageServiceQueue.SharedGson.GSON;
        List<TsLintFixReplacements> replacements =
          ContainerUtil.mapNotNull(element.getAsJsonArray(), el -> {
            return gson.fromJson(el, TsLintFixReplacements.class);
          });

        TsLintFixInfo info = new TsLintFixInfo();
        info.innerReplacements = replacements.toArray(new TsLintFixReplacements[0]);
        return info;
      }
      else {
        if (element.getAsJsonObject().has("innerReplacements")) {
          //tslint < 5 compatibility
          return JSLanguageServiceQueue.SharedGson.GSON.fromJson(element, TsLintFixInfo.class);
        }
        else {
          TsLintFixReplacements replacement = JSLanguageServiceQueue.SharedGson.GSON.fromJson(element, TsLintFixReplacements.class);

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

  public TsLintFixReplacements[] innerReplacements;
}
