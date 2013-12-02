package com.intellij.lang.javascript.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public class AngularJSIndex extends FileBasedIndexExtension<String, TObjectIntHashMap<String>> {
  public static final ID<String, TObjectIntHashMap<String>> INDEX_ID = ID.create("angular.index");
  private final DataIndexer<String, TObjectIntHashMap<String>, FileContent> myIndexer =
    new DataIndexer<String, TObjectIntHashMap<String>, FileContent>() {
      @Override
      @NotNull
      public Map<String, TObjectIntHashMap<String>> map(FileContent inputData) {
        return JSSymbolUtil.indexFile(inputData.getPsiFile(), inputData).indexEntry.getAdditionalData();
      }
    };

  private static final int BASE_VERSION = 2;
  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

  @NotNull
  @Override
  public DataIndexer<String, TObjectIntHashMap<String>, FileContent> getIndexer() {
    return myIndexer;
  }

  @NotNull
  @Override
  public ID<String, TObjectIntHashMap<String>> getName() {
    return INDEX_ID;
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public DataExternalizer<TObjectIntHashMap<String>> getValueExternalizer() {
    return new JavaScriptIndex.AdditionalDataExternalizer();
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
    return BASE_VERSION + JSEntryIndex.getVersionStatic();
  }
}
