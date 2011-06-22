package com.google.jstestdriver.idea.javascript.navigation;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.Nullable;

public class NavigationUtils {

  private static final Key<CachedValue<NavigationRegistry>> NAVIGATION_REGISTRY_KEY = Key.create(NavigationRegistry.class.getName());

  private NavigationUtils() {}

  @Nullable
  public static NavigationRegistry fetchNavigationRegistryByJsFile(final JSFile jsFile) {
    CachedValuesManager cachedValuesManager = CachedValuesManager.getManager(jsFile.getProject());
    return cachedValuesManager.getCachedValue(jsFile, NAVIGATION_REGISTRY_KEY, new CachedValueProvider<NavigationRegistry>() {
          @Override
          public Result<NavigationRegistry> compute() {
            NavigationRegistryBuilder nrb = NavigationRegistryBuilder.getInstance();
            NavigationRegistry navigationRegistry = nrb.buildNavigationRegistryByJsFile(jsFile);
            return Result.create(navigationRegistry, jsFile);
          }
        }, false);
  }

}
