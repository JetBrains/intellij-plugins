package org.intellij.plugins.markdown.ui.preview.lobo;

import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;

public final class LoboHtmlPanelProvider extends MarkdownHtmlPanelProvider {
  public static final ProviderInfo INFO = new ProviderInfo("Default", LoboHtmlPanelProvider.class.getName());

  @Override
  public MarkdownHtmlPanel createHtmlPanel() {
    return new LoboHtmlPanel();
  }

  @Override
  public boolean isAvailable() {
    return true;
  }
}
