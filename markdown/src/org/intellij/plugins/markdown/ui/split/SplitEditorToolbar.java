package org.intellij.plugins.markdown.ui.split;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class SplitEditorToolbar extends JPanel implements Disposable {
  private final MySpacingPanel mySpacingPanel;

  private final List<EditorGutterComponentEx> myGutters = new ArrayList<EditorGutterComponentEx>();

  private final ComponentAdapter myAdjustToGutterListener = new ComponentAdapter() {
    @Override
    public void componentResized(ComponentEvent e) {
      adjustSpacing();
    }

    @Override
    public void componentShown(ComponentEvent e) {
      adjustSpacing();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      adjustSpacing();
    }
  };

  public SplitEditorToolbar(@NotNull final JComponent toolbarComponent) {
    super(new BorderLayout());

    mySpacingPanel = new MySpacingPanel((int)toolbarComponent.getPreferredSize().getHeight());

    add(mySpacingPanel, BorderLayout.WEST);
    add(toolbarComponent, BorderLayout.CENTER);

    setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtil.CONTRAST_BORDER_COLOR));

    addComponentListener(myAdjustToGutterListener);
  }

  public void addGutterToTrack(@NotNull EditorGutterComponentEx gutterComponentEx) {
    myGutters.add(gutterComponentEx);

    gutterComponentEx.addComponentListener(myAdjustToGutterListener);
  }

  public void adjustSpacing() {
    EditorGutterComponentEx leftMostGutter = null;
    for (EditorGutterComponentEx gutter : myGutters) {
      if (!gutter.isShowing()) {
        continue;
      }
      if (leftMostGutter == null || leftMostGutter.getX() > gutter.getX()) {
        leftMostGutter = gutter;
      }
    }

    final int spacing;
    if (leftMostGutter == null) {
      spacing = 0;
    }
    else {
      spacing = leftMostGutter.getWhitespaceSeparatorOffset();
    }
    mySpacingPanel.setSpacing(spacing);

    revalidate();
    repaint();
  }

  @Override
  public void dispose() {
    removeComponentListener(myAdjustToGutterListener);
    for (EditorGutterComponentEx gutter : myGutters) {
      gutter.removeComponentListener(myAdjustToGutterListener);
    }
  }


  private static class MySpacingPanel extends JPanel {
    private final int myHeight;

    private int mySpacing;

    public MySpacingPanel(int height) {
      myHeight = height;
      mySpacing = 0;
      setOpaque(false);
    }

    @Override
    public Dimension getPreferredSize() {
      return new Dimension(mySpacing, myHeight);
    }

    public void setSpacing(int spacing) {
      mySpacing = spacing;
    }
  }

}
