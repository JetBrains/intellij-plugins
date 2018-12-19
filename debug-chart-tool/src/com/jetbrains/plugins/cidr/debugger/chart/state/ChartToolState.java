package com.jetbrains.plugins.cidr.debugger.chart.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartToolState {

  public final Map<Location, LineState> myLocations = new HashMap<>();
  public final List<ChartExpression> myExpressions = new ArrayList<>();
}
