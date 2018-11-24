package org.intellij.plugins.markdown.ui.preview.javafx;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class JFXPanelWrapper extends JFXPanel {
  public JFXPanelWrapper() {
    Platform.setImplicitExit(false);
  }

  /**
   * This override fixes the situation of using multiple JFXPanels
   * with jbtabs/splitters when some of them are not showing.
   * On getMinimumSize there is no layout manager nor peer so
   * the result could be #size() which is incorrect.
   * @return zero size
   */
  @Override
  public Dimension getMinimumSize() {
    return new Dimension(0, 0);
  }
}
