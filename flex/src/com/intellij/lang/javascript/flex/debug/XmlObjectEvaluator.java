package com.intellij.lang.javascript.flex.debug;

import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;
import com.intellij.xdebugger.frame.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class XmlObjectEvaluator {

  private final FakeCompositeNode myRootNode;

  static final Font MONOSPACED_FONT;

  static {
    final Font font = UIUtil.getToolTipFont();
    MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, font == null ? 12 : font.getSize());
  }

  public XmlObjectEvaluator(final @NotNull XValue value, final @NotNull XFullValueEvaluator.XFullValueEvaluationCallback callback) {
    myRootNode = new FakeCompositeNode(value, callback);
  }

  public void startEvaluation() {
    myRootNode.myValue.computeChildren(myRootNode);
  }


  private static class FakeCompositeNode implements XCompositeNode {
    private final XValue myValue;
    private final XFullValueEvaluator.XFullValueEvaluationCallback myCallback;
    private final FakeCompositeNode myParent;
    private final List<FakeCompositeNode> myChildren = new ArrayList<>();

    private static final String XML_MARKER = " class='XML@";
    private static final String XMLLIST_MARKER = " class='XMLList'";

    FakeCompositeNode(final @NotNull XValue value, final @NotNull XFullValueEvaluator.XFullValueEvaluationCallback callback) {
      myValue = value;
      myCallback = callback;
      myParent = null;
    }

    private FakeCompositeNode(final @NotNull XValue value, final @NotNull FakeCompositeNode parent) {
      myValue = value;
      myCallback = null;
      myParent = parent;
    }

    @Override
    public void setAlreadySorted(boolean alreadySorted) {
    }

    @Override
    public void addChildren(@NotNull XValueChildrenList children, boolean last) {
      if (getRootNode().myCallback.isObsolete()) {
        return;
      }

      for (int i = 0; i < children.size(); i++) {
        final XValue value = children.getValue(i);
        final FakeCompositeNode node = new FakeCompositeNode(value, this);
        myChildren.add(node);
        value.computeChildren(node);
      }

      getRootNode().myCallback.evaluated(getRootNode().toPresentableSting(0), MONOSPACED_FONT);
    }

    private FakeCompositeNode getRootNode() {
      FakeCompositeNode node = this;
      while (node.myCallback == null) {
        node = node.myParent;
      }
      return node;
    }

    private String toPresentableSting(final int level) {
      final StringBuilder buffer = new StringBuilder();

      final String rawText = ((FlexValue)myValue).getResult();

      if (rawText.contains(XMLLIST_MARKER)) {
        for (FakeCompositeNode child : myChildren) {
          buffer.append(child.toPresentableSting(level));
        }
        return buffer.toString();
      }

      final int xmlMarkerIndex = rawText.indexOf(XML_MARKER);
      final int xmlInfoStartIndex = xmlMarkerIndex < 0 ? -1 : rawText.indexOf(" ", xmlMarkerIndex + XML_MARKER.length());
      final int xmlInfoEndIndex = xmlMarkerIndex < 0 ? -1 : rawText.lastIndexOf("'");

      if (xmlInfoStartIndex > 0 && xmlInfoEndIndex > xmlInfoStartIndex) {
        final String xmlInfo = rawText.substring(xmlInfoStartIndex, xmlInfoEndIndex);

        final boolean isElement = xmlInfo.startsWith(FlexValue.ELEMENT_MARKER + "<") && xmlInfo.endsWith(">");
        final boolean isEmptyElement = isElement && xmlInfo.endsWith("/>");
        final boolean isText = !isElement && xmlInfo.startsWith(FlexValue.TEXT_MARKER);

        if (isText || isElement) {
          if (isText) {
            appendIndent(buffer, level);
            buffer.append(xmlInfo.substring(FlexValue.TEXT_MARKER.length()));
            buffer.append("\n");
          }
          else if (isEmptyElement) {
            appendIndent(buffer, level);
            buffer.append(xmlInfo.substring(FlexValue.ELEMENT_MARKER.length()));
            buffer.append("\n");
          }
          else {
            final String startTag = xmlInfo.substring(FlexValue.ELEMENT_MARKER.length());

            final int spaceIndex = startTag.indexOf(" ");
            final String tagName = startTag.substring(1, spaceIndex > 0 ? spaceIndex : startTag.length() - 1);
            appendIndent(buffer, level);
            buffer.append(startTag);
            buffer.append("\n");

            for (FakeCompositeNode child : myChildren) {
              buffer.append(child.toPresentableSting(level + 1));
            }

            appendIndent(buffer, level);
            buffer.append("</").append(tagName).append("> ");
            buffer.append("\n");
          }
        }
      }

      return buffer.toString();
    }

    private static void appendIndent(final StringBuilder buffer, final int level) {
      buffer.append("    ".repeat(level));
    }

    @Override
    public void tooManyChildren(int remaining) {
    }

    @Override
    public void setErrorMessage(@NotNull String errorMessage) {
    }

    @Override
    public void setErrorMessage(@NotNull String errorMessage, @Nullable XDebuggerTreeNodeHyperlink link) {
    }

    @Override
    public void setMessage(@NotNull String message,
                           final Icon icon, @NotNull final SimpleTextAttributes attributes, @Nullable XDebuggerTreeNodeHyperlink link) {
    }
  }
}
