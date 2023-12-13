// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class DartLibraryIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_LIBRARY_INDEX = ID.create("DartLibraryIndex");

  private static final Key<Pair<Long, BidirectionalMap<String, String>>> LIBRARIES_TIME_AND_MAP_KEY = Key.create("dart.internal.libraries");

  private final DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_LIBRARY_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new DefaultFileTypeSpecificInputFilter(DartFileType.INSTANCE);
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static Collection<VirtualFile> getFilesByLibName(@NotNull final GlobalSearchScope scope, @NotNull final String libraryName) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_LIBRARY_INDEX, libraryName, scope);
  }

  @Nullable
  public static String getSdkLibUriByRelativePath(final @NotNull Project project, final @NotNull String relativePath) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    final List<String> libNames = sdk == null ? null
                                              : getSdkLibUriToRelativePathMap(project, sdk.getHomePath()).getKeysByValue(relativePath);
    return libNames == null || libNames.isEmpty() ? null : libNames.get(0);
  }

  @Nullable
  public static VirtualFile getSdkLibByUri(@NotNull final Project project, @NotNull final String sdkLibUri) {
    final DartSdk sdk = DartSdk.getDartSdk(project);
    final String relativeLibPath = sdk == null ? null : getSdkLibUriToRelativePathMap(project, sdk.getHomePath()).get(sdkLibUri);
    return relativeLibPath == null ? null : LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/" + relativeLibPath);
  }

  @NotNull
  public static BidirectionalMap<String, String> getSdkLibUriToRelativePathMap(@NotNull final Project project,
                                                                               @NotNull final String sdkHomePath) {
    VirtualFile librariesDartFile = LocalFileSystem.getInstance().findFileByPath(sdkHomePath + "/lib/_internal/libraries.dart");
    if (librariesDartFile == null) {
      librariesDartFile =
        LocalFileSystem.getInstance().findFileByPath(sdkHomePath + "/lib/_internal/sdk_library_metadata/lib/libraries.dart");
    }
    if (librariesDartFile == null) return new BidirectionalMap<>();

    final Pair<Long, BidirectionalMap<String, String>> data = librariesDartFile.getUserData(LIBRARIES_TIME_AND_MAP_KEY);
    final Long cachedTimestamp = Pair.getFirst(data);
    final long modificationCount = librariesDartFile.getModificationCount();

    if (cachedTimestamp != null && cachedTimestamp.equals(modificationCount)) {
      return data.second;
    }

    final VirtualFile finalLibrariesDartFile = librariesDartFile;
    return ReadAction.compute(() -> {
      try {
        final String contents = StringUtil.convertLineSeparators(VfsUtilCore.loadText(finalLibrariesDartFile));
        final PsiFile psiFile =
          PsiFileFactory.getInstance(project).createFileFromText("libraries.dart", DartLanguage.INSTANCE, contents);
        if (!(psiFile instanceof DartFile)) {
          return new BidirectionalMap<>();
        }

        final Pair<Long, BidirectionalMap<String, String>> data1 =
          Pair.create(modificationCount, computeSdkLibUriToRelativePathMap((DartFile)psiFile));
        finalLibrariesDartFile.putUserData(LIBRARIES_TIME_AND_MAP_KEY, data1);
        return data1.second;
      }
      catch (IOException e) {
        return new BidirectionalMap<>();
      }
    });
  }

  private static BidirectionalMap<String, String> computeSdkLibUriToRelativePathMap(final @NotNull DartFile librariesDartFile) {
/*
const Map<String, LibraryInfo> LIBRARIES = const {

  "async": const LibraryInfo(
      "async/async.dart",
      maturity: Maturity.STABLE,
      dart2jsPatchPath: "_internal/lib/async_patch.dart"),

  "_chrome": const LibraryInfo(
      "_chrome/dart2js/chrome_dart2js.dart",
      documented: false,
      category: "Client"),

*/
    final BidirectionalMap<String, String> result = new BidirectionalMap<>();

    librariesDartFile.acceptChildren(new DartRecursiveVisitor() {
      @Override
      public void visitMapEntry(@NotNull DartMapEntry mapEntry) {
        final List<DartExpression> expressions = mapEntry.getExpressionList();
        if (expressions.size() != 2 ||
            !(expressions.get(0) instanceof DartStringLiteralExpression keyExpression) ||
            !(expressions.get(1) instanceof DartNewExpression newExpression)) {
          return;
        }

        final String libraryName = StringUtil.unquoteString(keyExpression.getText());

        final DartType dartType = newExpression.getType();
        if (dartType == null || !"LibraryInfo".equals(dartType.getText())) return;

        final DartArguments arguments = newExpression.getArguments();
        final DartArgumentList argumentList = arguments != null ? arguments.getArgumentList() : null;
        final List<DartExpression> expressionList = argumentList != null ? argumentList.getExpressionList() : null;
        final DartExpression firstExpression = expressionList == null || expressionList.isEmpty() ? null : expressionList.get(0);
        final String libraryRelativePath = firstExpression instanceof DartStringLiteralExpression
                                           ? StringUtil.unquoteString(firstExpression.getText())
                                           : null;

        if (libraryRelativePath != null/* && !libraryRelativePath.startsWith("_")*/) {
          final String libraryUri = DartUrlResolver.DART_PREFIX + libraryName;
          result.put(libraryUri, libraryRelativePath);
        }
      }
    });

    return result;
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      final DartFileIndexData indexData = DartIndexUtil.indexFile(inputData);

      return indexData.isPart() || indexData.getLibraryName() == null ? Collections.emptyMap()
                                                                      : Collections.singletonMap(indexData.getLibraryName(), null);
    }
  }
}
