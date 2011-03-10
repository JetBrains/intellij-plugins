package com.intellij.lang.javascript.generation;

public interface EventBinder {

  boolean isBindEvent();
  String getEventName(String parameterName);
  boolean isCreateEventConstant();
  String getEventConstantName(String parameterName);
}
