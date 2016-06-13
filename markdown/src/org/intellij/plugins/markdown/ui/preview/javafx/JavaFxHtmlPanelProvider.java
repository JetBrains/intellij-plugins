package org.intellij.plugins.markdown.ui.preview.javafx;

import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;

public class JavaFxHtmlPanelProvider extends MarkdownHtmlPanelProvider {

  @NotNull
  @Override
  public MarkdownHtmlPanel createHtmlPanel() {
    return new JavaFxHtmlPanel();
  }

  @NotNull
  @Override
  public AvailabilityInfo isAvailable() {
    try {
      if (Class.forName("javafx.scene.web.WebView", false, getClass().getClassLoader()) != null) {
        return AvailabilityInfo.AVAILABLE;
      }
    }
    catch (ClassNotFoundException ignored) {
    }

    return AvailabilityInfo.UNAVAILABLE;
  }

  @NotNull
  @Override
  public ProviderInfo getProviderInfo() {
    return new ProviderInfo("JavaFX WebView", JavaFxHtmlPanelProvider.class.getName());
  }

}
