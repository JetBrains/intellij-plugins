// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.groovy;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.cucumber.java.config.CucumberConfigUtil;

public final class GrCucumberCommonClassNames {
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_HOOKS_1_0 = "cucumber.runtime.groovy.Hooks";
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_1_0 = "cucumber.runtime.groovy";

  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_HOOKS_1_1 = "cucumber.api.groovy.Hooks";
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_1_1 = "cucumber.api.groovy";

  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_HOOKS_5_1 = "io.cucumber.groovy.Hooks";
  @NonNls private static final String CUCUMBER_RUNTIME_GROOVY_5_1 = "io.cucumber.groovy";

  @NonNls private static final String[] CUCUMBER_BASE_VERSIONS = new String[]{"1_0", "1_1", "5_1"};
  @NonNls private static final String[] CUCUMBER_PACKAGE_PREFIXES = new String[]{CUCUMBER_RUNTIME_GROOVY_1_0, CUCUMBER_RUNTIME_GROOVY_1_1,
    CUCUMBER_RUNTIME_GROOVY_5_1};

  @NonNls public static final String CUCUMBER_GROOVY_5_1_VERSION = "5.1";

  public static boolean isHookClassName(String qname) {
    return CUCUMBER_RUNTIME_GROOVY_HOOKS_1_0.equals(qname) || CUCUMBER_RUNTIME_GROOVY_HOOKS_1_1.equals(qname) || CUCUMBER_RUNTIME_GROOVY_HOOKS_5_1
      .equals(qname);
  }

  public static boolean isCucumberRuntimeGroovyPackage(String packageName) {
    return CUCUMBER_RUNTIME_GROOVY_1_0.equals(packageName) || CUCUMBER_RUNTIME_GROOVY_1_1.equals(packageName) || CUCUMBER_RUNTIME_GROOVY_5_1
      .equals(packageName);
  }

  public static String cucumberTemplateVersion(String version) {
    int arrayIndex = cucumberVersionIndex(version);
    return CUCUMBER_BASE_VERSIONS[arrayIndex];
  }

  public static String cucumberPackagePrefix(String version) {
    int arrayIndex = cucumberVersionIndex(version);
    return CUCUMBER_PACKAGE_PREFIXES[arrayIndex];
  }

  private GrCucumberCommonClassNames() {}

  private static int cucumberVersionIndex(String version) {
    if (version != null && version.compareTo(CucumberConfigUtil.CUCUMBER_VERSION_1_1) < 0) {
      return 0;
    }
    if (version != null && version.compareTo(CUCUMBER_GROOVY_5_1_VERSION) < 0) {
      return 1;
    }
    return 2;
  }
}
