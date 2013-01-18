package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author: Fedor.Korotkov
 */
public class DartReversedLibraryIndex extends FileBasedIndexExtension<String, String> {
  public static final ID<String, String> DART_LIBRARY_INDEX = ID.create("DartReversedLibraryIndex");
  private static final int INDEX_VERSION = 1;
  private DataIndexer<String, String, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, String> getName() {
    return DART_LIBRARY_INDEX;
  }

  @Override
  public int getVersion() {
    return DartIndexUtil.BASE_VERSION + INDEX_VERSION;
  }

  @NotNull
  @Override
  public DataIndexer<String, String, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public DataExternalizer<String> getValueExternalizer() {
    return new EnumeratorStringDescriptor();
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return DartInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Nullable
  public static String getLibraryName(Project project, @NotNull VirtualFile virtualFile) {
    final GlobalSearchScope scope = GlobalSearchScope.fileScope(project, virtualFile);
    final List<String> values = FileBasedIndex.getInstance().getValues(DART_LIBRARY_INDEX, virtualFile.getName(), scope);
    return values.isEmpty() ? virtualFile.getName() : values.iterator().next();
  }

  private static class MyDataIndexer implements DataIndexer<String, String, FileContent> {
    @Override
    @NotNull
    public Map<String, String> map(final FileContent inputData) {
      final String libraryName = DartIndexUtil.indexFile(inputData).getLibraryName();
      return libraryName == null
             ? Collections.<String, String>emptyMap()
             : Collections.singletonMap(inputData.getFile().getName(), libraryName);
    }
  }
}
