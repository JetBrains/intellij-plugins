package com.jetbrains.plugins.cidr.debugger.chart.state;

import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChartExpression {

  @NotNull
  private String myExpression = "";

  @Nullable
  private String myName = "";

  private double myXScale = 1;
  private double myYScale = 1;

  private double myXBase = 0;
  private double myYBase = 0;

  @NotNull
  private ExpressionState myState = ExpressionState.SAMPLE_ONCE;

  @Transient
  @NotNull
  private String myExpressionTrim = myExpression;

  public String getName() {

    return (myName == null || myName.isEmpty()) ? myExpression : myName;
  }

  public void setName(@Nullable String name) {
    this.myName = name;
  }

  @NotNull
  public String getExpression() {
    return myExpression;
  }

  public void setExpression(@NotNull String expression) {
    this.myExpression = expression;
    myExpressionTrim = expression.trim();
  }

  @NotNull
  public String getExpressionTrim() {
    return myExpressionTrim;
  }

  @NotNull
  public ExpressionState getState() {
    return myState;
  }

  public void setState(@NotNull ExpressionState state) {
    this.myState = state;
  }

  public double getXScale() {
    return myXScale;
  }

  public void setXScale(double xScale) {
    this.myXScale = xScale;
  }

  public double getYScale() {
    return myYScale;
  }

  public void setYScale(double yScale) {
    this.myYScale = yScale;
  }

  public double getXBase() {
    return myXBase;
  }

  public void setXBase(double xBase) {
    this.myXBase = xBase;
  }

  public double getYBase() {
    return myYBase;
  }

  public void setYBase(double yBase) {
    this.myYBase = yBase;
  }
}
