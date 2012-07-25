package org.jetbrains.plugins.cucumber.java.steps.reference;

import java.util.Arrays;
import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 7/23/12
 */
public class CucumberJavaAnnotationProvider {
  public static List<String> getCucumberAnnotations() {
    String[] result = {"Given", "Then", "And", "But", "When"};
    return Arrays.asList(result);
  }
}
