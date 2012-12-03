package org.jetbrains.plugins.cucumber.groovy;

import org.jetbrains.annotations.NonNls;

public class GrCucumberCommonClassNames {
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_HOOKS_1_0 = "cucumber.runtime.groovy.Hooks";
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_1_0 = "cucumber.runtime.groovy";

  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_HOOKS_1_1 = "cucumber.api.groovy.Hooks";
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_1_1 = "cucumber.api.groovy";


  public static boolean isHookClassName(String qname) {
    return CUCUMBER_RUNTIME_GROOVY_HOOKS_1_0.equals(qname) || CUCUMBER_RUNTIME_GROOVY_HOOKS_1_1.equals(qname);
  }

  public  static boolean isCucumberRuntimeGroovyPackage(String packageName) {
    return CUCUMBER_RUNTIME_GROOVY_1_0.equals(packageName) || CUCUMBER_RUNTIME_GROOVY_1_1.equals(packageName);
  }

  private GrCucumberCommonClassNames() {}
}
