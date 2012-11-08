package org.jetbrains.plugins.cucumber.java.steps.reference;

import java.util.Arrays;
import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 7/23/12
 */
public class CucumberJavaAnnotationProvider {
  public static final String[] STEP_MARKERS = {"Given", "Then", "And", "But", "When"};

  public static List<String> getCucumberAnnotations() {
    return Arrays.asList(STEP_MARKERS);
  }
}
