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

  @Attribute("UseGrayscaleRendering")
  private boolean myUseGrayscaleRendering = false;

  public MarkdownPreviewSettings() {
  }

  public MarkdownPreviewSettings(@NotNull SplitFileEditor.SplitEditorLayout splitEditorLayout,
                                 @NotNull MarkdownHtmlPanelProvider.ProviderInfo htmlPanelProviderInfo,
                                 boolean useGrayscaleRendering) {
    mySplitEditorLayout = splitEditorLayout;
    myHtmlPanelProviderInfo = htmlPanelProviderInfo;
    myUseGrayscaleRendering = useGrayscaleRendering;
  }

  @NotNull
  public SplitFileEditor.SplitEditorLayout getSplitEditorLayout() {
    return mySplitEditorLayout;
  }

  @NotNull
  public MarkdownHtmlPanelProvider.ProviderInfo getHtmlPanelProviderInfo() {
    return myHtmlPanelProviderInfo;
  }

  public boolean isUseGrayscaleRendering() {
    return myUseGrayscaleRendering;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MarkdownPreviewSettings settings = (MarkdownPreviewSettings)o;

    if (myUseGrayscaleRendering != settings.myUseGrayscaleRendering) return false;
    if (mySplitEditorLayout != settings.mySplitEditorLayout) return false;
    if (!myHtmlPanelProviderInfo.equals(settings.myHtmlPanelProviderInfo)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = mySplitEditorLayout.hashCode();
    result = 31 * result + myHtmlPanelProviderInfo.hashCode();
    result = 31 * result + (myUseGrayscaleRendering ? 1 : 0);
    return result;
  }

  public interface Holder {
    void setMarkdownPreviewSettings(@NotNull MarkdownPreviewSettings settings);

    @NotNull
    MarkdownPreviewSettings getMarkdownPreviewSettings();
  }
}
