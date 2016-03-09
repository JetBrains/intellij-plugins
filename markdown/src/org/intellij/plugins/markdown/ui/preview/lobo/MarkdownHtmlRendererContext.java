package org.intellij.plugins.markdown.ui.preview.lobo;

import org.lobobrowser.html.AbstractHtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.w3c.HTMLCollection;

import java.awt.*;

class MarkdownHtmlRendererContext extends AbstractHtmlRendererContext {
  private final HtmlPanel myHtmlPanel;

  private final UserAgentContext myUserAgentContext;

  public MarkdownHtmlRendererContext(HtmlPanel panel) {
    myHtmlPanel = panel;
    myUserAgentContext = new MarkdownUserAgentContext();
  }

  @Override
  public HTMLCollection getFrames() {
    Object rootNode = myHtmlPanel.getRootNode();
    if (rootNode instanceof HTMLDocumentImpl) {
      return ((HTMLDocumentImpl)rootNode).getFrames();
    }
    else {
      return null;
    }
  }

  @Override
  public UserAgentContext getUserAgentContext() {
    return myUserAgentContext;
  }

  @Override
  public boolean isImageLoadingEnabled() {
    return false;
  }

  @Override
  public void setCursor(Cursor cursor) {

  }
}
