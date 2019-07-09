// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.testIntegration;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.testIntegration.TestFinder;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * This is a {@link TestFinder} contribution for Dart sources to navigate ./lib/foo.dart <--> ./test/foo_test.dart.
 * Package layout for Dart described here: https://dart.dev/tools/pub/package-layout. See https://youtrack.jetbrains.com/issue/WEB-14127.
 */
public class DartTestFinder implements TestFinder {
  @Nullable
  @Override
  public PsiElement findSourceElement(@NotNull final PsiElement from) {
    return from.getContainingFile();
  }

  @NotNull
  @Override
  public Collection<PsiElement> findTestsForClass(@NotNull final PsiElement element) {
    if (!isDartLibFile(element)) {
      return Collections.emptySet();
    }

    final PsiFile file = element.getContainingFile();
    final String fileName = file.getVirtualFile().getNameWithoutExtension();

    final String grandParentPath = getGrandParentDirectoryPath(file);
    if (grandParentPath == null || grandParentPath.isEmpty()) {
      return Collections.emptySet();
    }

    final String needleFilePath = FileUtil.toSystemIndependentName(grandParentPath + "/test/" + fileName + "_test.dart");
    return getElementCollectionFromPath(element.getManager(), needleFilePath);
  }

  @NotNull
  @Override
  public Collection<PsiElement> findClassesForTest(@NotNull final PsiElement element) {
    if (!isDartTestFile(element)) {
      return Collections.emptySet();
    }
    final PsiFile file = element.getContainingFile();
    final String fileName = file.getVirtualFile().getNameWithoutExtension();

    final String grandParentPath = getGrandParentDirectoryPath(file);
    if (grandParentPath == null || grandParentPath.isEmpty()) {
      return Collections.emptySet();
    }

    final String needleFilePath = FileUtil.toSystemIndependentName(grandParentPath +
                                                                   "/" +
                                                                   PubspecYamlUtil.LIB_DIR_NAME +
                                                                   "/" +
                                                                   fileName.substring(0, fileName.length() - 5) +
                                                                   ".dart");
    return getElementCollectionFromPath(element.getManager(), needleFilePath);
  }

  @NotNull
  private static Collection<PsiElement> getElementCollectionFromPath(@NotNull final PsiManager psiManager,
                                                                     @NotNull final String needleFilePath) {
    final VirtualFile virtualFile =
      LocalFileSystem.getInstance().findFileByPath(needleFilePath);
    if (virtualFile != null) {
      final PsiFile psiFile = psiManager.findFile(virtualFile);
      if (psiFile != null) {
        return Collections.singleton(psiFile);
      }
    }
    return Collections.emptySet();
  }

  @Override
  public boolean isTest(@NotNull final PsiElement element) {
    return isDartTestFile(element);
  }

  /**
   * Verify that the containing file for this {@link PsiElement} is:
   * - a Dart file,
   * - the file name matches "*_test.dart",
   * - and that the file is in a parent directory named "test".
   */
  @Contract("null -> false")
  public static boolean isDartTestFile(@Nullable final PsiElement element) {
    if (element == null) {
      return false;
    }
    final VirtualFile virtualFile = element.getContainingFile().getVirtualFile();
    if (virtualFile == null || virtualFile.getFileType() != DartFileType.INSTANCE) {
      return false;
    }
    return virtualFile.getNameWithoutExtension().endsWith("_test") &&
           virtualFile.getParent().getName().equals("test");
  }

  /**
   * Verify that the containing file for this {@link PsiElement} is:
   * - a Dart file,
   * - and that the file is in a parent directory named "lib".
   */
  @Contract("null -> false")
  public static boolean isDartLibFile(@Nullable final PsiElement element) {
    if (element == null) {
      return false;
    }
    final VirtualFile vFile = element.getContainingFile().getVirtualFile();
    return vFile != null &&
           vFile.getFileType() == DartFileType.INSTANCE &&
           vFile.getParent().getName().equals(PubspecYamlUtil.LIB_DIR_NAME);
  }

  @Contract("null -> null")
  public static String getGrandParentDirectoryPath(@Nullable final PsiFile file) {
    if (file != null && file.getParent() != null && file.getParent().getParent() != null) {
      return file.getParent().getParent().getVirtualFile().getPath();
    }
    return null;
  }
}
