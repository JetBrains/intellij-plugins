package org.jetbrains.plugins.cucumber.java.steps.reference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CucumberJavaAnnotationProvider {
  public static final Set<String> STEP_MARKERS = new HashSet<>(Arrays.asList("Given", "Then", "And", "But", "When"));
  public static final Set<String> HOOK_MARKERS = new HashSet<>(Arrays.asList("Before", "After"));
}
