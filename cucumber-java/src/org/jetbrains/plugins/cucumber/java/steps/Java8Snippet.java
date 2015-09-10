package org.jetbrains.plugins.cucumber.java.steps;

import com.google.common.primitives.Primitives;

public class Java8Snippet extends JavaSnippet {
  protected String getArgType(Class<?> argType) {
    return argType.isPrimitive() ? Primitives.wrap(argType).getSimpleName() : argType.getSimpleName();
  }

  public String template() {
    return "{0}(\"{1}\", ({3}) -> \'{\'\n    // {4}\n{5}    throw new PendingException();\n\'}\')";
  }
}
