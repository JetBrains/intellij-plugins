package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.util.containers.ContainerUtil;

import java.util.Set;

public class CucumberJavaAnnotationProvider {
  public static final Set<String> STEP_MARKERS = ContainerUtil.set("Given", "Then", "And", "But", "When");
  public static final Set<String> HOOK_MARKERS = ContainerUtil.set("Before", "After");
}
