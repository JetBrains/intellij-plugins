package org.intellij.plugins.markdown.ui.preview.javafx;

import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;

public class JavaFxHtmlPanelProvider extends MarkdownHtmlPanelProvider {
  @NotNull
  @Override
  public MarkdownHtmlPanel createHtmlPanel() {
    try {
      return (MarkdownHtmlPanel)Class.forName("org.intellij.plugins.markdown.ui.preview.javafx.JavaFxHtmlPanel").newInstance();
    }
    catch (ClassNotFoundException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
    catch (InstantiationException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
    catch (IllegalAccessException e) {
      throw new IllegalStateException("Should not be called if unavailable", e);
    }
  }

  @Override
  public boolean isAvailable() {
    try {
      return Class.forName("javafx.scene.web.WebView") != null
             && Class.forName("org.intellij.plugins.markdown.ui.preview.javafx.JavaFxHtmlPanel") != null;
    }
    catch (ClassNotFoundException e) {
      return false;
    }
  }

  @NotNull
  @Override
  public ProviderInfo getProviderInfo() {
    return new ProviderInfo("JavaFX WebView", JavaFxHtmlPanelProvider.class.getName());
  }
}
