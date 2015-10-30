package org.intellij.plugins.markdown.ui.preview;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Range;
import org.intellij.markdown.html.HtmlGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Node;

import javax.swing.*;
import java.util.List;

public abstract class MarkdownHtmlPanel implements Disposable {
  @NotNull
  public abstract JComponent getComponent();

  public abstract void setHtml(@NotNull String html);

  public abstract void setCSS(@Nullable String inlineCss, @NotNull String... fileUris);

  public abstract void render();

  public abstract void scrollToMarkdownSrcOffset(int offset);

  @Nullable
  protected static Range<Integer> nodeToSrcRange(@NotNull Node node) {
    if (!node.hasAttributes()) {
      return null;
    }
    final Node attribute = node.getAttributes().getNamedItem(HtmlGenerator.Companion.getSRC_ATTRIBUTE_NAME());
    if (attribute == null) {
      return null;
    }
    final List<String> startEnd = StringUtil.split(attribute.getNodeValue(), "..");
    if (startEnd.size() != 2) {
      return null;
    }
    return new Range<Integer>(Integer.parseInt(startEnd.get(0)), Integer.parseInt(startEnd.get(1)));
  }

  @NotNull
  protected static String getCssLines(@Nullable String inlineCss, @NotNull String... fileUris) {
    StringBuilder result = new StringBuilder();

    for (String uri : fileUris) {
      if (uri == null) {
        continue;
      }
      result.append("<link rel=\"stylesheet\" href=\"").append(uri).append("\" />\n");
    }
    if (inlineCss != null) {
      result.append("<style>\n").append(inlineCss).append("\n</style>\n");
    }
    return result.toString();
  }
}
