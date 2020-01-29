package com.jetbrains.cidr.cpp.embedded.platformio;

import com.intellij.tools.Tool;

//todo remove when Tool class got relaxed setters
public class CustomTool extends Tool {
  private final CharSequence tabTitle;

  public CustomTool(CharSequence tabTitle) {
    this.tabTitle = tabTitle;
  }

  @Override
  public boolean isUseConsole() {
    return true;
  }

  @Override
  public boolean isShowConsoleOnStdOut() {
    return true;
  }

  @Override
  public boolean isShowConsoleOnStdErr() {
    return true;
  }

  @Override
  public String getName() {
    return tabTitle.toString();
  }
}
