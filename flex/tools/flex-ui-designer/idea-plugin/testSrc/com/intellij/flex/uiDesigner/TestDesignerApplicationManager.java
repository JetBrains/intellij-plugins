package com.intellij.flex.uiDesigner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;

import java.util.ArrayList;
import java.util.List;

class TestDesignerApplicationManager {
  public static String[] getLastProblems() {
    return ((MyDocumentProblemManager)DocumentProblemManager.getInstance()).getProblems();
  }

  static class MyDocumentProblemManager extends DocumentProblemManager {
    private final List<String> problems = new ArrayList<String>();

    public String[] getProblems() {
      final String[] strings = problems.toArray(new String[problems.size()]);
      problems.clear();
      return strings;
    }

    @Override
    public void report(Project project, String message, MessageType messageType) {
      problems.add(message);
    }
  }
}
