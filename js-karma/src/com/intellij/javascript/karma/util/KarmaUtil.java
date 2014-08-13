package com.intellij.javascript.karma.util;

import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;

/**
 * @author Sergey Simonchik
 */
public class KarmaUtil {

  private static final String[] FILE_NAME_SUFFIXES = new String[] {".conf.js", "-conf.js", ".config.js", "-config.js"};

  private KarmaUtil() {
  }

  public static void selectAndFocusIfNotDisposed(@NotNull RunnerLayoutUi ui,
                                                 @NotNull Content content,
                                                 boolean requestFocus,
                                                 boolean forced) {
    if (!ui.isDisposed()) {
      ui.selectAndFocus(content, requestFocus, forced);
    }
  }

  public static boolean isKarmaConfigFile(@NotNull String name) {
    for (String suffix : FILE_NAME_SUFFIXES) {
      if (name.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }
}
