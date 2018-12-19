package com.jetbrains.plugins.cidr.debugger.chart.ui;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.LinkedList;

public class Zoomer extends StackPane {

  public static final int ZOOM_GO_BACK_DEPTH = 5;

  private double zoomStartX = -1;
  private double zoomStartY = -1;
  private final Rectangle zoomRectangle;
  private final Node chartBackground;
  private final ValueAxis<Number> xAxis;
  private final ValueAxis<Number> yAxis;
  private final LinkedList<double[]> zoomStack = new LinkedList<>();

  public Zoomer(XYChart<Number, Number> chart) {
    super(chart);
    zoomRectangle = new Rectangle(0, 0, new Color(0, 0.5, 1, 0.2));
    zoomRectangle.setManaged(false);
    getChildren().add(zoomRectangle);
    chartBackground = chart.lookup(".chart-plot-background");
    chartBackground.setOnMouseDragged(this::mouseEventDrag);
    chartBackground.setOnMousePressed(this::mouseEventPress);
    chartBackground.setOnMouseReleased(this::mouseEventRelease);
    chartBackground.setOnMouseClicked(this::mouseEventClick);
    xAxis = (ValueAxis<Number>)chart.getXAxis();
    yAxis = (ValueAxis<Number>)chart.getYAxis();
  }


  private void mouseEventClick(MouseEvent mouseEvent) {
    if (mouseEvent.getButton() == MouseButton.SECONDARY) {
      if (zoomStack.isEmpty()) {
        resetZoom();
      }
      else {
        double[] zoom = zoomStack.pop();
        xAxis.setLowerBound(zoom[0]);
        xAxis.setUpperBound(zoom[1]);
        yAxis.setLowerBound(zoom[2]);
        yAxis.setUpperBound(zoom[3]);
      }
    }
  }

  private void mouseEventDrag(MouseEvent mouseEvent) {
    if (zoomStartX >= 0) {
      Bounds bounds = chartBackground.localToScene(chartBackground.getBoundsInLocal());
      double sceneX = mouseEvent.getSceneX();
      if (zoomStartX > sceneX) {
        zoomRectangle.setX(Math.max(bounds.getMinX(), sceneX));
        zoomRectangle.setWidth(Math.min(bounds.getMaxX(), zoomStartX) - zoomRectangle.getX());
      }
      else {
        zoomRectangle.setX(zoomStartX);
        zoomRectangle.setWidth(Math.min(bounds.getMaxX(), sceneX) - zoomRectangle.getX());
      }
      double sceneY = mouseEvent.getSceneY();
      if (zoomStartY > sceneY) {
        zoomRectangle.setY(Math.max(bounds.getMinX(), sceneY));
        zoomRectangle.setHeight(Math.min(bounds.getMaxY(), zoomStartY) - zoomRectangle.getY());
      }
      else {
        zoomRectangle.setY(zoomStartY);
        zoomRectangle.setHeight(Math.min(bounds.getMaxY(), sceneY) - zoomRectangle.getY());
      }

      zoomRectangle.setVisible(true);
    }
  }

  private void mouseEventPress(MouseEvent event) {
    if (zoomStartX < 0 && event.getButton() == MouseButton.PRIMARY) {
      zoomStartX = event.getSceneX();
      zoomStartY = event.getSceneY();
    }
  }

  private void mouseEventRelease(MouseEvent mouseEvent) {
    if (zoomStartX >= 0 && mouseEvent.getButton() == MouseButton.PRIMARY) {
      Bounds bounds = xAxis.localToScene(xAxis.getLayoutBounds());
      Number minZoomX = xAxis.getValueForDisplay(zoomRectangle.getX() - bounds.getMinX());
      Number maxZoomX = xAxis.getValueForDisplay(zoomRectangle.getX() + zoomRectangle.getWidth() - bounds.getMinX());
      bounds = yAxis.localToScene(yAxis.getLayoutBounds());
      Number maxZoomY = yAxis.getValueForDisplay(zoomRectangle.getY() - bounds.getMinY());
      Number minZoomY = yAxis.getValueForDisplay(zoomRectangle.getY() + zoomRectangle.getWidth() - bounds.getMinY());
      if (!xAxis.isAutoRanging()) {
        zoomStack.push(new double[]{xAxis.getLowerBound(), xAxis.getUpperBound(), yAxis.getLowerBound(), yAxis.getUpperBound()});
      }
      xAxis.setAutoRanging(false);
      xAxis.setLowerBound(minZoomX.doubleValue());
      xAxis.setUpperBound(maxZoomX.doubleValue());
      yAxis.setAutoRanging(false);
      yAxis.setLowerBound(minZoomY.doubleValue());
      yAxis.setUpperBound(maxZoomY.doubleValue());
      while (zoomStack.size() > ZOOM_GO_BACK_DEPTH) {
        zoomStack.removeLast();
      }
      zoomStartX = zoomStartY = -1;
      zoomRectangle.setVisible(false);
    }
  }

  public void resetZoom() {
    xAxis.setAutoRanging(true);
    yAxis.setAutoRanging(true);
    zoomStack.clear();
  }
}
