// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.config;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Max Medvedev
 */
@NotNullByDefault
public final class CucumberConfigUtil {
  private static final @NonNls Pattern CUCUMBER_PATTERN = Pattern.compile("cucumber-core-(.*)\\.jar");

  private static final @NonNls String CUCUMBER_CLI_MAIN_1_0 = "cucumber.cli.Main";

  private static final @NonNls String CUCUMBER_API_CLI_MAIN_1_1 = "cucumber.api.cli.Main";

  public static final @NonNls String CUCUMBER_VERSION_1_0 = "1.0";

  public static final @NonNls String CUCUMBER_VERSION_1_1 = "1.1";

  public static final @NonNls String CUCUMBER_VERSION_4_5 = "4.5";

  public static final @NonNls String CUCUMBER_VERSION_5_0 = "5.0";

  public static @Nullable String getCucumberCoreVersion(PsiElement place) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(place);
    if (module == null) {
      return null;
    }

    return CachedValuesManager.getManager(module.getProject()).getCachedValue(module, () -> {
      return CachedValueProvider.Result.create(getCucumberCoreVersionImpl(module), ProjectRootManager.getInstance(module.getProject()));
    });
  }

  private static @Nullable String getCucumberCoreVersionImpl(Module module) {
    for (OrderEntry orderEntry : ModuleRootManager.getInstance(module).getOrderEntries()) {
      if (orderEntry instanceof LibraryOrderEntry libraryOrderEntry) {
        final String libraryName = libraryOrderEntry.getLibraryName();
        final Library library = libraryOrderEntry.getLibrary();

        //libraryName is null for simple jar entries
        if ((libraryName == null || StringUtil.toLowerCase(libraryName).contains("cucumber")) && library != null) {
          final VirtualFile[] files = library.getFiles(OrderRootType.CLASSES);
          for (VirtualFile file : files) {
            final String version = getVersionByFile(file);
            if (version != null) return version;
          }
        }
      }
    }

    return getSimpleVersionFromMainClass(module);
  }

  private static @Nullable String getSimpleVersionFromMainClass(Module module) {
    final JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());

    final PsiClass oldMain = facade.findClass(CUCUMBER_CLI_MAIN_1_0, module.getModuleWithLibrariesScope());
    if (oldMain != null) return CUCUMBER_VERSION_1_0;

    final PsiClass newMain = facade.findClass(CUCUMBER_API_CLI_MAIN_1_1, module.getModuleWithLibrariesScope());
    if (newMain != null) return CUCUMBER_VERSION_1_1;

    return null;
  }

  private static @Nullable String getVersionByFile(VirtualFile file) {
    final String name = file.getName();
    final Matcher matcher = CUCUMBER_PATTERN.matcher(name);
    if (matcher.matches() && matcher.groupCount() == 1) {
      return matcher.group(1);
    }
    return null;
  }
}
