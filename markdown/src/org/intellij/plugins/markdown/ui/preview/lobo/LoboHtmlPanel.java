package org.intellij.plugins.markdown.ui.preview.lobo;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.ui.JBColor;
import com.intellij.util.Range;
import org.intellij.plugins.markdown.ui.preview.MarkdownHtmlPanel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lobobrowser.html.HtmlRendererContext;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.dombl.NodeVisitor;
import org.lobobrowser.html.domimpl.DOMNodeImpl;
import org.lobobrowser.html.gui.HtmlBlockPanel;
import org.lobobrowser.html.gui.HtmlPanel;
import org.lobobrowser.html.renderer.RBlock;
import org.lobobrowser.html.renderer.RBlockViewport;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

final class LoboHtmlPanel extends MarkdownHtmlPanel {
  private static final int FOCUS_ELEMENT_DY = 100;

  @NotNull
  private final HtmlPanel myPanel;
  @NotNull
  private final MarkdownHtmlRendererContext myHtmlRendererContext;
  @NotNull
  private String myLastRenderedHtml = "";
  @Nullable
  private String myCssInlineText = null;


  public LoboHtmlPanel() {
    myPanel = new HtmlPanel() {
      @Override
      protected HtmlBlockPanel createHtmlBlockPanel(UserAgentContext ucontext, HtmlRendererContext rcontext) {
        return new ScrollPreservingHtmlBlockPanel(JBColor.WHITE, true, ucontext, rcontext, this);
      }
    };
    myHtmlRendererContext = new MarkdownHtmlRendererContext(myPanel);

    myPanel.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        adjustBrowserSize();
      }
    });
  }

  @Override
  @NotNull
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public void setCSS(@Nullable String inlineCss, @NotNull String... fileUris) {
    myCssInlineText = inlineCss;
    if (fileUris.length > 0 && fileUris[0] != null) {
      try {
        final URL url = URI.create(fileUris[0]).toURL();
        final String cssText = StreamUtil.readText(url.openStream(), CharsetToolkit.UTF8);
        myCssInlineText += "\n" + cssText;
      }
      catch (IOException ignore) {
      }
    }
  }

  @Override
  public void setHtml(@NotNull String html) {
    myLastRenderedHtml = html;
    final String htmlToRender = html.replace("<head>", "<head>" + getCssLines(myCssInlineText));
    myPanel.setHtml(htmlToRender, "file://a.html", myHtmlRendererContext);
  }

  @Override
  public void render() {
    setHtml(myLastRenderedHtml);
  }

  private void adjustBrowserSize() {
  }

  @Override
  public void scrollToMarkdownSrcOffset(final int offset) {
    ApplicationManager.getApplication().invokeAndWait(() -> {
      final DOMNodeImpl root = myPanel.getRootNode();
      final Ref<Pair<Node, Integer>> resultY = new Ref<Pair<Node, Integer>>();
      root.visit(new NodeVisitor() {
        @Override
        public void visit(Node node) {
          Node child = node.getFirstChild();
          while (child != null) {
            final Range<Integer> range = nodeToSrcRange(child);
            if (range != null && child instanceof DOMNodeImpl) {
              int currentDist = Math.min(Math.abs(range.getFrom() - offset),
                                         Math.abs(range.getTo() - 1 - offset));
              if (resultY.get() == null || resultY.get().getSecond() > currentDist) {
                resultY.set(Pair.create(child, currentDist));
              }
            }

            if (range == null || range.getTo() <= offset) {
              child = child.getNextSibling();
              continue;
            }

            if (range.getFrom() > offset) {
              break;
            }
            if (range.getTo() > offset) {
              visit(child);
              break;
            }
          }
        }
      });

      if (resultY.get() != null) {
        myPanel.scrollTo(resultY.get().getFirst());

        final RBlockViewport viewport = ((RBlock)myPanel.getBlockRenderable()).getRBlockViewport();
        final Rectangle renderBounds = myPanel.getBlockRenderable().getBounds();

        if (viewport.getY() + viewport.getHeight() - renderBounds.getHeight() > 0) {
          myPanel.scrollBy(0, -FOCUS_ELEMENT_DY);
        }

        myPanel.repaint();
      }
    }, ModalityState.NON_MODAL);
  }

  @Override
  public void dispose() {

  }
}
