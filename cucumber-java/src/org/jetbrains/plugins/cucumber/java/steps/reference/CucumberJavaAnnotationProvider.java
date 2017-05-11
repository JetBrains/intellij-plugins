package org.jetbrains.plugins.cucumber.java.steps.reference;

import java.util.Arrays;
import java.util.List;

public class CucumberJavaAnnotationProvider {
  public static final String[] STEP_MARKERS = {"Given", "Then", "And", "But", "When"};
  public static final String[] HOOK_MARKERS = {"Before", "After"};

  public static List<String> getCucumberStepAnnotations() {
    return Arrays.asList(STEP_MARKERS);
  }

  public static List<String> getCucumberHookAnnotations() {
    return Arrays.asList(HOOK_MARKERS);
  }
}
