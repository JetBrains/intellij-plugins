package org.intellij.plugins.markdown.ui.preview.lobo;

import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.domimpl.DOMNodeImpl;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.renderer.FrameContext;
import org.lobobrowser.html.renderer.RBlock;
import org.lobobrowser.html.renderer.RBlockViewport;

import java.awt.*;

class ScrollPreservingHtmlBlockPanel extends HtmlBlockPanel {
  public ScrollPreservingHtmlBlockPanel(Color background,
                                        boolean opaque,
                                        UserAgentContext pcontext,
                                        HtmlRendererContext rcontext,
                                        FrameContext frameContext) {
    super(background, opaque, pcontext, rcontext, frameContext);
  }

  @Override
  public void setRootNode(DOMNodeImpl node) {
    if (node != null) {
      int oldX = 32768;
      int oldY = 32768;
      if (rblock != null) {
        final RBlockViewport viewport = rblock.getRBlockViewport();
        oldX = viewport.getX();
        oldY = viewport.getY();
      }
      final RBlock block = new RBlock(node, 0, this.ucontext, this.rcontext,
                                      this.frameContext, this);
      block.setDefaultMarginInsets(this.defaultMarginInsets);
      // block.setDefaultPaddingInsets(this.defaultPaddingInsets);
      block.setDefaultOverflowX(this.defaultOverflowX);
      block.setDefaultOverflowY(this.defaultOverflowY);

      block.getRBlockViewport().setX(oldX);
      block.getRBlockViewport().setY(oldY);

      node.setUINode(block);
      this.rblock = block;
    }
    else {
      this.rblock = null;
    }
    this.invalidate();
    this.validateAll();
    this.repaint();
  }
}
