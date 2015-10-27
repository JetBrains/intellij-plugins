package org.intellij.plugins.markdown.settings;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Tag;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.preview.lobo.LoboHtmlPanelProvider;
import org.intellij.plugins.markdown.ui.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;

public final class MarkdownPreviewSettings {
  public static final MarkdownPreviewSettings DEFAULT = new MarkdownPreviewSettings();

  @Attribute("DefaultSplitLayout")
  @NotNull
  private SplitFileEditor.SplitEditorLayout mySplitEditorLayout = SplitFileEditor.SplitEditorLayout.SPLIT;

  @Tag("HtmlPanelProviderInfo")
  @Property(surroundWithTag = false)
  @NotNull
  private MarkdownHtmlPanelProvider.ProviderInfo myHtmlPanelProviderInfo = LoboHtmlPanelProvider.INFO;

  public MarkdownPreviewSettings() {
  }

  public MarkdownPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
                                 @NotNull MarkdownHtmlPanelProvider.ProviderInfo htmlPanelProviderInfo) {
    mySplitEditorLayout = splitEditorLayout;
    myHtmlPanelProviderInfo = htmlPanelProviderInfo;
  }

  @NotNull
  public SplitFileEditor.SplitEditorLayout getSplitEditorLayout() {
    return mySplitEditorLayout;
  }

  @NotNull
  public MarkdownHtmlPanelProvider.ProviderInfo getHtmlPanelProviderInfo() {
    return myHtmlPanelProviderInfo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MarkdownPreviewSettings settings = (MarkdownPreviewSettings)o;

    if (mySplitEditorLayout != settings.mySplitEditorLayout) return false;
    if (!myHtmlPanelProviderInfo.equals(settings.myHtmlPanelProviderInfo)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySplitEditorLayout.hashCode();
    result = 31 * result + myHtmlPanelProviderInfo.hashCode();
    return result;
  }

  public interface Holder {
    void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings);

    @NotNull
    MarkdownPreviewSettings getMarkdownPreviewSettings();
  }
}
