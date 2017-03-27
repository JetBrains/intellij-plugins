package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.ide.BrowserUtil;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

class SafeOpener {
  static void openLink(@NotNull String link) {
    if (!BrowserUtil.isAbsoluteURL(link)) {
      try {
        link = new URI("http", link, null).toURL().toString();
      }
      catch (Exception ignore) {
      }
    }

    BrowserUtil.browse(link);
  }
}
