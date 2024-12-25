// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.CUCUMBER_1_0_MAIN_CLASS;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.CUCUMBER_1_1_MAIN_CLASS;


public final class CucumberJavaVersionUtil {
  public static final String CUCUMBER_CORE_VERSION_5 = "5";
  public static final String CUCUMBER_CORE_VERSION_4_5 = "4.5";
  public static final String CUCUMBER_CORE_VERSION_4 = "4";
  public static final String CUCUMBER_CORE_VERSION_3 = "3";
  public static final String CUCUMBER_CORE_VERSION_2 = "2";
  public static final String CUCUMBER_CORE_VERSION_1_2 = "1.2";
  public static final String CUCUMBER_CORE_VERSION_1_1 = "1.1";
  public static final String CUCUMBER_CORE_VERSION_1_0 = "1";

  private static final List<Pair<String, String>> VERSION_CLASS_MARKERS = new ArrayList<>();

  private static final Logger LOG = Logger.getInstance(CucumberJavaVersionUtil.class);

  static {
    VERSION_CLASS_MARKERS.add(Pair.create("io.cucumber.plugin.event.EventHandler", CUCUMBER_CORE_VERSION_5));
    VERSION_CLASS_MARKERS.add(Pair.create("io.cucumber.core.cli.Main", CUCUMBER_CORE_VERSION_4_5));
    VERSION_CLASS_MARKERS.add(Pair.create("cucumber.api.event.ConcurrentEventListener", CUCUMBER_CORE_VERSION_4));
    VERSION_CLASS_MARKERS.add(Pair.create("cucumber.runner.TestCase", CUCUMBER_CORE_VERSION_3));
    VERSION_CLASS_MARKERS.add(Pair.create("cucumber.api.formatter.Formatter", CUCUMBER_CORE_VERSION_2));
    VERSION_CLASS_MARKERS.add(Pair.create("cucumber.api.Plugin", CUCUMBER_CORE_VERSION_1_2));
    VERSION_CLASS_MARKERS.add(Pair.create(CUCUMBER_1_1_MAIN_CLASS, CUCUMBER_CORE_VERSION_1_1));
    VERSION_CLASS_MARKERS.add(Pair.create(CUCUMBER_1_0_MAIN_CLASS, CUCUMBER_CORE_VERSION_1_0));
  }

  /**
   * Computes and caches version of attached Cucumber Java library.
   * If {@code module} is not null module's scope with libraries used to look for Cucumber-Core library,
   * {@code project}'s scope used otherwise.
   */
  public static @NotNull String getCucumberCoreVersion(@Nullable Module module, @NotNull Project project) {
    CachedValuesManager manager = CachedValuesManager.getManager(project);

    CachedValue<String> result = manager.createCachedValue(
      () -> {
        String resultCucumberCoreVersion = computeCucumberCoreVersion(module, project);
        return CachedValueProvider.Result.create(resultCucumberCoreVersion, PsiModificationTracker.MODIFICATION_COUNT);
      },
      false);

    return result.getValue();
  }

  public static boolean isCucumber2OrMore(@NotNull Module module) {
    return VersionComparatorUtil.compare(getCucumberCoreVersion(module, module.getProject()), CUCUMBER_CORE_VERSION_2) >= 0;
  }

  public static boolean isCucumber3OrMore(@NotNull Module module) {
    return VersionComparatorUtil.compare(getCucumberCoreVersion(module, module.getProject()), CUCUMBER_CORE_VERSION_3) >= 0;
  }

  private static @NotNull String computeCucumberCoreVersion(@Nullable Module module, @NotNull Project project) {
    GlobalSearchScope scope =
      module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, true) : GlobalSearchScope.projectScope(project);

    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    for (Pair<String, String> marker : VERSION_CLASS_MARKERS) {
      if (facade.findClass(marker.first, scope) != null) {
        LOG.debug("Cucumber-core version detected by class: " + marker.first + ", version: " + marker.second);
        return marker.second;
      }
    }

    String theLatestVersion = VERSION_CLASS_MARKERS.get(VERSION_CLASS_MARKERS.size() - 1).second;
    LOG.debug("Can't detect cucumber-core version by marker class, assume the latest version: " + theLatestVersion);

    return theLatestVersion;
  }
}
