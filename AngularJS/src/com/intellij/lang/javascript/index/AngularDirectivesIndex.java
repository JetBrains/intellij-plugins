package com.intellij.lang.javascript.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class AngularDirectivesIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> INDEX_ID = ID.create("angularjs.directives.index");
  private final DataIndexer<String, Void, FileContent> myIndexer =
    new DataIndexer<String, Void, FileContent>() {
      @Override
      @NotNull
      public Map<String, Void> map(FileContent inputData) {
        return JSSymbolUtil.indexFile(inputData.getPsiFile(), inputData).indexEntry.getStoredNames(INDEX_ID.toString());
      }
    };

  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return INDEX_ID;
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return JSEntryIndex.ourIndexedFilesFilter;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION + JSEntryIndex.getVersionStatic();
  }
}
