package com.jetbrains.lang.dart.ide.index;

import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class DartSymbolIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> DART_SYMBOL_INDEX = ID.create("DartSymbolIndex");
  private final DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return DART_SYMBOL_INDEX;
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

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    @NotNull
    public Map<String, Void> map(@NotNull final FileContent inputData) {
      List<String> symbols = DartIndexUtil.indexFile(inputData).getSymbols();
      final Map<String, Void> result = new THashMap<>();
      for (String symbol : symbols) {
        result.put(symbol, null);
      }
      return result;
    }
  }
}
