package com.jetbrains.plugins.cidr.debugger.chart;

import com.jetbrains.plugins.cidr.debugger.chart.state.ChartExpression;
import com.jetbrains.plugins.cidr.debugger.chart.state.ExpressionState;
import com.jetbrains.plugins.cidr.debugger.chart.ui.Zoomer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChartsPanel extends JFXPanel {
  private boolean initialized = false;
  public static final int MAX_SERIES = 50;

  private LineChart<Number, Number> myLineChart;
  private final Map<String, ChartExpressionData> mySeriesByName = new ConcurrentHashMap<>();

  public ChartsPanel() {
    Platform.runLater(() -> {

      Button reset = new Button("Clear");
      reset.setOnAction(e -> clear());
      Button noZoom = new Button("Reset Zoom");
      //defining the axes
      final NumberAxis xAxis = new NumberAxis();
      final NumberAxis yAxis = new NumberAxis();
      //creating the chart
      myLineChart = new LineChart<>(xAxis, yAxis);
      myLineChart.setCreateSymbols(false);

      Zoomer zoomer = new Zoomer(myLineChart);
      noZoom.setOnAction(e -> zoomer.resetZoom());
      VBox vBox = new VBox(10, zoomer, new HBox(10, reset, noZoom));
      vBox.setPadding(new Insets(10));
      Scene scene = new Scene(vBox);

      myLineChart.setAnimated(false);
      vBox.setFillWidth(true);
      VBox.setVgrow(zoomer, Priority.ALWAYS);
      myLineChart.setScaleShape(true);
      setScene(scene);
      invalidate();
      initialized = true;
    });
  }

  public void clear() {
    mySeriesByName.clear();
    Platform.runLater(myLineChart.getData()::clear);
  }

  public void series(ChartExpression chartExpression, List<Number> numbers) {
    ChartExpressionData data = mySeriesByName
      .computeIfAbsent(chartExpression.getName(), a -> new ChartExpressionData());
    String name;
    if (chartExpression.getState() == ExpressionState.ACCUMULATE) {
      int index = data.currentIndex.getAndUpdate(i -> (i + 1) % MAX_SERIES);
      if (data.data.size() <= index) {
        data.data.add(numbers);
      }
      else {
        data.data.set(index, numbers);
      }
      name = accChartName(chartExpression, index);
    }
    else {
      data.data.clear();
      data.currentIndex.set(0);
      data.data.add(numbers);
      name = chartExpression.getName();
    }

    if (initialized) {
      ObservableList<XYChart.Data<Number, Number>> lineData = calcLineData(chartExpression, numbers);
      Platform.runLater(() -> {
        ObservableList<XYChart.Series<Number, Number>> chartData = myLineChart.getData();
        Optional<XYChart.Series<Number, Number>> foundSeries = chartData
          .stream()
          .filter(series -> name.equals(series.getName()))
          .findFirst();
        if (foundSeries.isPresent()) {
          foundSeries.get().setData(lineData);
        }
        else {
          chartData.add(new XYChart.Series<>(name, lineData));
        }
      });
    }
  }

  @NotNull
  protected String accChartName(ChartExpression chartExpression, int index) {
    return chartExpression.getName() + " #" + (index + 1);
  }


  @NotNull
  protected ObservableList<XYChart.Data<Number, Number>> calcLineData(ChartExpression chartExpression, List<Number> numbers) {
    return FXCollections
      .observableArrayList(IntStream.range(0, numbers.size()).mapToObj(
        i -> {
          double x = chartExpression.getXBase() + chartExpression.getXScale() * i;
          double y = chartExpression.getYBase() + chartExpression.getYScale() * numbers.get(i).doubleValue();
          return new XYChart.Data<>((Number)x, (Number)y);
        }
      ).collect(Collectors.toList()));
  }

  public void refreshData(Collection<ChartExpression> expressions) {
    if (!initialized) {
      return;
    }
    List<XYChart.Series<Number, Number>> chartData = new ArrayList<>();
    for (ChartExpression expression : expressions) {
      String name = expression.getName();
      ChartExpressionData chartExpressionData = mySeriesByName.get(name);
      if (chartExpressionData == null) {
        continue;
      }
      if (expression.getState() == ExpressionState.ACCUMULATE) {
        for (int i = 0; i < chartExpressionData.data.size(); i++) {
          @NotNull ObservableList<XYChart.Data<Number, Number>> numberNumberSeries =
            calcLineData(expression, chartExpressionData.data.get(i));
          chartData.add(new XYChart.Series<>(accChartName(expression, i), numberNumberSeries));
        }
      }
      else if (!chartExpressionData.data.isEmpty()) {
        chartData.add(new XYChart.Series<>(name, calcLineData(expression, chartExpressionData.data.get(0))));
      }
    }
    ObservableList<XYChart.Series<Number, Number>> observableChartData = FXCollections
      .observableArrayList(chartData);
    Platform.runLater(() -> myLineChart.setData(observableChartData));
  }

  public boolean isSampled(String name) {
    return mySeriesByName.containsKey(name);
  }

  private static class ChartExpressionData {
    private final List<List<Number>> data = new ArrayList<>(MAX_SERIES);
    private final AtomicInteger currentIndex = new AtomicInteger();
  }
}
