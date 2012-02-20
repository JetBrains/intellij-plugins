package com.intellij.flex.uiDesigner.preview;

import com.intellij.flex.uiDesigner.FlashUIDesignerBundle;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.util.ui.AbstractLayoutManager;
import com.intellij.util.ui.AsyncProcessIcon;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

class MxmlPreviewPanel extends JPanel implements Disposable {
  private static final double EPS = 0.0000001;
  private static final double MAX_ZOOM_FACTOR = 2.0;
  private static final double ZOOM_STEP = 1.25;

  private BufferedImage image;

  private double zoomFactor = 1.0;
  private boolean zoomToFit = true;

  private final LoadingDecorator loadingDecorator;

  private final JPanel imagePanel = new JPanel() {
    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (image == null) {
        return;
      }

      final Graphics2D g2 = (Graphics2D)g;
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.drawImage(image, 0, 0, getWidth(), getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
    }
  };

  public MxmlPreviewPanel() {
    setBackground(Color.WHITE);
    setOpaque(true);
    imagePanel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.GRAY));

    JPanel imageWrapper = new JPanel(new MyLayout());
    imageWrapper.add(imagePanel);

    loadingDecorator = new LoadingDecorator(imageWrapper, this, -1) {
      @Override
      protected NonOpaquePanel customizeLoadingLayer(JPanel parent, JLabel text, AsyncProcessIcon icon) {
        final NonOpaquePanel panel = super.customizeLoadingLayer(parent, text, icon);
        final Font font = text.getFont();
        text.setFont(font.deriveFont(font.getStyle(), font.getSize() + 6));
        text.setForeground(new Color(0, 0, 0, 150));
        return panel;
      }
    };
    loadingDecorator.setLoadingText(FlashUIDesignerBundle.message("rendering"));

    add(loadingDecorator.getComponent());
  }

  @Override
  public void doLayout() {
    super.doLayout();
    for (int i = 0; i < getComponentCount(); i++) {
      getComponent(i).setBounds(0, 0, getWidth(), getHeight());
    }
  }

  public LoadingDecorator getLoadingDecorator() {
    return loadingDecorator;
  }

  public void setImage(@Nullable final BufferedImage image) {
    this.image = image;
    doRevalidate();
  }

  private void doRevalidate() {
    revalidate();
    updateImageSize();
    repaint();
  }

  void updateImageSize() {
    if (image == null) {
      imagePanel.setSize(0, 0);
    }
    else {
      imagePanel.setSize(getScaledDimension());
    }
  }

  private Dimension getScaledDimension() {
    if (zoomToFit) {
      final Dimension panelSize = getParent().getSize();
      if (image.getWidth() <= panelSize.width && image.getHeight() <= panelSize.height) {
        return new Dimension(image.getWidth(), image.getHeight());
      }

      if (image.getWidth() <= panelSize.width) {
        final double f = panelSize.getHeight() / image.getHeight();
        return new Dimension((int)(image.getWidth() * f), (int)(image.getHeight() * f));
      }
      else if (image.getHeight() <= panelSize.height) {
        final double f = panelSize.getWidth() / image.getWidth();
        return new Dimension((int)(image.getWidth() * f), (int)(image.getHeight() * f));
      }

      double f = panelSize.getWidth() / image.getWidth();
      int candidateWidth = (int)(image.getWidth() * f);
      int candidateHeight = (int)(image.getHeight() * f);
      if (candidateWidth <= panelSize.getWidth() && candidateHeight <= panelSize.getHeight()) {
        return new Dimension(candidateWidth, candidateHeight);
      }
      f = panelSize.getHeight() / image.getHeight();
      return new Dimension((int)(image.getWidth() * f), (int)(image.getHeight() * f));
    }
    return new Dimension((int)(image.getWidth() * zoomFactor), (int)(image.getHeight() * zoomFactor));
  }

  private void setZoomFactor(double zoomFactor) {
    this.zoomFactor = zoomFactor;
    doRevalidate();
  }

  private double computeCurrentZoomFactor() {
    if (image == null) {
      return zoomFactor;
    }
    return (double)imagePanel.getWidth() / (double)image.getWidth();
  }

  private double getZoomFactor() {
    return zoomToFit ? computeCurrentZoomFactor() : zoomFactor;
  }

  public void zoomOut() {
    setZoomFactor(Math.max(getMinZoomFactor(), zoomFactor / ZOOM_STEP));
  }

  public boolean canZoomOut() {
    return image != null && zoomFactor > getMinZoomFactor() + EPS;
  }

  private double getMinZoomFactor() {
    return Math.min(1.0, (double)getParent().getWidth() / (double)image.getWidth());
  }

  public void zoomIn() {
    if (zoomToFit) {
      zoomToFit = false;
      setZoomFactor(computeCurrentZoomFactor() * ZOOM_STEP);
      return;
    }
    setZoomFactor(zoomFactor * ZOOM_STEP);
  }

  public boolean canZoomIn() {
    return getZoomFactor() * ZOOM_STEP < MAX_ZOOM_FACTOR - EPS;
  }

  public void zoomActual() {
    if (image == null) {
      return;
    }
    if (zoomToFit && imagePanel.getWidth() >= image.getWidth() && imagePanel.getHeight() >= image.getHeight()) {
      return;
    }
    zoomToFit = false;
    setZoomFactor(1.0);
  }

  public void setZoomToFit(boolean zoomToFit) {
    this.zoomToFit = zoomToFit;
    doRevalidate();
  }

  public boolean isZoomToFit() {
    return zoomToFit;
  }

  @Override
  public void dispose() {
  }

  private class MyLayout extends AbstractLayoutManager {
    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return parent.getComponent(0).getSize();
    }

    @Override
    public void layoutContainer(Container target) {
      Component component = target.getComponent(0);

      Dimension size = image == null ? new Dimension() : getScaledDimension();
      int diff = target.getWidth() - size.width;
      component.setSize(size);
      component.setLocation(diff > 0 ? diff / 2 : 0, component.getY());
    }
  }
}
