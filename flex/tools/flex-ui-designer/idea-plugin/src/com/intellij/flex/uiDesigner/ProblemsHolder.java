package com.intellij.flex.uiDesigner;

import com.intellij.openapi.diagnostic.Logger;

import java.util.ArrayList;
import java.util.List;

public class ProblemsHolder {
  private static final Logger LOG = Logger.getInstance(ProblemsHolder.class.getName());

  private final List<String> problems = new ArrayList<String>();

  public void add(String message) {
    problems.add(message);
  }

  public String[] getResultList() {
    return problems.toArray(new String[problems.size()]);
  }

  public void clear() {
    problems.clear();
  }

  public void add(InvalidPropertyException e) {
    problems.add(e.getMessage());
    if (e.getCause() != null) {
      LOG.error(e.getCause());
    }
  }

  public void add(RuntimeException e, String propertyName) {
    String error;
    if (e instanceof NumberFormatException) {
      error = e.getMessage();
      final String prefix = "For input string: \"";
      if (error.startsWith(prefix)) {
        error = FlexUIDesignerBundle.message("error.write.property.numeric.value",
          error.substring(prefix.length(), error.charAt(error.length() - 1) == '"' ? error.length() - 1 : error.length()), propertyName);
      }
    }
    else {
      error = FlexUIDesignerBundle.message("error.write.property", propertyName);
    }

    LOG.error(e);

    problems.add(error);
  }

  public boolean isEmpty() {
    return problems.isEmpty();
  }

  public void add(RuntimeException e) {
    LOG.error(e);
  }

  public void add(AssertionError e) {
    LOG.error(e);
  }

  public void add(Throwable e) {
    if (e instanceof InvalidPropertyException) {
      add(((InvalidPropertyException)e));
    }
    else if (e instanceof RuntimeException) {
      add(((RuntimeException)e));
    }
    else {
      add(((AssertionError)e));
    }
  }
}
