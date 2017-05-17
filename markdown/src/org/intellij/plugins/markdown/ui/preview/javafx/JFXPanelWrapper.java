package org.intellij.plugins.markdown.ui.preview.javafx;

import com.intellij.util.FieldAccessor;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

import java.awt.*;

public class JFXPanelWrapper extends JFXPanel {
  private static FieldAccessor<Integer> myScaleFactorAccessor = new FieldAccessor<>(JFXPanel.class, "scaleFactor");

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

  @Override
  public void addNotify() {
    // JFXPanel is scaled asynchronously after first repaint, what may lead
    // to showing unscaled content. To work it around, set "scaleFactor" ahead.
    if (UIUtil.isJreHiDPIEnabled() && myScaleFactorAccessor.isAvailable()) {
      myScaleFactorAccessor.set(this, Math.round(JBUI.sysScale(this)));
    }
    super.addNotify();
  }
}
