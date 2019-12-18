package org.jetbrains.plugins.cucumber.java;

import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

public class CucumberJavaBundle extends DynamicBundle {
  @NonNls public static final String BUNDLE = "org.jetbrains.plugins.cucumber.java.CucumberJavaBundle";
  private static final CucumberJavaBundle INSTANCE = new CucumberJavaBundle();

  private CucumberJavaBundle() { super(BUNDLE); }

  @NotNull
  public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
    return INSTANCE.getMessage(key, params);
  }
}
