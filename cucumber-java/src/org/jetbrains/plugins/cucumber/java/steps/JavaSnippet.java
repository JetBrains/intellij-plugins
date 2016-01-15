package org.jetbrains.plugins.cucumber.java.steps;

import cucumber.runtime.snippets.Snippet;

import java.util.List;

class JavaSnippet implements Snippet {
  public JavaSnippet() {}

  public String arguments(List<Class<?>> argumentTypes) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < argumentTypes.size(); i++) {
      Class<?> arg = argumentTypes.get(i);
      if (i > 0) {
        result.append(", ");
      }
      result.append(getArgType(arg)).append(" arg").append(i);
    }

    return result.toString();
  }

  protected String getArgType(Class<?> arg) {
    return arg.getSimpleName();
  }

  public String template() {
    return "@{0}(\"{1}\")\npublic void {2}({3}) \'{\'\n    // {4}\n{5}    throw new PendingException();\n\'}\'\n";
  }

  public String tableHint() {
    return "";
  }

  public String namedGroupStart() {
    return null;
  }

  public String namedGroupEnd() {
    return null;
  }

  public String escapePattern(String pattern) {
    return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
