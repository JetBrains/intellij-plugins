package org.intellij.plugins.markdown.ui.preview.lobo;

import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.jetbrains.annotations.NotNull;

public final class LoboHtmlPanelProvider extends MarkdownHtmlPanelProvider {
  public static final ProviderInfo INFO = new ProviderInfo("Default", LoboHtmlPanelProvider.class.getName());

  @NotNull
  @Override
  public MarkdownHtmlPanel createHtmlPanel() {
    return new LoboHtmlPanel();
  }

  @Override
  public boolean isAvailable() {
    return true;
  }

  @NotNull
  @Override
  public ProviderInfo getProviderInfo() {
    return INFO;
  }
}
