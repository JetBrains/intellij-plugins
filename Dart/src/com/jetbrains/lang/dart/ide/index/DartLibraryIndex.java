package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.sdk.DartSdk;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;

public class DartLibraryIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_LIBRARY_INDEX = ID.create("DartLibraryIndex");
  private static final int INDEX_VERSION = 2;

  private static final Key<Pair<Long, BidirectionalMap<String, String>>> LIBRARIES_TIME_AND_MAP_KEY = Key.create("dart.internal.libraries");

  private DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_LIBRARY_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @NotNull
  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static List<VirtualFile> findLibraryClass(@NotNull PsiElement context, String libraryName) {
    if (libraryName.startsWith("dart:")) {
      String stdLibName = libraryName.substring("dart:".length());
      VirtualFile stdLibFile = getStandardLibraryFromSdk(context.getProject(), stdLibName);
      if (stdLibFile != null) {
        return Arrays.asList(stdLibFile);
      }
    }
    return new ArrayList<VirtualFile>(findSingleLibraryClass(context.getProject(), libraryName));
  }

  private static Collection<VirtualFile> findSingleLibraryClass(Project project, String libraryName) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_LIBRARY_INDEX, libraryName, GlobalSearchScope.allScope(project));
  }

  public static Collection<VirtualFile> findSingleLibraryClass(String libraryName, GlobalSearchScope scope) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_LIBRARY_INDEX, libraryName, scope);
  }

  @NotNull
  public static Set<String> getAllLibraryNames(Project project) {
    final Collection<String> allKeys = FileBasedIndex.getInstance().getAllKeys(DART_LIBRARY_INDEX, project);
    return new THashSet<String>(allKeys);
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(final FileContent inputData) {
      final String libraryName = DartIndexUtil.indexFile(inputData).getLibraryName();
      return libraryName == null
             ? Collections.<String, Void>emptyMap()
             : Collections.<String, Void>singletonMap(libraryName, null);
    }
  }

  @Nullable
  public static String getStandardLibraryNameByRelativePath(final @NotNull Project project, final @NotNull String relativePath) {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    final List<String> libNames = sdk == null ? null : getLibraryNameToRelativePathMap(project, sdk).getKeysByValue(relativePath);
    return libNames == null || libNames.isEmpty() ? null : libNames.get(0);
  }

  @Nullable
  public static VirtualFile getStandardLibraryFromSdk(final @NotNull Project project, final @NotNull String libraryName) {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    final String relativeLibPath = sdk == null ? null : getLibraryNameToRelativePathMap(project, sdk).get(libraryName);
    return relativeLibPath == null ? null : LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/" + relativeLibPath);
  }

  public static Collection<String> getAllStandardLibrariesFromSdk(final @NotNull Project project) {
    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    return sdk == null ? Collections.<String>emptyList() : getLibraryNameToRelativePathMap(project, sdk).keySet();
  }

  @NotNull
  private static BidirectionalMap<String, String> getLibraryNameToRelativePathMap(final @NotNull Project project,
                                                                                  final @NotNull DartSdk sdk) {
    final VirtualFile librariesDartFile = LocalFileSystem.getInstance().findFileByPath(sdk.getHomePath() + "/lib/_internal/libraries.dart");
    if (librariesDartFile == null) return new BidirectionalMap<String, String>();

    Pair<Long, BidirectionalMap<String, String>> data = librariesDartFile.getUserData(LIBRARIES_TIME_AND_MAP_KEY);
    final Long cachedTimestamp = data == null ? null : data.first;
    final long modificationCount = librariesDartFile.getModificationCount();

    if (cachedTimestamp == null || !cachedTimestamp.equals(modificationCount)) {
      try {
        final String contents = VfsUtilCore.loadText(librariesDartFile);
        final PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("libraries.dart", DartLanguage.INSTANCE, contents);
        if (!(psiFile instanceof DartFile)) return new BidirectionalMap<String, String>();

        data = Pair.create(modificationCount, computeLibraryNameToRelativePathMap((DartFile)psiFile));
        librariesDartFile.putUserData(LIBRARIES_TIME_AND_MAP_KEY, data);
      }
      catch (IOException e) {
        return new BidirectionalMap<String, String>();
      }
    }
    return data.getSecond();
  }

  private static BidirectionalMap<String, String> computeLibraryNameToRelativePathMap(final @NotNull DartFile librariesDartFile) {
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
    final BidirectionalMap<String, String> result = new BidirectionalMap<String, String>();

    librariesDartFile.acceptChildren(new DartRecursiveVisitor() {
      public void visitMapLiteralEntry(final @NotNull DartMapLiteralEntry mapLiteralEntry) {
        final List<DartExpression> expressions = mapLiteralEntry.getExpressionList();
        if (expressions.size() != 2 ||
            !(expressions.get(0) instanceof DartStringLiteralExpression) ||
            !(expressions.get(1) instanceof DartConstConstructorExpression)) {
          return;
        }

        final DartStringLiteralExpression keyExpression = (DartStringLiteralExpression)expressions.get(0);
        final DartConstConstructorExpression constructorExpression = (DartConstConstructorExpression)expressions.get(1);

        final String libraryName = StringUtil.unquoteString(keyExpression.getText());
        if (libraryName.startsWith("_")) return;

        final DartType dartType = constructorExpression.getType();
        if (dartType == null || !"LibraryInfo".equals(dartType.getText())) return;

        final DartArguments arguments = constructorExpression.getArguments();
        final DartArgumentList argumentList = arguments != null ? arguments.getArgumentList() : null;
        final List<DartExpression> expressionList = argumentList != null ? argumentList.getExpressionList() : null;
        final DartExpression firstExpression = expressionList == null || expressionList.isEmpty() ? null : expressionList.get(0);
        final String libraryRelativePath = firstExpression instanceof DartStringLiteralExpression
                                           ? StringUtil.unquoteString(firstExpression.getText())
                                           : null;

        if (libraryRelativePath != null) {
          result.put(libraryName, libraryRelativePath);
        }
      }
    });

    return result;
  }
}
