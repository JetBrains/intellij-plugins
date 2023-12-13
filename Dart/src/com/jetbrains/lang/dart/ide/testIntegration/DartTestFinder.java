// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.testIntegration;

import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.testIntegration.TestFinder;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.util.DartBuildFileUtil;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public final class DartTestFinder implements TestFinder {
  private static final String TEST_DART_SUFFIX = "_test.dart";

  @Nullable
  @Override
  public PsiElement findSourceElement(@NotNull final PsiElement from) {
    return from.getContainingFile();
  }

  @NotNull
  @Override
  public Collection<PsiElement> findTestsForClass(@NotNull final PsiElement element) {
    Project project = element.getProject();
    VirtualFile vFile = element.getContainingFile().getVirtualFile();
    String topLevelDirName = getTopLevelDirNameForDartFile(project, vFile);
    if (topLevelDirName == null || topLevelDirName.equals("test")) return Collections.emptyList();

    GlobalSearchScope testScope = getDirScope(project, vFile, "test");
    String testFileName = vFile.getNameWithoutExtension() + TEST_DART_SUFFIX;
    Collection<VirtualFile> testFiles = FilenameIndex.getVirtualFilesByName(testFileName, testScope);
    return ContainerUtil.mapNotNull(testFiles, file -> element.getManager().findFile(file));
  }

  @NotNull
  @Override
  public Collection<PsiElement> findClassesForTest(@NotNull final PsiElement element) {
    if (!isTest(element)) return Collections.emptyList();

    VirtualFile vFile = element.getContainingFile().getVirtualFile();
    // https://dart.dev/tools/pub/package-layout
    GlobalSearchScope subjectScope = getDirScope(element.getProject(), vFile, "benchmark", "bin", "lib", "tool", "web");
    String subjectFileName = StringUtil.trimEnd(vFile.getName(), TEST_DART_SUFFIX) + ".dart";
    Collection<VirtualFile> subjectFiles = FilenameIndex.getVirtualFilesByName(subjectFileName, subjectScope);
    return ContainerUtil.mapNotNull(subjectFiles, file -> element.getManager().findFile(file));
  }

  @Override
  public boolean isTest(@NotNull final PsiElement element) {
    return element.getContainingFile().getName().endsWith(TEST_DART_SUFFIX) &&
           "test".equals(getTopLevelDirNameForDartFile(element.getProject(), element.getContainingFile().getVirtualFile()));
  }

  @Contract("_, null -> null")
  private static String getTopLevelDirNameForDartFile(@NotNull Project project, @Nullable VirtualFile file) {
    if (file == null || !FileTypeRegistry.getInstance().isFileOfType(file, DartFileType.INSTANCE)) return null;

    VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                          ? DartBuildFileUtil.findPackageRootBuildFile(project, file)
                          : PubspecYamlUtil.findPubspecYamlFile(project, file);
    if (pubspec == null) return null;

    String rootPathWithSlash = pubspec.getParent().getPath() + "/";
    if (!file.getPath().startsWith(rootPathWithSlash)) return null;

    String relPath = file.getPath().substring(rootPathWithSlash.length());
    int slashIndex = relPath.indexOf('/');
    if (slashIndex <= 0) return null;

    return relPath.substring(0, slashIndex);
  }

  @NotNull
  private static GlobalSearchScope getDirScope(@NotNull Project project, @NotNull VirtualFile contextFile, String... topLevelDirNames) {
    VirtualFile pubspec = Registry.is("dart.projects.without.pubspec", false)
                          ? DartBuildFileUtil.findPackageRootBuildFile(project, contextFile)
                          : PubspecYamlUtil.findPubspecYamlFile(project, contextFile);
    if (pubspec == null) return GlobalSearchScope.EMPTY_SCOPE;

    VirtualFile[] dirs = ContainerUtil.mapNotNull(topLevelDirNames, pubspec.getParent()::findChild, VirtualFile.EMPTY_ARRAY);
    return GlobalSearchScopesCore.directoriesScope(project, true, dirs);
  }
}
