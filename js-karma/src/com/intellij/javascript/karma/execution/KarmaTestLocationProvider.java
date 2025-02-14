// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.javascript.testFramework.JsTestSelector;
import com.intellij.javascript.testFramework.qunit.DefaultQUnitModuleStructure;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.javascript.testing.detection.JsTestFrameworkDetectionUtilsKt;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.NioFiles;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public class KarmaTestLocationProvider implements SMTestLocator {
  private static final String PROTOCOL_ID__CONFIG_FILE = "config";
  private static final String PROTOCOL_ID__TEST_SUITE = "suite";
  private static final String PROTOCOL_ID__TEST = "test";

  public static final KarmaTestLocationProvider INSTANCE = new KarmaTestLocationProvider();

  @Override
  public @NotNull List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
    final Location<?> location = switch (protocol) {
      case PROTOCOL_ID__CONFIG_FILE -> getConfigLocation(project, path);
      case PROTOCOL_ID__TEST_SUITE -> getTestLocation(project, path, true);
      case PROTOCOL_ID__TEST -> getTestLocation(project, path, false);
      default -> null;
    };
    return ContainerUtil.createMaybeSingletonList(location);
  }

  private static @Nullable Location<PsiFile> getConfigLocation(Project project, @NotNull String locationData) {
    Path location = NioFiles.toPath(locationData);
    VirtualFile virtualFile = location != null ? LocalFileSystem.getInstance().findFileByNioFile(location) : null;
    if (virtualFile != null && virtualFile.isValid()) {
      PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
      if (psiFile != null && psiFile.isValid()) {
        return PsiLocation.fromPsiElement(psiFile);
      }
    }
    return null;
  }

  private static @Nullable Location getTestLocation(Project project, @NotNull String locationData, boolean isSuite) {
    List<String> path = EscapeUtils.split(locationData, '.');
    if (path.isEmpty()) {
      return null;
    }
    final List<String> suiteNames;
    final String testName;
    if (isSuite) {
      suiteNames = path;
      testName = null;
    }
    else {
      suiteNames = path.subList(0, path.size() - 1);
      testName = path.get(path.size() - 1);
    }
    JsTestSelector testSelector = new JsTestSelector(suiteNames, testName);
    PsiElement psiElement = JsTestFrameworkDetectionUtilsKt.findPsiElementInJsTestNamesIndexes(project, getKarmaDetector(), testSelector);
    if (psiElement == null) {
      String moduleName = null;
      if (suiteNames.isEmpty()) {
        moduleName = DefaultQUnitModuleStructure.NAME;
      }
      else if (suiteNames.size() == 1) {
        moduleName = suiteNames.get(0);
      }
      if (moduleName != null) {
        JsTestSelector specialQunitSelector = new JsTestSelector(List.of(moduleName), testName);
        // save working with the index for following old logic
        psiElement = JsTestFrameworkDetectionUtilsKt.findPsiElementInJsTestNamesIndexes(project, getKarmaDetector(), specialQunitSelector);
      }
    }
    if (psiElement != null) {
      return PsiLocation.fromPsiElement(psiElement);
    }
    return null;
  }

  public static @NotNull KarmaDetector getKarmaDetector() {
    return KarmaDetector.Companion.getInstance();
  }
}
