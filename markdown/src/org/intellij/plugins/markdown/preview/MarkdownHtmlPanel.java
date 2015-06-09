package org.intellij.plugins.markdown.preview;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lobobrowser.html.gui.HtmlPanel;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public final class MarkdownHtmlPanel {
  @NotNull
  private final HtmlPanel myPanel;
  @NotNull
  private final MarkdownHtmlRendererContext myHtmlRendererContext;
  @NotNull
  private String myLastRenderedHtml = "";
  @Nullable
  private String myCssFileUri = null;
  @Nullable
  private String myCssInlineText = null;


  public MarkdownHtmlPanel() {
    myPanel = new HtmlPanel();
    myHtmlRendererContext = new MarkdownHtmlRendererContext(myPanel);

    myPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        adjustBrowserSize();
      }
    });
  }

  @NotNull
  public JComponent getComponent() {
    return myPanel;
  }

  public void setCssFile(@Nullable String uri) {
    myCssFileUri = uri;
  }

  public void setCssInlineText(@Nullable String css) {
    myCssInlineText = css;
  }

  public void setHtml(@NotNull String html) {
    myLastRenderedHtml = html;
    final String htmlToRender = html.replace("<head>", "<head>" + getCssLines());
    myPanel.setHtml(htmlToRender, "file://a.html", myHtmlRendererContext);
  }

  public void refresh() {
    setHtml(myLastRenderedHtml);
  }

  @NotNull
  private String getCssLines() {
    StringBuilder result = new StringBuilder();
    if (myCssFileUri != null) {
      result.append("<link rel=\"stylesheet\" href=\"").append(myCssFileUri).append("\" />\n");
    }
    if (myCssInlineText != null) {
      result.append("<style>\n").append(myCssInlineText).append("\n</style>\n");
    }
    return result.toString();
  }

  private void adjustBrowserSize() {
  }
}
