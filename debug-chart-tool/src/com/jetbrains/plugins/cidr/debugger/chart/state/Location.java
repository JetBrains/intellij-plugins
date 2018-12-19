package com.jetbrains.plugins.cidr.debugger.chart.state;

import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Location {
  @NotNull
  public String myFileUrl = "";
  public int myLineNo;

  public Location(@NotNull XLineBreakpoint<?> breakpoint) {
    this(breakpoint.getFileUrl(), breakpoint.getLine());
  }

  public Location(@NotNull String fileUrl, int lineNo) {
    this.myFileUrl = fileUrl;
    this.myLineNo = lineNo;
  }

  @SuppressWarnings("unused")
  public Location() {
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Location location = (Location)o;
    return myLineNo == location.myLineNo &&
           Objects.equals(myFileUrl, location.myFileUrl);
  }

  @Override
  public int hashCode() {

    return Objects.hash(myFileUrl, myLineNo);
  }
}
