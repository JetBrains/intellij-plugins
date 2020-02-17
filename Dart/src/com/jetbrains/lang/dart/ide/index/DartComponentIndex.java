package com.jetbrains.lang.dart.ide.index;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class DartComponentIndex extends FileBasedIndexExtension<String, DartComponentInfo> {
  private static final ID<String, DartComponentInfo> DART_COMPONENT_INDEX = ID.create("DartComponentIndex");

  @NotNull
  @Override
  public ID<String, DartComponentInfo> getName() {
    return DART_COMPONENT_INDEX;
  }

  @NotNull
  @Override
  public DataIndexer<String, DartComponentInfo, FileContent> getIndexer() {
    return inputData -> DartIndexUtil.indexFile(inputData).getComponentInfoMap();
  }

  @NotNull
  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @NotNull
  @Override
  public DataExternalizer<DartComponentInfo> getValueExternalizer() {
    return new DartComponentInfoExternalizer();
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

  @Override
  public int getVersion() {
    return DartIndexUtil.INDEX_VERSION;
  }

  public static Collection<VirtualFile> getAllFiles(@NotNull final String componentName, @NotNull final GlobalSearchScope scope) {
    return FileBasedIndex.getInstance().getContainingFiles(DART_COMPONENT_INDEX, componentName, scope);
  }
}
