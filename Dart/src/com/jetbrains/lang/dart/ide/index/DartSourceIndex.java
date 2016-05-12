package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DartSourceIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_SOURCE_INDEX = ID.create("DartSourceIndex");
  private DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_SOURCE_INDEX;
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
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  public static List<VirtualFile> findLibraries(final PsiElement context,
                                                @NotNull String fileName,
                                                @NotNull final GlobalSearchScope scope) {
    return new ArrayList<VirtualFile>(FileBasedIndex.getInstance().getContainingFiles(DART_SOURCE_INDEX, fileName, scope));
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      final Map<String, Void> result = new THashMap<String, Void>();
      for (String partUri : DartIndexUtil.indexFile(inputData).getPartUris()) {
        result.put(partUri.substring(partUri.lastIndexOf('/') + 1), null);
      }
      return result;
    }
  }
}
