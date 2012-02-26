package com.google.jstestdriver.idea.util;

import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

public class SwingUtils {

  private SwingUtils() {}

  public static void addTextChangeListener(final JTextComponent textComponent, final TextChangeListener textChangeListener) {
    final String[] oldTextContainer = { textComponent.getText() };
    textChangeListener.textChanged("", textComponent.getText());
    textComponent.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        textChanged();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        textChanged();
      }

      public void textChanged() {
        String oldText = ObjectUtils.notNull(oldTextContainer[0], "");
        String newText = ObjectUtils.notNull(textComponent.getText(), "");
        if (!oldText.equals(newText)) {
          textChangeListener.textChanged(oldText, newText);
          oldTextContainer[0] = newText;
        }
      }
    });
  }

  @NotNull
  public static BufferedImage convertIconToImage(@NotNull Icon icon) {
    if (icon instanceof ImageIcon) {
      Image image = ((ImageIcon)icon).getImage();
      if (image instanceof BufferedImage) {
        return (BufferedImage) image;
      }
    }
    BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(UIUtil.TRANSPARENT_COLOR);
    graphics.fillRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
    icon.paintIcon(null, graphics, 0, 0);
    graphics.dispose();
    return image;
  }

  @NotNull
  public static Icon getGreyIcon(@NotNull Icon icon) {
    ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    BufferedImage originalImage = convertIconToImage(icon);
    BufferedImage greyImage = op.filter(originalImage, null);
    return new ImageIcon(greyImage);
  }
}
