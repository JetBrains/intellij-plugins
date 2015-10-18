package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.JBColor;
import com.intellij.util.Range;
import org.intellij.markdown.html.HtmlGenerator;
import org.intellij.plugins.markdown.ui.preview.lobo.ScrollPreservingHtmlBlockPanel;
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
import java.util.List;

public final class MarkdownHtmlPanel {
  private static final int FOCUS_ELEMENT_DY = 100;

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

  public void scrollToSrcOffset(final int offset) {
    ApplicationManager.getApplication().invokeAndWait(new Runnable() {
      @Override
      public void run() {
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
      }
    }, ModalityState.NON_MODAL);

  }

  @Nullable
  private static Range<Integer> nodeToSrcRange(@NotNull Node node) {
    if (!node.hasAttributes()) {
      return null;
    }
    final Node attribute = node.getAttributes().getNamedItem(HtmlGenerator.SRC_ATTRIBUTE_NAME);
    if (attribute == null) {
      return null;
    }
    final List<String> startEnd = StringUtil.split(attribute.getNodeValue(), "..");
    if (startEnd.size() != 2) {
      return null;
    }
    return new Range<Integer>(Integer.parseInt(startEnd.get(0)), Integer.parseInt(startEnd.get(1)));
  }
}
