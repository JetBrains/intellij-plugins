package io.cucumber.cucumberexpressions;

public final class ParameterType<T> implements Comparable<ParameterType<?>> {
  public ParameterType(String name, String regexp, Class<T> type, Transformer<T> transformer) {
  }
}