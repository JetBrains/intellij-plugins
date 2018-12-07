// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.*;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CucumberJavaVersionUtil {
  public static final String CUCUMBER_CORE_VERSION_3 = "3";
  public static final String CUCUMBER_CORE_VERSION_2 = "2";
  public static final String CUCUMBER_CORE_VERSION_1_2 = "1.2";
  public static final String CUCUMBER_CORE_VERSION_1_0 = "1";

  private static final String CUCUMBER_1_2_PLUGIN_CLASS = "cucumber.api.Plugin";
  private static final String CUCUMBER_2_CLASS_MARKER = "cucumber.api.formatter.Formatter";
  private static final String CUCUMBER_3_CLASS_MARKER = "cucumber.runner.TestCase";

  /**
   * Computes and caches version of attached Cucumber Java library.
   * If {@code module} is not null module's scope with libraries used to look for Cucumber-Core library, 
   * {@code project}'s scope used otherwise. 
   */
  @NotNull
  public static String getCucumberCoreVersion(@Nullable Module module, @NotNull Project project) {
    CachedValuesManager manager = CachedValuesManager.getManager(project);

    CachedValue<String> result = manager.createCachedValue(
      () -> {
        String resultCucumberCoreVersion = computeCucumberCoreVersion(module, project);
        return CachedValueProvider.Result.create(resultCucumberCoreVersion, PsiModificationTracker.MODIFICATION_COUNT);
      },
      false);

    return result.getValue();
  }
  
  public static boolean isCucumber3OrMore(@NotNull PsiElement context) {
    Module module = ModuleUtilCore.findModuleForPsiElement(context);
    return VersionComparatorUtil.compare(getCucumberCoreVersion(module, context.getProject()), CUCUMBER_CORE_VERSION_3) >= 0;
  }

  @NotNull
  private static String computeCucumberCoreVersion(@Nullable Module module, @NotNull Project project) {
    GlobalSearchScope scope =
      module != null ? GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module, true) : GlobalSearchScope.projectScope(project);
    
    JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
    if (facade.findClass(CUCUMBER_3_CLASS_MARKER, scope) != null) {
      return CUCUMBER_CORE_VERSION_3;
    } else if (facade.findClass(CUCUMBER_2_CLASS_MARKER, scope) != null) {
      return CUCUMBER_CORE_VERSION_2;
    } else if (facade.findClass(CUCUMBER_1_2_PLUGIN_CLASS, scope) != null) {
      return CUCUMBER_CORE_VERSION_1_2;
    }
    return CUCUMBER_CORE_VERSION_3;
  }
}
