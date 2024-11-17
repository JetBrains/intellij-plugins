package org.jetbrains.qodana.staticAnalysis.sarif.textFormat;

import com.vladsch.flexmark.html2md.converter.CustomHtmlNodeRenderer;
import com.vladsch.flexmark.html2md.converter.HtmlConverterOptions;
import com.vladsch.flexmark.html2md.converter.HtmlMarkdownWriter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeConverterContext;
import com.vladsch.flexmark.html2md.converter.internal.HtmlConverterCoreNodeRenderer;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.misc.Utils;
import com.vladsch.flexmark.util.sequence.LineAppendable;
import com.vladsch.flexmark.util.sequence.RepeatedSequence;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

// Workaround for QD-3230
class PreElementHtmlRenderer implements CustomHtmlNodeRenderer<Element> {
  private final @NotNull HtmlConverterOptions myHtmlConverterOptions;

  PreElementHtmlRenderer(@NotNull DataHolder options) {
    myHtmlConverterOptions = new HtmlConverterOptions(options);
  }

  @Override
  public void render(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
    context.pushState(element);

    String text;
    boolean hadCode = false;
    String className = "";

    HtmlNodeConverterContext preText = context.getSubContext();
    preText.getMarkdown()
      .setOptions(out.getOptions() & ~(LineAppendable.F_COLLAPSE_WHITESPACE | LineAppendable.F_TRIM_TRAILING_WHITESPACE));
    preText.getMarkdown().openPreFormatted(false);

    Node next;
    while ((next = context.next()) != null) {
      if (next.nodeName().equalsIgnoreCase("code") || next.nodeName().equalsIgnoreCase("tt")) {
        hadCode = true;
        Element code = (Element)next;
        //text = code.toString();
        preText.inlineCode(() -> preText.renderChildren(code, false, null));
        if (className.isEmpty()) className = Utils.removePrefix(code.className(), "language-");
      }
      else if (next.nodeName().equalsIgnoreCase("br")) {
        preText.getMarkdown().append("\n");
      }
      else if (next.nodeName().equalsIgnoreCase("#text")) {
        preText.getMarkdown().append(((TextNode)next).getWholeText());
      }
      else {
        preText.renderChildren(next, false, null);
      }
    }

    preText.getMarkdown().closePreFormatted();
    text = preText.getMarkdown().toString(Integer.MAX_VALUE, 2);

    //int start = text.indexOf('>');
    //int end = text.lastIndexOf('<');
    //text = text.substring(start + 1, end);
    //text = Escaping.unescapeHtml(text);

    int backTickCount = HtmlConverterCoreNodeRenderer.getMaxRepeatedChars(text, '`', 3);
    CharSequence backTicks = RepeatedSequence.repeatOf("`", backTickCount);

    if (!myHtmlConverterOptions.skipFencedCode && (!className.isEmpty() || text.trim().isEmpty() || !hadCode)) {
      out.blankLine().append(backTicks);
      if (!className.isEmpty()) {
        out.append(className);
      }
      out.line();
      out.openPreFormatted(true);
      out.append(text.isEmpty() ? "\n" : text);
      out.closePreFormatted();
      out.line().append(backTicks).line();
      out.tailBlankLine();
    }
    else {
      // we indent the whole thing by 4 spaces
      out.blankLine();
      out.pushPrefix();
      out.addPrefix(myHtmlConverterOptions.codeIndent);
      out.openPreFormatted(true);
      out.append(text.isEmpty() ? "\n" : text);
      out.closePreFormatted();
      out.line();
      out.tailBlankLine();
      out.popPrefix();
    }

    context.popState(out);
  }
}
