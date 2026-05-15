package com.intellij.lang.javascript.linter.jshint;

import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.option.OptionEnumType;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.CheckboxTree;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.PlatformColors;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JTree;
import java.awt.Color;
import java.awt.Point;

/**
 * @author Sergey Simonchik
 */
public class JSHintTreeCellRenderer extends CheckboxTree.CheckboxTreeCellRenderer {

  private static final Object LINK_MARKER = new Object();
  private JSHintTreeNode myCurrentNode;

  public JSHintTreeCellRenderer() {
    super(false);
  }

  @Override
  public void customizeRenderer(final JTree tree,
                                final Object value,
                                final boolean selected,
                                final boolean expanded,
                                final boolean leaf,
                                final int row,
                                final boolean hasFocus) {
    JSHintTreeNode node = ObjectUtils.tryCast(value, JSHintTreeNode.class);
    if (node != null) {
      doCustomizeRenderer(node, selected);
    }
  }

  private void doCustomizeRenderer(final JSHintTreeNode node, final boolean selected) {
    myCurrentNode = node;

    Color background = UIUtil.getTreeBackground(selected, true);
    UIUtil.changeBackGround(this, background);

    Color foreground = UIUtil.getTreeForeground(selected, true);

    final JSHintOption option = node.getUserDataAsOption();
    final JSHintOptionGroup optionGroup = node.getUserDataAsOptionGroup();
    final int style = option != null ? SimpleTextAttributes.STYLE_PLAIN : SimpleTextAttributes.STYLE_BOLD;

    ColoredTreeCellRenderer textRenderer = getTextRenderer();
    textRenderer.setEnabled(node.isEnabled());


    if (optionGroup != null) {
      textRenderer.append(optionGroup.getTitle(), new SimpleTextAttributes(
        null, foreground, null, SimpleTextAttributes.STYLE_BOLD
      ));
    }
    else {
      if (option == null) {
        throw new NullPointerException();
      }
      if (node.isEditLinkNeeded()) {
        myCheckbox.setVisible(false);
      }
      appendShortDescription(option, foreground, textRenderer);
      String optionValue = formatOptionValue(option, node.getValue());
      if (optionValue != null) {
        textRenderer.append(": " + optionValue, new SimpleTextAttributes(
          background, foreground, null, style
        ));
      }
      addEditHyperlinkIfNeeded(textRenderer, node);
      addOptionKey(textRenderer, option);
    }

    setForeground(foreground);
  }

  private static void addOptionKey(@NotNull SimpleColoredComponent textRenderer, @NotNull JSHintOption option) {
    final String keys;
    String keyAlias = option.getKeyAlias();
    if (keyAlias != null) {
      keys = option.getKey() + " (" + keyAlias + ")";
    }
    else {
      keys = option.getKey();
    }
    textRenderer.append("   " + keys, SimpleTextAttributes.GRAYED_SMALL_ATTRIBUTES);
  }

  private static void addEditHyperlinkIfNeeded(@NotNull SimpleColoredComponent textRenderer, @NotNull JSHintTreeNode node) {
    if (node.isEditLinkNeeded()) {
      textRenderer.append("  ", new SimpleTextAttributes(null, null, null, SimpleTextAttributes.STYLE_PLAIN));
      int style = SimpleTextAttributes.STYLE_PLAIN;
      if (node.isMouseInside()) {
        style |= SimpleTextAttributes.STYLE_UNDERLINE;
      }
      textRenderer.append(JSHintBundle.message("jshint.tree.link.set"),
                          new SimpleTextAttributes(null, PlatformColors.BLUE, null, style), LINK_MARKER);
    }
  }

  private static @Nullable @NlsSafe String formatOptionValue(@NotNull JSHintOption option, @Nullable Object value) {
    if (OptionTypes.isEnumOption(option)) {
      OptionEnumVariant variant = OptionEnumType.findVariantByValueOrFail(option, value);
      return variant.getValueAsJsonStr();
    }
    if (OptionTypes.isIntegerOption(option)) {
      Integer intValue = JSHintUtil.castToInteger(value);
      if (intValue != null) {
        return String.valueOf(intValue);
      }
      return OptionTypes.ANY_VALUE;
    }
    return null;
  }

  private static void appendShortDescription(final @NotNull JSHintOption option,
                                             final Color foreground,
                                             final SimpleColoredComponent textRenderer) {
    for (JSHintOption.Fragment fragment : option.getShortDescriptionFragments()) {
      String text = fragment.getText();
      Color background = null;
      Color localForeground = foreground;
      int style = SimpleTextAttributes.STYLE_PLAIN;
      if (fragment.isCode()) {
        style |= SimpleTextAttributes.STYLE_OPAQUE;
        background = new JBColor(0xf5f7f9, 0x2b2b2b);
        localForeground = new JBColor(0x080808, 0xa9b7c6);
        text = " " + text + " ";
      }
      textRenderer.append(text, new SimpleTextAttributes(background, localForeground, null, style));
    }
  }

  public boolean isPointInsideEditLink(@NotNull JSHintTreeNode node, @NotNull Point pointRelativeToRow) {
    SimpleColoredComponent coloredComponent = getTextRenderer();
    if (myCurrentNode != node) {
      coloredComponent.clear();
      doCustomizeRenderer(node, false);
    }
    return coloredComponent.getFragmentTagAt(pointRelativeToRow.x) == LINK_MARKER;
  }

}
