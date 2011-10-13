package com.google.jstestdriver.idea.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

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
}
