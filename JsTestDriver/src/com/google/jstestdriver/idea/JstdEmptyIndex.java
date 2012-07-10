package com.google.jstestdriver.idea;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

public class JstdEmptyIndex extends FileBasedIndexExtension<Boolean, Void> {

  public static final ID<Boolean, Void> KEY = ID.create("jsTestDriver.empty.index");

  @NotNull
  @Override
  public DataIndexer<Boolean, Void, FileContent> getIndexer() {
    return new DataIndexer<Boolean, Void, FileContent>() {
      @Override
      @NotNull
      public Map<Boolean, Void> map(final FileContent inputData) {
        return Collections.emptyMap();
      }
    };
  }

  @Override
  public DataExternalizer<Void> getValueExternalizer() {
    return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return new FileBasedIndex.InputFilter() {
      @Override
      public boolean acceptInput(final VirtualFile file) {
        return false;
      }
    };
  }

  @NotNull
  @Override
  public ID<Boolean, Void> getName() {
    return KEY;
  }

  @Override
  public KeyDescriptor<Boolean> getKeyDescriptor() {
    return BooleanDataDescriptor.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return false;
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
