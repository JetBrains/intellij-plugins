package com.jetbrains.plugins.cidr.debugger.chart.state;

public enum ExpressionState {
  DISABLED("No Sampling", "Skip"),
  SAMPLE_ONCE("Sample once after clear data", "Once"),
  ALWAYS_REFRESH("Refresh on breakpoint", "Refresh"),
  ACCUMULATE("Keep All Series", "Accumulate");
  public final String myButtonLabel;
  public final String myHint;

  ExpressionState(String hint, String buttonLabel) {
    this.myHint = hint;
    this.myButtonLabel = buttonLabel;
  }

  @Override
  public String toString() {
    return myButtonLabel;
  }
}
