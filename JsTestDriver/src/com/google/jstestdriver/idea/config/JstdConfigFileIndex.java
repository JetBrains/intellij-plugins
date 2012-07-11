package com.google.jstestdriver.idea.config;

import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class JstdConfigFileIndex extends ScalarIndexExtension<JstdConfigFileType> {

  private static final ID<JstdConfigFileType, Void> KEY = ID.create("jsTestDriver.config.index");

  private static final DataIndexer<JstdConfigFileType, Void, FileContent> INDEXER = new DataIndexer<JstdConfigFileType, Void, FileContent>() {
    @Override
    @NotNull
    public Map<JstdConfigFileType, Void> map(final FileContent inputData) {
      return Collections.singletonMap(JstdConfigFileType.INSTANCE, null);
    }
  };

  private static final FileBasedIndex.InputFilter INPUT_FILTER = new FileBasedIndex.InputFilter() {
    @Override
    public boolean acceptInput(VirtualFile file) {
      return !file.isDirectory() && file.getFileType() == JstdConfigFileType.INSTANCE;
    }
  };

  private static final KeyDescriptor<JstdConfigFileType> KEY_DESCRIPTOR = new KeyDescriptor<JstdConfigFileType>() {
    @Override
    public void save(DataOutput out, JstdConfigFileType value) throws IOException {}

    @Override
    public JstdConfigFileType read(DataInput in) throws IOException {
      return JstdConfigFileType.INSTANCE;
    }

    @Override
    public int getHashCode(JstdConfigFileType value) {
      return 0;
    }

    @Override
    public boolean isEqual(JstdConfigFileType val1, JstdConfigFileType val2) {
      return val1 == val2;
    }
  };

  @NotNull
  @Override
  public DataIndexer<JstdConfigFileType, Void, FileContent> getIndexer() {
    return INDEXER;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return INPUT_FILTER;
  }

  @NotNull
  @Override
  public ID<JstdConfigFileType, Void> getName() {
    return KEY;
  }

  @Override
  public KeyDescriptor<JstdConfigFileType> getKeyDescriptor() {
    return KEY_DESCRIPTOR;
  }

  @Override
  public boolean dependsOnFileContent() {
    return false;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  public static Collection<VirtualFile> getJstdConfigFilesInScope(@NotNull GlobalSearchScope scope) {
    return FileBasedIndex.getInstance().getContainingFiles(KEY, JstdConfigFileType.INSTANCE, scope);
  }

  public static boolean areJstdConfigFilesInScope(@NotNull GlobalSearchScope scope) {
    final Ref<Boolean> jstdConfigFound = Ref.create(false);
    FileBasedIndex.getInstance().processValues(
      KEY,
      JstdConfigFileType.INSTANCE,
      null,
      new FileBasedIndex.ValueProcessor<Void>() {
        @Override
        public boolean process(final VirtualFile file, final Void value) {
          jstdConfigFound.set(true);
          return false;
        }
      },
      scope
    );
    return jstdConfigFound.get();
  }

}
